package com.pmu.nfc_data_transfer_app.data.model;

import android.net.Uri;

public class FileItem {
    private String fileName;
    private long fileSize;
    private String fileType;
    private Uri fileUri;
    private boolean isImage;

    public FileItem(
            String fileName,
            long fileSize,
            String fileType,
            Uri fileUri,
            boolean isImage
    ) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.fileUri = fileUri;
        this.isImage = isImage;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileItem fileItem = (FileItem) o;

        return fileUri.equals(fileItem.fileUri);
    }

    @Override
    public int hashCode() {
        return fileUri.hashCode();
    }
}