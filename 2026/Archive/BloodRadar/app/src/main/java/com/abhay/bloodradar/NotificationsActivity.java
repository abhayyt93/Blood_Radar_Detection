package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.abhay.bloodradar.api.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import okhttp3.*;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout llNotifList;
    private TextView tvNotifCount;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llNotifList = findViewById(R.id.llNotifList);
        tvNotifCount = findViewById(R.id.tvNotifCount);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        View btnDeleteAll = findViewById(R.id.btnDeleteAll);
        if (btnDeleteAll != null) {
            btnDeleteAll.setOnClickListener(v -> deleteAllBroadcasts());
        }

        loadNotifications();
    }

    private void loadNotifications() {
        String url = AppConfig.BASE_URL + "/notifications";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(NotificationsActivity.this,
                        "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(NotificationsActivity.this,
                            "Error " + response.code() + ": " + body, Toast.LENGTH_LONG).show());
                    return;
                }
                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray notifications = json.optJSONArray("data");
                    if (notifications == null) notifications = new JSONArray();

                    final JSONArray finalNotifs = notifications;
                    markAllRead(finalNotifs);

                    runOnUiThread(() -> {
                        int count = finalNotifs.length();
                        tvNotifCount.setText(count + " notification" + (count != 1 ? "s" : ""));
                        llNotifList.removeAllViews();

                        if (count == 0) {
                            TextView empty = new TextView(NotificationsActivity.this);
                            empty.setText("🔔 No notifications yet");
                            empty.setPadding(40, 60, 40, 40);
                            empty.setTextSize(16);
                            empty.setTextColor(0xFF9E9E9E);
                            llNotifList.addView(empty);
                            return;
                        }

                        for (int i = 0; i < count; i++) {
                            try {
                                addNotifCard(finalNotifs.getJSONObject(i));
                            } catch (Exception ignored) {}
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(NotificationsActivity.this,
                            "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void addNotifCard(JSONObject notif) {
        String message = notif.optString("message", "");
        String title = notif.optString("title", "Notification");
        boolean isRead = notif.optBoolean("isRead", true);
        boolean isBroadcast = notif.optBoolean("isBroadcast", false);
        String id = notif.optString("_id", "");
        String createdAt = notif.optString("createdAt", "");

        View card = getLayoutInflater().inflate(R.layout.item_notification, llNotifList, false);

        TextView tvTitle = card.findViewById(R.id.tvNotifTitle);
        TextView tvMessage = card.findViewById(R.id.tvNotifMessage);
        TextView tvTime = card.findViewById(R.id.tvNotifTime);

        if (tvTitle != null) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }
        tvMessage.setText(message);
        tvTime.setText(formatTime(createdAt));

        // Show red dot if unread
        View dot = card.findViewById(R.id.dotUnread);
        if (!isRead) {
            dot.setVisibility(View.VISIBLE);
            card.setBackgroundColor(0xFFFFF3F3); // light red tint for unread
        }

        // Add delete button for broadcast notifications
        if (isBroadcast) {
            View btnDelete = card.findViewById(R.id.btnDeleteNotif);
            if (btnDelete != null) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> deleteNotification(id));
            }
        }

        llNotifList.addView(card);
    }

    private void deleteNotification(String id) {
        String url = AppConfig.BASE_URL + "/notifications/" + id;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(NotificationsActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(NotificationsActivity.this, "Notification deleted", Toast.LENGTH_SHORT).show();
                        loadNotifications(); // Refresh list
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(NotificationsActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteAllBroadcasts() {
        String url = AppConfig.BASE_URL + "/notifications/broadcasts/all";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(NotificationsActivity.this, "Failed to delete all", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(NotificationsActivity.this, "All broadcasts deleted", Toast.LENGTH_SHORT).show();
                        loadNotifications(); // Refresh list
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(NotificationsActivity.this, "Failed to delete all", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void markAllRead(JSONArray notifications) {
        // Mark each unread notification as read
        for (int i = 0; i < notifications.length(); i++) {
            try {
                JSONObject notif = notifications.getJSONObject(i);
                if (!notif.optBoolean("isRead", true)) {
                    String id = notif.optString("_id");
                    String url = AppConfig.BASE_URL + "/notifications/" + id + "/read";
                    client.newCall(new Request.Builder()
                            .url(url)
                            .addHeader("Authorization", "Bearer " + authToken)
                            .put(RequestBody.create("", null))
                            .build()).enqueue(new Callback() {
                        @Override public void onFailure(Call call, IOException e) {}
                        @Override public void onResponse(Call call, Response response) throws IOException {}
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat output = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            output.setTimeZone(TimeZone.getDefault());
            Date date = input.parse(timestamp);
            return output.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}
