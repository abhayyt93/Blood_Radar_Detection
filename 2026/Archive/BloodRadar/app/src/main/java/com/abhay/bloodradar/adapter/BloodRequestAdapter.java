package com.abhay.bloodradar.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.abhay.bloodradar.ChatActivity;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.api.ContactRequestService;
import com.abhay.bloodradar.model.BloodRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.ViewHolder> {
    private List<BloodRequest> requests;
    private Context context;

    public BloodRequestAdapter(Context context, List<BloodRequest> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blood_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodRequest request = requests.get(position);

        holder.tvBloodGroup.setText(request.getBloodGroup());
        holder.tvUserName.setText(request.getUserName());
        holder.tvCity.setText(request.getCity());
        holder.tvMessage.setText(request.getMessage());
        holder.tvUrgency.setText(request.getUrgency().toUpperCase());

        // Set urgency circle background color based on level
        int urgencyColor;
        switch (request.getUrgency().toLowerCase()) {
            case "high":
                urgencyColor = android.graphics.Color.parseColor("#D32F2F"); // Red
                break;
            case "medium":
                urgencyColor = android.graphics.Color.parseColor("#FF9800"); // Orange
                break;
            default: // low
                urgencyColor = android.graphics.Color.parseColor("#4CAF50"); // Green
                break;
        }

        // Apply color tint to the circular background
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            holder.tvUrgency.setBackgroundTintList(android.content.res.ColorStateList.valueOf(urgencyColor));
        }

        SharedPreferences prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String userId = prefs.getString("userId", null);

        boolean isOwnRequest = request.getUserId() != null && request.getUserId().equals(userId);

        // Request Number/Call button - apni request pe hide karo
        if (isOwnRequest) {
            holder.tvRequestNumber.setVisibility(View.GONE);
        } else {
            holder.tvRequestNumber.setVisibility(View.VISIBLE);
            // Status check karo API se
            setRequestButtonState(holder.tvRequestNumber, token, request.getId(), request.getUserPhone());
        }

        // Chat button click listener
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", request.getUserId());
            intent.putExtra("userName", request.getUserName());
            intent.putExtra("bloodGroup", request.getBloodGroup());
            intent.putExtra("city", request.getCity());
            context.startActivity(intent);
        });
    }

    private void setRequestButtonState(TextView btn, String token, String requestId, String existingPhone) {
        // Default state dikhao pehle
        btn.setText("REQUEST NUMBER");
        btn.setEnabled(true);
        btn.setAlpha(1.0f);

        if (token == null) return;

        // Background thread pe status check karo
        ContactRequestService.checkStatus(token, requestId, new ContactRequestService.StatusCallback() {
            @Override
            public void onSuccess(String status, String requestId, String phone) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        switch (status) {
                            case "accepted":
                                // GREEN — Call karo
                                String phoneToCall = (phone != null) ? phone : existingPhone;
                                btn.setText("📞 CALL");
                                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#4CAF50")));
                                btn.setEnabled(true);
                                btn.setAlpha(1.0f);
                                btn.setOnClickListener(v -> {
                                    if (phoneToCall != null) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + phoneToCall));
                                        context.startActivity(intent);
                                    } else {
                                        Toast.makeText(context, "Phone number unavailable", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;

                            case "pending":
                                // GRAY — Pending
                                btn.setText("PENDING...");
                                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#9E9E9E")));
                                btn.setEnabled(false);
                                btn.setAlpha(0.7f);
                                break;

                            case "rejected":
                                // Rejected — allow to re-request
                                btn.setText("REQUEST NUMBER");
                                btn.setEnabled(true);
                                btn.setAlpha(1.0f);
                                btn.setOnClickListener(v -> sendContactRequest(btn, token, requestId));
                                break;

                            default: // "none"
                                btn.setText("REQUEST NUMBER");
                                btn.setEnabled(true);
                                btn.setAlpha(1.0f);
                                btn.setOnClickListener(v -> sendContactRequest(btn, token, requestId));
                                break;
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Error pe default state rakho
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        btn.setOnClickListener(v -> sendContactRequest(btn, token, requestId));
                    });
                }
            }
        });
    }

    private void sendContactRequest(TextView btn, String token, String requestId) {
        btn.setEnabled(false);
        btn.setText("Sending...");

        ContactRequestService.sendRequestForBloodRequest(token, requestId, new ContactRequestService.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        btn.setText("PENDING...");
                        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#9E9E9E")));
                        btn.setEnabled(false);
                        btn.setAlpha(0.7f);
                        Toast.makeText(context, "Number request bheji gayi! User ke accept karne ka wait karo.", Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        btn.setText("REQUEST NUMBER");
                        btn.setEnabled(true);
                        btn.setAlpha(1.0f);
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateData(List<BloodRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvUserName, tvCity, tvMessage, tvUrgency, tvRequestNumber, btnChat;

        ViewHolder(View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvRequestNumber = itemView.findViewById(R.id.tvRequestNumber);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}

