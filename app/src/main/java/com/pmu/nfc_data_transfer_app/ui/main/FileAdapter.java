package com.pmu.nfc_data_transfer_app.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.bumptech.glide.Glide;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    public interface OnFileClickListener {
        void onFileRemoveClick(int position);
    }

    private final OnFileClickListener listener;
    private List<FileItem> fileList = new ArrayList<>();

    public FileAdapter(OnFileClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FileItem> newFileList) {
        this.fileList = newFileList != null ? new ArrayList<>(newFileList) : new ArrayList<>();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view, listener);
    }


    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);
        holder.bind(fileItem);
    }


    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        TextView fileType;
        ImageButton btnRemove;
        Context context;

        public FileViewHolder(@NonNull View itemView, OnFileClickListener listener) {
            super(itemView);
            context = itemView.getContext(); // Get context from the item view
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileType = itemView.findViewById(R.id.fileType);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onFileRemoveClick(position);
                }
            });
        }

        void bind(FileItem fileItem) {
            fileName.setText(fileItem.getFileName());
            fileSize.setText(formatFileSize(fileItem.getFileSize()));
            fileType.setText(getFileTypeDescription(fileItem.getFileType()));

            if (fileItem.isImage()) {
                Glide.with(context) // Use context from itemView
                        .load(fileItem.getFileUri())
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_broken_image) // Add error placeholder
                        .override(150, 150) // Consistent thumbnail size
                        .centerCrop()
                        .into(fileIcon);
            } else {
                int iconResId = getIconForFileType(fileItem.getFileType());
                // Check if iconResId is valid before setting
                if (iconResId != 0) {
                    fileIcon.setImageResource(iconResId);
                } else {
                    fileIcon.setImageResource(R.drawable.ic_file); // Default fallback
                }
            }
        }

        // --- Helper methods moved inside ViewHolder or kept in Adapter ---
        private String formatFileSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            // Ensure digitGroups is within bounds
            digitGroups = Math.max(0, Math.min(digitGroups, units.length - 1));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        private String getFileTypeDescription(String mimeType) {
            if (mimeType == null) return "Unknown";
            // Simplified descriptions
            if (mimeType.startsWith("image/")) return "Image";
            if (mimeType.startsWith("video/")) return "Video";
            if (mimeType.startsWith("audio/")) return "Audio";
            if (mimeType.equals("application/pdf")) return "PDF";
            if (mimeType.contains("word")) return "Word Doc";
            if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) return "Excel Doc";
            if (mimeType.contains("presentation")) return "PowerPoint";
            if (mimeType.startsWith("text/")) return "Text";
            if (mimeType.equals("application/zip")) return "ZIP Archive";
            // Fallback to the main type
            int slashIndex = mimeType.indexOf('/');
            if(slashIndex > 0) return mimeType.substring(0, slashIndex);
            return "File"; // Generic fallback
        }

        private int getIconForFileType(String mimeType) {
            if (mimeType == null) return R.drawable.ic_file;
            // isImage handled by Glide, so no need to check here
            if (mimeType.startsWith("video/")) return R.drawable.ic_video;
            if (mimeType.startsWith("audio/")) return R.drawable.ic_audio;
            if (mimeType.equals("application/pdf")) return R.drawable.ic_pdf;
            if (mimeType.contains("word")) return R.drawable.ic_word;
            if (mimeType.contains("excel") || mimeType.contains("spreadsheetml")) return R.drawable.ic_excel;

            return R.drawable.ic_file; // Default icon
        }
    }
}