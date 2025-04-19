package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.service.HCEService;
import com.pmu.nfc_data_transfer_app.service.NfcService;
import com.pmu.nfc_data_transfer_app.service.ReceiveManagerService;
import com.pmu.nfc_data_transfer_app.service.TransferManagerFactory;
import com.pmu.nfc_data_transfer_app.ui.util.FileReceiveUiHelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class FileReceiveActivity extends BaseFileTransferActivity implements ReceiveManagerService.ReceiveProgressCallback {

    private static final String TAG = "FileReceiveActivity";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "extra_bluetooth_device_address";

    private String bluetoothDeviceAddress;

    private ReceiveManagerService receiveManager;

    private NfcService nfcService;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_file_receive;
    }

    @Override
    protected void processIntent() {
        nfcService = new NfcService(NfcAdapter.getDefaultAdapter(this));

        if (getIntent().hasExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS)) {
            bluetoothDeviceAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS);

            // Get NFC adapter
            NfcAdapter nfcAdapter = nfcService.getNfcAdapter();

            // Check if NFC is available and enabled
            if (nfcAdapter != null) {
                // We'll set up the NFC foreground dispatch in onResume()

                Log.d(TAG, "Received MAC address: " + bluetoothDeviceAddress);
            } else {
                Log.e(TAG, "NFC adapter not available");
                Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Log.e(TAG, "ERROR: No MAC address found in intent!");
            Toast.makeText(this, "Error: No Bluetooth device address provided", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Disable foreground dispatch when activity is paused
        if (nfcService.getNfcAdapter() != null) {
            nfcService.getNfcAdapter().disableForegroundDispatch(this);
        }
    }

    @Override
    protected void setupUiHelper() {
        uiHelper = new FileReceiveUiHelper(
                this, titleText, transferAnimation, statusText,
                progressIndicator, progressText, filesRecyclerView,
                cancelButton, successContainer, successAnimation,
                successText, transferSummary, doneButton, filesListTitle, adapter
        );
    }

    @Override
    protected void setupTransfer() {
        receiveManager = TransferManagerFactory.createReceiveManager(
                bluetoothDeviceAddress, dbHelper, this
        );

        receiveManager.startReceiving();
    }

    @Override
    protected void onCancelClicked() {
        if (receiveManager != null) {
            receiveManager.cancelTransfer();
        }

        showCancelledToast(R.string.receive_canceled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiveManager != null) {
            receiveManager.shutdown();
        }
    }

    @Override
    public void onProgressUpdated(int receivedFiles, int totalFiles, int progress) {
        ((FileReceiveUiHelper) uiHelper).updateProgressText(receivedFiles, totalFiles, progress);
    }

    @Override
    public void onReceiveCompleted(boolean success) {
        if (success) {
            ((FileReceiveUiHelper) uiHelper).showReceiveCompleted(
                    receiveManager.getTotalFiles(), receiveManager.getTotalSize());
        } else {
            ((FileReceiveUiHelper) uiHelper).showReceiveFailed(
                    receiveManager.getReceivedFiles(),
                    receiveManager.getTotalFiles(),
                    receiveManager.getTotalSize()
            );
            showFailedToast("Receive failed. Some files could not be received.");
        }
    }

    @Override
    public void onFileReceiveFailed(int fileIndex, String errorMessage) {
        Toast.makeText(
                this,
                "Failed to receive file: " + transferItems.get(fileIndex).getName(),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onFilesDiscovered(ArrayList<TransferFileItem> items) {
        transferItems.clear();
        transferItems.addAll(items);

        adapter.notifyDataSetChanged();

        ((FileReceiveUiHelper) uiHelper).setStatusReceivingFiles();
    }

    public static void start(AppCompatActivity activity, String bluetoothDeviceAddress) {
        Intent intent = new Intent(activity, FileReceiveActivity.class);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);
        HCEService hceService = new HCEService();
        hceService.setResponseString(bluetoothDeviceAddress);
        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}