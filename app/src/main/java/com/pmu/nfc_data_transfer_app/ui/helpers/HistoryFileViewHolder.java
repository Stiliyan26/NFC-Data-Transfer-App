package com.pmu.nfc_data_transfer_app.ui.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;

/**
 * ViewHolder for file items in history view (without remove button)
 */
public class HistoryFileViewHolder extends RecyclerView.ViewHolder {
    private final ImageView fileIcon;
    private final ImageView fileIconBackground;
    private final TextView fileName;
    private final TextView fileInfo;

    public HistoryFileViewHolder(@NonNull View itemView) {
        super(itemView);
        fileIcon = itemView.findViewById(R.id.fileIcon);
        fileIconBackground = itemView.findViewById(R.id.fileIconBackground);
        fileName = itemView.findViewById(R.id.fileName);
        fileInfo = itemView.findViewById(R.id.fileInfo);
    }

    public void bind(TransferFileItem fileItem) {
        if (fileItem == null) return;

        fileName.setText(fileItem.getName());

        String fileSize = FileUtils.formatFileSize(fileItem.getSize());

        String fileTypeStr = FileUtils.getFileTypeDescription(fileItem.getMimeType());
        fileInfo.setText(fileSize + " • " + fileTypeStr);

        int iconResource = FileUtils.getIconForFileType(fileItem.getMimeType());
        fileIcon.setImageResource(iconResource);

        if (fileItem.isImage()) {
            fileIconBackground.setVisibility(View.VISIBLE);
        } else {
            fileIconBackground.setVisibility(View.GONE);
        }
    }
}