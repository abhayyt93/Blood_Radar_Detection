package com.abhay.bloodradar;

import android.graphics.Rect;
import android.os.Bundle;
import android.content.Intent;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.abhay.bloodradar.repository.AuthRepository;
import com.abhay.bloodradar.utils.ToastUtil;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etPhone, etPassword, etCity, etBio;
    private AutoCompleteTextView etBloodGroup;
    private Button btnSignUp;
    private TextView tvLogin;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        initializeViews();
        setupClickListener();
        setupScrollBehavior();
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollViewRegister);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etCity = findViewById(R.id.etCity);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etBio = findViewById(R.id.etBio);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        // Setup blood group dropdown with custom layout
        String[] bloodGroups = getResources().getStringArray(R.array.blood_groups);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            R.layout.dropdown_item,
            bloodGroups
        );
        etBloodGroup.setAdapter(adapter);
        etBloodGroup.setDropDownBackgroundResource(R.drawable.dropdown_bg);
    }

    private void setupScrollBehavior() {
        // Keyboard open hone par focused field ko scroll karke visible karo
        View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = rootView.getHeight();
            int keyboardHeight = screenHeight - rect.bottom;

            if (keyboardHeight > screenHeight * 0.15) {
                // Keyboard khula hai — focused view ko scroll karke dikhao
                View focused = getCurrentFocus();
                if (focused != null) {
                    scrollView.postDelayed(() -> {
                        int[] location = new int[2];
                        focused.getLocationInWindow(location);
                        int fieldBottom = location[1] + focused.getHeight();
                        int visibleBottom = screenHeight - keyboardHeight;
                        if (fieldBottom > visibleBottom) {
                            int scrollBy = fieldBottom - visibleBottom + 32;
                            scrollView.smoothScrollBy(0, scrollBy);
                        }
                    }, 100);
                }
            }
        });
    }

    private void setupClickListener() {
        btnSignUp.setOnClickListener(v -> handleSignUp());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void handleSignUp() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (validateInput(name, phone, password, city, bloodGroup)) {
            Intent intent = new Intent(this, OtpVerificationActivity.class);
            intent.putExtra("NAME", name);
            intent.putExtra("PHONE", phone);
            intent.putExtra("PASSWORD", password);
            intent.putExtra("CITY", city);
            intent.putExtra("BLOOD_GROUP", bloodGroup);
            intent.putExtra("BIO", bio);
            startActivity(intent);
        }
    }

    private boolean validateInput(String name, String phone, String password, String city, String bloodGroup) {
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }
        if (phone.isEmpty() || phone.length() != 10) {
            etPhone.setError("Valid 10-digit phone required");
            etPhone.requestFocus();
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return false;
        }
        if (bloodGroup.isEmpty()) {
            etBloodGroup.setError("Select blood group");
            etBloodGroup.requestFocus(); // Might not work perfectly for dropdown but okay
            return false;
        }
        return true;
    }


    private void navigateToLogin() {
        startActivity(new android.content.Intent(this, MainActivity.class));
        finish();
    }

    private void clearFields() {
        etName.setText("");
        etPhone.setText("");
        etPassword.setText("");
        etCity.setText("");
        etBloodGroup.setText("");
        etBio.setText("");
    }
}
