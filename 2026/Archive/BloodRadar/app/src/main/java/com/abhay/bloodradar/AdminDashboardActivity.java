package com.abhay.bloodradar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.abhay.bloodradar.api.AppConfig;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalDonors, tvActiveRequests,
            tvCriticalRequests, tvActiveDonations, tvPendingContactReqs,
            tvReportedChats, tvBlockedUsers, tvAdminName;

    private LinearLayout cardUsers, cardRequests, cardDonations,
            cardContactRequests, cardChats, cardBroadcast, cardAnalytics;

    private OkHttpClient httpClient = new OkHttpClient();
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Hide action bar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Get token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        // Get admin name
        try {
            JSONObject userObj = new JSONObject(prefs.getString("user", "{}"));
            String adminName = userObj.optString("name", "Admin");
            tvAdminName = findViewById(R.id.tvAdminName);
            if (tvAdminName != null) tvAdminName.setText("Welcome, " + adminName + " 👋");
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeViews();
        setupClickListeners();
        loadDashboardStats();
    }

    private void initializeViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalDonors = findViewById(R.id.tvTotalDonors);
        tvActiveRequests = findViewById(R.id.tvActiveRequests);
        tvCriticalRequests = findViewById(R.id.tvCriticalRequests);
        tvActiveDonations = findViewById(R.id.tvActiveDonations);
        tvPendingContactReqs = findViewById(R.id.tvPendingContactReqs);
        tvReportedChats = findViewById(R.id.tvReportedChats);
        tvBlockedUsers = findViewById(R.id.tvBlockedUsers);

        cardUsers = findViewById(R.id.cardUsers);
        cardRequests = findViewById(R.id.cardRequests);
        cardDonations = findViewById(R.id.cardDonations);
        cardContactRequests = findViewById(R.id.cardContactRequests);
        cardChats = findViewById(R.id.cardChats);
        cardBroadcast = findViewById(R.id.cardBroadcast);
        cardAnalytics = findViewById(R.id.cardAnalytics);
    }

    private void setupClickListeners() {
        if (cardUsers != null) cardUsers.setOnClickListener(v ->
            startActivity(new Intent(this, AdminUsersActivity.class)));

        if (cardRequests != null) cardRequests.setOnClickListener(v ->
            startActivity(new Intent(this, AdminRequestsActivity.class)));

        if (cardDonations != null) cardDonations.setOnClickListener(v ->
            startActivity(new Intent(this, AdminDonationsActivity.class)));

        if (cardContactRequests != null) cardContactRequests.setOnClickListener(v ->
            startActivity(new Intent(this, AdminContactRequestsActivity.class)));

        if (cardChats != null) cardChats.setOnClickListener(v ->
            startActivity(new Intent(this, AdminChatsActivity.class)));

        if (cardBroadcast != null) cardBroadcast.setOnClickListener(v ->
            startActivity(new Intent(this, AdminBroadcastActivity.class)));

        if (cardAnalytics != null) cardAnalytics.setOnClickListener(v ->
            startActivity(new Intent(this, AdminAnalyticsActivity.class)));

        View btnMenu = findViewById(R.id.btnAdminMenu);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.menu_logout) {
                showLogoutDialog();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void loadDashboardStats() {
        String url = AppConfig.BASE_URL + "/admin/dashboard";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(AdminDashboardActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    if (json.optBoolean("success")) {
                        JSONObject data = json.getJSONObject("data");

                        JSONObject users = data.getJSONObject("users");
                        JSONObject requests = data.getJSONObject("requests");
                        JSONObject donations = data.getJSONObject("donations");
                        JSONObject contactReqs = data.getJSONObject("contactRequests");
                        JSONObject chats = data.getJSONObject("chats");

                        runOnUiThread(() -> {
                            if (tvTotalUsers != null)
                                tvTotalUsers.setText(String.valueOf(users.optInt("total")));
                            if (tvTotalDonors != null)
                                tvTotalDonors.setText(String.valueOf(users.optInt("donors")));
                            if (tvBlockedUsers != null)
                                tvBlockedUsers.setText(String.valueOf(users.optInt("blocked")));
                            if (tvActiveRequests != null)
                                tvActiveRequests.setText(String.valueOf(requests.optInt("active")));
                            if (tvCriticalRequests != null)
                                tvCriticalRequests.setText(String.valueOf(requests.optInt("critical")));
                            if (tvActiveDonations != null)
                                tvActiveDonations.setText(String.valueOf(donations.optInt("active")));
                            if (tvPendingContactReqs != null)
                                tvPendingContactReqs.setText(String.valueOf(contactReqs.optInt("pending")));
                            if (tvReportedChats != null)
                                tvReportedChats.setText(String.valueOf(chats.optInt("reported")));
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats(); // har baar screen pe wapas aane par reload
    }

    @Override
    public void onBackPressed() {
        // Double back press recommended — admin panel shouldn't go to user home
        showLogoutDialog();
    }
}
