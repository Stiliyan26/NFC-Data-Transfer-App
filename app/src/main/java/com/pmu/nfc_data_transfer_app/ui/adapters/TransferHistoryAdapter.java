package com.pmu.nfc_data_transfer_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.viewholder.HistoryViewHolder;

import java.util.ArrayList;
import java.util.List;

public class TransferHistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private List<TransferHistory> historyItems = new ArrayList<>();
    private final HistoryItemListener listener;

    public interface HistoryItemListener {
        void onHistoryItemClicked(TransferHistory history);
        void onHistoryItemRemoved(TransferHistory history);
    }

    public TransferHistoryAdapter(HistoryItemListener listener) {
        this.listener = listener;
    }

    public void setHistoryItems(List<TransferHistory> historyItems) {
        this.historyItems = new ArrayList<>(historyItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transfer_history, parent, false);

        return new HistoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TransferHistory history = historyItems.get(position);

        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }
}
