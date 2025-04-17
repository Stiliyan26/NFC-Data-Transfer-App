package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.format.Formatter;
import android.util.Log;
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
import com.pmu.nfc_data_transfer_app.core.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.core.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;
import com.pmu.nfc_data_transfer_app.feature.main.MainActivity;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileSendActivity extends AppCompatActivity {

    // Constants
    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "receiver_mac_address";
    private static final long SIMULATED_TRANSFER_DURATION = 5000; // For demo purposes
    private static final String DEMO_DEVICE_NAME = "Demo Test Device"; // Demo device name

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
    private ArrayList<TransferFileItem> completedItems = new ArrayList<>(); // Track completed items for DB
    private String bluetoothDeviceAddress;
    private TransferFileAdapter adapter;
    private int totalFiles = 0;
    private int completedFiles = 0;
    private long totalSize = 0;
    private boolean transferCancelled = false;
    private boolean transferCompleted = false;

    // Database helper
    private DatabaseHelper dbHelper;
    private long currentTransferId = -1;

    // Threads
    private ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_send);

        dbHelper = DatabaseHelper.getInstance(this);

        processIntent();

        // Calculate total size
        for (TransferFileItem item : transferItems) {
            totalSize += item.getSize();
        }

        // Initialize UI components
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Create initial database entry for this transfer session
        createInitialTransferRecord();

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

    private void createInitialTransferRecord() {
        TransferHistory initialTransfer = new TransferHistory(
                0, // ID will be set by database
                DEMO_DEVICE_NAME,
                new Date(),
                "send",
                new ArrayList<>(),
                0
        );

        currentTransferId = dbHelper.addTransferEventToDatabase(initialTransfer);

        Log.d("Database", "Created initial transfer record with ID: " + currentTransferId);
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void startTransfer() {

        for (int i = 0; i < transferItems.size(); i++) {
            final int index = i;

            updateFileStatus(index, FileTransferStatus.PENDING);
        }

        executorService.execute(() -> {
            for (int i = 0; i < transferItems.size(); i++) {
                if (transferCancelled) break;

                final int index = i;
                final TransferFileItem currentFile = transferItems.get(index);

                mainHandler.post(() -> {
                    transferStatusText.setText(getString(R.string.transfer_in_progress));
                    updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                });


                simulateFileTransfer(index, currentFile);

                if (transferCancelled) break;


                completedFiles++;

                completedItems.add(currentFile);

                updateDatabaseWithCompletedFile(currentFile);

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

                // Final database update to mark transfer as complete
                updateDatabaseWithCompletedTransfer();

                mainHandler.post(this::showTransferCompleted);
            }
        });
    }

    private void updateDatabaseWithCompletedFile(TransferFileItem completedFile) {
        try {
            if (currentTransferId == -1) {
                Log.e("Database", "Attempting to update transfer but no valid ID exists");
                return;
            }

            // Use our new helper method to add the file to the transfer record
            boolean success = dbHelper.addFileToTransfer((int)currentTransferId, completedFile);

            if (success) {
                Log.d("Database", "Successfully added file to database: " + completedFile.getName());
            } else {
                Log.e("Database", "Failed to add file to database: " + completedFile.getName());

                // As a fallback, create a new transfer record if updating failed
                TransferHistory newTransfer = new TransferHistory(
                        0, // New ID
                        DEMO_DEVICE_NAME,
                        new Date(),
                        "send",
                        new ArrayList<>(List.of(completedFile)), // Just this file
                        completedFile.getSize()
                );

                dbHelper.addTransferEventToDatabase(newTransfer);
                Log.d("Database", "Created new transfer record for file as fallback");
            }

        } catch (Exception e) {
            Log.e("Database", "Error updating database with completed file", e);
        }
    }

    /**
     * Final update to the database to mark the entire transfer as complete
     */
    private void updateDatabaseWithCompletedTransfer() {
        try {
            // Check if we have a valid transfer ID
            if (currentTransferId == -1) {
                Log.e("Database", "Attempting to finalize transfer but no valid ID exists");
                return;
            }

            TransferHistory currentTransfer = dbHelper.getTransferredFilesByTransferId(
                    Integer.parseInt(String.valueOf(currentTransferId))
            );

            if (currentTransfer == null) {
                Log.e("Database", "Could not find transfer record with ID: " + currentTransferId);
                return;
            }


            int expectedFileCount = transferItems.size();
            int actualFileCount = currentTransfer.getFiles().size();

            if (expectedFileCount == actualFileCount) {
                Log.d("Database", "Transfer completed successfully with all " +
                        actualFileCount + " files saved to database record: " + currentTransferId);
            } else {
                Log.w("Database", "Transfer completed but only " + actualFileCount +
                        " of " + expectedFileCount + " files were saved to database");
            }

            long expectedTotalSize = totalSize;
            long actualTotalSize = currentTransfer.getTotalSize();

            if (expectedTotalSize != actualTotalSize) {
                Log.w("Database", "Transfer total size mismatch: expected " +
                        expectedTotalSize + ", actual " + actualTotalSize);

                TransferHistory updatedTransfer = new TransferHistory(
                        (int)currentTransferId,
                        currentTransfer.getDeviceName(),
                        currentTransfer.getTransferDate(),
                        currentTransfer.getTransferType(),
                        currentTransfer.getFiles(),
                        expectedTotalSize
                );

                dbHelper.updateTransferEvent(Integer.parseInt(String.valueOf(currentTransferId)), updatedTransfer);
            }

        } catch (Exception e) {
            Log.e("Database", "Error finalizing completed transfer in database", e);
        }
    }

    private void simulateFileTransfer(int fileIndex, TransferFileItem fileItem) {
        try {
            long duration = Math.max(5000, Math.min(SIMULATED_TRANSFER_DURATION * 2, fileItem.getSize() / 512));

            for (int progress = 0; progress <= 100; progress += 2) {
                if (transferCancelled) break;

                final int currentProgress = progress;
                mainHandler.post(() -> adapter.updateFileProgress(fileIndex, currentProgress));

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


    public static void start(AppCompatActivity activity, ArrayList<TransferFileItem> fileItems, String bluetoothDeviceAddress) {
        Intent intent = new Intent(activity, FileSendActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_FILE_ITEMS, fileItems);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);

        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}