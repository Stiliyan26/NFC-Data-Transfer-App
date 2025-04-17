package com.pmu.nfc_data_transfer_app.ui.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pmu.nfc_data_transfer_app.R;

import com.google.android.material.button.MaterialButton;
import com.pmu.nfc_data_transfer_app.core.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.adapters.TransferHistoryAdapter;
import com.pmu.nfc_data_transfer_app.util.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HistoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView transferDirectionIcon;
        private TextView deviceNameText;
        private TextView transferDateText;
        private TextView fileCountText;
        private TextView fileSizeText;
        private MaterialButton viewDetailsButton;
        private TransferHistoryAdapter.HistoryItemListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        public HistoryViewHolder(@NonNull View itemView, TransferHistoryAdapter.HistoryItemListener listener) {
            super(itemView);

            this.listener = listener;

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
                if (deviceNameText != null) {
                    deviceNameText.setText(history.getDeviceName());
                }

                if (transferDateText != null) {
                    transferDateText.setText(dateFormat.format(history.getTransferDate()));
                }

                if (fileCountText != null) {
                    fileCountText.setText(String.valueOf(history.getFileCount()));
                }

                if (fileSizeText != null) {
                    fileSizeText.setText(FileUtils.formatFileSize(history.getTotalSize()));
                }

                if (transferDirectionIcon != null) {
                    if ("send".equals(history.getTransferType())) {
                        transferDirectionIcon.setImageResource(R.drawable.ic_send);
                        transferDirectionIcon.setBackgroundResource(R.drawable.circle_background_blue);
                    } else {
                        transferDirectionIcon.setImageResource(R.drawable.ic_receive);
                        transferDirectionIcon.setBackgroundResource(R.drawable.circle_background_green);
                    }
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onHistoryItemClicked(history);
                    }
                });

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