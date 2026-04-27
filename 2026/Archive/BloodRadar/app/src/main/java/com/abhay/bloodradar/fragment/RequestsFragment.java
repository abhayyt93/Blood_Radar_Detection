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
import com.abhay.bloodradar.adapter.BloodRequestAdapter;
import com.abhay.bloodradar.api.BloodRequestService;
import com.abhay.bloodradar.model.BloodRequest;
import java.util.ArrayList;
import java.util.List;

public class RequestsFragment extends Fragment {
    private RecyclerView recyclerView;
    private BloodRequestAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<BloodRequest> requestList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRequests);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BloodRequestAdapter(getContext(), requestList);
        recyclerView.setAdapter(adapter);

        // Set refresh loader colors to match theme
        swipeRefresh.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.red_gradient_start
        );
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white);

        swipeRefresh.setOnRefreshListener(this::loadRequests);

        loadRequests();

        return view;
    }

    private void loadRequests() {
        swipeRefresh.setRefreshing(true);
        BloodRequestService.getAllRequests(new BloodRequestService.RequestCallback() {
            @Override
            public void onSuccess(List<BloodRequest> requests) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        requestList.clear();
                        requestList.addAll(requests);
                        adapter.updateData(requestList);
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

