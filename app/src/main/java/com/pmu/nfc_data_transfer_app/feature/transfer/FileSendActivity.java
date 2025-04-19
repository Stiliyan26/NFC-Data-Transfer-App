package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.service.NfcService;
import com.pmu.nfc_data_transfer_app.service.SendManagerService;
import com.pmu.nfc_data_transfer_app.service.TransferManagerFactory;
import com.pmu.nfc_data_transfer_app.ui.util.FileSendUiHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSendActivity extends BaseFileTransferActivity implements SendManagerService.TransferProgressCallback {

    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "receiver_mac_address";

    private String bluetoothDeviceAddress;
    private SendManagerService sendManager;

    // Views for the device proximity screen
    private ConstraintLayout deviceProximityContainer;
    private ConstraintLayout transferContainer;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_file_send;
    }

    @Override
    protected void processIntent() {
        if (getIntent().hasExtra(EXTRA_FILE_ITEMS)) {
            ArrayList<? extends Parcelable> parcelables = getIntent().getParcelableArrayListExtra(EXTRA_FILE_ITEMS);

            if (parcelables != null) {
                for (Parcelable parcelable : parcelables) {
                    if (parcelable instanceof TransferFileItem) {
                        transferItems.add((TransferFileItem) parcelable);
                    }
                }
            }
        }

        if (getIntent().hasExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS)) {
            bluetoothDeviceAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS);
        }
    }

    @Override
    protected void initViews() {
        super.initViews();

        // Initialize the containers for the different screens
        deviceProximityContainer = findViewById(R.id.deviceProximityContainer);
        transferContainer = findViewById(R.id.transferContainer);

        // Show the proximity screen, hide the transfer screen
        if (deviceProximityContainer != null) {
            deviceProximityContainer.setVisibility(View.VISIBLE);
        }

        if (transferContainer != null) {
            transferContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start a 5-second timer to hide the proximity screen
        handler.postDelayed(this::showTransferUI, 5000);
    }

    private void showTransferUI() {
        try {
            // Hide proximity screen, show transfer UI
            if (deviceProximityContainer != null) {
                deviceProximityContainer.setVisibility(View.GONE);
            }

            if (transferContainer != null) {
                transferContainer.setVisibility(View.VISIBLE);
            }

            // Start the file transfer
            startFileTransfer();
        } catch (Exception e) {
            Log.e("FileSendActivity", "Error showing transfer UI", e);
            // Start transfer even if there's an error with UI
            startFileTransfer();
        }
    }

    private void startFileTransfer() {
        sendManager = TransferManagerFactory.createSendManager(
                transferItems, dbHelper, this
        );
        sendManager.startTransfer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcService nfcService = new NfcService(NfcAdapter.getDefaultAdapter(this));
        try {
            nfcService.getNfcAdapter().enableReaderMode(this, tag -> {
                IsoDep isoDep = IsoDep.get(tag);
                try {
                    isoDep.connect();
                    byte[] selectAidApdu = hexStringToByteArray(GlobalConstants.HCE_AID);
                    byte[] response = isoDep.transceive(selectAidApdu);

                    // Remove trailing 0x9000 status word
                    byte[] data = Arrays.copyOf(response, response.length - 2);
                    String message = new String(data, StandardCharsets.UTF_8);
                    Log.d("HCE", "Received: " + message);

                    isoDep.close();

                    // If we detect an NFC device, show the UI immediately
                    runOnUiThread(this::showTransferUI);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        } catch (Exception e) {
            Log.e("FileSendActivity", "Error with NFC", e);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        return data;
    }

    @Override
    protected void setupUiHelper() {
        uiHelper = new FileSendUiHelper(
                this, titleText, transferAnimation, statusText,
                progressIndicator, progressText, filesRecyclerView,
                cancelButton, successContainer, successAnimation,
                successText, transferSummary, doneButton, filesListTitle, adapter
        );
    }

    @Override
    protected void setupTransfer() {
        // Transfer will be started after the timer or when NFC is detected
        // See showTransferUI method
    }

    @Override
    protected void onCancelClicked() {
        if (sendManager != null) {
            sendManager.cancelTransfer();
        }

        showCancelledToast(R.string.transfer_canceled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove any pending callbacks
        handler.removeCallbacksAndMessages(null);

        if (sendManager != null) {
            sendManager.shutdown();
        }
    }

    @Override
    public void onProgressUpdated(int completedFiles, int totalFiles, int progress) {
        ((FileSendUiHelper) uiHelper).updateProgressText(completedFiles, totalFiles, progress);
    }

    @Override
    public void onTransferCompleted(boolean success) {
        if (success) {
            ((FileSendUiHelper) uiHelper).showTransferCompleted(sendManager.getTotalFiles(), sendManager.getTotalSize());
        } else {
            ((FileSendUiHelper) uiHelper).showTransferFailed(
                    sendManager.getCompletedFiles(),
                    sendManager.getTotalFiles(),
                    sendManager.getTotalSize()
            );

            showFailedToast("Transfer failed. Some files could not be transferred.");
        }
    }

    @Override
    public void onFileTransferFailed(int fileIndex, String errorMessage) {
        Toast.makeText(this, "Failed to transfer file: " + transferItems.get(fileIndex).getName(),
                Toast.LENGTH_SHORT).show();
    }

    public static void start(
            AppCompatActivity activity,
            ArrayList<TransferFileItem> fileItems,
            String bluetoothDeviceAddress
    ) {
        Intent intent = new Intent(activity, FileSendActivity.class);

        intent.putParcelableArrayListExtra(EXTRA_FILE_ITEMS, fileItems);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE_ADDRESS, bluetoothDeviceAddress);
        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}