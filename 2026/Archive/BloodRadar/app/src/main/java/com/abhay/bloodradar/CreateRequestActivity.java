package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abhay.bloodradar.api.BloodRequestService;
import com.abhay.bloodradar.model.BloodRequest;

public class CreateRequestActivity extends AppCompatActivity {
    private AutoCompleteTextView etBloodGroup, etUrgency;
    private EditText etCity, etMessage;
    private Button btnSubmit;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_request);

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


        etBloodGroup = findViewById(R.id.etRequestBloodGroup);
        etUrgency = findViewById(R.id.etUrgency);
        etCity = findViewById(R.id.etRequestCity);
        etMessage = findViewById(R.id.etRequestMessage);
        btnSubmit = findViewById(R.id.btnSubmitRequest);

        // Get token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        token = prefs.getString("token", null);

        // Setup blood group dropdown
        String[] bloodGroups = getResources().getStringArray(R.array.blood_groups);
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroups);
        etBloodGroup.setAdapter(bloodGroupAdapter);

        // Setup urgency dropdown
        String[] urgencyLevels = {"low", "medium", "high"};
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencyLevels);
        etUrgency.setAdapter(urgencyAdapter);
        etUrgency.setText("medium", false); // Default

        btnSubmit.setOnClickListener(v -> createRequest());
    }

    private void createRequest() {
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String urgency = etUrgency.getText().toString().trim();

        if (bloodGroup.isEmpty() || city.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Creating...");

        BloodRequestService.createRequest(token, bloodGroup, city, message, urgency,
            new BloodRequestService.CreateRequestCallback() {
                @Override
                public void onSuccess(BloodRequest request) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateRequestActivity.this, "Request created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateRequestActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit Request");
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

