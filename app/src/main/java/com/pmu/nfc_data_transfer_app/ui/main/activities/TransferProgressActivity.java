package com.pmu.nfc_data_transfer_app.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.pmu.nfc_data_transfer_app.data.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.main.helpers.TransferFileAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferProgressActivity extends AppCompatActivity {

    // Constants
    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "receiver_mac_address";
    private static final long SIMULATED_TRANSFER_DURATION = 5000; // For demo purposes

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

    // Data
    private ArrayList<TransferFileItem> transferItems = new ArrayList<>();
    private String bluetoothDeviceAddress;
    private TransferFileAdapter adapter;
    private int totalFiles = 0;
    private int completedFiles = 0;
    private long totalSize = 0;
    private boolean transferCancelled = false;
    private boolean transferCompleted = false;

    // Threads
    private ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_progress);

        // Get intent extras and convert to TransferFileItems
        processIntent();

        // Calculate total size
        for (TransferFileItem item : transferItems) {
            totalSize += item.getSize();
        }

        // Initialize UI components
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Start transfer process
        executorService = Executors.newFixedThreadPool(2);
        startTransfer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
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
            totalFiles = transferItems.size();
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

        // Setup initial progress
        progressIndicator.setProgress(0);
        updateProgressText(0);
    }

    private void setupRecyclerView() {
        adapter = new TransferFileAdapter(transferItems);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> {
            transferCancelled = true;
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

    private void startTransfer() {
        // In a real app, you would connect to the Bluetooth device and start the actual transfer
        // For demo purposes, we'll simulate the transfer with delays

        for (int i = 0; i < transferItems.size(); i++) {
            final int index = i;

            // Update status to pending for all files initially
            updateFileStatus(index, FileTransferStatus.PENDING);
        }

        // Start simulated file transfers one by one
        executorService.execute(() -> {
            for (int i = 0; i < transferItems.size(); i++) {
                if (transferCancelled) break;

                final int index = i;
                final TransferFileItem currentFile = transferItems.get(index);

                // Update UI for current file
                mainHandler.post(() -> {
                    transferStatusText.setText(getString(R.string.transfer_in_progress));
                    updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                });

                // Simulate transfer progress for current file
                simulateFileTransfer(index, currentFile);

                if (transferCancelled) break;

                // Mark as completed
                completedFiles++;
                mainHandler.post(() -> {
                    updateFileStatus(index, FileTransferStatus.COMPLETED);
                    int totalProgress = (completedFiles * 100) / totalFiles;
                    progressIndicator.setProgress(totalProgress);
                    updateProgressText(totalProgress);
                });
            }

            // Check if all files were transferred successfully
            if (!transferCancelled && completedFiles == totalFiles) {
                transferCompleted = true;
                mainHandler.post(this::showTransferCompleted);
            }
        });
    }

    private void simulateFileTransfer(int fileIndex, TransferFileItem fileItem) {
        try {
            // Make the minimum duration longer (5 seconds) to ensure you can see the progress
            long duration = Math.max(5000, Math.min(SIMULATED_TRANSFER_DURATION * 2, fileItem.getSize() / 512));

            // Use smaller increments for a smoother progress animation
            for (int progress = 0; progress <= 100; progress += 2) {
                if (transferCancelled) break;

                final int currentProgress = progress;
                mainHandler.post(() -> adapter.updateFileProgress(fileIndex, currentProgress));

                // Sleep longer between updates (at least 100ms)
                Thread.sleep(duration / 100);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateFileStatus(int index, FileTransferStatus status) {
        TransferFileItem item = transferItems.get(index);
        item.setStatus(status);
        adapter.notifyItemChanged(index);
    }

    private void updateProgressText(int progress) {
        progressText.setText(getString(R.string.transfer_of, completedFiles, totalFiles, progress));
    }

    private void showTransferCompleted() {
        // Hide transfer UI
        transferAnimation.setVisibility(View.GONE);
        transferStatusText.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        filesRecyclerView.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        findViewById(R.id.filesListTitle).setVisibility(View.GONE);

        // Show success UI
        successContainer.setVisibility(View.VISIBLE);

        // Apply animation to success icon
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        successAnimation.startAnimation(fadeIn);

        // Format the summary text
        String formattedSize = Formatter.formatFileSize(this, totalSize);
        transferSummary.setText(getString(R.string.transfer_summary, totalFiles, formattedSize));
    }

    private void showTransferCancelled() {
        Toast.makeText(this, R.string.transfer_canceled, Toast.LENGTH_LONG).show();

        // Return to previous screen after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish();
        }, 1500);
    }

    /**
     * Static method to start this activity
     * @param activity Source activity
     * @param fileItems List of files to transfer
     * @param bluetoothDeviceAddress Bluetooth device MAC address
     */
    public static void start(AppCompatActivity activity, ArrayList<TransferFileItem> fileItems, String bluetoothDeviceAddress) {
        Intent intent = new Intent(activity, TransferProgressActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_FILE_ITEMS, fileItems);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);
        activity.startActivity(intent);

        // Optional: add a transition animation
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

}