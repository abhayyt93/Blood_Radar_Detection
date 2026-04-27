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
import com.abhay.bloodradar.model.Conversation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<Conversation> conversations;
    private Context context;

    public ConversationAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        // Set user name
        holder.tvUserName.setText(conversation.getOtherUserName());

        // Set last message
        if (conversation.getLastMessage() != null && !conversation.getLastMessage().isEmpty()) {
            holder.tvLastMessage.setText(conversation.getLastMessage());
        } else {
            holder.tvLastMessage.setText("No messages yet");
        }

        // Set timestamp
        if (conversation.getUpdatedAt() != null && !conversation.getUpdatedAt().isEmpty()) {
            holder.tvTimestamp.setText(formatTimestamp(conversation.getUpdatedAt()));
        } else {
            holder.tvTimestamp.setText("");
        }

        // Set user initial
        String name = conversation.getOtherUserName();
        if (name != null && !name.isEmpty()) {
            holder.tvUserInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        } else {
            holder.tvUserInitial.setText("?");
        }

        // Set unread count
        if (conversation.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // Click listener to open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", conversation.getOtherUserId());
            intent.putExtra("userName", conversation.getOtherUserName());
            context.startActivity(intent);
        });
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Set UTC timezone for parsing
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault()); // Use device's local timezone
            
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void updateData(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserInitial, tvUserName, tvLastMessage, tvTimestamp, tvUnreadCount;

        ViewHolder(View itemView) {
            super(itemView);
            tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}
