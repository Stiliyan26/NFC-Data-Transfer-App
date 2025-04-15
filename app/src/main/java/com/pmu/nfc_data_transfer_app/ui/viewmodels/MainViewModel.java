package com.pmu.nfc_data_transfer_app.ui.viewmodels;

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
import com.pmu.nfc_data_transfer_app.R;
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
    public final MutableLiveData<Event<String>> messageToast = new MutableLiveData<>();
    public final LiveData<Event<String>> toastMessage = messageToast;

    public final MutableLiveData<Boolean> currentlyLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = currentlyLoading; // To show progress during file reading


    public MainViewModel(@NonNull Application application) {
        super(application);

        this.fileDataSource = new AndroidFileDataSource(application);
        this.fileRepository = new SelectedFileRepository(); // Singleton or scoped instance preferred with DI

        this.fileList = fileRepository.getSelectedFiles();
        this.isTransferEnabled = Transformations.map(fileRepository.getSelectedFileCount(), count -> count > 0);
        this.selectedCountText = Transformations.map(fileRepository.getSelectedFileCount(), count -> {
            if (count == 1) {
                return getApplication().getString(R.string.file_selected, count);
            } else {
                return getApplication().getString(R.string.files_selected, count);
            }
        });
    }

    public void addFileUris(List<Uri> uris) {
        currentlyLoading.setValue(true);

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
                            messageToast.postValue(new Event<>("Could not add file: " + uri.getLastPathSegment()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing URI: " + uri, e);
                        messageToast.postValue(new Event<>("Error adding file: " + uri.getLastPathSegment()));
                    }
                }

                final int finalAddedCount = addedCount;

                // Show appropriate toast messages
                if (!duplicateFiles.isEmpty()) {
                    if (duplicateFiles.size() == 1) {
                        messageToast.postValue(new Event<>("File already added: " + duplicateFiles.get(0)));
                    } else if (duplicateFiles.size() <= 3) {
                        messageToast.postValue(new Event<>("Files already added: " + String.join(", ", duplicateFiles)));
                    } else {
                        messageToast.postValue(new Event<>(duplicateFiles.size() + " files were already added"));
                    }
                }

                if (finalAddedCount > 0) {
                    messageToast.postValue(new Event<>(finalAddedCount + " file(s) added."));
                }

                currentlyLoading.postValue(false);
                completer.set(null);
            });
            return "addFileUris";
        });
    }

    public void removeFile(int position) {
        // Repository updates its own LiveData
        fileRepository.removeFile(position);
        messageToast.postValue(new Event<>("File removed"));
    }


