package com.abhay.bloodradar.api;

import com.abhay.bloodradar.model.BloodRequest;
import com.abhay.bloodradar.model.Donor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class BloodRequestService {
    private static final String BASE_URL = AppConfig.BASE_URL;
    private static final OkHttpClient client = new OkHttpClient();

    public interface RequestCallback {
        void onSuccess(List<BloodRequest> requests);
        void onError(String error);
    }

    public interface DonorCallback {
        void onSuccess(List<Donor> donors);
        void onError(String error);
    }

    public interface CreateRequestCallback {
        void onSuccess(BloodRequest request);
        void onError(String error);
    }

    // Get All Blood Requests
    public static void getAllRequests(RequestCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/requests")
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

                        List<BloodRequest> requests = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            requests.add(BloodRequest.fromJson(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(requests);
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Search Requests
    public static void searchRequests(String city, String bloodGroup, String urgency, RequestCallback callback) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/requests/search?");
        if (city != null && !city.isEmpty()) {
            urlBuilder.append("city=").append(city).append("&");
        }
        if (bloodGroup != null && !bloodGroup.isEmpty()) {
            urlBuilder.append("bloodGroup=").append(bloodGroup).append("&");
        }
        if (urgency != null && !urgency.isEmpty()) {
            urlBuilder.append("urgency=").append(urgency);
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

                        List<BloodRequest> requests = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            requests.add(BloodRequest.fromJson(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(requests);
                    } else {
                        callback.onError("Search failed");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Get All Donors
    public static void getAllDonors(DonorCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/donors")
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

                        List<Donor> donors = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            donors.add(Donor.fromJson(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(donors);
                    } else {
                        callback.onError("Failed to fetch donors");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Search Donors
    public static void searchDonors(String city, String bloodGroup, DonorCallback callback) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/donors/search?");
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

                        List<Donor> donors = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            donors.add(Donor.fromJson(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(donors);
                    } else {
                        callback.onError("Search failed");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Create Blood Request
    public static void createRequest(String token, String bloodGroup, String city, String message, String urgency, CreateRequestCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("bloodGroup", bloodGroup);
            bodyJson.put("city", city);
            bodyJson.put("message", message);
            bodyJson.put("urgency", urgency);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/requests")
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
                        BloodRequest request = BloodRequest.fromJson(json.getJSONObject("data"));
                        callback.onSuccess(request);
                    } else {
                        callback.onError("Failed to create request");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Delete Request
    public static void deleteRequest(String token, String requestId, UpdateStatusCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/requests/" + requestId)
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

    public interface UpdateStatusCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Update Request Status
    public static void updateRequestStatus(String token, String requestId, String status, UpdateStatusCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("status", status);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/requests/" + requestId + "/status")
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
}