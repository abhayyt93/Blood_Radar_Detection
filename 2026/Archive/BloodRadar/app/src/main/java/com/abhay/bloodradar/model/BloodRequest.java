package com.abhay.bloodradar.model;

import org.json.JSONException;
import org.json.JSONObject;

public class BloodRequest {
    private String id;
    private String userId;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String userCity;
    private String bloodGroup;
    private String city;
    private String message;
    private String urgency;
    private String status;
    private String image;
    private String createdAt;

    public static BloodRequest fromJson(JSONObject json) throws JSONException {
        BloodRequest request = new BloodRequest();
        request.id = json.optString("_id");
        request.bloodGroup = json.optString("bloodGroup");
        request.city = json.optString("city");
        request.message = json.optString("message");
        request.urgency = json.optString("urgency");
        request.status = json.optString("status");
        request.image = json.optString("image");
        request.createdAt = json.optString("createdAt");

        // Parse user object
        JSONObject user = json.optJSONObject("user");
        if (user != null) {
            request.userId = user.optString("_id");
            request.userName = user.optString("name");
            request.userPhone = user.optString("phone");
            request.userEmail = user.optString("email");
            request.userCity = user.optString("city");
        }

        return request;
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserPhone() { return userPhone; }
    public String getUserEmail() { return userEmail; }
    public String getUserCity() { return userCity; }
    public String getBloodGroup() { return bloodGroup; }
    public String getCity() { return city; }
    public String getMessage() { return message; }
    public String getUrgency() { return urgency; }
    public String getStatus() { return status; }
    public String getImage() { return image; }
    public String getCreatedAt() { return createdAt; }
}

