package com.abhay.bloodradar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.abhay.bloodradar.api.AppConfig;
import com.abhay.bloodradar.repository.AuthRepository;
import com.abhay.bloodradar.utils.ToastUtil;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerify;
    private TextView tvResend, tvSubtitle;
    private ProgressBar progressBar;

    private String name, phone, password, city, bloodGroup, bio;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        initViews();
        getIntentData();
        sendOtp(); // OTP bhejo jab activity khule
    }

    private void initViews() {
        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerify);
        tvResend = findViewById(R.id.tvResend);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        progressBar = findViewById(R.id.progressBar);

        btnVerify.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (code.isEmpty() || code.length() < 4) {
                etOtp.setError("Enter OTP");
                return;
            }
            verifyOtp(code);
        });

        tvResend.setOnClickListener(v -> sendOtp());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        name = intent.getStringExtra("NAME");
        phone = intent.getStringExtra("PHONE");
        password = intent.getStringExtra("PASSWORD");
        city = intent.getStringExtra("CITY");
        bloodGroup = intent.getStringExtra("BLOOD_GROUP");
        bio = intent.getStringExtra("BIO");

        if (phone != null) {
            tvSubtitle.setText("Enter the OTP sent to +91" + phone);
        }
    }

    // OTP bhejo backend ke through (2Factor.in)
    private void sendOtp() {
        if (phone == null || phone.isEmpty()) return;
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        executor.execute(() -> {
            try {
                URL url = new URL(AppConfig.BASE_URL + "/auth/send-otp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject body = new JSONObject();
                body.put("phone", phone);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.close();

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String responseBody = scanner.hasNext() ? scanner.next() : "";
                conn.disconnect();

                JSONObject response = new JSONObject(responseBody);
                boolean success = response.optBoolean("success", false);
                String message = response.optString("message", "");

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    if (success) {
                        ToastUtil.showSuccess(this, "OTP sent to +91" + phone);
                    } else {
                        ToastUtil.showError(this, "Failed: " + message);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    ToastUtil.showError(this, "Error: " + e.getMessage());
                });
            }
        });
    }

    // OTP verify karo backend se
    private void verifyOtp(String otp) {
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);

        executor.execute(() -> {
            try {
                URL url = new URL(AppConfig.BASE_URL + "/auth/verify-otp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject body = new JSONObject();
                body.put("phone", phone);
                body.put("otp", otp);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.close();

                int responseCode = conn.getResponseCode();
                java.io.InputStream is = responseCode >= 200 && responseCode < 300
                        ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String responseBody = scanner.hasNext() ? scanner.next() : "";
                conn.disconnect();

                JSONObject response = new JSONObject(responseBody);
                boolean success = response.optBoolean("success", false);
                String message = response.optString("message", "");

                runOnUiThread(() -> {
                    if (success) {
                        // OTP verified! Ab backend par register karo
                        registerUserOnBackend();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnVerify.setEnabled(true);
                        ToastUtil.showError(this, message);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    ToastUtil.showError(this, "Error: " + e.getMessage());
                });
            }
        });
    }

    // OTP verify hone ke baad user register karo
    private void registerUserOnBackend() {
        AuthRepository.registerUser(name, phone, password, city, bloodGroup, bio,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onRegistrationSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            String msg = response.optString("message", "Registration successful! Please login.");
                            ToastUtil.showSuccess(OtpVerificationActivity.this, msg);
                            new android.os.Handler().postDelayed(() -> navigateToLogin(), 1000);
                        });
                    }

                    @Override
                    public void onRegistrationError(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnVerify.setEnabled(true);
                            ToastUtil.showError(OtpVerificationActivity.this, "Registration Failed: " + error);
                        });
                    }

                    @Override
                    public void onLoginSuccess(JSONObject response) {}

                    @Override
                    public void onLoginError(String error) {}
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
