package com.pmu.nfc_data_transfer_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.viewholder.FileViewHolder;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends ListAdapter<TransferFileItem, FileViewHolder> {

    public interface OnFileClickListener {
        void onFileRemoveClick(int position);
    }

    private final OnFileClickListener listener;

    public FileAdapter(OnFileClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TransferFileItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransferFileItem oldItem, @NonNull TransferFileItem newItem) {
            return oldItem.getUri().equals(newItem.getUri());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransferFileItem oldItem, @NonNull TransferFileItem newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getSize() == newItem.getSize() &&
                    (oldItem.getMimeType() == null && newItem.getMimeType() == null ||
                            oldItem.getMimeType() != null && oldItem.getMimeType().equals(newItem.getMimeType()));
        }
    };

    // For backwards compatibility with existing code
    public void submitList(List<TransferFileItem> newFileList) {
        List<TransferFileItem> fileList = newFileList != null
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
        TransferFileItem fileItem = getItem(position);
        holder.bind(fileItem);
    }
}
