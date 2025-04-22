package com.pmu.nfc_data_transfer_app.core.model;

/**
 * Enum representing the status of a file during transfer
 */
public enum FileTransferStatus {
    PENDING,       // File is waiting to be transferred
    IN_PROGRESS,   // File is currently being transferred
    COMPLETED,     // File transfer completed successfully
    FAILED         // File transfer failed
}
