package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.abhay.bloodradar.api.AppConfig;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class AdminBroadcastActivity extends AppCompatActivity {

    private EditText etMessage;
    private EditText etTitle;
    private EditText etCity;
    private Spinner spinnerBloodGroup;
    private String authToken;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_broadcast);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        authToken = prefs.getString("token", "");

        etMessage = findViewById(R.id.etBroadcastMessage);
        etTitle = findViewById(R.id.etBroadcastTitle);
        etCity = findViewById(R.id.etBroadcastCity);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Blood group spinner options
        String[] bloodGroups = {"Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bloodGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);

        // Send to ALL users
        findViewById(R.id.btnSendToAll).setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            if (message.isEmpty()) { Toast.makeText(this, "Message is required", Toast.LENGTH_SHORT).show(); return; }
            sendBroadcast("/admin/broadcast/all", createJson(title, message, null, null), message);
        });

        // Send to City
        findViewById(R.id.btnSendToCity).setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            if (message.isEmpty() || city.isEmpty()) { Toast.makeText(this, "Message and City are required", Toast.LENGTH_SHORT).show(); return; }
            sendBroadcast("/admin/broadcast/city", createJson(title, message, city, null), message);
        });

        // Send to Blood Group
        findViewById(R.id.btnSendToBloodGroup).setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            String bg = spinnerBloodGroup.getSelectedItem().toString();
            if (message.isEmpty() || bg.equals("Select Blood Group")) {
                Toast.makeText(this, "Message and Blood Group are required", Toast.LENGTH_SHORT).show(); return;
            }
            sendBroadcast("/admin/broadcast/blood-group", createJson(title, message, null, bg), message);
        });
    }

    private JSONObject createJson(String title, String message, String city, String bloodGroup) {
        JSONObject json = new JSONObject();
        try {
            json.put("message", message);
            if (title != null && !title.isEmpty()) json.put("title", title);
            if (city != null) json.put("city", city);
            if (bloodGroup != null) json.put("bloodGroup", bloodGroup);
        } catch (Exception ignored) {}
        return json;
    }

    private void sendBroadcast(String endpoint, JSONObject extra, String message) {
        try {
            RequestBody body = RequestBody.create(extra.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(AppConfig.BASE_URL + endpoint)
                    .addHeader("Authorization", "Bearer " + authToken)
                    .post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(AdminBroadcastActivity.this, "Failed to send", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String resp = response.body().string();
                        JSONObject json = new JSONObject(resp);
                        String msg = json.optString("message", "Sent!");
                        runOnUiThread(() -> {
                            Toast.makeText(AdminBroadcastActivity.this, "✅ " + msg, Toast.LENGTH_LONG).show();
                            etMessage.setText("");
                            etTitle.setText("");
                            etCity.setText("");
                            spinnerBloodGroup.setSelection(0);
                        });
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }
}
