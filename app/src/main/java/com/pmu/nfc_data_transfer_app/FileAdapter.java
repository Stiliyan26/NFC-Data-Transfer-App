package com.pmu.nfc_data_transfer_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    public interface OnFileClickListener {
        void onFileRemoveClick(int position);
    }

    private final Context context;
    private final List<FileItem> fileList;
    private final OnFileClickListener listener;

    public FileAdapter(Context context, List<FileItem> fileList, OnFileClickListener listener) {
        this.context = context;
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
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

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileType = itemView.findViewById(R.id.fileType);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String getFileTypeDescription(String mimeType) {
        if (mimeType == null) return "Unknown";
        if (mimeType.startsWith("image/")) return "Image";
        if (mimeType.startsWith("video/")) return "Video";
        if (mimeType.startsWith("audio/")) return "Audio";
        if (mimeType.equals("application/pdf")) return "PDF";
        if (mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "Word Document";
        }
        if (mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return "Excel Spreadsheet";
        }
        return mimeType;
    }

    private int getIconForFileType(String mimeType, boolean isImage) {
        if (mimeType == null) return R.drawable.ic_file;
        if (isImage) return 0; // Return 0 for images to handle separately
        if (mimeType.startsWith("video/")) return R.drawable.ic_video;
        if (mimeType.startsWith("audio/")) return R.drawable.ic_audio;
        if (mimeType.equals("application/pdf")) return R.drawable.ic_pdf;
        if (mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return R.drawable.ic_word;
        }
        if (mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return R.drawable.ic_excel;
        }
        return R.drawable.ic_file;
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);

        holder.fileName.setText(fileItem.getFileName());
        holder.fileSize.setText(formatFileSize(fileItem.getFileSize()));
        holder.fileType.setText(getFileTypeDescription(fileItem.getFileType()));

        if (fileItem.isImage()) {
            // Load actual image thumbnail
            Glide.with(context)
                    .load(fileItem.getFileUri())
                    .placeholder(R.drawable.ic_image) // Fallback icon while loading
                    .override(200, 200) // Thumbnail size
                    .centerCrop()
                    .into(holder.fileIcon);
        } else {
            // Use icon for non-image files
            int iconResId = getIconForFileType(fileItem.getFileType(), false);
            holder.fileIcon.setImageResource(iconResId);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileRemoveClick(position);
            }
        });
    }
}