package com.abhay.bloodradar.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.adapter.DonorAdapter;
import com.abhay.bloodradar.api.BloodRequestService;
import com.abhay.bloodradar.model.Donor;
import java.util.ArrayList;
import java.util.List;

public class DonorsFragment extends Fragment {
    private RecyclerView recyclerView;
    private DonorAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<Donor> donorList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donors, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDonors);
        swipeRefresh = view.findViewById(R.id.swipeRefreshDonors);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DonorAdapter(getContext(), donorList);
        recyclerView.setAdapter(adapter);

        // Set refresh loader colors to match theme
        swipeRefresh.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.red_gradient_start
        );
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white);

        swipeRefresh.setOnRefreshListener(this::loadDonors);

        loadDonors();

        return view;
    }

    private void loadDonors() {
        swipeRefresh.setRefreshing(true);
        BloodRequestService.getAllDonors(new BloodRequestService.DonorCallback() {
            @Override
            public void onSuccess(List<Donor> donors) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        donorList.clear();
                        donorList.addAll(donors);
                        adapter.updateData(donorList);
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

