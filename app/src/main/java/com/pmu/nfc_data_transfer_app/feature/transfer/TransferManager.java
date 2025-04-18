package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pmu.nfc_data_transfer_app.core.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.core.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransferManager {

    private static final String TAG = "TransferManager";
    private static final long SIMULATED_TRANSFER_DURATION = 5000;
    private static final String DEMO_DEVICE_NAME = "Demo Test Device";

    private final List<TransferFileItem> transferItems;
    private final List<TransferFileItem> completedItems = new ArrayList<>();
    private final DatabaseHelper dbHelper;
    private final TransferProgressCallback callback;
    private final ExecutorService executorService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int totalFiles = 0;
    private int completedFiles = 0;
    private int failedFiles = 0;
    private long totalSize = 0;
    private boolean transferCancelled = false;
    private boolean transferCompleted = false;

    public interface TransferProgressCallback {
        void onFileStatusUpdated(int index, FileTransferStatus status);
        void onProgressUpdated(int completedFiles, int totalFiles, int progress);
        void onFileProgressUpdated(int fileIndex, int progress);
        void onTransferCompleted(boolean success);
        void onFileTransferFailed(int fileIndex, String errorMessage);
    }

    public TransferManager(
            List<TransferFileItem> items,
            DatabaseHelper dbHelper,
            TransferProgressCallback callback
    ) {
        this.transferItems = new ArrayList<>(items);
        this.dbHelper = dbHelper;
        this.callback = callback;
        this.totalFiles = items.size();
        this.executorService = Executors.newFixedThreadPool(2);

        for (TransferFileItem item : transferItems) {
            totalSize += item.getSize();
        }
    }

    public void startTransfer() {

        for (int i = 0; i < transferItems.size(); i++) {
            final int index = i;
            updateFileStatus(index, FileTransferStatus.PENDING);
        }

        executorService.execute(() -> {
            boolean allSuccessful = true;

            for (int i = 0; i < transferItems.size(); i++) {
                if (transferCancelled) {
                    allSuccessful = false;
                    break;
                }

                final int index = i;
                final TransferFileItem currentFile = transferItems.get(index);

                mainHandler.post(() -> {
                    updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                });

                //TOOD: Remove the simulation when done testing
                boolean fileSuccess = simulateFileTransfer(index, currentFile);

                if (!fileSuccess) {
                    allSuccessful = false;
                    failedFiles++;

                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.FAILED);
                        callback.onFileTransferFailed(index, "Failed to transfer file");
                    });

                    continue;
                }

                if (transferCancelled) {
                    allSuccessful = false;
                    break;
                }

                completedFiles++;
                completedItems.add(currentFile);

                mainHandler.post(() -> {
                    updateFileStatus(index, FileTransferStatus.COMPLETED);

                    int totalProgress = (completedFiles * 100) / totalFiles;

                    callback.onProgressUpdated(completedFiles, totalFiles, totalProgress);
                });
            }

            this.transferCompleted = true;

            if (allSuccessful && !transferCancelled) {
                saveAllFilesToDatabase();
                mainHandler.post(() -> callback.onTransferCompleted(true));
            } else {
                mainHandler.post(() -> callback.onTransferCompleted(false));
            }
        });
    }

    private void saveAllFilesToDatabase() {
        try {
            String deviceName = getDeviceName();

            TransferHistory transferHistory = new TransferHistory(
                    0,
                    deviceName,
                    new Date(),
                    "send",
                    new ArrayList<>(completedItems), // All successfully transferred files
                    totalSize
            );

            long newId = dbHelper.addTransferEventToDatabase(transferHistory);

            if (newId > 0) {
                Log.d(TAG, "Successfully saved all transferred files to database with ID: " + newId);
            } else {
                Log.e(TAG, "Error saving files to database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving files to database", e);
        }
    }

    private String getDeviceName() {

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (manufacturer.length() > 0) {
            manufacturer = manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1);
        }

        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public void cancelTransfer() {
        transferCancelled = true;
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    // Refresh the ui when updating state of the transferred file
    private void updateFileStatus(int index, FileTransferStatus status) {
        TransferFileItem item = transferItems.get(index);
        item.setStatus(status);
        callback.onFileStatusUpdated(index, status);
    }

    private boolean simulateFileTransfer(int fileIndex, TransferFileItem fileItem) {
        try {
            long duration = Math.max(5000, Math.min(SIMULATED_TRANSFER_DURATION * 2, fileItem.getSize() / 512));

            //TODO: Remove in production
            // For demo purposes: Simulate some random failures (can remove this in production)
            // Uncomment to test failure scenarios
            /*
            if (Math.random() < 0.2) { // 20% chance of failure
                Log.d(TAG, "Simulating random failure for file: " + fileItem.getName());
                return false;
            }
            */

            for (int progress = 0; progress <= 100; progress += 2) {
                if (transferCancelled) return false;

                final int currentProgress = progress;
                mainHandler.post(() -> callback.onFileProgressUpdated(fileIndex, currentProgress));

                Thread.sleep(duration / 100);
            }

            return true;
        } catch (InterruptedException e) {
            Log.e(TAG, "Transfer interrupted", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error transferring file", e);
            return false;
        }
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public int getTotalFiles() {
        return totalFiles;
    }

    public int getCompletedFiles() {
        return completedFiles;
    }

    public int getFailedFiles() {
        return failedFiles;
    }
}