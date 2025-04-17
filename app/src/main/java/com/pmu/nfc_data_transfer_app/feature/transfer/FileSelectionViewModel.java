package com.pmu.nfc_data_transfer_app.feature.transfer;

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
import com.pmu.nfc_data_transfer_app.data.source.AndroidFileDataSource;
import com.pmu.nfc_data_transfer_app.data.source.FileDataSource;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.repository.FileRepository;
import com.pmu.nfc_data_transfer_app.data.repository.FileRepositoryImpl;
import com.pmu.nfc_data_transfer_app.util.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FileSelectionViewModel extends AndroidViewModel {

    private static final String TAG = "MainViewModel";

    private final FileRepository fileRepository;
    private final FileDataSource fileDataSource;
    private final Executor backgroundExecutor = Executors.newCachedThreadPool();

    // LiveData observed by the UI
    public final LiveData<List<TransferFileItem>> fileList;
    public final LiveData<String> selectedCountText;
    public final LiveData<Boolean> isTransferEnabled;

    // For one-time events like showing Toasts or triggering navigation
    public final MutableLiveData<Event<String>> messageToast = new MutableLiveData<>();
    public final LiveData<Event<String>> toastMessage = messageToast;

    public final MutableLiveData<Boolean> currentlyLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = currentlyLoading; // To show progress during file reading


    public FileSelectionViewModel(@NonNull Application application) {
        super(application);

        this.fileDataSource = new AndroidFileDataSource(application);
        this.fileRepository = new FileRepositoryImpl(); // Singleton or scoped instance preferred with DI

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
                            TransferFileItem newItem = new TransferFileItem(name, size, mimeType, uri, isImage);

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

    public ListenableFuture<Map<String, byte[]>> readFilesForTransfer() {
        List<TransferFileItem> currentFiles = fileRepository.getAllFilesSnapshot();

        return CallbackToFutureAdapter.getFuture(completer -> {
            backgroundExecutor.execute(() -> {
                Map<String, byte[]> filesToTransfer = new HashMap<>();
                boolean allFilesRead = true;
                int successCount = 0;

                for (TransferFileItem item : currentFiles) {
                    try {
                        byte[] fileBytes = fileDataSource.getFileBytes(item.getUri());

                        if (fileBytes != null) {
                            if (item.getSize() == 0) {
                                TransferFileItem updatedItem = new TransferFileItem(
                                        item.getName(),
                                        fileBytes.length,
                                        item.getMimeType(),
                                        item.getUri(),
                                        item.isImage()
                                );

                                fileRepository.removeFile(item);
                                fileRepository.addFile(updatedItem);
                                filesToTransfer.put(updatedItem.getName(), fileBytes);
                            } else {
                                filesToTransfer.put(item.getName(), fileBytes);
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