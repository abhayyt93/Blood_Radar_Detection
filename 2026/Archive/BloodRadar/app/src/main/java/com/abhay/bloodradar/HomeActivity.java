package com.abhay.bloodradar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.abhay.bloodradar.adapter.HomeViewPagerAdapter;
import com.abhay.bloodradar.utils.ToastUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class HomeActivity extends AppCompatActivity {
    private JSONObject userJson;
    private TextView tvUserInitial;
    private ImageView ivUserProfilePic;
    private TextView tvUserName;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCreateRequest;
    private android.widget.FrameLayout flNotification;
    private TextView tvNotificationBadge;
    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable unreadCheckRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        tvUserInitial = findViewById(R.id.tvUserInitial);
        ivUserProfilePic = findViewById(R.id.ivUserProfilePic);
        tvUserName = findViewById(R.id.tvUserName);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabCreateRequest = findViewById(R.id.fabCreateRequest);
        flNotification = findViewById(R.id.flNotification);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);

        // Get user data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String userData = prefs.getString("user", null);

        if (userData != null) {
            try {
                userJson = new JSONObject(userData);
                String name = userJson.optString("name", "User");

                // Load profile picture if exists
                String profilePic = userJson.optString("profilePic", "");
                if (profilePic != null && !profilePic.isEmpty()) {
                    ivUserProfilePic.setVisibility(View.VISIBLE);
                    tvUserInitial.setVisibility(View.GONE);
                    String baseUrlForImages = com.abhay.bloodradar.api.AppConfig.BASE_URL.replace("/api", "");
                    String imageUrl = baseUrlForImages + profilePic;
                    com.bumptech.glide.Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_circle_bg)
                            .circleCrop()
                            .into(ivUserProfilePic);
                } else {
                    ivUserProfilePic.setVisibility(View.GONE);
                    tvUserInitial.setVisibility(View.VISIBLE);
                    if (!name.isEmpty()) {
                        tvUserInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    } else {
                        tvUserInitial.setText("U");
                    }
                }
                tvUserName.setText(name);
            } catch (Exception e) {
                userJson = new JSONObject();
                tvUserInitial.setText("U");
                tvUserName.setText("User");
            }
        } else {
            userJson = new JSONObject();
            tvUserInitial.setText("U");
            tvUserName.setText("User");
        }

        // Setup ViewPager and Tabs
        setupViewPager();

        // Menu button click listener
        ivMenu.setOnClickListener(v -> showPopupMenu(v));

        // Profile picture/initial click - open full screen viewer
        tvUserInitial.setOnClickListener(v -> openProfileImageViewer());
        ivUserProfilePic.setOnClickListener(v -> openProfileImageViewer());

        // FAB click listener
        fabCreateRequest.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateRequestActivity.class);
            startActivity(intent);
        });

        // Notification bell click → open NotificationsActivity
        flNotification.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
        });

        // Start polling for unread messages
        startUnreadCheck();
    }

    private void setupViewPager() {
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Requests");
                    tab.setIcon(R.drawable.ic_blood_request);
                    break;
                case 1:
                    tab.setText("Donations");
                    tab.setIcon(R.drawable.ic_donation);
                    break;
                case 2:
                    tab.setText("Donors");
                    tab.setIcon(R.drawable.ic_donors);
                    break;
                case 3:
                    tab.setText("Messages");
                    tab.setIcon(R.drawable.ic_messages);
                    break;
            }
        }).attach();

        // Change FAB action based on selected tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    // Blood Requests tab - Create Request
                    fabCreateRequest.setVisibility(View.VISIBLE);
                    fabCreateRequest.setOnClickListener(v -> {
                        Intent intent = new Intent(HomeActivity.this, CreateRequestActivity.class);
                        startActivity(intent);
                    });
                } else if (position == 1) {
                    // Donations tab - Create Donation
                    fabCreateRequest.setVisibility(View.VISIBLE);
                    fabCreateRequest.setOnClickListener(v -> {
                        Intent intent = new Intent(HomeActivity.this, CreateDonationActivity.class);
                        startActivity(intent);
                    });
                } else {
                    // Donors tab and Messages tab - Hide FAB
                    fabCreateRequest.setVisibility(View.GONE);
                    fabCreateRequest.setOnClickListener(null);
                }
            }
        });
    }

    // ...existing code...

    private void openProfileImageViewer() {
        String profilePic = userJson.optString("profilePic", "");
        if (profilePic != null && !profilePic.isEmpty()) {
            String baseUrlForImages = com.abhay.bloodradar.api.AppConfig.BASE_URL.replace("/api", "");
            String imageUrl = baseUrlForImages + profilePic;
            
            Intent intent = new Intent(HomeActivity.this, ImageViewerActivity.class);
            intent.putExtra("IMAGE_URL", imageUrl);
            intent.putExtra("TITLE", "My Profile Photo");
            startActivity(intent);
        } else {
            android.widget.Toast.makeText(this, "No profile photo to view", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_profile) {
                // Open Profile Activity
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_logout) {
                // Show logout confirmation dialog
                showLogoutDialog();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                // Clear user data
                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                prefs.edit().clear().apply();

                // Show logout success message
                ToastUtil.showSuccess(this, "Logged out successfully");

                // Go to login screen
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }
    private void startUnreadCheck() {
        unreadCheckRunnable = new Runnable() {
            @Override
            public void run() {
                updateUnreadCount();
                handler.postDelayed(this, 10000); // Check every 10 seconds
            }
        };
        handler.post(unreadCheckRunnable);
    }

    private void updateUnreadCount() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) return;

        String url = com.abhay.bloodradar.api.AppConfig.BASE_URL + "/notifications";
        new OkHttpClient().newCall(new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get().build()
        ).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONArray data = new JSONObject(body).getJSONArray("data");
                    int unread = 0;
                    for (int i = 0; i < data.length(); i++) {
                        if (!data.getJSONObject(i).optBoolean("isRead", true)) unread++;
                    }
                    final int count = unread;
                    runOnUiThread(() -> {
                        if (count > 0) {
                            tvNotificationBadge.setVisibility(View.VISIBLE);
                            tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                        } else {
                            tvNotificationBadge.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception ignored) {}
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile picture when returning to HomeActivity
        loadUserProfile();
        updateUnreadCount();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String userData = prefs.getString("user", null);

        if (userData != null) {
            try {
                userJson = new JSONObject(userData);
                String name = userJson.optString("name", "User");

                // Load profile picture if exists
                String profilePic = userJson.optString("profilePic", "");
                if (profilePic != null && !profilePic.isEmpty()) {
                    ivUserProfilePic.setVisibility(View.VISIBLE);
                    tvUserInitial.setVisibility(View.GONE);
                    String baseUrlForImages = com.abhay.bloodradar.api.AppConfig.BASE_URL.replace("/api", "");
                    String imageUrl = baseUrlForImages + profilePic;
                    com.bumptech.glide.Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_circle_bg)
                            .circleCrop()
                            .into(ivUserProfilePic);
                } else {
                    ivUserProfilePic.setVisibility(View.GONE);
                    tvUserInitial.setVisibility(View.VISIBLE);
                    if (!name.isEmpty()) {
                        tvUserInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    } else {
                        tvUserInitial.setText("U");
                    }
                }
                tvUserName.setText(name);
            } catch (Exception e) {
                userJson = new JSONObject();
                tvUserInitial.setText("U");
                tvUserName.setText("User");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && unreadCheckRunnable != null) {
            handler.removeCallbacks(unreadCheckRunnable);
        }
    }
}
