package com.abhay.bloodradar.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.adapter.DonationAdapter;
import com.abhay.bloodradar.api.DonationService;
import com.abhay.bloodradar.model.Donation;
import java.util.ArrayList;
import java.util.List;

public class DonationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private DonationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<Donation> donationList = new ArrayList<>();
    private AutoCompleteTextView etFilterCity, etFilterBloodGroup;
    private android.view.View layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donations, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDonations);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        etFilterCity = view.findViewById(R.id.etFilterCity);
        etFilterBloodGroup = view.findViewById(R.id.etFilterBloodGroup);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DonationAdapter(getContext(), donationList);
        recyclerView.setAdapter(adapter);

        // Setup blood group filter dropdown with compact layout
        String[] bloodGroups = getResources().getStringArray(R.array.blood_groups);
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item_filter, bloodGroups);
        etFilterBloodGroup.setAdapter(bloodGroupAdapter);
        etFilterBloodGroup.setDropDownBackgroundResource(R.drawable.dropdown_bg);

        // Filter listeners
        etFilterCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });

        etFilterBloodGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });

        // Set refresh loader colors to match theme
        swipeRefresh.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.red_gradient_start
        );
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white);
        
        swipeRefresh.setOnRefreshListener(this::loadDonations);

        loadDonations();

        return view;
    }

    private void applyFilters() {
        String city = etFilterCity.getText().toString().trim();
        String bloodGroup = etFilterBloodGroup.getText().toString().trim();

        if (city.isEmpty() && bloodGroup.isEmpty()) {
            loadDonations();
        } else {
            searchDonations(city, bloodGroup);
        }
    }

    private void showEmptyState(boolean empty, String city, String bloodGroup) {
        if (empty) {
            recyclerView.setVisibility(android.view.View.GONE);
            layoutEmptyState.setVisibility(android.view.View.VISIBLE);
        } else {
            recyclerView.setVisibility(android.view.View.VISIBLE);
            layoutEmptyState.setVisibility(android.view.View.GONE);
        }
    }

    private void loadDonations() {
        swipeRefresh.setRefreshing(true);
        DonationService.getAllDonations(new DonationService.DonationCallback() {
            @Override
            public void onSuccess(List<Donation> donations) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        donationList.clear();
                        donationList.addAll(donations);
                        adapter.updateData(donationList);
                        showEmptyState(donations.isEmpty(), "", "");
                        swipeRefresh.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    });
                }
            }
        });
    }

    private void searchDonations(String city, String bloodGroup) {
        swipeRefresh.setRefreshing(true);
        DonationService.searchDonations(city, bloodGroup, new DonationService.DonationCallback() {
            @Override
            public void onSuccess(List<Donation> donations) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        donationList.clear();
                        donationList.addAll(donations);
                        adapter.updateData(donationList);
                        showEmptyState(donations.isEmpty(), city, bloodGroup);
                        swipeRefresh.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    });
                }
            }
        });
    }
}
