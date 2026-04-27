package com.abhay.bloodradar.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Conversation {
    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserPhone;
    private String otherUserBloodGroup;
    private String otherUserCity;
    private String lastMessage;
    private String timestamp;
    private int unreadCount;
    private boolean isRead;

    public Conversation() {}

    public Conversation(JSONObject json) {
        try {
            this.conversationId = json.optString("conversationId");
            this.lastMessage = json.optString("lastMessage");
            this.timestamp = json.optString("timestamp");
            this.unreadCount = json.optInt("unreadCount", 0);
            this.isRead = json.optBoolean("isRead", true);

            // Parse other user info
            JSONObject otherUser = json.optJSONObject("otherUser");
            if (otherUser != null) {
                this.otherUserId = otherUser.optString("_id");
                this.otherUserName = otherUser.optString("name");
                this.otherUserPhone = otherUser.optString("phone");
                this.otherUserBloodGroup = otherUser.optString("bloodGroup");
                this.otherUserCity = otherUser.optString("city");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserPhone() { return otherUserPhone; }
    public void setOtherUserPhone(String otherUserPhone) { this.otherUserPhone = otherUserPhone; }

    public String getOtherUserBloodGroup() { return otherUserBloodGroup; }
    public void setOtherUserBloodGroup(String otherUserBloodGroup) { this.otherUserBloodGroup = otherUserBloodGroup; }

    public String getOtherUserCity() { return otherUserCity; }
    public void setOtherUserCity(String otherUserCity) { this.otherUserCity = otherUserCity; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getUpdatedAt() { return timestamp; }
    public void setUpdatedAt(String updatedAt) { this.timestamp = updatedAt; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
