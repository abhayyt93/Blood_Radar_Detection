package com.abhay.bloodradar.api;

import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class ContactRequestService {
    private static final String BASE_URL = AppConfig.BASE_URL + "/contact-requests";
    private static final OkHttpClient client = new OkHttpClient();

    public interface StatusCallback {
        void onSuccess(String status, String requestId, String phone);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ReceivedRequestsCallback {
        void onSuccess(org.json.JSONArray requests);
        void onError(String error);
    }

    // Get received requests
    public static void getReceivedRequests(String token, ReceivedRequestsCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/received")
                .addHeader("Authorization", "Bearer " + token)
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
                        callback.onSuccess(json.getJSONArray("data"));
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Send request for DONATION
    public static void sendRequestForDonation(String token, String donationId, SimpleCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("donationId", donationId);
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
                    String respBody = response.body().string();
                    if (response.isSuccessful()) {
                        callback.onSuccess("Request sent!");
                    } else {
                        JSONObject json = new JSONObject(respBody);
                        callback.onError(json.optString("message", "Failed"));
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Send request for BLOOD REQUEST
    public static void sendRequestForBloodRequest(String token, String requestId, SimpleCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("requestId", requestId);
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
                    String respBody = response.body().string();
                    if (response.isSuccessful()) {
                        callback.onSuccess("Request sent!");
                    } else {
                        JSONObject json = new JSONObject(respBody);
                        callback.onError(json.optString("message", "Failed"));
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Deprecated: Use sendRequestForDonation or sendRequestForBloodRequest instead
    public static void sendRequest(String token, String itemId, SimpleCallback callback) {
        // For backward compatibility, try donation first
        sendRequestForDonation(token, itemId, callback);
    }

    // Check status (works for both donation and request)
    public static void checkStatus(String token, String itemId, StatusCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/status/" + itemId)
                .addHeader("Authorization", "Bearer " + token)
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
                        JSONObject data = json.getJSONObject("data");
                        String status = data.optString("status", "none");
                        String requestId = data.optString("requestId", null);
                        String phone = data.optString("phone", null);
                        if ("null".equals(phone)) phone = null;
                        callback.onSuccess(status, requestId, phone);
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // Respond to request (accept/reject)
    public static void respondToRequest(String token, String requestId, String action, SimpleCallback callback) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("action", action);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + requestId)
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
                    callback.onSuccess(action.equals("accepted") ? "Accepted!" : "Rejected!");
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }
}