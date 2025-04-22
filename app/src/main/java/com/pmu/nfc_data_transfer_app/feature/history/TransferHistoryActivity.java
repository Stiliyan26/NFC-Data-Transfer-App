package com.pmu.nfc_data_transfer_app.feature.history;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferHistoryAdapter;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;

import java.util.ArrayList;
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

            adapter = new TransferHistoryAdapter(this);

            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views: " + e.getMessage());
        }
    }

    private void loadTransferHistory() {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            historyItems = dbHelper.getAllDevicesInfo();

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

    @Override
    public void onHistoryItemClicked(TransferHistory history) {
        try {
            Intent intent = new Intent(this, TransferDetailsActivity.class);

            intent.putExtra("TRANSFER_ID", history.getId());

            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to details: " + e.getMessage());
        }
    }

    @Override
    public void onHistoryItemRemoved(TransferHistory history) {
        removeHistoryItem(history);
    }

    private void removeHistoryItem(TransferHistory history) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Премахване на история")
                    .setMessage("Сигурни ли сте, че искате да изтриете тази история на трансфер?")
                    .setPositiveButton("Изтрий", (dialog, which) -> {
                        try {
                            int position = -1;
                            for (int i = 0; i < historyItems.size(); i++) {
                                if (historyItems.get(i).getId() == history.getId()) {
                                    position = i;
                                    break;
                                }
                            }

                            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
                            boolean success = dbHelper.deleteTransferById(history.getId());

                            if (success && position != -1) {
                                historyItems.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, historyItems.size());

                                if (historyItems.isEmpty()) {
                                    showEmptyState();
                                }
                            } else {
                                loadTransferHistory();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting history: " + e.getMessage());
                            loadTransferHistory();
                        }
                    })
                    .setNegativeButton("Отказ", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete dialog: " + e.getMessage());
        }
    }
}