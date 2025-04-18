package com.pmu.nfc_data_transfer_app.ui.util;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;

public class FileSendUiHelper {
    private final Context context;
    private final TextView titleText;
    private final ProgressBar transferAnimation;
    private final TextView transferStatusText;
    private final ProgressBar progressIndicator;
    private final TextView progressText;
    private final RecyclerView filesRecyclerView;
    private final Button cancelButton;
    private final ConstraintLayout successContainer;
    private final ImageView successAnimation;
    private final TextView successText;
    private final TextView transferSummary;
    private final Button doneButton;
    private final View filesListTitle;
    private final TransferFileAdapter adapter;

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
        this.context = context;
        this.titleText = titleText;
        this.transferAnimation = transferAnimation;
        this.transferStatusText = transferStatusText;
        this.progressIndicator = progressIndicator;
        this.progressText = progressText;
        this.filesRecyclerView = filesRecyclerView;
        this.cancelButton = cancelButton;
        this.successContainer = successContainer;
        this.successAnimation = successAnimation;
        this.successText = successText;
        this.transferSummary = transferSummary;
        this.doneButton = doneButton;
        this.filesListTitle = filesListTitle;
        this.adapter = adapter;

        initializeProgressUi();
    }

    private void initializeProgressUi() {
        progressIndicator.setProgress(0);
    }

    public void updateProgressText(int completedFiles, int totalFiles, int progress) {
        progressText.setText(context.getString(R.string.transfer_of, completedFiles, totalFiles, progress));
        progressIndicator.setProgress(progress);
    }

    public void updateFileProgress(int fileIndex, int progress) {
        adapter.updateFileProgress(fileIndex, progress);
    }

    public void showTransferCompleted(int totalFiles, long totalSize) {
        // Hide transfer UI
        transferAnimation.setVisibility(View.GONE);
        transferStatusText.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        filesRecyclerView.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        filesListTitle.setVisibility(View.GONE);

        // Show success UI
        successContainer.setVisibility(View.VISIBLE);

        // Apply animation to success icon
        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        successAnimation.startAnimation(fadeIn);

        // Format the summary text
        String formattedSize = Formatter.formatFileSize(context, totalSize);
        transferSummary.setText(context.getString(R.string.transfer_summary, totalFiles, formattedSize));
    }

    public void setTransferInProgress() {
        transferStatusText.setText(context.getString(R.string.transfer_in_progress));
    }

    public void setTransferFailed() {
        transferStatusText.setText(context.getString(R.string.transfer_failed));
    }

    public void showTransferFailed(int completedFiles, int totalFiles, long totalSize) {
        transferAnimation.setVisibility(View.GONE);
        transferStatusText.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        filesRecyclerView.setVisibility(View.VISIBLE);

        cancelButton.setVisibility(View.GONE);
        filesListTitle.setVisibility(View.VISIBLE);

        // Show success UI but with error message
        successContainer.setVisibility(View.VISIBLE);
        successText.setText(R.string.transfer_failed);

        // Change the success image (assuming you have an error image resource)
        // If you don't have an error image, you can keep the success image
        // successAnimation.setImageResource(R.drawable.ic_error);

        String formattedSize = Formatter.formatFileSize(context, totalSize);

        String summaryText = "Completed " + completedFiles + " of " +
                totalFiles + " files (" + formattedSize + ")";

        transferSummary.setText(summaryText);
    }
}