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
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);

        holder.fileName.setText(fileItem.getFileName());
        holder.fileSize.setText(formatFileSize(fileItem.getFileSize()));
        holder.fileType.setText(getFileTypeDescription(fileItem.getFileType()));

        int iconResId = getSystemIconForFileType(fileItem.getFileType(), fileItem.isImage());
        holder.fileIcon.setImageResource(iconResId);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileRemoveClick(position);
            }
        });
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

    private int getSystemIconForFileType(String mimeType, boolean isImage) {
        if (isImage) return android.R.drawable.ic_menu_gallery;
        if (mimeType == null) return android.R.drawable.ic_dialog_info;  // Fallback icon

        if (mimeType.startsWith("video/")) {
            return android.R.drawable.ic_media_play;  // Video icon
        }
        if (mimeType.startsWith("audio/")) {
            return android.R.drawable.ic_btn_speak_now;  // Audio icon
        }
        if (mimeType.equals("application/pdf")) {
            return android.R.drawable.ic_dialog_dialer;  // Document-like icon
        }
        if (mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return android.R.drawable.ic_menu_edit;  // Word-like icon
        }
        if (mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return android.R.drawable.ic_menu_agenda;  // Excel-like icon
        }
        return android.R.drawable.ic_menu_save;  // Generic file icon
    }
}