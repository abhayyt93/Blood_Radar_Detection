package com.abhay.bloodradar.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.abhay.bloodradar.ChatActivity;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.api.ContactRequestService;
import com.abhay.bloodradar.api.DonationService;
import com.abhay.bloodradar.model.Donation;
import java.util.List;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {
    private List<Donation> donations;
    private Context context;

    public DonationAdapter(Context context, List<Donation> donations) {
        this.context = context;
        this.donations = donations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donation donation = donations.get(position);

        holder.tvBloodGroup.setText(donation.getBloodGroup());
        holder.tvUserName.setText(donation.getUserName());
        holder.tvCity.setText(donation.getCity());
        holder.tvMessage.setText(donation.getMessage());
        holder.tvStatus.setText(donation.getStatus().toUpperCase());

        // Status color
        int statusColor;
        if ("donated".equalsIgnoreCase(donation.getStatus())) {
            statusColor = android.graphics.Color.parseColor("#757575");
        } else {
            statusColor = android.graphics.Color.parseColor("#4CAF50");
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
        }

        SharedPreferences prefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String userId = prefs.getString("userId", null);

        boolean isOwnDonation = donation.getUserId() != null && donation.getUserId().equals(userId);

        // Mark as Donated button (sirf apni donation pe)
        if (isOwnDonation && "available".equalsIgnoreCase(donation.getStatus())) {
            holder.btnMarkDonated.setVisibility(View.VISIBLE);
            holder.btnMarkDonated.setOnClickListener(v -> markAsDonated(donation.getId(), token, holder.btnMarkDonated));
        } else {
            holder.btnMarkDonated.setVisibility(View.GONE);
        }

        // Request Number button — apni donation pe hide karo
        if (isOwnDonation) {
            holder.tvRequestNumber.setVisibility(View.GONE);
        } else {
            holder.tvRequestNumber.setVisibility(View.VISIBLE);
            // Status check karo API se
            setRequestButtonState(holder.tvRequestNumber, token, donation.getId(), donation.getUserPhone());
        }

        // Chat button
        holder.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", donation.getUserId());
            intent.putExtra("userName", donation.getUserName());
            intent.putExtra("bloodGroup", donation.getBloodGroup());
            intent.putExtra("city", donation.getCity());
            context.startActivity(intent);
        });
    }

    private void setRequestButtonState(TextView btn, String token, String donationId, String existingPhone) {
        // Default state dikhao pehle
        btn.setText("REQUEST NUMBER");
        btn.setEnabled(true);
        btn.setAlpha(1.0f);

        if (token == null) return;

        // Background thread pe status check karo
        ContactRequestService.checkStatus(token, donationId, new ContactRequestService.StatusCallback() {
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
                                btn.setOnClickListener(v -> sendContactRequest(btn, token, donationId));
                                break;

                            default: // "none"
                                btn.setText("REQUEST NUMBER");
                                btn.setEnabled(true);
                                btn.setAlpha(1.0f);
                                btn.setOnClickListener(v -> sendContactRequest(btn, token, donationId));
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
                        btn.setOnClickListener(v -> sendContactRequest(btn, token, donationId));
                    });
                }
            }
        });
    }

    private void sendContactRequest(TextView btn, String token, String donationId) {
        btn.setEnabled(false);
        btn.setText("Sending...");

        ContactRequestService.sendRequestForDonation(token, donationId, new ContactRequestService.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        btn.setText("PENDING...");
                        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#9E9E9E")));
                        btn.setEnabled(false);
                        btn.setAlpha(0.7f);
                        Toast.makeText(context, "Number request bheji gayi! Donor ke accept karne ka wait karo.", Toast.LENGTH_LONG).show();
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

    private void markAsDonated(String donationId, String token, Button button) {
        button.setEnabled(false);
        button.setText("Updating...");

        DonationService.updateDonationStatus(token, donationId, "donated", new DonationService.UpdateStatusCallback() {
            @Override
            public void onSuccess(String message) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Marked as donated!", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                        button.setEnabled(true);
                        button.setText("Mark as Donated");
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    public void updateData(List<Donation> newDonations) {
        this.donations = newDonations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBloodGroup, tvUserName, tvCity, tvMessage, tvStatus, tvRequestNumber, btnChat;
        Button btnMarkDonated;

        ViewHolder(View itemView) {
            super(itemView);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRequestNumber = itemView.findViewById(R.id.tvRequestNumber);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnMarkDonated = itemView.findViewById(R.id.btnMarkDonated);
        }
    }
}
