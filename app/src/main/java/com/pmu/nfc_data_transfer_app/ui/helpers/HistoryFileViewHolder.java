package com.pmu.nfc_data_transfer_app.ui.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;

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

    public void bind(FileItem fileItem) {
        if (fileItem == null) return;

        // Set file name
        fileName.setText(fileItem.getFileName());

        // Set file info (size and type)
        String fileSize = FileUtils.formatFileSize(fileItem.getFileSize());
        String fileTypeStr = FileUtils.getFileTypeDescription(fileItem);
        fileInfo.setText(fileSize + " • " + fileTypeStr);

        // Set the appropriate icon based on file type
        int iconResource = FileUtils.getIconForFileType(fileItem.getFileType());
        fileIcon.setImageResource(iconResource);

        // For image files, set the image background visible
        if (fileItem.isImage()) {
            fileIconBackground.setVisibility(View.VISIBLE);
        } else {
            fileIconBackground.setVisibility(View.GONE);
        }
    }
}