package com.pmu.nfc_data_transfer_app.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransferHistory {
    private final int id;
    private final String deviceName;
    private final Date transferDate;
    private final String transferType;
    private final List<FileItem> files;
    private final long totalSize;

    public TransferHistory(String deviceName, Date transferDate, String transferType, 
                          List<FileItem> files, long totalSize) {
        this.id = 0; // Overwritten in database anyways
        this.deviceName = deviceName;
        this.transferDate = transferDate;
        this.transferType = transferType;
        this.files = new ArrayList<>(files);
        this.totalSize = totalSize;
    }

    public TransferHistory(int id, String deviceName, Date transferDate, String transferType,
                           List<FileItem> files, long totalSize) {
        this.id = id;
        this.deviceName = deviceName;
        this.transferDate = transferDate;
        this.transferType = transferType;
        this.files = new ArrayList<>(files);
        this.totalSize = totalSize;
    }

    public int getId() {
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