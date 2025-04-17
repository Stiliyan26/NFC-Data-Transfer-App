package com.pmu.nfc_data_transfer_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.helpers.HistoryFileViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying files in history views (without remove options)
 */
public class HistoryFileAdapter extends ListAdapter<TransferFileItem, HistoryFileViewHolder> {

    public HistoryFileAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransferFileItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransferFileItem>() {
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
    public HistoryFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_file, parent, false);

        return new HistoryFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryFileViewHolder holder, int position) {
        TransferFileItem fileItem = getItem(position);
        holder.bind(fileItem);
    }
}