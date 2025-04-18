package com.pmu.nfc_data_transfer_app.service;

import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;

import java.util.List;

/**
 * Factory class to create various transfer manager services
 */
public class TransferManagerFactory {
    
    public static SendManagerService createSendManager(
            List<TransferFileItem> items,
            DatabaseHelper dbHelper,
            SendManagerService.TransferProgressCallback callback) {
        
        return new SendManagerService(items, dbHelper, callback);
    }
    public static ReceiveManagerService createReceiveManager(
            String bluetoothDeviceAddress,
            DatabaseHelper dbHelper,
            ReceiveManagerService.ReceiveProgressCallback callback) {
        
        return new ReceiveManagerService(bluetoothDeviceAddress, dbHelper, callback);
    }
}