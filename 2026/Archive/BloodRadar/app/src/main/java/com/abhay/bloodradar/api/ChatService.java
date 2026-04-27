package com.abhay.bloodradar.api;

import com.abhay.bloodradar.model.ChatMessage;
import com.abhay.bloodradar.model.Conversation;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class ChatService {
    private static final String BASE_URL = AppConfig.BASE_URL + "/chat";
    private static final OkHttpClient client = new OkHttpClient();

    // ==================== SEND MESSAGE ====================

    public interface ChatCallback {
        void onSuccess(ChatMessage message);
        void onError(String error);
    }

    public static void sendMessage(String token, String receiverId, String message, ChatCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("receiverId", receiverId);
            json.put("message", message);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/send")
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
                        JSONObject jsonResponse = new JSONObject(body);
                        JSONObject data = jsonResponse.optJSONObject("data");
                        ChatMessage chatMessage = new ChatMessage(data);
                        callback.onSuccess(chatMessage);
                    } else {
                        JSONObject jsonResponse = new JSONObject(body);
                        callback.onError(jsonResponse.optString("message", "Failed"));
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // ==================== GET CONVERSATIONS ====================

    public interface ConversationsCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(String error);
    }

    public static void getConversations(String token, ConversationsCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/conversations")
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
                        JSONArray dataArray = json.getJSONArray("data");
                        List<Conversation> conversations = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            conversations.add(new Conversation(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(conversations);
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // ==================== GET MESSAGES ====================

    public interface MessageListCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String error);
    }

    public static void getMessages(String token, String userId, MessageListCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/messages/" + userId)
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
                        JSONArray dataArray = json.getJSONArray("data");
                        List<ChatMessage> messages = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            messages.add(new ChatMessage(dataArray.getJSONObject(i)));
                        }
                        callback.onSuccess(messages);
                    } else {
                        callback.onError("Failed: " + response.code());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // ==================== MARK AS READ ====================

    public static void markAsRead(String token, String userId, SimpleCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
        } catch (Exception ignored) {}

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/mark-read")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null && response.isSuccessful()) {
                    callback.onSuccess("Marked as read");
                }
            }
        });
    }

    // ==================== DELETE CONVERSATION ====================

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void deleteConversation(String token, String userId, SimpleCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/conversation/" + userId)
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
                    callback.onError("Failed: " + response.code());
                }
            }
        });
    }
}