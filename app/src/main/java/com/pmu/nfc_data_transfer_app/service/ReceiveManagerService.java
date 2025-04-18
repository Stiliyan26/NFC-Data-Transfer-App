package com.pmu.nfc_data_transfer_app.service;

import com.pmu.nfc_data_transfer_app.core.model.FileTransferStatus;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;

import java.util.ArrayList;

public class ReceiveManagerService extends BaseTransferManagerService {

    private static final String TAG = "ReceiveManager";

    private ArrayList<TransferFileItem> receivedItems = new ArrayList<>();
    private final ReceiveProgressCallback callback;
    private final String bluetoothDeviceAddress;

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
            String bluetoothDeviceAddress,
            DatabaseHelper dbHelper,
            ReceiveProgressCallback callback
    ) {
        super(dbHelper);
        this.bluetoothDeviceAddress = bluetoothDeviceAddress;
        this.callback = callback;
        this.transferItems = new ArrayList<>();
    }

    public void startReceiving() {
        //TODO: Remove the simulation when done testing
        // In a real app, you would connect to the Bluetooth device and start listening for files
        // For demo purposes, we'll simulate receiving files with delays

        executorService.execute(() -> {
            try {
                // Simulate waiting for connection
                mainHandler.post(() -> callback.onProgressUpdated(0, 0, 0));
                Thread.sleep(2000);

                // Simulate receiving file metadata
                Thread.sleep(1500);

                // Add simulated files - in a real app these would come from the sender
                simulateIncomingFiles();
                
                // Update the transferItems with the received items
                transferItems = receivedItems;

                // Calculate total size
                for (TransferFileItem item : receivedItems) {
                    totalSize += item.getSize();
                }

                // Update UI with file information
                mainHandler.post(() -> {
                    totalFiles = receivedItems.size();
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
                    final TransferFileItem currentFile = receivedItems.get(index);

                    // Update UI for current file
                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.IN_PROGRESS);
                    });

                    // Simulate receiving progress for current file
                    boolean fileSuccess = simulateFileReceive(index, currentFile);
                    
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
                    completedItems.add(currentFile);
                    
                    mainHandler.post(() -> {
                        updateFileStatus(index, FileTransferStatus.COMPLETED);
                        int totalProgress = (completedFiles * 100) / totalFiles;
                        callback.onProgressUpdated(completedFiles, totalFiles, totalProgress);
                    });
                }

                transferCompleted = true;
                
                if (allSuccessful && !transferCancelled) {
                    // TODO: use real device name
                    saveToDatabase("receive", "Unknown Device");
                    mainHandler.post(() -> callback.onReceiveCompleted(true));
                } else {
                    mainHandler.post(() -> callback.onReceiveCompleted(false));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onReceiveCompleted(false));
            }
        });
    }

    private void simulateIncomingFiles() {
        receivedItems.add(new TransferFileItem(
                "vacation_photo.jpg",
                3_500_000L,
                "image/jpeg",
                null,
                true
        ));

        receivedItems.add(new TransferFileItem(
                "family_picture.png",
                2_200_000L,
                "image/png",
                null,
                true
        ));

        receivedItems.add(new TransferFileItem(
                "screenshot.jpg",
                1_800_000L,
                "image/jpeg",
                null,
                true
        ));

        for (TransferFileItem item : receivedItems) {
            item.setStatus(FileTransferStatus.PENDING);
        }
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