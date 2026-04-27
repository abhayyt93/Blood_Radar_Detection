package com.abhay.bloodradar.api;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class ApiService {
    private static final String BASE_URL = AppConfig.BASE_URL;
    private static final String TAG = "ApiService";
    private static final OkHttpClient client = new OkHttpClient();

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    // ==================== AUTH ====================

    public static void register(
            String name,
            String phone,
            String password,
            String city,
            String bloodGroup,
            String bio,
            ApiCallback<JSONObject> callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("phone", phone);
            json.put("password", password);
            json.put("city", city);
            json.put("bloodGroup", bloodGroup);
            json.put("bio", bio);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/register")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    try {
                        if (response.isSuccessful() && responseBody != null) {
                            JSONObject responseJson = new JSONObject(responseBody);
                            callback.onSuccess(responseJson);
                        } else {
                            String errorMsg = "Server Error: " + response.code();
                            if (responseBody != null) {
                                try {
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMsg = errorJson.optString("message", errorMsg);
                                } catch (Exception e) {}
                            }
                            callback.onError(errorMsg);
                        }
                    } catch (Exception e) {
                        callback.onError("Parse Error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    public static void login(
            String phone,
            String password,
            ApiCallback<JSONObject> callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("phone", phone);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/login")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    try {
                        if (response.isSuccessful() && responseBody != null) {
                            JSONObject responseJson = new JSONObject(responseBody);
                            callback.onSuccess(responseJson);
                        } else {
                            String errorMsg = "Server Error: " + response.code();
                            if (responseBody != null) {
                                try {
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMsg = errorJson.optString("message", errorMsg);
                                } catch (Exception e) {}
                            }
                            callback.onError(errorMsg);
                        }
                    } catch (Exception e) {
                        callback.onError("Parse Error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }

    // ==================== PROFILE ====================

    public static void getProfile(String token, ApiCallback<JSONObject> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/profile")
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
                        callback.onSuccess(new JSONObject(body));
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public static void updateProfile(String token, JSONObject updates, ApiCallback<JSONObject> callback) {
        RequestBody body = RequestBody.create(
                updates.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL + "/auth/profile")
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
                try {
                    String body = response.body().string();
                    if (response.isSuccessful()) {
                        callback.onSuccess(new JSONObject(body));
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // ==================== NOTIFICATIONS ====================

    public static void getNotifications(String token, ApiCallback<JSONArray> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/notifications")
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

    public static void markNotificationRead(String token, String notifId, ApiCallback<Void> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/notifications/" + notifId + "/read")
                .addHeader("Authorization", "Bearer " + token)
                .put(RequestBody.create("", null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }

    public static void deleteNotification(String token, String notifId, ApiCallback<Void> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/notifications/" + notifId)
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
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }

    public static void deleteAllBroadcasts(String token, ApiCallback<Void> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/notifications/broadcasts/all")
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
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }

    // ==================== ADMIN ====================

    public static void getAdminDashboard(String token, ApiCallback<JSONObject> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/admin/dashboard")
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
                        callback.onSuccess(new JSONObject(body));
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public static void getAdminUsers(String token, ApiCallback<JSONArray> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/admin/users")
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

    public static void toggleBlockUser(String token, String userId, ApiCallback<Void> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/admin/users/" + userId + "/toggle-block")
                .addHeader("Authorization", "Bearer " + token)
                .patch(RequestBody.create("", null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }

    public static void deleteUser(String token, String userId, ApiCallback<Void> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/admin/users/" + userId)
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
                    callback.onSuccess(null);
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }

    // ==================== UTIL ====================

    public static void showApiMessage(Activity activity, View rootView, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
        }
    }
}