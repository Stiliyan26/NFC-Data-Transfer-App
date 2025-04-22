package com.pmu.nfc_data_transfer_app.ui.viewholder;

import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.util.FileUtils;

public class TransferFileViewHolder extends RecyclerView.ViewHolder {
    private final ImageView fileIcon;
    private final ImageView fileIconBackground;
    private final TextView fileName;
    private final TextView fileInfo;
    private final ImageView fileStatus;
    private final ProgressBar fileProgress;

    public TransferFileViewHolder(@NonNull View itemView) {
        super(itemView);
        fileIcon = itemView.findViewById(R.id.fileIcon);
        fileIconBackground = itemView.findViewById(R.id.fileIconBackground);
        fileName = itemView.findViewById(R.id.fileName);
        fileInfo = itemView.findViewById(R.id.fileInfo);
        fileStatus = itemView.findViewById(R.id.fileStatus);
        fileProgress = itemView.findViewById(R.id.fileProgress);
    }

    public void bind(TransferFileItem item) {
        fileName.setText(item.getName());

        String formattedSize = Formatter.formatFileSize(itemView.getContext(), item.getSize());
        fileInfo.setText(formattedSize);

        setFileIcon(item);

        updateStatus(item);

        fileProgress.setProgress(item.getProgress());
    }

    public ProgressBar getFileProgress() {
        return fileProgress;
    }

    private void setFileIcon(TransferFileItem item) {

        String fileName = item.getName().toLowerCase();
        String mimeType = FileUtils.getMimeTypeFromFileName(fileName);

        // Clear any previous image loading if using Glide
        if (fileIcon.getContext() != null) {
            Glide.with(fileIcon.getContext()).clear(fileIcon);
        }

        // Reset icon view properties
        fileIcon.setColorFilter(null);
        fileIcon.setPadding(0, 0, 0, 0);

        if (mimeType != null && mimeType.startsWith("image/")) {
            if (item.getUri() != null) {
                if (fileIconBackground != null) {
                    fileIconBackground.setVisibility(View.GONE);
                }

                fileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(fileIcon.getContext())
                        .load(item.getUri())
                        .apply(new RequestOptions()
                                .placeholder(FileUtils.getIconForFileType(mimeType))
                                .error(FileUtils.getIconForFileType(mimeType))
                                .centerCrop()
                                .override(200, 200))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(fileIcon);
            } else {
                setupFileIcon(FileUtils.getIconForFileType(mimeType));
            }
        } else {
            setupFileIcon(FileUtils.getIconForFileType(mimeType));
        }
    }

    private void setupFileIcon(int iconResId) {
        // Show background if available
        if (fileIconBackground != null) {
            fileIconBackground.setVisibility(View.VISIBLE);
        }

        // Apply padding for better look with icons
        int padding = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.file_icon_padding);
        fileIcon.setPadding(padding, padding, padding, padding);

        // Set the icon resource
        fileIcon.setImageResource(iconResId);
        fileIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }


    private void updateStatus(TransferFileItem item) {
        int statusIcon;

        boolean showProgress = false;

        switch (item.getStatus()) {
            case PENDING:
                statusIcon = android.R.drawable.ic_menu_recent_history;
                showProgress = false;
                break;
            case IN_PROGRESS:
                statusIcon = android.R.drawable.ic_popup_sync;
                showProgress = true;
                break;
            case COMPLETED:
                statusIcon = android.R.drawable.ic_menu_send;
                showProgress = false;
                break;
            case FAILED:
                statusIcon = android.R.drawable.ic_dialog_alert;
                showProgress = false;
                break;
            default:
                statusIcon = android.R.drawable.ic_menu_help;
                showProgress = false;
        }

        fileStatus.setImageResource(statusIcon);

        fileProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }
}
