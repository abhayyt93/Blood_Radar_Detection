package com.abhay.bloodradar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.abhay.bloodradar.api.AppConfig;
import com.abhay.bloodradar.utils.ToastUtil;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private EditText etPhone, etOtp, etNewPassword, etConfirmPassword;
    private MaterialButton btnSendOtp, btnVerifyOtp, btnResetPassword;
    private TextView tvOtpSentTo, tvResendOtp;
    private ImageButton btnBack;

    private String phoneNumber = "";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_screen);

        initViews();
        setupListeners();
    }

    private void initViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        tvOtpSentTo = findViewById(R.id.tvOtpSentTo);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSendOtp.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (phone.length() != 10) {
                ToastUtil.showError(this, "Please enter a valid 10-digit phone number");
                return;
            }
            phoneNumber = phone;
            sendForgotOtp(phone);
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                ToastUtil.showError(this, "Please enter the OTP");
                return;
            }
            // OTP verified on backend during reset — just move to step 3
            showStep(3);
        });

        tvResendOtp.setOnClickListener(v -> sendForgotOtp(phoneNumber));

        btnResetPassword.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (otp.isEmpty()) {
                ToastUtil.showError(this, "OTP is missing. Please go back and verify.");
                return;
            }
            if (newPass.length() < 6) {
                ToastUtil.showError(this, "Password must be at least 6 characters");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                ToastUtil.showError(this, "Passwords do not match");
                return;
            }
            resetPassword(phoneNumber, otp, newPass);
        });
    }

    private void sendForgotOtp(String phone) {
        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Sending...");

        executor.execute(() -> {
            try {
                URL url = new URL(AppConfig.BASE_URL + "/auth/forgot-password");
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
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                java.io.InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                JSONObject json = new JSONObject(response);

                boolean success = json.optBoolean("success", false);
                String message = json.optString("message", "");

                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    btnSendOtp.setText("SEND OTP");
                    if (success) {
                        tvOtpSentTo.setText("OTP sent to +" + phone);
                        showStep(2);
                        ToastUtil.showSuccess(this, "OTP sent successfully!");
                    } else {
                        ToastUtil.showError(this, message);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSendOtp.setEnabled(true);
                    btnSendOtp.setText("SEND OTP");
                    ToastUtil.showError(this, "Network error: " + e.getMessage());
                });
            }
        });
    }

    private void resetPassword(String phone, String otp, String newPassword) {
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");

        executor.execute(() -> {
            try {
                URL url = new URL(AppConfig.BASE_URL + "/auth/reset-password");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject body = new JSONObject();
                body.put("phone", phone);
                body.put("otp", otp);
                body.put("newPassword", newPassword);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                java.io.InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                JSONObject json = new JSONObject(response);

                boolean success = json.optBoolean("success", false);
                String message = json.optString("message", "");

                runOnUiThread(() -> {
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("RESET PASSWORD");
                    if (success) {
                        ToastUtil.showSuccess(this, message);
                        // Go back to login
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        ToastUtil.showError(this, message);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("RESET PASSWORD");
                    ToastUtil.showError(this, "Network error: " + e.getMessage());
                });
            }
        });
    }

    private void showStep(int step) {
        layoutStep1.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
