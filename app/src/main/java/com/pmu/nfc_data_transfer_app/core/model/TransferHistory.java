package com.pmu.nfc_data_transfer_app.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransferHistory {
    private final int id;
    private final String deviceName;
    private final Date transferDate;
    private final String transferType;
    private final List<TransferFileItem> files;
    private final long totalSize;

    public TransferHistory(
            int id,
            String deviceName,
            Date transferDate,
            String transferType,
            List<TransferFileItem> files,
            long totalSize
    ) {
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

    public List<TransferFileItem> getFiles() {
        return new ArrayList<>(files);
    }

    public int getFileCount() {
        return files.size();
    }

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransferHistory that = (TransferHistory) o;

        return this.getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
