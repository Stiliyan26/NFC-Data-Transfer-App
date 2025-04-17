package com.pmu.nfc_data_transfer_app.data.repository;

import androidx.lifecycle.LiveData;

import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;

import java.util.List;

public interface FileRepository {
    LiveData<List<TransferFileItem>> getSelectedFiles();
    LiveData<Integer> getSelectedFileCount();
    void addFile(TransferFileItem fileItem);
    void removeFile(int position);
    void removeFile(TransferFileItem fileItem);
    List<TransferFileItem> getAllFilesSnapshot();
    boolean containsFile(TransferFileItem fileItem);
    void clearAllFiles();
}