//    public void transferFiles() {
//        List<FileItem> currentFiles = fileRepository.getAllFilesSnapshot();
//        if (currentFiles.isEmpty()) {
//            _toastMessage.postValue(new Event<>("No files selected"));
//            return;
//        }
//
//        _isLoading.setValue(true); // Show loading indicator
//        _toastMessage.postValue(new Event<>("Preparing files for transfer..."));
//
//        ListenableFuture<Void> future = CallbackToFutureAdapter.getFuture(completer -> {
//            backgroundExecutor.execute(() -> {
//                Map<String, byte[]> filesToTransfer = new HashMap<>();
//                boolean allFilesRead = true;
//                int successCount = 0;
//
//                Log.d(TAG, "Starting file reading process for " + currentFiles.size() + " files.");
//
//                for (FileItem item : currentFiles) {
//                    try {
//                        byte[] fileBytes = fileDataSource.getFileBytes(item.getFileUri());
//                        if (fileBytes != null) {
//                            // Update the file size with the actual size of the byte array
//                            if (item.getFileSize() == 0) {
//                                // Create a new FileItem with the correct size
//                                FileItem updatedItem = new FileItem(
//                                        item.getFileName(),
//                                        fileBytes.length,  // Use actual byte array length
//                                        item.getFileType(),
//                                        item.getFileUri(),
//                                        item.isImage()
//                                );
//
//                                // Replace the old item with the updated one
//                                fileRepository.removeFile(item);
//                                fileRepository.addFile(updatedItem);
//
//                                // Use the updated item's name in the map
//                                filesToTransfer.put(updatedItem.getFileName(), fileBytes);
//                            } else {
//                                filesToTransfer.put(item.getFileName(), fileBytes);
//                            }
//
//                            Log.d(TAG, "Successfully read " + fileBytes.length + " bytes for: " + item.getFileName());
//                            successCount++;
//                        } else {
//                            Log.w(TAG, "getFileBytes returned null for: " + item.getFileName());
//                            _toastMessage.postValue(new Event<>("Could not read file: " + item.getFileName()));
//                            allFilesRead = false;
//                        }
//                    } catch (IOException e) {
//                        Log.e(TAG, "Error reading file: " + item.getFileName(), e);
//                        _toastMessage.postValue(new Event<>("Error reading file: " + item.getFileName()));
//                        allFilesRead = false;
//                    } catch (OutOfMemoryError oom) {
//                        Log.e(TAG, "Out of Memory reading file: " + item.getFileName() + " (Size: " + item.getFileSize() + ")", oom);
//                        _toastMessage.postValue(new Event<>("Out of Memory reading: " + item.getFileName()));
//                        allFilesRead = false;
//                        break;
//                    }
//                }

                // --- >>> Implement your actual transfer logic here <<< ---
                // You now have the 'filesToTransfer' map containing file names and their byte arrays.
//                if (!filesToTransfer.isEmpty()) {
//                    Log.i(TAG, "Ready to transfer " + filesToTransfer.size() + " files.");
//                    startBluetoothTransfer(filesToTransfer, socket);
//                    // Simulate transfer delay
//                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//
//                    _toastMessage.postValue(new Event<>(filesToTransfer.size() + " files processed for transfer."));
//                } else if (!currentFiles.isEmpty()) {
//                    Log.e(TAG, "Failed to read any files for transfer.");
//                    _toastMessage.postValue(new Event<>("Failed to read files for transfer."));
//                }
//
//                if (!allFilesRead && successCount > 0) {
//                    Log.w(TAG, "Some files could not be read. Processed " + successCount + " files.");
//                }
//
//                _isLoading.postValue(false);
//                completer.set(null);
//            });
//            return "transferFiles";
//        });
//    }


    public ListenableFuture<Map<String, byte[]>> readFilesForTransfer() {
        List<FileItem> currentFiles = fileRepository.getAllFilesSnapshot();

        return CallbackToFutureAdapter.getFuture(completer -> {
            backgroundExecutor.execute(() -> {
                Map<String, byte[]> filesToTransfer = new HashMap<>();
                boolean allFilesRead = true;
                int successCount = 0;

                for (FileItem item : currentFiles) {
                    try {
                        byte[] fileBytes = fileDataSource.getFileBytes(item.getFileUri());

                        if (fileBytes != null) {
                            if (item.getFileSize() == 0) {
                                FileItem updatedItem = new FileItem(
                                        item.getFileName(),
                                        fileBytes.length,
                                        item.getFileType(),
                                        item.getFileUri(),
                                        item.isImage()
                                );

                                fileRepository.removeFile(item);
                                fileRepository.addFile(updatedItem);
                                filesToTransfer.put(updatedItem.getFileName(), fileBytes);
                            } else {
                                filesToTransfer.put(item.getFileName(), fileBytes);
                            }

                            successCount++;
                        } else {
                            allFilesRead = false;
                        }
                    } catch (IOException | OutOfMemoryError e) {
                        allFilesRead = false;
                        break;
                    }
                }

                completer.set(filesToTransfer);
            });

            return "readFilesForTransfer";
        });
    }

    public int getSelectedCount() {
        return fileList.getValue() != null ? fileList.getValue().size() : 0;
    }
}