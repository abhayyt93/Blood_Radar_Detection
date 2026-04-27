package com.abhay.bloodradar.api;

import com.abhay.bloodradar.model.Donation;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class DonationService {
    private static final String BASE_URL = AppConfig.BASE_URL + "/donations";
    private static final OkHttpClient client = new OkHttpClient();

    public interface DonationCallback {
        void onSuccess(List<Donation> donations);
        void onError(String error);
    }

    public interface CreateDonationCallback {
        void onSuccess(Donation donation);
        void onError(String error);
    }

    public interface UpdateStatusCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Get All Donations
    public static void getAllDonations(DonationCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject json = new JSONObject(body);
                        JSONArray dataArray = json.getJSONArray("data");

                        List<Donation> donations = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            donations.add(Donation.fromJson(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(donations);
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Search Donations
    public static void searchDonations(String city, String bloodGroup, DonationCallback callback) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/search?");
        if (city != null && !city.isEmpty()) {
            urlBuilder.append("city=").append(city).append("&");
        }
        if (bloodGroup != null && !bloodGroup.isEmpty()) {
            urlBuilder.append("bloodGroup=").append(bloodGroup);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject json = new JSONObject(body);
                        JSONArray dataArray = json.getJSONArray("data");

                        List<Donation> donations = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            donations.add(Donation.fromJson(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(donations);
                    } else {
                        callback.onError("Search failed");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Create Donation
    public static void createDonation(String token, String bloodGroup, String city, String message, String availableUntil, CreateDonationCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("bloodGroup", bloodGroup);
            bodyJson.put("city", city);
            bodyJson.put("message", message);
            bodyJson.put("availableUntil", availableUntil);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject json = new JSONObject(body);
                        Donation donation = Donation.fromJson(json.getJSONObject("data"));
                        callback.onSuccess(donation);
                    } else {
                        callback.onError("Failed to create donation");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Update Donation Status
    public static void updateDonationStatus(String token, String donationId, String status, UpdateStatusCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("status", status);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + donationId + "/status")
                .addHeader("Authorization", "Bearer " + token)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess("Status updated");
                } else {
                    callback.onError("Failed to update status");
                }
            }
        });
    }

    // Delete Donation
    public static void deleteDonation(String token, String donationId, UpdateStatusCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + donationId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess("Deleted");
                } else {
                    callback.onError("Failed to delete");
                }
            }
        });
    }
}