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

public class AdminAnalyticsActivity extends AppCompatActivity {

    private LinearLayout llAnalytics;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llAnalytics = findViewById(R.id.llAnalytics);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadAnalytics();
    }

    private void loadAnalytics() {
        String url = AppConfig.BASE_URL + "/admin/analytics";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminAnalyticsActivity.this, "Failed to load analytics", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    if (json.optBoolean("success")) {
                        JSONObject data = json.getJSONObject("data");
                        JSONArray bloodGroups = data.getJSONArray("bloodGroupStats");
                        JSONArray cities = data.getJSONArray("cityStats");
                        JSONArray urgency = data.getJSONArray("urgencyStats");
                        String acceptanceRate = data.optString("contactRequestAcceptanceRate", "0%");

                        runOnUiThread(() -> {
                            llAnalytics.removeAllViews();
                            addSection("🩸 Blood Group Wise Donors");
                            for (int i = 0; i < bloodGroups.length(); i++) {
                                try {
                                    JSONObject item = bloodGroups.getJSONObject(i);
                                    addStatRow(item.optString("_id", "?"), item.optInt("count", 0) + " donors");
                                } catch (Exception ignored) {}
                            }
                            addSection("🏙️ Top Cities (Donor Count)");
                            for (int i = 0; i < cities.length(); i++) {
                                try {
                                    JSONObject item = cities.getJSONObject(i);
                                    addStatRow(item.optString("_id", "?"), item.optInt("count", 0) + " donors");
                                } catch (Exception ignored) {}
                            }
                            addSection("📋 Request Urgency Distribution");
                            for (int i = 0; i < urgency.length(); i++) {
                                try {
                                    JSONObject item = urgency.getJSONObject(i);
                                    addStatRow(item.optString("_id", "?").toUpperCase(), item.optInt("count", 0) + " requests");
                                } catch (Exception ignored) {}
                            }
                            addSection("🤝 Contact Request Acceptance Rate");
                            addStatRow("Acceptance Rate", acceptanceRate);
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void addSection(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(16);
        tv.setTextColor(0xFFC62828);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad + 8, pad, 4);
        llAnalytics.addView(tv);
    }

    private void addStatRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int padH = (int)(16 * getResources().getDisplayMetrics().density);
        int padV = (int)(8 * getResources().getDisplayMetrics().density);
        row.setPadding(padH, padV, padH, padV);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(14);
        tvLabel.setTextColor(0xFF333333);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvLabel.setLayoutParams(lp);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextSize(14);
        tvValue.setTextColor(0xFF1565C0);
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(tvLabel);
        row.addView(tvValue);
        llAnalytics.addView(row);

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFFEEEEEE);
        llAnalytics.addView(divider);
    }
}
