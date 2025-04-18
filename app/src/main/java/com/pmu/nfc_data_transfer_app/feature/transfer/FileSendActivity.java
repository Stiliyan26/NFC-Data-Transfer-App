package com.pmu.nfc_data_transfer_app.feature.transfer;

import android.content.Intent;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.service.SendManagerService;
import com.pmu.nfc_data_transfer_app.service.TransferManagerFactory;
import com.pmu.nfc_data_transfer_app.ui.util.FileSendUiHelper;

import java.util.ArrayList;

public class FileSendActivity extends BaseFileTransferActivity implements SendManagerService.TransferProgressCallback {

    private static final String EXTRA_FILE_ITEMS = "extra_file_items";
    private static final String EXTRA_BLUETOOTH_DEVICE_ADDRESS = "receiver_mac_address";

    private String bluetoothDeviceAddress;

    private SendManagerService sendManager;

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