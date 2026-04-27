package com.abhay.bloodradar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if user is already logged in
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String token = prefs.getString("token", null);

            Intent intent;
            if (token != null && !token.isEmpty()) {
                // Check saved role for routing
                String role = prefs.getString("role", "user");
                if ("admin".equals(role)) {
                    intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                }
            } else {
                // User not logged in, go to Login
                intent = new Intent(SplashActivity.this, MainActivity.class);
            }
            
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
