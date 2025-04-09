package com.pmu.nfc_data_transfer_app.data.repository;

import androidx.lifecycle.LiveData;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import java.util.List;

public interface FileRepository {
    LiveData<List<FileItem>> getSelectedFiles();
    LiveData<Integer> getSelectedFileCount();
    void addFile(FileItem fileItem);
    void removeFile(int position);
    void removeFile(FileItem fileItem);
    List<FileItem> getAllFilesSnapshot();
    boolean containsFile(FileItem fileItem);
    void clearAllFiles();
}