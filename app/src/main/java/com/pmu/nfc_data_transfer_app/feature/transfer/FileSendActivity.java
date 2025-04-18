package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
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
import com.pmu.nfc_data_transfer_app.service.TransferManagerService;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;
import com.pmu.nfc_data_transfer_app.ui.util.FileSendUiHelper;

import java.util.ArrayList;

public class FileSendActivity extends AppCompatActivity implements TransferManagerService.TransferProgressCallback {

    // Constants
    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "receiver_mac_address";

    // UI Components
    private TextView titleText;
    private ProgressBar transferAnimation;
    private TextView transferStatusText;
    private ProgressBar progressIndicator;
    private TextView progressText;
    private RecyclerView filesRecyclerView;
    private Button cancelButton;
    private ConstraintLayout successContainer;
    private ImageView successAnimation;
    private TextView successText;
    private TextView transferSummary;
    private Button doneButton;
    private View filesListTitle;

    // Data
    private ArrayList<TransferFileItem> transferItems = new ArrayList<>();
    private String bluetoothDeviceAddress;
    private TransferFileAdapter adapter;

    // Helpers
    private TransferManagerService transferManager;
    private FileSendUiHelper uiHelper;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_send);

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);

        // Process intent data
        processIntent();

        // Initialize UI components
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Set up the UI helper
        uiHelper = new FileSendUiHelper(
                this, titleText, transferAnimation, transferStatusText,
                progressIndicator, progressText, filesRecyclerView,
                cancelButton, successContainer, successAnimation,
                successText, transferSummary, doneButton, filesListTitle, adapter
        );

        // Set up transfer manager and start the transfer
        setupTransfer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (transferManager != null) {
            transferManager.shutdown();
        }
    }

    private void setupTransfer() {
        transferManager = new TransferManagerService(transferItems, dbHelper, this);

        transferManager.startTransfer();
    }

    private void processIntent() {
        if (getIntent().hasExtra(EXTRA_FILE_ITEMS)) {
            ArrayList<? extends Parcelable> parcelables = getIntent().getParcelableArrayListExtra(EXTRA_FILE_ITEMS);

            if (parcelables != null) {
                for (Parcelable parcelable : parcelables) {
                    if (parcelable instanceof TransferFileItem) {
                        transferItems.add((TransferFileItem) parcelable);
                    }
                }
            }
        }

        if (getIntent().hasExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS)) {
            bluetoothDeviceAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS);
        }
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        transferAnimation = findViewById(R.id.transferAnimation);
        transferStatusText = findViewById(R.id.transferStatusText);
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

    private void setupRecyclerView() {
        adapter = new TransferFileAdapter(transferItems);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> {
            if (transferManager != null) {
                transferManager.cancelTransfer();
            }
            showTransferCancelled();
        });

        doneButton.setOnClickListener(v -> {
            // Return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showTransferCancelled() {
        Toast.makeText(this, R.string.transfer_canceled, Toast.LENGTH_LONG).show();

        // Return to previous screen after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 1500);
    }

    private void showTransferFailed() {
        Toast.makeText(this, "Transfer failed. Some files could not be transferred.", Toast.LENGTH_LONG).show();

        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 2000);
    }
    @Override
    public void onFileStatusUpdated(int index, FileTransferStatus status) {
        adapter.notifyItemChanged(index);
    }

    @Override
    public void onProgressUpdated(int completedFiles, int totalFiles, int progress) {
        uiHelper.updateProgressText(completedFiles, totalFiles, progress);
    }

    @Override
    public void onFileProgressUpdated(int fileIndex, int progress) {
        uiHelper.updateFileProgress(fileIndex, progress);
    }

    @Override
    public void onTransferCompleted(boolean success) {
        if (success) {
            uiHelper.showTransferCompleted(transferManager.getTotalFiles(), transferManager.getTotalSize());
        } else {
            uiHelper.showTransferFailed(
                    transferManager.getCompletedFiles(),
                    transferManager.getTotalFiles(),
                    transferManager.getTotalSize()
            );

            showTransferFailed();
        }
    }

    @Override
    public void onFileTransferFailed(int fileIndex, String errorMessage) {
        Toast.makeText(this, "Failed to transfer file: " + transferItems.get(fileIndex).getName(),
                Toast.LENGTH_SHORT).show();
    }

    public static void start(
            AppCompatActivity activity,
            ArrayList<TransferFileItem> fileItems,
            String bluetoothDeviceAddress
    ) {
        Intent intent = new Intent(activity, FileSendActivity.class);

        intent.putParcelableArrayListExtra(EXTRA_FILE_ITEMS, fileItems);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);
        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}