package com.pmu.nfc_data_transfer_app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.helpers.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferHistoryAdapter extends RecyclerView.Adapter<TransferHistoryAdapter.HistoryViewHolder> {

    private List<TransferHistory> historyItems = new ArrayList<>();
    private final HistoryItemListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public interface HistoryItemListener {
        void onHistoryItemClicked(TransferHistory history);
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
        return new HistoryViewHolder(view);
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

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView transferDirectionIcon;
        private TextView deviceNameText;
        private TextView transferDateText;
        private TextView fileCountText;
        private TextView fileSizeText;
        private MaterialButton viewDetailsButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all views by ID with null checks
            try {
                transferDirectionIcon = itemView.findViewById(R.id.transferDirectionIcon);
                deviceNameText = itemView.findViewById(R.id.deviceNameText);
                transferDateText = itemView.findViewById(R.id.transferDateText);
                fileCountText = itemView.findViewById(R.id.fileCountText);
                fileSizeText = itemView.findViewById(R.id.fileSizeText);
                viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void bind(final TransferHistory history) {
            if (history == null) return;

            try {
                // Set device name if view exists
                if (deviceNameText != null) {
                    deviceNameText.setText(history.getDeviceName());
                }

                // Format and set date if view exists
                if (transferDateText != null) {
                    transferDateText.setText(dateFormat.format(history.getTransferDate()));
                }

                // Set file count if view exists
                if (fileCountText != null) {
                    fileCountText.setText(String.valueOf(history.getFileCount()));
                }

                // Format and set file size if view exists
                if (fileSizeText != null) {
                    fileSizeText.setText(FileUtils.formatFileSize(history.getTotalSize()));
                }

                // Set the correct icon based on transfer type if view exists
                if (transferDirectionIcon != null) {
                    if ("send".equals(history.getTransferType())) {
                        transferDirectionIcon.setImageResource(R.drawable.ic_send);
                        transferDirectionIcon.setBackgroundResource(R.drawable.circle_background_blue);
                    } else {
                        transferDirectionIcon.setImageResource(R.drawable.ic_receive);
                        transferDirectionIcon.setBackgroundResource(R.drawable.circle_background_green);
                    }
                }

                // Set click listeners
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onHistoryItemClicked(history);
                    }
                });

                // Set the details button click listener if view exists
                if (viewDetailsButton != null) {
                    viewDetailsButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onHistoryItemClicked(history);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}