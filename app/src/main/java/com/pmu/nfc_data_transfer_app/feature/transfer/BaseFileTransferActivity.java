package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;
import com.pmu.nfc_data_transfer_app.feature.main.MainActivity;
import com.pmu.nfc_data_transfer_app.service.BaseTransferManagerService;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;
import com.pmu.nfc_data_transfer_app.ui.util.BaseFileTransferUiHelper;

import java.util.ArrayList;

/**
 * Base activity class for file transfer activities
 * Contains common functionality for both sending and receiving files
 */
public abstract class BaseFileTransferActivity extends AppCompatActivity implements BaseTransferManagerService.TransferProgressCallback {

    // UI Components
    protected TextView titleText;
    protected ProgressBar transferAnimation;
    protected TextView statusText;
    protected ProgressBar progressIndicator;
    protected TextView progressText;
    protected RecyclerView filesRecyclerView;
    protected Button cancelButton;
    protected ConstraintLayout successContainer;
    protected ImageView successAnimation;
    protected TextView successText;
    protected TextView transferSummary;
    protected Button doneButton;
    protected View filesListTitle;

    // Data
    protected ArrayList<TransferFileItem> transferItems = new ArrayList<>();

    protected TransferFileAdapter adapter;

    // Helpers
    protected DatabaseHelper dbHelper;
    protected BaseFileTransferUiHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);

        // Process intent data
        processIntent();

        // Initialize UI components
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Set up the UI helper
        setupUiHelper();

        // Set up transfer and start it
        setupTransfer();
    }

    /**
     * Get the layout resource for this activity
     * 
     * @return Layout resource ID
     */
    protected abstract int getLayoutResourceId();

    /**
     * Process the intent data
     */
    protected abstract void processIntent();

    /**
     * Set up the UI helper
     */
    protected abstract void setupUiHelper();

    /**
     * Set up the transfer manager and start the transfer
     */
    protected abstract void setupTransfer();

    /**
     * Initialize views by finding them in the layout
     */
    protected void initViews() {
        titleText = findViewById(R.id.titleText);
        transferAnimation = findViewById(R.id.transferAnimation);
        statusText = findViewById(R.id.transferStatusText);
        progressIndicator = findViewById(R.id.progressIndicator);
        progressText = findViewById(R.id.progressText);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        cancelButton = findViewById(R.id.cancelButton);
        successContainer = findViewById(R.id.successContainer);
        successAnimation = findViewById(R.id.successAnimation);
        successText = findViewById(R.id.successText);
        transferSummary = findViewById(R.id.transferSummary);
        doneButton = findViewById(R.id.doneButton);
        filesListTitle = findViewById(R.id.filesListTitle);
    }

    /**
     * Set up the RecyclerView with an adapter
     */
    protected void setupRecyclerView() {
        adapter = new TransferFileAdapter(transferItems);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesRecyclerView.setAdapter(adapter);
    }

    /**
     * Set up click listeners for buttons
     */
    protected void setupClickListeners() {
        cancelButton.setOnClickListener(v -> {
            onCancelClicked();
        });

        doneButton.setOnClickListener(v -> {
            // Return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Handler for cancel button clicks
     */
    protected abstract void onCancelClicked();

    /**
     * Show a toast that the transfer was cancelled
     * 
     * @param messageResId Resource ID for the cancel message
     */
    protected void showCancelledToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();

        // Return to previous screen after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
    }

    /**
     * Show a toast that the transfer failed
     * 
     * @param message Failure message
     */
    protected void showFailedToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
    }

    @Override
    public void onFileStatusUpdated(int index, FileTransferStatus status) {
        adapter.notifyItemChanged(index);
    }

    @Override
    public void onFileProgressUpdated(int fileIndex, int progress) {
        if (uiHelper != null) {
            uiHelper.updateFileProgress(fileIndex, progress);
        }
    }
}