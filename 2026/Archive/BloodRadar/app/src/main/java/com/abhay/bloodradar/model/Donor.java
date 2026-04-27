package com.abhay.bloodradar.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Donor {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String bloodGroup;
    private String city;
    private boolean isDonor;
    private boolean isAvailable;
    private String createdAt;

    public static Donor fromJson(JSONObject json) throws JSONException {
        Donor donor = new Donor();
        donor.id = json.optString("_id");
        donor.name = json.optString("name");
        donor.email = json.optString("email");
        donor.phone = json.optString("phone");
        donor.bloodGroup = json.optString("bloodGroup");
        donor.city = json.optString("city");
        donor.isDonor = json.optBoolean("isDonor");
        donor.isAvailable = json.optBoolean("isAvailable");
        donor.createdAt = json.optString("createdAt");
        return donor;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBloodGroup() { return bloodGroup; }
    public String getCity() { return city; }
    public boolean isDonor() { return isDonor; }
    public boolean isAvailable() { return isAvailable; }
    public String getCreatedAt() { return createdAt; }
}

