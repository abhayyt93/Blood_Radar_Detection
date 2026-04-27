package com.abhay.bloodradar.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Donation {
    private String id;
    private String userId;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String userCity;
    private String bloodGroup;
    private String city;
    private String message;
    private String status;
    private String availableUntil;
    private String createdAt;

    public static Donation fromJson(JSONObject json) throws JSONException {
        Donation donation = new Donation();
        donation.id = json.optString("_id");
        donation.bloodGroup = json.optString("bloodGroup");
        donation.city = json.optString("city");
        donation.message = json.optString("message");
        donation.status = json.optString("status", "available");
        donation.availableUntil = json.optString("availableUntil");
        donation.createdAt = json.optString("createdAt");

        // Parse user object
        JSONObject user = json.optJSONObject("user");
        if (user != null) {
            donation.userId = user.optString("_id");
            donation.userName = user.optString("name");
            donation.userPhone = user.optString("phone");
            donation.userEmail = user.optString("email");
            donation.userCity = user.optString("city");
        }

        return donation;
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
    public String getStatus() { return status; }
    public String getAvailableUntil() { return availableUntil; }
    public String getCreatedAt() { return createdAt; }
}
