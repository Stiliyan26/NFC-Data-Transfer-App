package com.pmu.nfc_data_transfer_app.ui.main;

import android.content.Context;
import android.graphics.Color;
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
        private final ImageView fileIcon;
        private final ImageView fileIconBackground;
        private final TextView fileName;
        private final TextView fileInfo;
        private final ImageButton btnRemove;
        private final OnFileClickListener listener;
        private final Context context;

        public FileViewHolder(@NonNull View itemView, OnFileClickListener listener) {
            super(itemView);
            this.listener = listener;
            this.context = itemView.getContext();
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileIconBackground = itemView.findViewById(R.id.fileIconBackground);
            fileName = itemView.findViewById(R.id.fileName);
            fileInfo = itemView.findViewById(R.id.fileInfo);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(FileItem fileItem) {
            fileName.setText(fileItem.getFileName());
            
            // Format file size
            String size = formatFileSize(fileItem.getFileSize());
            
            // Get file type description
            String type = getFileTypeDescription(fileItem);
            
            // Combine size and type
            fileInfo.setText(String.format("%s • %s", size, type));

            // Set file icon based on file type
            setFileIcon(fileItem);

            // Set remove button click listener
            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFileRemoveClick(position);
                }
            });
        }

        private String formatFileSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        private String getFileTypeDescription(FileItem fileItem) {
            String mimeType = fileItem.getFileType();
            if (mimeType == null) return "Unknown";
            
            if (mimeType.startsWith("image/")) return "Image";
            if (mimeType.startsWith("video/")) return "Video";
            if (mimeType.startsWith("audio/")) return "Audio";
            if (mimeType.startsWith("text/")) return "Text";
            if (mimeType.equals("application/pdf")) return "PDF";
            if (mimeType.equals("application/msword") || 
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) return "Document";
            if (mimeType.equals("application/vnd.ms-excel") || 
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) return "Spreadsheet";
            if (mimeType.equals("application/vnd.ms-powerpoint") || 
                mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) return "Presentation";
            
            return "File";
        }

        private void setFileIcon(FileItem fileItem) {
            String mimeType = fileItem.getFileType();
            if (mimeType == null) {
                setupFileIcon(R.drawable.ic_file);
                return;
            }

            if (mimeType.startsWith("image/")) {
                // For images, hide background and load actual image
                fileIconBackground.setVisibility(View.GONE);
                fileIcon.setPadding(0, 0, 0, 0);
                
                // Load image thumbnail using Glide
                Glide.with(context)
                    .load(fileItem.getFileUri())
                    .placeholder(R.drawable.ic_file)
                    .error(R.drawable.ic_file)
                    .centerCrop()
                    .override(200, 200)
                    .into(fileIcon);
            } else {
                // For other file types, show background and icon
                setupFileIcon(getIconForFileType(mimeType));
            }
        }

        private void setupFileIcon(int iconResId) {
            fileIconBackground.setVisibility(View.VISIBLE);
            fileIcon.setPadding(
                context.getResources().getDimensionPixelSize(R.dimen.file_icon_padding),
                context.getResources().getDimensionPixelSize(R.dimen.file_icon_padding),
                context.getResources().getDimensionPixelSize(R.dimen.file_icon_padding),
                context.getResources().getDimensionPixelSize(R.dimen.file_icon_padding)
            );
            fileIcon.setImageResource(iconResId);
            fileIcon.setColorFilter(Color.BLACK);
        }

        private int getIconForFileType(String mimeType) {
            if (mimeType.startsWith("video/")) {
                return R.drawable.ic_video;
            } else if (mimeType.startsWith("audio/")) {
                return R.drawable.ic_audio;
            } else if (mimeType.startsWith("text/")) {
                return R.drawable.ic_text;
            } else if (mimeType.equals("application/pdf")) {
                return R.drawable.ic_pdf;
            } else if (mimeType.equals("application/msword") || 
                     mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                return R.drawable.ic_document;
            } else if (mimeType.equals("application/vnd.ms-excel") || 
                     mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                return R.drawable.ic_spreadsheet;
            } else if (mimeType.equals("application/vnd.ms-powerpoint") || 
                     mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                return R.drawable.ic_presentation;
            }
            return R.drawable.ic_file;
        }
    }
}