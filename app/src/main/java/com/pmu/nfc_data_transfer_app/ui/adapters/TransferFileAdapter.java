package com.pmu.nfc_data_transfer_app.ui.adapters;

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
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.pmu.nfc_data_transfer_app.util.FileUtils;

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
        private final ImageView fileIconBackground;
        private final TextView fileName;
        private final TextView fileInfo;
        private final ImageView fileStatus;
        private final ProgressBar fileProgress;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileIconBackground = itemView.findViewById(R.id.fileIconBackground);
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
            // Get the file mime type from the file name
            String fileName = item.getName().toLowerCase();
            String mimeType = getMimeTypeFromFileName(fileName);

            // Clear any previous image loading if using Glide
            if (fileIcon.getContext() != null) {
                Glide.with(fileIcon.getContext()).clear(fileIcon);
            }

            // Reset icon view properties
            fileIcon.setColorFilter(null);
            fileIcon.setPadding(0, 0, 0, 0);

            // Check if it's an image file
            if (mimeType != null && mimeType.startsWith("image/")) {
                // For images, if we have a URI, load the actual image
                if (item.getUri() != null) {
                    // Hide background
                    if (fileIconBackground != null) {
                        fileIconBackground.setVisibility(View.GONE);
                    }

                    fileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    // Load image using Glide
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
                    // If no URI but it's an image type, use the image icon
                    setupFileIcon(FileUtils.getIconForFileType(mimeType));
                }
            } else {
                // For other file types, use icons from FileUtils
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

        private String getMimeTypeFromFileName(String fileName) {
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else if (fileName.endsWith(".pdf")) {
                return "application/pdf";
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                return "application/msword";
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                return "application/vnd.ms-excel";
            } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                return "application/vnd.ms-powerpoint";
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov")) {
                return "video/mp4";
            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".ogg")) {
                return "audio/mpeg";
            } else if (fileName.endsWith(".txt")) {
                return "text/plain";
            }

            return "application/octet-stream"; // Default mime type
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