package com.pmu.nfc_data_transfer_app.feature.transfer;

import static com.pmu.nfc_data_transfer_app.service.HCEService.toHex;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.service.HCEService;
import com.pmu.nfc_data_transfer_app.service.NfcService;
import com.pmu.nfc_data_transfer_app.service.SendManagerService;
import com.pmu.nfc_data_transfer_app.service.TransferManagerFactory;
import com.pmu.nfc_data_transfer_app.ui.util.FileSendUiHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class FileSendActivity extends BaseFileTransferActivity implements SendManagerService.TransferProgressCallback, NfcAdapter.ReaderCallback {

    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String TAG = "FileSendActivity";
    private static String bluetoothDeviceMacAddress;

    private SendManagerService sendManager;
    private NfcAdapter nfcAdapter;

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
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        // TODO here will be the handshake code
//        while(unconnected){
//            get_mac_address();
//            wait(100);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(
                    this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
            );
        }

        // return mac address of other device
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        if (tag == null) return;

        final IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) return;

        new Thread(() -> {
            try {
                isoDep.connect();
                byte[] command = HCEService.hexStringToByteArray("00A4040007A0000002471001");
                byte[] response = isoDep.transceive(command);

                Log.d(TAG, "\nCard Response: " + toHex(response));

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    isoDep.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

        sendManager.startTransfer(this);
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
            ArrayList<TransferFileItem> fileItems
    ) {
        Intent intent = new Intent(activity, FileSendActivity.class);

        intent.putParcelableArrayListExtra(EXTRA_FILE_ITEMS, fileItems);
        activity.startActivity(intent);

        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}