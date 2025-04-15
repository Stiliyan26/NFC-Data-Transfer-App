package com.pmu.nfc_data_transfer_app.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TransferHistory {
    private final String id;
    private final String deviceName;
    private final Date transferDate;
    private final String transferType; // "send" or "receive"
    private final List<FileItem> files;
    private final long totalSize;

    public TransferHistory(String deviceName, Date transferDate, String transferType, 
                          List<FileItem> files, long totalSize) {
        this.id = UUID.randomUUID().toString();
        this.deviceName = deviceName;
        this.transferDate = transferDate;
        this.transferType = transferType;
        this.files = new ArrayList<>(files);
        this.totalSize = totalSize;
    }

    public String getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public String getTransferType() {
        return transferType;
    }

    public List<FileItem> getFiles() {
        return new ArrayList<>(files);
    }

    public int getFileCount() {
        return files.size();
    }

    public long getTotalSize() {
        return totalSize;
    }
}