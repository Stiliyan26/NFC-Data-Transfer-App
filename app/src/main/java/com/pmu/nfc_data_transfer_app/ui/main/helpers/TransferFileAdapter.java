package com.pmu.nfc_data_transfer_app.ui.main.helpers;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;

import java.util.List;

public class TransferFileAdapter extends RecyclerView.Adapter<TransferFileAdapter.FileViewHolder> {

    private final List<TransferFileItem> fileItems;

    public TransferFileAdapter(List<TransferFileItem> fileItems) {
        this.fileItems = fileItems;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transfer_progress_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        TransferFileItem item = fileItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    /**
     * Update progress for a specific file
     *
     * @param position Position of the file in the list
     * @param progress Progress value (0-100)
     */
    public void updateFileProgress(int position, int progress) {
        if (position >= 0 && position < fileItems.size()) {
            fileItems.get(position).setProgress(progress);
            notifyItemChanged(position, progress);
        }
    }

    /**
     * ViewHolder for file items
     */
    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView fileIcon;
        private final TextView fileName;
        private final TextView fileInfo;
        private final ImageView fileStatus;
        private final ProgressBar fileProgress;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileName = itemView.findViewById(R.id.fileName);
            fileInfo = itemView.findViewById(R.id.fileInfo);
            fileStatus = itemView.findViewById(R.id.fileStatus);
            fileProgress = itemView.findViewById(R.id.fileProgress);
        }

        public void bind(TransferFileItem item) {
            // Set file name
            fileName.setText(item.getName());

            // Set file size
            String formattedSize = Formatter.formatFileSize(itemView.getContext(), item.getSize());
            fileInfo.setText(formattedSize);

            // Set file icon based on type
            setFileIcon(item);

            // Set status icon and progress visibility
            updateStatus(item);

            // Set progress
            fileProgress.setProgress(item.getProgress());
        }

        private void setFileIcon(TransferFileItem item) {
            // Set icon based on file type - using standard Android icons
            String fileName = item.getName().toLowerCase();
            int iconRes;

            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                    fileName.endsWith(".png") || fileName.endsWith(".gif")) {
                iconRes = android.R.drawable.ic_menu_gallery; // Image file
            } else if (fileName.endsWith(".pdf")) {
                iconRes = android.R.drawable.ic_menu_agenda; // Document
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                iconRes = android.R.drawable.ic_menu_edit; // Text document
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                iconRes = android.R.drawable.ic_menu_sort_by_size; // Spreadsheet
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                    fileName.endsWith(".mov")) {
                iconRes = android.R.drawable.ic_media_play; // Video
            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                    fileName.endsWith(".ogg")) {
                iconRes = android.R.drawable.ic_lock_silent_mode_off; // Audio
            } else {
                iconRes = android.R.drawable.ic_menu_report_image; // Generic file
            }

            fileIcon.setImageResource(iconRes);
        }

        private void updateStatus(TransferFileItem item) {
            int statusIcon;
            boolean showProgress = false;

            switch (item.getStatus()) {
                case PENDING:
                    statusIcon = android.R.drawable.ic_menu_recent_history; // Clock icon for pending
                    showProgress = false;
                    break;
                case IN_PROGRESS:
                    statusIcon = android.R.drawable.ic_popup_sync; // Sync icon for in progress
                    showProgress = true;
                    break;
                case COMPLETED:
                    statusIcon = android.R.drawable.ic_menu_send; // Checkmark for completed
                    showProgress = false;
                    break;
                case FAILED:
                    statusIcon = android.R.drawable.ic_dialog_alert; // Alert for failed
                    showProgress = false;
                    break;
                default:
                    statusIcon = android.R.drawable.ic_menu_help; // Question mark for unknown
                    showProgress = false;
            }

            fileStatus.setImageResource(statusIcon);

            // Show/hide progress bar
            fileProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0) instanceof Integer) {
            // Update just the progress
            int progress = (int) payloads.get(0);
            holder.fileProgress.setProgress(progress);
        } else {
            // Full bind if no payload
            super.onBindViewHolder(holder, position, payloads);
        }
    }
}