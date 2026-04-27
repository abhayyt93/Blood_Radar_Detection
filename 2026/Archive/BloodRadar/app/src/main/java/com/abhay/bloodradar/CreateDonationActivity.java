package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abhay.bloodradar.api.DonationService;
import com.abhay.bloodradar.model.Donation;
import com.abhay.bloodradar.utils.ToastUtil;

public class CreateDonationActivity extends AppCompatActivity {
    private AutoCompleteTextView etBloodGroup;
    private EditText etCity, etMessage, etAvailableUntil;
    private Button btnSubmit;
    private ScrollView scrollView;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_donation);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set navigation icon (back arrow) to white
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
        }

        scrollView = findViewById(R.id.scrollViewDonation);
        etBloodGroup = findViewById(R.id.etDonationBloodGroup);
        etCity = findViewById(R.id.etDonationCity);
        etMessage = findViewById(R.id.etDonationMessage);
        etAvailableUntil = findViewById(R.id.etAvailableUntil);
        btnSubmit = findViewById(R.id.btnSubmitDonation);

        // Get token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        token = prefs.getString("token", null);

        // Setup blood group dropdown with custom layout
        String[] bloodGroups = getResources().getStringArray(R.array.blood_groups);
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, bloodGroups);
        etBloodGroup.setAdapter(bloodGroupAdapter);
        etBloodGroup.setDropDownBackgroundResource(R.drawable.dropdown_bg);

        // Setup scroll behavior
        setupScrollBehavior();

        // DatePicker for availableUntil — user type na kare, calendar se choose kare
        etAvailableUntil.setFocusable(false);
        etAvailableUntil.setFocusableInTouchMode(false);
        etAvailableUntil.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int year = cal.get(java.util.Calendar.YEAR);
            int month = cal.get(java.util.Calendar.MONTH);
            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(CreateDonationActivity.this,
                (datePicker, y, m, d) -> {
                    // Format: YYYY-MM-DD (MongoDB wala format)
                    String date = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                    etAvailableUntil.setText(date);
                }, year, month, day)
                .show();
        });

        btnSubmit.setOnClickListener(v -> createDonation());
    }

    private void setupScrollBehavior() {
        // Auto scroll when fields get focus
        View.OnFocusChangeListener scrollListener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> {
                    int scrollY = v.getTop() - 100;
                    scrollView.smoothScrollTo(0, scrollY);
                }, 100);
            }
        };

        etBloodGroup.setOnFocusChangeListener(scrollListener);
        etCity.setOnFocusChangeListener(scrollListener);
        etAvailableUntil.setOnFocusChangeListener(scrollListener);
        
        // Special handling for message field
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> {
                    scrollView.smoothScrollTo(0, etMessage.getBottom());
                }, 200);
            }
        });
    }

    private void createDonation() {
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String availableUntil = etAvailableUntil.getText().toString().trim();

        if (bloodGroup.isEmpty() || city.isEmpty() || message.isEmpty() || availableUntil.isEmpty()) {
            ToastUtil.showError(this, "Please fill all fields");
            return;
        }

        if (token == null) {
            ToastUtil.showError(this, "Please login first");
            finish();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Creating...");

        DonationService.createDonation(token, bloodGroup, city, message, availableUntil,
            new DonationService.CreateDonationCallback() {
                @Override
                public void onSuccess(Donation donation) {
                    runOnUiThread(() -> {
                        ToastUtil.showSuccess(CreateDonationActivity.this, "Donation post created successfully!");
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        ToastUtil.showError(CreateDonationActivity.this, "Error: " + error);
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("POST DONATION");
                    });
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
