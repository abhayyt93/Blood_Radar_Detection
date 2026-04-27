package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.abhay.bloodradar.api.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class AdminUsersActivity extends AppCompatActivity {

    private LinearLayout llUserList;
    private TextView tvUserCount;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llUserList = findViewById(R.id.llUserList);
        tvUserCount = findViewById(R.id.tvUserCount);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {
        String url = AppConfig.BASE_URL + "/admin/users";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    if (json.optBoolean("success")) {
                        JSONArray users = json.getJSONArray("data");
                        runOnUiThread(() -> {
                            tvUserCount.setText("Total: " + users.length() + " users");
                            llUserList.removeAllViews();
                            for (int i = 0; i < users.length(); i++) {
                                try {
                                    JSONObject user = users.getJSONObject(i);
                                    addUserCard(user);
                                } catch (Exception ignored) {}
                            }
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void addUserCard(JSONObject user) {
        String userId = user.optString("_id");
        String name = user.optString("name", "Unknown");
        String phone = user.optString("phone", "");
        String bloodGroup = user.optString("bloodGroup", "");
        String city = user.optString("city", "");
        boolean isBlocked = user.optBoolean("isBlocked", false);

        View card = getLayoutInflater().inflate(R.layout.item_admin_user, llUserList, false);

        TextView tvName = card.findViewById(R.id.tvUserName);
        TextView tvInfo = card.findViewById(R.id.tvUserInfo);
        TextView tvStatus = card.findViewById(R.id.tvUserStatus);
        View btnBlock = card.findViewById(R.id.btnToggleBlock);
        View btnDelete = card.findViewById(R.id.btnDeleteUser);

        tvName.setText(name);
        tvInfo.setText(bloodGroup + " • " + city + " • " + phone);
        tvStatus.setText(isBlocked ? "🚫 Blocked" : "✅ Active");
        tvStatus.setTextColor(isBlocked ? 0xFFE53935 : 0xFF2E7D32);

        btnBlock.setOnClickListener(v -> {
            String action = isBlocked ? "unblock" : "block";
            new AlertDialog.Builder(this)
                    .setTitle((isBlocked ? "Unblock" : "Block") + " User?")
                    .setMessage("Are you sure you want to " + action + " " + name + "?")
                    .setPositiveButton("Yes", (d, w) -> toggleBlock(userId))
                    .setNegativeButton("Cancel", null).show();
        });

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("This will permanently delete " + name + "'s account.")
                .setPositiveButton("Delete", (d, w) -> deleteUser(userId))
                .setNegativeButton("Cancel", null).show());

        llUserList.addView(card);
    }

    private void toggleBlock(String userId) {
        String url = AppConfig.BASE_URL + "/admin/users/" + userId + "/toggle-block";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .patch(RequestBody.create("", null)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminUsersActivity.this, "Failed", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> { Toast.makeText(AdminUsersActivity.this, "Done!", Toast.LENGTH_SHORT).show(); loadUsers(); });
            }
        });
    }

    private void deleteUser(String userId) {
        String url = AppConfig.BASE_URL + "/admin/users/" + userId;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .delete().build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminUsersActivity.this, "Failed", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> { Toast.makeText(AdminUsersActivity.this, "User deleted", Toast.LENGTH_SHORT).show(); loadUsers(); });
            }
        });
    }
}
