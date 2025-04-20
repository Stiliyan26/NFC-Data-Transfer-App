package com.pmu.nfc_data_transfer_app.feature.transfer;

import static com.pmu.nfc_data_transfer_app.service.HCEService.toHex;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.service.HCEService;
import com.pmu.nfc_data_transfer_app.service.ReceiveManagerService;
import com.pmu.nfc_data_transfer_app.service.TransferManagerFactory;
import com.pmu.nfc_data_transfer_app.ui.util.FileReceiveUiHelper;


import java.io.IOException;
import java.util.ArrayList;

public class FileReceiveActivity extends BaseFileTransferActivity implements ReceiveManagerService.ReceiveProgressCallback/*, NfcAdapter.ReaderCallback*/ {

    private static final String TAG = "FileReceiveActivity";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "extra_bluetooth_device_address";

    private ReceiveManagerService receiveManager;
//    private NfcAdapter nfcAdapter;


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_file_receive;
    }

    @Override
    protected void processIntent() {
        // Initialize nfc adapter
//        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // TODO here will be the handshake code
//        bluetooth_service.connect_server();
//        set_mac_address_to_nfc();
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
                dbHelper, this
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

    public static void start(AppCompatActivity activity) {
        Intent intent = new Intent(activity, FileReceiveActivity.class);
        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (nfcAdapter != null) {
//            nfcAdapter.enableReaderMode(
//                    this,
//                    this,
//                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
//                    null
//            );
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (nfcAdapter != null) {
//            nfcAdapter.disableReaderMode(this);
//        }
//    }
//
//    @Override
//    public void onTagDiscovered(Tag tag) {
//        if (tag == null) return;
//
//        final IsoDep isoDep = IsoDep.get(tag);
//        if (isoDep == null) return;
//
//        new Thread(() -> {
//            try {
//                isoDep.connect();
//                byte[] command = HCEService.hexStringToByteArray("00A4040007A0000002471001");
//                byte[] response = isoDep.transceive(command);
//
//                Log.d(TAG, "\nCard Response: " + toHex(response));
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    isoDep.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
}