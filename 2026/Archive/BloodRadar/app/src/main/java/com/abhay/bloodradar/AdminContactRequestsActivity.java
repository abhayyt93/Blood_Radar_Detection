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

public class AdminContactRequestsActivity extends AppCompatActivity {

    private LinearLayout llContactList;
    private TextView tvContactCount;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_contact_requests);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llContactList = findViewById(R.id.llContactList);
        tvContactCount = findViewById(R.id.tvContactCount);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadContactRequests();
    }

    private void loadContactRequests() {
        String url = AppConfig.BASE_URL + "/admin/contact-requests";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminContactRequestsActivity.this, "Failed to load", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    if (json.optBoolean("success")) {
                        JSONArray items = json.getJSONArray("data");
                        runOnUiThread(() -> {
                            tvContactCount.setText("Total: " + items.length() + " contact requests");
                            llContactList.removeAllViews();
                            for (int i = 0; i < items.length(); i++) {
                                try { addContactCard(items.getJSONObject(i)); }
                                catch (Exception ignored) {}
                            }
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void addContactCard(JSONObject item) {
        String status = item.optString("status", "pending");
        JSONObject requester = item.optJSONObject("requester");
        JSONObject donor = item.optJSONObject("donor");
        JSONObject donation = item.optJSONObject("donation");

        String requesterName = requester != null ? requester.optString("name", "?") : "?";
        String donorName = donor != null ? donor.optString("name", "?") : "?";
        String bloodGroup = donation != null ? donation.optString("bloodGroup", "?") : "?";
        String city = donation != null ? donation.optString("city", "?") : "?";

        View card = getLayoutInflater().inflate(R.layout.item_admin_contact_request, llContactList, false);

        ((TextView) card.findViewById(R.id.tvRequesterName)).setText("👤 " + requesterName + " → " + donorName);
        ((TextView) card.findViewById(R.id.tvDonationInfo)).setText("🩸 " + bloodGroup + " • " + city);
        TextView tvStatus = card.findViewById(R.id.tvStatus);
        tvStatus.setText(status.toUpperCase());
        int color = status.equals("accepted") ? 0xFF2E7D32 :
                    status.equals("rejected") ? 0xFFE53935 : 0xFFF57C00;
        tvStatus.setTextColor(color);

        llContactList.addView(card);
    }
}
