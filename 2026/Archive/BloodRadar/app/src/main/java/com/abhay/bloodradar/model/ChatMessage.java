package com.abhay.bloodradar.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    private String id;
    private String conversationId;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String receiverName;
    private String message;
    private String timestamp;
    private boolean isRead;

    public ChatMessage() {}

    public ChatMessage(JSONObject json) {
        try {
            this.id = json.optString("_id");
            this.conversationId = json.optString("conversationId");
            this.message = json.optString("message");
            this.timestamp = json.optString("timestamp");
            this.isRead = json.optBoolean("isRead", false);

            // Parse sender
            JSONObject sender = json.optJSONObject("senderId");
            if (sender != null) {
                this.senderId = sender.optString("_id");
                this.senderName = sender.optString("name");
            }

            // Parse receiver
            JSONObject receiver = json.optJSONObject("receiverId");
            if (receiver != null) {
                this.receiverId = receiver.optString("_id");
                this.receiverName = receiver.optString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
