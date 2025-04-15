package com.pmu.nfc_data_transfer_app.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.pmu.nfc_data_transfer_app.data.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileReceiveActivity extends AppCompatActivity {

    // Constants
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "extra_bluetooth_device_address";
    private static final long SIMULATED_TRANSFER_DURATION = 5000; // For demo purposes

    // UI Components
    private TextView titleText;
    private ProgressBar receiveAnimation;
    private TextView receiveStatusText;
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
    private ArrayList<TransferFileItem> receivedItems = new ArrayList<>();
    private String bluetoothDeviceAddress;
    private TransferFileAdapter adapter;
    private int totalFiles = 0;
    private int receivedFiles = 0;
    private long totalSize = 0;
    private boolean transferCancelled = false;
    private boolean transferCompleted = false;

    // Threads
    private ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_receive);

        // Process incoming intent
        processIntent();

        // Initialize UI components
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Start listening for files
        executorService = Executors.newFixedThreadPool(2);
        startReceiving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void processIntent() {
        if (getIntent().hasExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS)) {
            bluetoothDeviceAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS);
        }
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        receiveAnimation = findViewById(R.id.transferAnimation);
        receiveStatusText = findViewById(R.id.transferStatusText);
        progressIndicator = findViewById(R.id.progressIndicator);
        progressText = findViewById(R.id.progressText);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        cancelButton = findViewById(R.id.cancelButton);
        successContainer = findViewById(R.id.successContainer);
        successAnimation = findViewById(R.id.successAnimation);
        successText = findViewById(R.id.successText);
        transferSummary = findViewById(R.id.transferSummary);
        doneButton = findViewById(R.id.doneButton);

        // Set title for receiving
        titleText.setText(R.string.receiving_files);
        receiveStatusText.setText(R.string.waiting_for_files);

        // Setup initial progress
        progressIndicator.setProgress(0);
        updateProgressText(0);
    }

    private void setupRecyclerView() {
        adapter = new TransferFileAdapter(receivedItems);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        filesRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> {
            transferCancelled = true;
            showReceiveCancelled();
        });

        doneButton.setOnClickListener(v -> {
            // Return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void startReceiving() {
        // In a real app, you would connect to the Bluetooth device and start listening for files
        // For demo purposes, we'll simulate receiving files with delays

        // Simulate discovering files to receive
        executorService.execute(() -> {
            // Simulate waiting for connection
            try {
                mainHandler.post(() -> receiveStatusText.setText(R.string.connecting_to_sender));
                Thread.sleep(2000);

                // Simulate receiving file metadata
                mainHandler.post(() -> receiveStatusText.setText(R.string.receiving_file_info));
                Thread.sleep(1500);

                // Add simulated files - in a real app these would come from the sender
                simulateIncomingFiles();

                // Calculate total size
                for (TransferFileItem item : receivedItems) {
                    totalSize += item.getSize();
                }

                // Update UI with file information
                mainHandler.post(() -> {
                    adapter.notifyDataSetChanged();
                    totalFiles = receivedItems.size();
                    updateProgressText(0);
                    receiveStatusText.setText(R.string.receiving_files);
                });

                // Start receiving files one by one
                for (int i = 0; i < receivedItems.size(); i++) {
                    if (transferCancelled) break;

                    final int index = i;
                    final TransferFileItem currentFile = receivedItems.get(index);

                    // Update UI for current file
                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                    });

                    // Simulate receiving progress for current file
                    simulateFileReceive(index, currentFile);

                    if (transferCancelled) break;

                    // Mark as completed
                    receivedFiles++;
                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.COMPLETED);
                        int totalProgress = (receivedFiles * 100) / totalFiles;
                        progressIndicator.setProgress(totalProgress);
                        updateProgressText(totalProgress);
                    });
                }

                // Check if all files were received successfully
                if (!transferCancelled && receivedFiles == totalFiles) {
                    transferCompleted = true;
                    mainHandler.post(this::showReceiveCompleted);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void simulateIncomingFiles() {
        // Simulate 3 files being received - in a real app these would come from the sender
        receivedItems.add(new TransferFileItem("vacation_photo.jpg", 3_500_000L,
                "image/jpeg", Uri.parse("content://mock/image1")));
        receivedItems.add(new TransferFileItem("family_picture.png", 2_200_000L,
                "image/png", Uri.parse("content://mock/image2")));
        receivedItems.add(new TransferFileItem("screenshot.jpg", 1_800_000L,
                "image/jpeg", Uri.parse("content://mock/image3")));

        // Set all to pending initially
        for (TransferFileItem item : receivedItems) {
            item.setStatus(FileTransferStatus.PENDING);
        }
    }

    private void simulateFileReceive(int fileIndex, TransferFileItem fileItem) {
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
        TransferFileItem item = receivedItems.get(index);
        item.setStatus(status);
        adapter.notifyItemChanged(index);
    }

    private void updateProgressText(int progress) {
        progressText.setText(getString(R.string.receive_progress, receivedFiles, totalFiles, progress));
    }

    private void showReceiveCompleted() {
        // Hide receive UI
        receiveAnimation.setVisibility(View.GONE);
        receiveStatusText.setVisibility(View.GONE);
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
        transferSummary.setText(getString(R.string.receive_summary, totalFiles, formattedSize));
        successText.setText(R.string.files_received_successfully);
    }

    private void showReceiveCancelled() {
        Toast.makeText(this, R.string.receive_canceled, Toast.LENGTH_LONG).show();

        // Return to previous screen after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish();
        }, 1500);
    }

    /**
     * Static method to start this activity
     * @param activity Source activity
     * @param bluetoothDeviceAddress Bluetooth device MAC address
     */
    public static void start(AppCompatActivity activity, String bluetoothDeviceAddress) {
        Intent intent = new Intent(activity, FileReceiveActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);
        activity.startActivity(intent);

        // Optional: add a transition animation
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}