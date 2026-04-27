package com.abhay.bloodradar.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abhay.bloodradar.R;
import com.abhay.bloodradar.adapter.ConversationAdapter;
import com.abhay.bloodradar.api.ChatService;
import com.abhay.bloodradar.model.Conversation;
import com.abhay.bloodradar.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Get user token
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserData", requireContext().MODE_PRIVATE);
        token = prefs.getString("token", null);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ConversationAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadConversations);

        // Load conversations
        loadConversations();

        return view;
    }

    private void loadConversations() {
        // Check if token exists
        if (token == null || token.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Please login again\n\nYour session has expired");
            ToastUtil.showError(getContext(), "Session expired. Please login again.");
            return;
        }

        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        tvEmptyState.setVisibility(View.GONE);

        ChatService.getConversations(token, new ChatService.ConversationsCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        
                        if (conversations.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("No conversations yet\n\nStart chatting by tapping the\nChat button on any blood request\nor donation");
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.updateData(conversations);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        
                        // Show specific error message
                        if (error.contains("token") || error.contains("expired") || error.contains("Invalid")) {
                            tvEmptyState.setText("Session expired\n\nPlease logout and login again");
                            ToastUtil.showError(getContext(), "Session expired. Please login again.");
                        } else {
                            tvEmptyState.setText("Failed to load conversations\n\n" + error);
                            ToastUtil.showError(getContext(), "Error: " + error);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload conversations when fragment becomes visible
        if (token != null) {
            loadConversations();
        }
    }
}
