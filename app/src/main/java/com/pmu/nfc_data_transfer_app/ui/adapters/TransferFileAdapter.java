package com.pmu.nfc_data_transfer_app.ui.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.viewholder.TransferFileViewHolder;

import java.util.List;

public class TransferFileAdapter extends RecyclerView.Adapter<TransferFileViewHolder> {

    private final List<TransferFileItem> fileItems;

    public TransferFileAdapter(List<TransferFileItem> fileItems) {
        this.fileItems = fileItems;
    }

    @NonNull
    @Override
    public TransferFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transfer_progress_file, parent, false);

        return new TransferFileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferFileViewHolder holder, int position) {
        TransferFileItem item = fileItems.get(position);
        holder.bind(item);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferFileViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.get(0) instanceof Integer) {
            int progress = (int) payloads.get(0);

            holder.getFileProgress().setProgress(progress);
        } else {
            // Full bind if no payload
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    /**
     * Update progress for a specific file
     */
    public void updateFileProgress(int position, int progress) {
        if (0 <= position && position < fileItems.size()) {
            fileItems.get(position).setProgress(progress);
            notifyItemChanged(position, progress);
        }
    }
}
