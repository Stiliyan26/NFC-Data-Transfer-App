package com.pmu.nfc_data_transfer_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.pmu.nfc_data_transfer_app.data.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferHistoryAdapter;
import com.pmu.nfc_data_transfer_app.ui.helpers.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransferHistoryActivity extends AppCompatActivity implements TransferHistoryAdapter.HistoryItemListener {

    private static final String TAG = "TransferHistoryActivity";
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private TransferHistoryAdapter adapter;
    private List<TransferHistory> historyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_history);

        setupToolbar();
        setupViews();
        loadTransferHistory();
    }

    private void setupToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
            } else {
                Log.e(TAG, "Toolbar is null in setupToolbar()");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage());
        }
    }

    private void setupViews() {
        try {
            recyclerView = findViewById(R.id.historyRecyclerView);
            emptyStateLayout = findViewById(R.id.emptyStateLayout);

            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView is null in setupViews()");
                return;
            }

            adapter = new TransferHistoryAdapter(this);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views: " + e.getMessage());
        }
    }

    private void loadTransferHistory() {
        try {
            // In a real app, this would load from a database or shared preferences
            // For demo purposes, we'll create some sample data
            historyItems = getSampleHistoryItems();

            if (historyItems.isEmpty()) {
                showEmptyState();
            } else {
                showHistoryList();
                if (adapter != null) {
                    adapter.setHistoryItems(historyItems);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading history: " + e.getMessage());
            showEmptyState();
        }
    }

    private void showEmptyState() {
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void showHistoryList() {
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
    }

    private List<TransferHistory> getSampleHistoryItems() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        return db.getAllTransferEvents();
    }

    @Override
    public void onHistoryItemClicked(TransferHistory history) {
        try {
            Intent intent = new Intent(this, TransferDetailsActivity.class);
            intent.putExtra("TRANSFER_ID", history.getId());
            // Also pass the device name to ensure it's used in details
            intent.putExtra("DEVICE_NAME", history.getDeviceName());
            intent.putExtra("TRANSFER_TYPE", history.getTransferType());
            intent.putExtra("TRANSFER_DATE", history.getTransferDate().getTime());
            intent.putExtra("TOTAL_SIZE", history.getTotalSize());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to details: " + e.getMessage());
        }
    }
}