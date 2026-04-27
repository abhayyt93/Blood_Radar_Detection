package com.abhay.bloodradar;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.abhay.bloodradar.adapter.MessageAdapter;
import com.abhay.bloodradar.api.AppConfig;
import com.abhay.bloodradar.api.ChatService;
import com.abhay.bloodradar.model.ChatMessage;
import com.abhay.bloodradar.utils.ToastUtil;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private EditText etMessage;
    private ImageView btnSend;
    private TextView tvChatUserName, tvChatUserBloodGroup;
    
    private List<ChatMessage> messages = new ArrayList<>();
    private String token;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserBloodGroup;
    private String otherUserCity;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshMessagesRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Get user data
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        token = prefs.getString("token", null);
        String userData = prefs.getString("user", null);
        
        // Validate token
        if (token == null || token.isEmpty()) {
            ToastUtil.showError(this, "Session expired. Please login again.");
            finish();
            return;
        }
        
        try {
            JSONObject user = new JSONObject(userData);
            currentUserId = user.getString("_id");
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showError(this, "Failed to load user data");
            finish();
            return;
        }

        // Get intent data
        otherUserId = getIntent().getStringExtra("userId");
        otherUserName = getIntent().getStringExtra("userName");
        otherUserBloodGroup = getIntent().getStringExtra("bloodGroup");
        otherUserCity = getIntent().getStringExtra("city");

        // Initialize views
        tvChatUserName = findViewById(R.id.tvChatUserName);
        tvChatUserBloodGroup = findViewById(R.id.tvChatUserBloodGroup);
        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Set user info
        tvChatUserName.setText(otherUserName);

        // bloodGroup ya city null ho toh subtitle hide karo
        if (otherUserBloodGroup != null && otherUserCity != null) {
            tvChatUserBloodGroup.setText(otherUserBloodGroup + " • " + otherUserCity);
            tvChatUserBloodGroup.setVisibility(android.view.View.VISIBLE);
        } else if (otherUserBloodGroup != null) {
            tvChatUserBloodGroup.setText(otherUserBloodGroup);
            tvChatUserBloodGroup.setVisibility(android.view.View.VISIBLE);
        } else if (otherUserCity != null) {
            tvChatUserBloodGroup.setText(otherUserCity);
            tvChatUserBloodGroup.setVisibility(android.view.View.VISIBLE);
        } else {
            tvChatUserBloodGroup.setVisibility(android.view.View.GONE);
        }

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, messages, currentUserId);
        recyclerView.setAdapter(adapter);

        // Long press → Report message
        adapter.setOnMessageLongClickListener(messageId -> showReportDialog(messageId));

        // Send button click
        btnSend.setOnClickListener(v -> sendMessage());

        // Load messages
        loadMessages();

        // Auto-refresh messages every 5 seconds
        refreshMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(refreshMessagesRunnable, 5000);
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        btnSend.setEnabled(false);
        
        ChatService.sendMessage(token, otherUserId, messageText, new ChatService.ChatCallback() {
            @Override
            public void onSuccess(ChatMessage message) {
                runOnUiThread(() -> {
                    adapter.addMessage(message);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    etMessage.setText("");
                    btnSend.setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (error.contains("token") || error.contains("expired") || error.contains("Invalid")) {
                        ToastUtil.showError(ChatActivity.this, "Session expired. Please login again.");
                        finish();
                    } else {
                        ToastUtil.showError(ChatActivity.this, "Failed to send: " + error);
                    }
                    btnSend.setEnabled(true);
                });
            }
        });
    }

    private void loadMessages() {
        ChatService.getMessages(token, otherUserId, new ChatService.MessageListCallback() {
            @Override
            public void onSuccess(List<ChatMessage> newMessages) {
                runOnUiThread(() -> {
                    messages.clear();
                    messages.addAll(newMessages);
                    adapter.updateMessages(messages);
                    if (messages.size() > 0) {
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Silent fail for auto-refresh
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshMessagesRunnable != null) {
            handler.removeCallbacks(refreshMessagesRunnable);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showReportDialog(String messageId) {
        String[] reasons = {"Spam", "Abusive language", "Harassment", "Inappropriate content", "Other"};
        new AlertDialog.Builder(this)
                .setTitle("Report Message")
                .setItems(reasons, (dialog, which) -> {
                    reportMessage(messageId, reasons[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reportMessage(String messageId, String reason) {
        String url = AppConfig.BASE_URL + "/chat/" + messageId + "/report";
        try {
            JSONObject body = new JSONObject();
            body.put("reportReason", reason);
            RequestBody requestBody = RequestBody.create(
                    body.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .patch(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                            "Failed to report. Try again.", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                                "✅ Message reported", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                                "Report failed (Error " + response.code() + ")", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
