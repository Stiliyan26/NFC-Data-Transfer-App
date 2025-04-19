package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.service.SendManagerService;
import com.pmu.nfc_data_transfer_app.service.TransferManagerFactory;
import com.pmu.nfc_data_transfer_app.ui.util.FileSendUiHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FileSendActivity extends BaseFileTransferActivity implements SendManagerService.TransferProgressCallback {

    private final String TAG = "FileSendActivity";
    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "receiver_mac_address";

    private String bluetoothDeviceAddress;

    private SendManagerService sendManager;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] filters;
    private String[][] techList = new String[][] { new String[] { IsoDep.class.getName() } };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        // Само IsoDep (HCE) ще бъде прихващан
        filters = new IntentFilter[] {
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            IsoDep iso = IsoDep.get(tag);

            if (iso != null) {
                try {
                    iso.connect();
                    byte[] selectApdu = buildSelectApdu();
                    byte[] response = iso.transceive(selectApdu);
                    iso.close();

                    // Последните два байта са статус word, останалото е MAC
                    int len = response.length - 2;
                    String serverMac = new String(response, 0, len, StandardCharsets.UTF_8);

                    Toast.makeText(this, "Получен MAC: " + serverMac, Toast.LENGTH_SHORT).show();
                    bluetoothDeviceAddress = serverMac;
                    setupTransfer();
                } catch (IOException e) {
                    Log.e(TAG, "NFC IO error", e);
                }
            }
        }
    }

    private byte[] buildSelectApdu() {
        byte[] header = {0x00, (byte)0xA4, 0x04, 0x00};
        byte[] aid = {(byte)0xD2,0x76,0x00,0x00,(byte)0x85,0x01,0x01};
        byte lc = (byte)aid.length;
        byte le = 0; // очакваме целия отговор
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(header, 0, header.length);
        bout.write(lc);
        bout.write(aid, 0, aid.length);
        bout.write(le);
        return bout.toByteArray();
    }

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
        sendManager = TransferManagerFactory.createSendManager(
                transferItems, dbHelper, this
        );

        sendManager.startTransfer();
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