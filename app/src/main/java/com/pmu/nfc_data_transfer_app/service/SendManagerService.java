package com.pmu.nfc_data_transfer_app.service;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.pmu.nfc_data_transfer_app.core.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;
import com.pmu.nfc_data_transfer_app.util.AppPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SendManagerService extends BaseTransferManagerService {

    private static final String TAG = "SendManager";
    private final TransferProgressCallback callback;
    protected List<TransferFileItem> transferItems;

    /**
     * Interface for transfer progress callbacks
     * Extends the base transfer progress callback with send-specific methods
     */
    public interface TransferProgressCallback extends BaseTransferManagerService.TransferProgressCallback {
        void onTransferCompleted(boolean success);

        void onFileTransferFailed(int fileIndex, String errorMessage);
    }

    public SendManagerService(
            List<TransferFileItem> items,
            DatabaseHelper dbHelper,
            TransferProgressCallback callback
    ) {
        super(dbHelper);
        this.transferItems = new ArrayList<>(items);
        this.callback = callback;
        this.totalFiles = items.size();

        for (TransferFileItem item : transferItems) {
            totalSize += item.getSize();
        }
    }

    public void startTransfer(Context context) {
        // Set all files to pending status
        for (int i = 0; i < transferItems.size(); i++) {
            final int index = i;
            updateFileStatus(index, FileTransferStatus.PENDING);
        }
        try {
            executorService.execute(() -> {
                try {
                    boolean allSuccessful = true;

                    BluetoothService bs = new BluetoothService(AppPreferences.getOtherDeviceMacAddress(context), (Application) context.getApplicationContext());
                    BluetoothSocket bluetoothSocket = bs.connectClient(context);

                    // Send files totalSize and metadata
                    bs.sendTotalSizeTFIL(bluetoothSocket, transferItems);
                    bs.sendMetadataTFIL(bluetoothSocket, transferItems);

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

                        boolean fileSuccess = bs.sendFileDataTFI(bluetoothSocket, currentFile);

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

                    this.transferCompleted = bs.closeGracefully(bluetoothSocket);

                    if (allSuccessful && !transferCancelled) {
                        saveToDatabase("send", getDeviceName());
                        mainHandler.post(() -> callback.onTransferCompleted(true));
                    } else {
                        mainHandler.post(() -> callback.onTransferCompleted(false));
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Exception in executor thread", t);
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Executor fails");
        }
    }



    // Refresh the UI when updating state of the transferred file
    private void updateFileStatus(int index, FileTransferStatus status) {
        TransferFileItem item = transferItems.get(index);
        item.setStatus(status);
        callback.onFileStatusUpdated(index, status);
    }

    private boolean simulateFileTransfer(int fileIndex, TransferFileItem fileItem) {
        try {
            long duration = Math.max(5000, Math.min(SIMULATED_TRANSFER_DURATION * 2, fileItem.getSize() / 512));

            // TODO: Remove in production
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

                Thread.sleep(duration / 10);
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
}