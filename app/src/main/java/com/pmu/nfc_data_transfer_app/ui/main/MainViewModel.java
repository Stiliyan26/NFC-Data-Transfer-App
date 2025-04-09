package com.pmu.nfc_data_transfer_app.ui.main;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.ListenableFuture;
import com.pmu.nfc_data_transfer_app.data.datasource.AndroidFileDataSource;
import com.pmu.nfc_data_transfer_app.data.datasource.FileDataSource;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.pmu.nfc_data_transfer_app.data.repository.FileRepository;
import com.pmu.nfc_data_transfer_app.data.repository.SelectedFileRepository;
import com.pmu.nfc_data_transfer_app.ui.util.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = "MainViewModel";

    private final FileRepository fileRepository;
    private final FileDataSource fileDataSource;
    private final Executor backgroundExecutor = Executors.newCachedThreadPool();

    // LiveData observed by the UI
    public final LiveData<List<FileItem>> fileList;
    public final LiveData<String> selectedCountText;
    public final LiveData<Boolean> isTransferEnabled;

    // For one-time events like showing Toasts or triggering navigation
    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public final LiveData<Event<String>> toastMessage = _toastMessage;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading; // To show progress during file reading


    public MainViewModel(@NonNull Application application) {
        super(application);

        this.fileDataSource = new AndroidFileDataSource(application);
        this.fileRepository = new SelectedFileRepository(); // Singleton or scoped instance preferred with DI

        this.fileList = fileRepository.getSelectedFiles();
        this.isTransferEnabled = Transformations.map(fileRepository.getSelectedFileCount(), count -> count > 0);
        this.selectedCountText = Transformations.map(fileRepository.getSelectedFileCount(), count ->
                String.format("%d file%s selected", count, count == 1 ? "" : "s")
        );
    }

    public void addFileUris(List<Uri> uris) {
        _isLoading.setValue(true);

        ListenableFuture<Void> future = CallbackToFutureAdapter.getFuture(completer -> {
            backgroundExecutor.execute(() -> {
                int addedCount = 0;
                List<String> duplicateFiles = new ArrayList<>();

                for (Uri uri : uris) {
                    try {
                        String name = fileDataSource.getFileName(uri);
                        long size = fileDataSource.getFileSize(uri);
                        String mimeType = fileDataSource.getMimeType(uri);
                        boolean isImage = mimeType != null && mimeType.startsWith("image/");

                        if (name != null) {
                            FileItem newItem = new FileItem(name, size, mimeType, uri, isImage);

                            if (!fileRepository.containsFile(newItem)) {
                                fileRepository.addFile(newItem);
                                addedCount++;
                            } else {
                                Log.w(TAG, "File already selected: " + name);
                                duplicateFiles.add(name);
                            }
                        } else {
                            Log.e(TAG, "Could not get file name for URI: " + uri);
                            _toastMessage.postValue(new Event<>("Could not add file: " + uri.getLastPathSegment()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing URI: " + uri, e);
                        _toastMessage.postValue(new Event<>("Error adding file: " + uri.getLastPathSegment()));
                    }
                }

                final int finalAddedCount = addedCount;

                // Show appropriate toast messages
                if (!duplicateFiles.isEmpty()) {
                    if (duplicateFiles.size() == 1) {
                        _toastMessage.postValue(new Event<>("File already added: " + duplicateFiles.get(0)));
                    } else if (duplicateFiles.size() <= 3) {
                        _toastMessage.postValue(new Event<>("Files already added: " + String.join(", ", duplicateFiles)));
                    } else {
                        _toastMessage.postValue(new Event<>(duplicateFiles.size() + " files were already added"));
                    }
                }

                if (finalAddedCount > 0) {
                    _toastMessage.postValue(new Event<>(finalAddedCount + " file(s) added."));
                }

                _isLoading.postValue(false);
                completer.set(null);
            });
            return "addFileUris";
        });
    }

    public void removeFile(int position) {
        // Repository updates its own LiveData
        fileRepository.removeFile(position);
        _toastMessage.postValue(new Event<>("File removed"));
    }

    public void transferFiles() {
        List<FileItem> currentFiles = fileRepository.getAllFilesSnapshot();
        if (currentFiles.isEmpty()) {
            _toastMessage.postValue(new Event<>("No files selected"));
            return;
        }

        _isLoading.setValue(true); // Show loading indicator
        _toastMessage.postValue(new Event<>("Preparing files for transfer..."));

        ListenableFuture<Void> future = CallbackToFutureAdapter.getFuture(completer -> {
            backgroundExecutor.execute(() -> {
                Map<String, byte[]> filesToTransfer = new HashMap<>();
                boolean allFilesRead = true;
                int successCount = 0;

                Log.d(TAG, "Starting file reading process for " + currentFiles.size() + " files.");

                for (FileItem item : currentFiles) {
                    try {
                        byte[] fileBytes = fileDataSource.getFileBytes(item.getFileUri());
                        if (fileBytes != null) {
                            // Update the file size with the actual size of the byte array
                            if (item.getFileSize() == 0) {
                                // Create a new FileItem with the correct size
                                FileItem updatedItem = new FileItem(
                                        item.getFileName(),
                                        fileBytes.length,  // Use actual byte array length
                                        item.getFileType(),
                                        item.getFileUri(),
                                        item.isImage()
                                );

                                // Replace the old item with the updated one
                                fileRepository.removeFile(item);
                                fileRepository.addFile(updatedItem);

                                // Use the updated item's name in the map
                                filesToTransfer.put(updatedItem.getFileName(), fileBytes);
                            } else {
                                filesToTransfer.put(item.getFileName(), fileBytes);
                            }

                            Log.d(TAG, "Successfully read " + fileBytes.length + " bytes for: " + item.getFileName());
                            successCount++;
                        } else {
                            Log.w(TAG, "getFileBytes returned null for: " + item.getFileName());
                            _toastMessage.postValue(new Event<>("Could not read file: " + item.getFileName()));
                            allFilesRead = false;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading file: " + item.getFileName(), e);
                        _toastMessage.postValue(new Event<>("Error reading file: " + item.getFileName()));
                        allFilesRead = false;
                    } catch (OutOfMemoryError oom) {
                        Log.e(TAG, "Out of Memory reading file: " + item.getFileName() + " (Size: " + item.getFileSize() + ")", oom);
                        _toastMessage.postValue(new Event<>("Out of Memory reading: " + item.getFileName()));
                        allFilesRead = false;
                        break;
                    }
                }

                // --- >>> Implement your actual transfer logic here <<< ---
                // You now have the 'filesToTransfer' map containing file names and their byte arrays.
                if (!filesToTransfer.isEmpty()) {
                    Log.i(TAG, "Ready to transfer " + filesToTransfer.size() + " files.");
                    // Example: startNfcTransfer(filesToTransfer);
                    // Simulate transfer delay
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

                    _toastMessage.postValue(new Event<>(filesToTransfer.size() + " files processed for transfer."));
                } else if (!currentFiles.isEmpty()) {
                    Log.e(TAG, "Failed to read any files for transfer.");
                    _toastMessage.postValue(new Event<>("Failed to read files for transfer."));
                }

                if (!allFilesRead && successCount > 0) {
                    Log.w(TAG, "Some files could not be read. Processed " + successCount + " files.");
                }

                _isLoading.postValue(false);
                completer.set(null);
            });
            return "transferFiles";
        });
    }
}