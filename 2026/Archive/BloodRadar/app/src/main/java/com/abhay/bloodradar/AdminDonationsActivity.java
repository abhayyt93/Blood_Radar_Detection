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

public class AdminDonationsActivity extends AppCompatActivity {

    private LinearLayout llDonationList;
    private TextView tvDonationCount;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_donations);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llDonationList = findViewById(R.id.llDonationList);
        tvDonationCount = findViewById(R.id.tvDonationCount);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadDonations();
    }

    private void loadDonations() {
        String url = AppConfig.BASE_URL + "/admin/donations";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminDonationsActivity.this, "Failed to load", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    if (json.optBoolean("success")) {
                        JSONArray donations = json.getJSONArray("data");
                        runOnUiThread(() -> {
                            tvDonationCount.setText("Total: " + donations.length() + " donations");
                            llDonationList.removeAllViews();
                            for (int i = 0; i < donations.length(); i++) {
                                try { addDonationCard(donations.getJSONObject(i)); }
                                catch (Exception ignored) {}
                            }
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void addDonationCard(JSONObject donation) {
        String donationId = donation.optString("_id");
        String bloodGroup = donation.optString("bloodGroup", "?");
        String city = donation.optString("city", "?");
        String status = donation.optString("status", "active");
        String message = donation.optString("message", "");
        String availableUntil = donation.optString("availableUntil", "");
        JSONObject user = donation.optJSONObject("user");
        String userName = user != null ? user.optString("name", "Unknown") : "Unknown";

        View card = getLayoutInflater().inflate(R.layout.item_admin_donation, llDonationList, false);

        ((TextView) card.findViewById(R.id.tvBloodGroup)).setText(bloodGroup);
        ((TextView) card.findViewById(R.id.tvCity)).setText(city);
        ((TextView) card.findViewById(R.id.tvMessage)).setText(message);
        ((TextView) card.findViewById(R.id.tvDonorName)).setText("Donor: " + userName);
        TextView tvStatus = card.findViewById(R.id.tvStatus);
        tvStatus.setText(status.toUpperCase());
        tvStatus.setTextColor(status.equals("active") ? 0xFF2E7D32 : 0xFF757575);

        card.findViewById(R.id.btnDeleteDonation).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                    .setTitle("Delete Donation Post?")
                    .setMessage("Remove this donation post permanently?")
                    .setPositiveButton("Delete", (d, w) -> deleteDonation(donationId))
                    .setNegativeButton("Cancel", null).show()
        );

        llDonationList.addView(card);
    }

    private void deleteDonation(String id) {
        String url = AppConfig.BASE_URL + "/admin/donations/" + id;
        client.newCall(new Request.Builder().url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .delete().build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> { Toast.makeText(AdminDonationsActivity.this, "Deleted", Toast.LENGTH_SHORT).show(); loadDonations(); });
            }
        });
    }
}
