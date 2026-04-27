package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.abhay.bloodradar.repository.AuthRepository;
import com.abhay.bloodradar.utils.ToastUtil;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etPhone, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String userData = prefs.getString("user", null);
        if (userData != null) {
            // User already logged in, go to home
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.login_screen);

        initializeViews();
        setupClickListeners();
        setupScrollBehavior();
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        etPhone = findViewById(R.id.etEmail);  // Using etEmail ID from layout
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvSignUp.setOnClickListener(v -> navigateToSignUp());
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void setupScrollBehavior() {
        // Auto scroll when fields get focus
        View.OnFocusChangeListener scrollListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> {
                    int scrollY = v.getTop() - 100; // 100dp offset from top
                    scrollView.smoothScrollTo(0, scrollY);
                }, 100);
            }
        };
        
        etPhone.setOnFocusChangeListener(scrollListener);
        etPassword.setOnFocusChangeListener(scrollListener);
    }

    private void handleLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        AuthRepository.loginUser(phone, password,
                new AuthRepository.AuthCallback() {
                    @Override
                    public void onLoginSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            String msg = response.optString("message", "Login successful");
                            ToastUtil.showSuccess(MainActivity.this, msg);
                            JSONObject data = response.optJSONObject("data");
                            if (data != null && data.has("user")) {
                                JSONObject userObj = data.optJSONObject("user");
                                String token = data.optString("token", "");
                                String role = userObj != null ? userObj.optString("role", "user") : "user";

                                // Save user data, token, and role to SharedPreferences
                                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("user", userObj.toString());
                                editor.putString("token", token);
                                editor.putString("role", role);
                                editor.apply();

                                // 🔀 Role-based routing
                                Intent intent;
                                if ("admin".equals(role)) {
                                    intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                                } else {
                                    intent = new Intent(MainActivity.this, HomeActivity.class);
                                }
                                startActivity(intent);
                                finish();
                            }
                            clearFields();
                        });
                    }

                    @Override
                    public void onLoginError(String error) {
                        runOnUiThread(() ->
                            ToastUtil.showError(MainActivity.this, error)
                        );
                    }

                    @Override
                    public void onRegistrationSuccess(JSONObject response) {
                        // Not used in MainActivity
                    }

                    @Override
                    public void onRegistrationError(String error) {
                        // Not used in MainActivity
                    }
                });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void clearFields() {
        etPhone.setText("");
        etPassword.setText("");
    }
}