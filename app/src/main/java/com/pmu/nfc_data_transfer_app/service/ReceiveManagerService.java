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
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class ReceiveManagerService extends BaseTransferManagerService {

    private static final String TAG = "ReceiveManager";

    private ArrayList<TransferFileItem> receivedItems = new ArrayList<>();
    private final ReceiveProgressCallback callback;
    private Future<?> interruptableOnCancelExecutorTask;

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
        this.transferItems = new ArrayList<>();
    }

    public void startReceiving(Context context) {

        interruptableOnCancelExecutorTask = executorService.submit(() -> {
            // Turn on bluetooth server
            try {

                if (cancelIsPressed()) {
                    // Gracefully stop
                    Log.d(TAG, "User pressed cancel");
                    return;
                }

                BluetoothService bs = new BluetoothService();
                // Simulate waiting for connection
                mainHandler.post(() -> callback.onProgressUpdated(0, 0, 0));
                BluetoothSocket bluetoothSocket = bs.connectServer(context);

                if (cancelThenCleanup(bluetoothSocket)) {
                    return;
                }

                // Receive files totalSize and metadata
                int t_totalSize = bs.recieveTotalSizeTFIL(bluetoothSocket);

                receivedItems = bs.recieveMetadataTFIL(bluetoothSocket);
                totalFiles = receivedItems.size();

                if (cancelThenCleanup(bluetoothSocket)) {
                    return;
                }

                // Update UI with file information
                mainHandler.post(() -> {
                    totalSize = t_totalSize;
                    callback.onFilesDiscovered(receivedItems);
                    callback.onProgressUpdated(0, totalFiles, 0);
                });

                // Start receiving files one by one
                boolean allSuccessful = true;

                // Array of the byteArrays of the received files collected internally in order to be passed together
                byte[][] AllFilesBytes = new byte[receivedItems.size()][];

                if (cancelThenCleanup(bluetoothSocket)) {
                    return;
                }

                for (int i = 0; i < receivedItems.size(); i++) {
                    if (cancelIsPressed()) {
                        allSuccessful = false;
                        Log.d(TAG, "User pressed cancel");
                        break;
                    }

                    final int index = i;

                    // Update UI for current file
                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                    });

                    // Receive data for current file
                    byte[] fileSuccess = bs.recieveFileDataTFI(bluetoothSocket, receivedItems.get(i).getName());

                    if (null == fileSuccess) {
                        allSuccessful = false;
                        failedFiles++;

                        mainHandler.post(() -> {
                            updateFileStatus(index, FileTransferStatus.FAILED);
                            callback.onFileReceiveFailed(index, "Failed to receive file");
                        });

                        break;
                    }

                    if (cancelIsPressed()) {
                        allSuccessful = false;
                        Log.d(TAG, "User pressed cancel");
                        break;
                    }

                    AllFilesBytes[i] = fileSuccess;

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

                if (allSuccessful && !cancelIsPressed()) {
                    for (int i = 0; i < receivedItems.size(); ++i) {
                        bs.saveFile(receivedItems.get(i).getName(), AllFilesBytes[i], context);
                    }
                    saveToDatabase("receive", getDeviceName());
                    mainHandler.post(() -> callback.onReceiveCompleted(true));
                } else {
                    mainHandler.post(() -> callback.onReceiveCompleted(false));
                }

            } catch (InterruptedIOException e) {
                Log.d(TAG, "User interrupted");
            } catch (IOException e) {
                Log.e(TAG, "Problem with file receive in receive manager service");
                throw new RuntimeException(e);
            }
        });
    }


    private void updateFileStatus(int index, FileTransferStatus status) {
        TransferFileItem item = receivedItems.get(index);
        item.setStatus(status);
        callback.onFileStatusUpdated(index, status);
    }

    public int getReceivedFiles() {
        return completedFiles;
    }

    public Future<?> getInterruptableOnCancelExecutorTask() {
        return interruptableOnCancelExecutorTask;
    }

    public boolean cancelIsPressed() {
        return Thread.currentThread().isInterrupted() || transferCancelled;
    }

    private boolean cancelThenCleanup(BluetoothSocket socket) {
        if (cancelIsPressed()) {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {
            }
            Log.d(TAG, "User pressed cancel");
            return true;
        }
        return false;
    }
}