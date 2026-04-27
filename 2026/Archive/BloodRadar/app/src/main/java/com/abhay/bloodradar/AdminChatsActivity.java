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
import okhttp3.*;

public class AdminChatsActivity extends AppCompatActivity {

    private LinearLayout llChatList;
    private TextView tvChatCount;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chats);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llChatList = findViewById(R.id.llChatList);
        tvChatCount = findViewById(R.id.tvChatCount);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadReportedChats();
    }

    private void loadReportedChats() {
        String url = AppConfig.BASE_URL + "/admin/chats/reported";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminChatsActivity.this, "Failed to load", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    JSONArray chats = json.getJSONArray("data");
                    runOnUiThread(() -> {
                        tvChatCount.setText("Reported: " + chats.length() + " chats");
                        llChatList.removeAllViews();
                        if (chats.length() == 0) {
                            TextView empty = new TextView(AdminChatsActivity.this);
                            empty.setText("✅ No reported chats");
                            empty.setPadding(40, 40, 40, 40);
                            empty.setTextSize(16);
                            llChatList.addView(empty);
                            return;
                        }
                        for (int i = 0; i < chats.length(); i++) {
                            try { addChatCard(chats.getJSONObject(i)); }
                            catch (Exception ignored) {}
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void addChatCard(JSONObject chat) {
        String chatId = chat.optString("_id");
        String message = chat.optString("message", "");
        String reason = chat.optString("reportReason", "No reason given");
        JSONObject sender = chat.optJSONObject("senderId");
        JSONObject receiver = chat.optJSONObject("receiverId");
        String senderName = sender != null ? sender.optString("name", "?") : "?";
        String receiverName = receiver != null ? receiver.optString("name", "?") : "?";

        View card = getLayoutInflater().inflate(R.layout.item_admin_chat, llChatList, false);

        ((TextView) card.findViewById(R.id.tvSenderReceiver)).setText(senderName + " → " + receiverName);
        ((TextView) card.findViewById(R.id.tvChatMessage)).setText("\"" + message + "\"");
        ((TextView) card.findViewById(R.id.tvReportReason)).setText("Reason: " + reason);

        card.findViewById(R.id.btnClearReport).setOnClickListener(v -> clearReport(chatId));

        llChatList.addView(card);
    }

    private void clearReport(String chatId) {
        String url = AppConfig.BASE_URL + "/admin/chats/" + chatId + "/clear-report";
        client.newCall(new Request.Builder().url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .patch(RequestBody.create("", null)).build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> { Toast.makeText(AdminChatsActivity.this, "Report cleared", Toast.LENGTH_SHORT).show(); loadReportedChats(); });
            }
        });
    }
}
