package com.pmu.nfc_data_transfer_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.pmu.nfc_data_transfer_app.ui.helpers.FileViewHolder;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends ListAdapter<FileItem, FileViewHolder> {

    public interface OnFileClickListener {
        void onFileRemoveClick(int position);
    }

    private final OnFileClickListener listener;

    public FileAdapter(OnFileClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<FileItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<FileItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull FileItem oldItem, @NonNull FileItem newItem) {
            return oldItem.getFileUri().equals(newItem.getFileUri());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FileItem oldItem, @NonNull FileItem newItem) {
            return oldItem.getFileName().equals(newItem.getFileName()) &&
                    oldItem.getFileSize() == newItem.getFileSize() &&
                    (oldItem.getFileType() == null && newItem.getFileType() == null ||
                            oldItem.getFileType() != null && oldItem.getFileType().equals(newItem.getFileType()));
        }
    };

    // For backwards compatibility with existing code
    public void submitList(List<FileItem> newFileList) {
        List<FileItem> fileList = newFileList != null
                ? new ArrayList<>(newFileList)
                : new ArrayList<>();

        super.submitList(fileList);
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
        FileItem fileItem = getItem(position);
        holder.bind(fileItem);
    }
}