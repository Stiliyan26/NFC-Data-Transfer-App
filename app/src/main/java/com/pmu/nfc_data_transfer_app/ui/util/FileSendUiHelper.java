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

public class FileSendUiHelper extends BaseFileTransferUiHelper {

    public FileSendUiHelper(Context context,
                            TextView titleText,
                            ProgressBar transferAnimation,
                            TextView transferStatusText,
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
        super(context, titleText, transferAnimation, transferStatusText,
                progressIndicator, progressText, filesRecyclerView,
                cancelButton, successContainer, successAnimation,
                successText, transferSummary, doneButton, filesListTitle, adapter);
    }

    @Override
    protected void initializeUi() {
        // Set title for sending
        titleText.setText(R.string.sending_files);
        statusText.setText(R.string.preparing_files);

        // Setup initial progress
        progressIndicator.setProgress(0);
    }

    public void updateProgressText(int completedFiles, int totalFiles, int progress) {
        progressText.setText(context.getString(R.string.transfer_of, completedFiles, totalFiles, progress));
        progressIndicator.setProgress(progress);
    }

    public void setTransferInProgress() {
        statusText.setText(R.string.transfer_in_progress);
    }

    public void setTransferFailed() {
        statusText.setText(R.string.transfer_failed);
    }

    public void showTransferCompleted(int totalFiles, long totalSize) {
        showTransferCompleted(totalFiles, totalSize, "transfer_summary", "files_sent_successfully");
    }

    public void showTransferFailed(int completedFiles, int totalFiles, long totalSize) {
        showTransferFailed(completedFiles, totalFiles, totalSize, "transfer_failed");
    }
}