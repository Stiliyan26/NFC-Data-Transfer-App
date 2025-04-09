package com.pmu.nfc_data_transfer_app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import java.util.ArrayList;
import java.util.List;

public class SelectedFileRepository implements FileRepository {

    private final MutableLiveData<List<FileItem>> selectedFilesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> selectedFileCountLiveData = new MutableLiveData<>(0);
    private final List<FileItem> fileList = new ArrayList<>();

    @Override
    public LiveData<List<FileItem>> getSelectedFiles() {
        return selectedFilesLiveData;
    }

    @Override
    public LiveData<Integer> getSelectedFileCount() {
        return selectedFileCountLiveData;
    }

    @Override
    public void addFile(FileItem fileItem) {
        if (!containsFile(fileItem)) {
            fileList.add(fileItem);
            updateLiveData();
        }
    }

    @Override
    public void removeFile(int position) {
        if (position >= 0 && position < fileList.size()) {
            fileList.remove(position);
            updateLiveData();
        }
    }

    @Override
    public void removeFile(FileItem fileItem) {
        if (fileList.remove(fileItem)) {
            updateLiveData();
        }
    }

    @Override
    public List<FileItem> getAllFilesSnapshot() {
        // Copy to avoid concurrent modification
        return new ArrayList<>(fileList);
    }

    @Override
    public boolean containsFile(FileItem fileItem) {
        return fileList.contains(fileItem);
    }


    @Override
    public void clearAllFiles() {
        fileList.clear();
        updateLiveData();
    }

    private void updateLiveData() {
        // Crate new list to show live data
        selectedFilesLiveData.postValue(new ArrayList<>(fileList));
        selectedFileCountLiveData.postValue(fileList.size());
    }
}