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

import com.pmu.nfc_data_transfer_app.ui.adapters.TransferFileAdapter;

/**
 * Base UI helper class for file transfer activities
 * Provides common UI handling functionality for both sending and receiving files
 */
public abstract class BaseFileTransferUiHelper {
    protected final Context context;
    protected final TextView titleText;
    protected final ProgressBar transferAnimation;
    protected final TextView statusText;
    protected final ProgressBar progressIndicator;
    protected final TextView progressText;
    protected final RecyclerView filesRecyclerView;
    protected final Button cancelButton;
    protected final ConstraintLayout successContainer;
    protected final ImageView successAnimation;
    protected final TextView successText;
    protected final TextView transferSummary;
    protected final Button doneButton;
    protected final View filesListTitle;
    protected final TransferFileAdapter adapter;

    public BaseFileTransferUiHelper(Context context,
                                    TextView titleText,
                                    ProgressBar transferAnimation,
                                    TextView statusText,
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
        this.statusText = statusText;
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

        initializeUi();
    }

    /**
     * Initialize the UI components with default values
     */
    protected abstract void initializeUi();

    /**
     * Update the file transfer progress in the adapter
     */
    public void updateFileProgress(int fileIndex, int progress) {
        adapter.updateFileProgress(fileIndex, progress);
    }

    /**
     * Show the transfer completed UI
     */
    protected void showTransferCompleted(
            int totalFiles,
            long totalSize,
            String summaryStringResource,
            String successStringResource
    ) {
        // Hide transfer UI
        transferAnimation.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
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

        transferSummary.setText(context.getString(context.getResources().getIdentifier(
                        summaryStringResource, "string", context.getPackageName()),
                totalFiles, formattedSize));

        successText.setText(context.getResources().getIdentifier(
                successStringResource, "string", context.getPackageName()));
    }

    /**
     * Show the transfer failed UI
     */
    protected void showTransferFailed(
            String failedStringResource
    ) {
        // Hide transfer UI
        transferAnimation.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        filesRecyclerView.setVisibility(View.GONE);

        cancelButton.setVisibility(View.GONE);
        filesListTitle.setVisibility(View.GONE);

        // Show success UI but with error message
        successContainer.setVisibility(View.VISIBLE);
        successText.setText(context.getResources().getIdentifier(
                failedStringResource, "string", context.getPackageName()));

        String summaryText = "";

        transferSummary.setText(summaryText);
    }
}
