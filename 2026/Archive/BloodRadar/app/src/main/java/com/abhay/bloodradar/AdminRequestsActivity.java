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

public class AdminRequestsActivity extends AppCompatActivity {

    private LinearLayout llRequestList;
    private TextView tvRequestCount;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_requests);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        llRequestList = findViewById(R.id.llRequestList);
        tvRequestCount = findViewById(R.id.tvRequestCount);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadRequests();
    }

    private void loadRequests() {
        String url = AppConfig.BASE_URL + "/admin/requests";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminRequestsActivity.this, "Failed to load", Toast.LENGTH_SHORT).show());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    if (json.optBoolean("success")) {
                        JSONArray requests = json.getJSONArray("data");
                        runOnUiThread(() -> {
                            tvRequestCount.setText("Total: " + requests.length() + " requests");
                            llRequestList.removeAllViews();
                            for (int i = 0; i < requests.length(); i++) {
                                try { addRequestCard(requests.getJSONObject(i)); }
                                catch (Exception ignored) {}
                            }
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void addRequestCard(JSONObject req) {
        String reqId = req.optString("_id");
        String bloodGroup = req.optString("bloodGroup", "?");
        String city = req.optString("city", "?");
        String urgency = req.optString("urgency", "medium");
        String status = req.optString("status", "active");
        String message = req.optString("message", "");
        JSONObject user = req.optJSONObject("user");
        String userName = user != null ? user.optString("name", "Unknown") : "Unknown";

        View card = getLayoutInflater().inflate(R.layout.item_admin_request, llRequestList, false);

        TextView tvBlood = card.findViewById(R.id.tvBloodGroup);
        TextView tvCityUrgency = card.findViewById(R.id.tvCityUrgency);
        TextView tvMessage = card.findViewById(R.id.tvMessage);
        TextView tvUserName = card.findViewById(R.id.tvUserName);
        TextView tvStatus = card.findViewById(R.id.tvStatus);
        View btnDelete = card.findViewById(R.id.btnDeleteRequest);
        View btnFulfill = card.findViewById(R.id.btnFulfillRequest);

        tvBlood.setText(bloodGroup);
        tvCityUrgency.setText(city + " • " + urgency.toUpperCase());
        tvMessage.setText(message);
        tvUserName.setText("By: " + userName);
        tvStatus.setText(status.toUpperCase());

        int urgencyColor = urgency.equals("critical") ? 0xFFE53935 :
                           urgency.equals("high") ? 0xFFF57C00 :
                           urgency.equals("medium") ? 0xFF1565C0 : 0xFF424242;
        tvCityUrgency.setTextColor(urgencyColor);

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete Request?")
                .setMessage("Remove this blood request permanently?")
                .setPositiveButton("Delete", (d, w) -> deleteRequest(reqId))
                .setNegativeButton("Cancel", null).show());

        btnFulfill.setOnClickListener(v -> updateStatus(reqId, "fulfilled"));
        llRequestList.addView(card);
    }

    private void deleteRequest(String id) {
        String url = AppConfig.BASE_URL + "/admin/requests/" + id;
        client.newCall(new Request.Builder().url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .delete().build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> { Toast.makeText(AdminRequestsActivity.this, "Deleted", Toast.LENGTH_SHORT).show(); loadRequests(); });
            }
        });
    }

    private void updateStatus(String id, String status) {
        String url = AppConfig.BASE_URL + "/admin/requests/" + id + "/status";
        JSONObject body = new JSONObject();
        try { body.put("status", status); } catch (Exception ignored) {}
        client.newCall(new Request.Builder().url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .patch(RequestBody.create(body.toString(), MediaType.parse("application/json"))).build())
                .enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> { Toast.makeText(AdminRequestsActivity.this, "Marked fulfilled", Toast.LENGTH_SHORT).show(); loadRequests(); });
            }
        });
    }
}
