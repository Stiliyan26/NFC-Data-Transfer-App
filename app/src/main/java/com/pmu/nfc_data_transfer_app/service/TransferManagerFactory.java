package com.pmu.nfc_data_transfer_app.service;

import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;

import java.util.List;

/**
 * Factory class to create various transfer manager services
 */
public class TransferManagerFactory {
    
    /**
     * Creates a TransferManagerService for sending files
     * 
     * @param items List of files to send
     * @param dbHelper Database helper for storing transfer history
     * @param callback Callback for transfer progress updates
     * @return A configured TransferManagerService
     */
    public static SendManagerService createSendManager(
            List<TransferFileItem> items,
            DatabaseHelper dbHelper,
            SendManagerService.TransferProgressCallback callback) {
        
        return new SendManagerService(items, dbHelper, callback);
    }
    
    /**
     * Creates a ReceiveManagerService for receiving files
     * 
     * @param bluetoothDeviceAddress Address of the Bluetooth device to receive from
     * @param dbHelper Database helper for storing transfer history
     * @param callback Callback for receive progress updates
     * @return A configured ReceiveManagerService
     */
    public static ReceiveManagerService createReceiveManager(
            String bluetoothDeviceAddress,
            DatabaseHelper dbHelper,
            ReceiveManagerService.ReceiveProgressCallback callback) {
        
        return new ReceiveManagerService(bluetoothDeviceAddress, dbHelper, callback);
    }
}