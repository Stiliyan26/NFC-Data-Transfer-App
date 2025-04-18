package com.pmu.nfc_data_transfer_app.ui.util;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;

public class FileReceiveUiHelper extends BaseFileTransferUiHelper {

    public FileReceiveUiHelper(Context context,
                               TextView titleText,
                               ProgressBar receiveAnimation,
                               TextView receiveStatusText,
                               ProgressBar progressIndicator,
                               TextView progressText,
                               RecyclerView filesRecyclerView,
                               Button cancelButton,
                               ConstraintLayout successContainer,
                               ImageView successAnimation,
                               TextView successText,
                               TextView transferSummary,
                               Button doneButton,
                               View filesListTitle,
                               TransferFileAdapter adapter
    ) {
        super(context, titleText, receiveAnimation, receiveStatusText,
                progressIndicator, progressText, filesRecyclerView,
                cancelButton, successContainer, successAnimation,
                successText, transferSummary, doneButton, filesListTitle, adapter);
    }

    @Override
    protected void initializeUi() {
        // Set title for receiving
        titleText.setText(R.string.receiving_files);
        statusText.setText(R.string.waiting_for_files);

        // Setup initial progress
        progressIndicator.setProgress(0);
        updateProgressText(0, 0, 0);
    }

    public void updateProgressText(int receivedFiles, int totalFiles, int progress) {
        progressText.setText(context.getString(R.string.receive_progress, receivedFiles, totalFiles, progress));
        progressIndicator.setProgress(progress);
    }

    public void setStatusConnecting() {
        statusText.setText(R.string.connecting_to_sender);
    }

    public void setStatusReceivingFileInfo() {
        statusText.setText(R.string.receiving_file_info);
    }

    public void setStatusReceivingFiles() {
        statusText.setText(R.string.receiving_files);
    }

    public void showReceiveCompleted(int totalFiles, long totalSize) {
        showTransferCompleted(totalFiles, totalSize, "receive_summary", "files_received_successfully");
    }

    public void showReceiveFailed(int receivedFiles, int totalFiles, long totalSize) {
        showTransferFailed(receivedFiles, totalFiles, totalSize, "receive_failed");
    }
}