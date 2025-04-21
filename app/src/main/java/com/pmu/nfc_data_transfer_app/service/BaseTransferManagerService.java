package com.pmu.nfc_data_transfer_app.service;

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

/**
 * Base class for transfer manager services
 * Contains common functionality for both sending and receiving files
 */
public abstract class BaseTransferManagerService {
    private static final String TAG = "BaseTransferManager";
    protected static final long SIMULATED_TRANSFER_DURATION = 5000; // For demo purposes

    protected List<TransferFileItem> transferItems;
    protected final List<TransferFileItem> completedItems = new ArrayList<>();
    protected final DatabaseHelper dbHelper;
    protected final ExecutorService executorService;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    protected int totalFiles = 0;
    protected int completedFiles = 0;
    protected int failedFiles = 0;
    protected long totalSize = 0;
    protected boolean transferCancelled = false;
    protected boolean transferCompleted = false;

    /**
     * Base interface for transfer progress callbacks
     */
    public interface TransferProgressCallback {
        void onFileStatusUpdated(int index, FileTransferStatus status);

        void onProgressUpdated(int completedFiles, int totalFiles, int progress);

        void onFileProgressUpdated(int fileIndex, int progress);
    }

    protected BaseTransferManagerService(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.executorService = Executors.newFixedThreadPool(10);
    }


    public void cancelTransfer() {
        transferCancelled = true;
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    protected void saveToDatabase(String transferType, String deviceName) {
        try {
            TransferHistory transferHistory = new TransferHistory(
                    0,
                    deviceName,
                    new Date(),
                    transferType,
                    new ArrayList<>(completedItems),
                    totalSize
            );

            long newId = dbHelper.addTransferEventToDatabase(transferHistory);

            if (newId > 0) {
                Log.d(TAG, "Successfully saved transfer to database with ID: " + newId);
            } else {
                Log.e(TAG, "Error saving transfer to database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving transfer to database", e);
        }
    }

    protected String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (!manufacturer.isEmpty()) {
            manufacturer = manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1);
        }

        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
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