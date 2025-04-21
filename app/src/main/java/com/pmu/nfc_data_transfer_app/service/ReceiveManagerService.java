package com.pmu.nfc_data_transfer_app.service;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.pmu.nfc_data_transfer_app.core.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;
import com.pmu.nfc_data_transfer_app.util.AppPreferences;

import java.io.IOException;
import java.util.ArrayList;

public class ReceiveManagerService extends BaseTransferManagerService {

    private static final String TAG = "ReceiveManager";

    private ArrayList<TransferFileItem> receivedItems = new ArrayList<>();
    private final ReceiveProgressCallback callback;

    /**
     * Interface for receive progress callbacks
     * Extends the base transfer progress callback with receive-specific methods
     */
    public interface ReceiveProgressCallback extends TransferProgressCallback {
        void onReceiveCompleted(boolean success);

        void onFileReceiveFailed(int fileIndex, String errorMessage);

        void onFilesDiscovered(ArrayList<TransferFileItem> items);
    }

    public ReceiveManagerService(
            DatabaseHelper dbHelper,
            ReceiveProgressCallback callback
    ) {
        super(dbHelper);
        this.callback = callback;
    }

    public void startReceiving(Context context) {
        executorService.execute(() -> {
            // Turn on bluetooth server
            try {
                BluetoothService bs = new BluetoothService();
                // Simulate waiting for connection
                mainHandler.post(() -> callback.onProgressUpdated(0, 0, 0));
                BluetoothSocket bluetoothSocket = bs.connectServer(context);

                // Receive files totalSize and metadata
                int t_totalSize = bs.recieveTotalSizeTFIL(bluetoothSocket);

                receivedItems = bs.recieveMetadataTFIL(bluetoothSocket);
                totalFiles = receivedItems.size();

                // Update UI with file information
                mainHandler.post(() -> {
                    totalSize = t_totalSize;
                    callback.onFilesDiscovered(receivedItems);
                    callback.onProgressUpdated(0, totalFiles, 0);
                });

                // Start receiving files one by one
                boolean allSuccessful = true;

                for (int i = 0; i < receivedItems.size(); i++) {
                    if (transferCancelled) {
                        allSuccessful = false;
                        break;
                    }

                    final int index = i;

                    // Update UI for current file
                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                    });

                    // Recieve data for current file
                    boolean fileSuccess = bs.recieveFileDataTFI(bluetoothSocket, receivedItems.get(i).getName(), context);

                    if (!fileSuccess) {
                        allSuccessful = false;
                        failedFiles++;

                        mainHandler.post(() -> {
                            updateFileStatus(index, FileTransferStatus.FAILED);
                            callback.onFileReceiveFailed(index, "Failed to receive file");
                        });

                        continue;
                    }

                    if (transferCancelled) {
                        allSuccessful = false;
                        break;
                    }

                    completedFiles++;
                    completedItems.add(receivedItems.get(i));

                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.COMPLETED);
                        int totalProgress = (completedFiles * 100) / totalFiles;
                        callback.onProgressUpdated(completedFiles, totalFiles, totalProgress);
                    });
                }

                transferCompleted = true;

                if (null != bluetoothSocket) {
                    bluetoothSocket.close();
                }

                if (allSuccessful && !transferCancelled) {
                    // TODO: use real device name
                    saveToDatabase("receive", getDeviceName());
                    mainHandler.post(() -> callback.onReceiveCompleted(true));
                } else {
                    mainHandler.post(() -> callback.onReceiveCompleted(false));
                }

            } catch (IOException e) {
                Log.e(TAG, "Problem with file receive in receive manager service");
                throw new RuntimeException(e);
            }
        });
    }

    private boolean simulateFileReceive(int fileIndex, TransferFileItem fileItem) {
        try {
            // Make the minimum duration longer (5 seconds) to ensure you can see the progress
            long duration = Math.max(5000, Math.min(SIMULATED_TRANSFER_DURATION * 2, fileItem.getSize() / 512));

            // Use smaller increments for a smoother progress animation
            for (int progress = 0; progress <= 100; progress += 2) {
                if (transferCancelled) return false;

                final int currentProgress = progress;
                mainHandler.post(() -> callback.onFileProgressUpdated(fileIndex, currentProgress));

                // Sleep longer between updates (at least 100ms)
                Thread.sleep(duration / 100);
            }

            return true;

        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateFileStatus(int index, FileTransferStatus status) {
        TransferFileItem item = receivedItems.get(index);
        item.setStatus(status);
        callback.onFileStatusUpdated(index, status);
    }

    public int getReceivedFiles() {
        return completedFiles;
    }
}