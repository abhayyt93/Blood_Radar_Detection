package com.abhay.bloodradar.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.api.ContactRequestService;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContactRequestAdapter extends RecyclerView.Adapter<ContactRequestAdapter.ViewHolder> {
    private JSONArray requests;
    private Context context;
    private String token;

    public ContactRequestAdapter(Context context, String token) {
        this.context = context;
        this.token = token;
        this.requests = new JSONArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject req = requests.getJSONObject(position);
            String requestId = req.optString("_id");
            String status = req.optString("status", "pending");

            // Requester info
            JSONObject requester = req.optJSONObject("requester");
            String requesterName = requester != null ? requester.optString("name", "Unknown") : "Unknown";
            String requesterBlood = requester != null ? requester.optString("bloodGroup", "") : "";
            String requesterCity = requester != null ? requester.optString("city", "") : "";

            holder.tvRequesterName.setText(requesterName);
            holder.tvRequesterInfo.setText("🩸 " + requesterBlood + "  •  📍 " + requesterCity);

            // Donation info
            JSONObject donation = req.optJSONObject("donation");
            if (donation != null) {
                holder.tvDonationInfo.setText("📋 Requesting your contact for: "
                    + donation.optString("bloodGroup", "") + " donation in "
                    + donation.optString("city", ""));
            }

            // Show accepted/rejected badge or buttons
            if ("accepted".equals(status)) {
                holder.layoutButtons.setVisibility(View.GONE);
                holder.tvResponseStatus.setVisibility(View.VISIBLE);
                holder.tvResponseStatus.setText("✅ Accepted");
                holder.tvResponseStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("rejected".equals(status)) {
                holder.layoutButtons.setVisibility(View.GONE);
                holder.tvResponseStatus.setVisibility(View.VISIBLE);
                holder.tvResponseStatus.setText("❌ Rejected");
                holder.tvResponseStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
            } else {
                holder.layoutButtons.setVisibility(View.VISIBLE);
                holder.tvResponseStatus.setVisibility(View.GONE);

                holder.btnAccept.setOnClickListener(v -> respond(holder, requestId, "accepted", position));
                holder.btnReject.setOnClickListener(v -> respond(holder, requestId, "rejected", position));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void respond(ViewHolder holder, String requestId, String action, int position) {
        holder.btnAccept.setEnabled(false);
        holder.btnReject.setEnabled(false);

        ContactRequestService.respondToRequest(token, requestId, action, new ContactRequestService.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        try {
                            JSONObject req = requests.getJSONObject(position);
                            req.put("status", action);
                        } catch (Exception ignored) {}

                        notifyItemChanged(position);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        holder.btnAccept.setEnabled(true);
                        holder.btnReject.setEnabled(true);
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.length();
    }

    public void updateData(JSONArray newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequesterName, tvRequesterInfo, tvDonationInfo, tvResponseStatus;
        Button btnAccept, btnReject;
        View layoutButtons;

        ViewHolder(View itemView) {
            super(itemView);
            tvRequesterName = itemView.findViewById(R.id.tvRequesterName);
            tvRequesterInfo = itemView.findViewById(R.id.tvRequesterInfo);
            tvDonationInfo = itemView.findViewById(R.id.tvDonationInfo);
            tvResponseStatus = itemView.findViewById(R.id.tvResponseStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            layoutButtons = itemView.findViewById(R.id.layoutButtonsContainer);
        }
    }
}
