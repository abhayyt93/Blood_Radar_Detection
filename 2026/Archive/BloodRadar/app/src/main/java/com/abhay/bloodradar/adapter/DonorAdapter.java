package com.abhay.bloodradar.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.abhay.bloodradar.ChatActivity;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.model.Donor;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.ViewHolder> {
    private List<Donor> donors;
    private Context context;

    public DonorAdapter(Context context, List<Donor> donors) {
        this.context = context;
        this.donors = donors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Donor donor = donors.get(position);

        holder.tvName.setText(donor.getName());
        holder.tvBloodGroup.setText(donor.getBloodGroup());
        holder.tvCity.setText("📍 " + donor.getCity());
        holder.tvAvailability.setText(donor.isAvailable() ? "✅ Available" : "❌ Not Available");
        holder.tvAvailability.setTextColor(context.getResources().getColor(
            donor.isAvailable() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
        ));

        // Chat button — number nahi, chat karo pehle
        holder.btnDonorChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", donor.getId());
            intent.putExtra("userName", donor.getName());
            intent.putExtra("bloodGroup", donor.getBloodGroup());
            intent.putExtra("city", donor.getCity());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return donors.size();
    }

    public void updateData(List<Donor> newDonors) {
        this.donors = newDonors;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBloodGroup, tvCity, tvAvailability, btnDonorChat;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDonorName);
            tvBloodGroup = itemView.findViewById(R.id.tvDonorBloodGroup);
            tvCity = itemView.findViewById(R.id.tvDonorCity);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            btnDonorChat = itemView.findViewById(R.id.btnDonorChat);
        }
    }
}
