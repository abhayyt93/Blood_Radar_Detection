package com.abhay.bloodradar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abhay.bloodradar.adapter.ContactRequestAdapter;
import com.abhay.bloodradar.api.ContactRequestService;

public class ContactRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ContactRequestAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutEmptyRequests;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_requests);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
        }

        token = getSharedPreferences("UserData", MODE_PRIVATE).getString("token", null);

        recyclerView = findViewById(R.id.recyclerViewRequests);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutEmptyRequests = findViewById(R.id.layoutEmptyRequests);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactRequestAdapter(this, token);
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadRequests);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        loadRequests();
    }

    private void loadRequests() {
        swipeRefresh.setRefreshing(true);

        if (token == null) {
            swipeRefresh.setRefreshing(false);
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        ContactRequestService.getReceivedRequests(token, new ContactRequestService.ReceivedRequestsCallback() {
            @Override
            public void onSuccess(org.json.JSONArray requests) {
                runOnUiThread(() -> {
                    adapter.updateData(requests);
                    swipeRefresh.setRefreshing(false);

                    if (requests.length() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        layoutEmptyRequests.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutEmptyRequests.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(ContactRequestsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
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
