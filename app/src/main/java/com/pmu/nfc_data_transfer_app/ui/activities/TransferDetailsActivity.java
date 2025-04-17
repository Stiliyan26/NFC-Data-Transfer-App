package com.pmu.nfc_data_transfer_app.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.adapters.HistoryFileAdapter;
import com.pmu.nfc_data_transfer_app.ui.helpers.DatabaseHelper;
import com.pmu.nfc_data_transfer_app.ui.helpers.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransferDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TransferDetailsActivity";
    private ImageView transferDirectionIcon;
    private TextView deviceNameText;
    private TextView transferTypeText;
    private TextView fileCountText;
    private TextView fileSizeText;
    private RecyclerView filesRecyclerView;
    private HistoryFileAdapter fileAdapter;

    private int transferId;
    private TransferHistory transferHistory;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_details);

        setupViews();
        setupToolbar();

        transferId = getIntent().getIntExtra("TRANSFER_ID", -1);

        if (transferId != -1) {
            loadTransferDetails();
        } else {
            finish(); // Close if no ID was passed
        }
    }

    private void setupViews() {
        try {
            transferDirectionIcon = findViewById(R.id.transferDirectionIcon);
            deviceNameText = findViewById(R.id.deviceNameText);
            transferTypeText = findViewById(R.id.transferTypeText);
            fileCountText = findViewById(R.id.fileCountText);
            fileSizeText = findViewById(R.id.fileSizeText);
            filesRecyclerView = findViewById(R.id.filesRecyclerView);

            fileAdapter = new HistoryFileAdapter();
            filesRecyclerView.setAdapter(fileAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views: " + e.getMessage());
        }
    }

    private void setupToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);

            if (toolbar != null) {
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(v -> onBackPressed());

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("");
                }
            } else {
                Log.e(TAG, "Toolbar is null in setupToolbar()");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage());
        }
    }

    private void loadTransferDetails() {
        try {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            transferHistory = dbHelper.getTransferredFilesByTransferId(transferId);

            if (transferHistory != null) {
                displayTransferDetails();
            } else {
                Log.e(TAG, "Transfer history not found for ID: " + transferId);
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading transfer details: " + e.getMessage());
        }
    }

    private void displayTransferDetails() {
        try {
            // Set device name
            if (deviceNameText != null) {
                deviceNameText.setText(transferHistory.getDeviceName());
            }

            // Set transfer type text
            if (transferTypeText != null) {
                String typePrefix = "send".equals(transferHistory.getTransferType()) ?
                        getString(R.string.files_sent) : getString(R.string.files_received);

                transferTypeText.setText(typePrefix + " • " +
                        dateFormat.format(transferHistory.getTransferDate()));
            }

            // Set file count
            if (fileCountText != null) {
                fileCountText.setText(String.format(getString(R.string.total_files), transferHistory.getFileCount()));
            }

            // Set total file size
            if (fileSizeText != null) {
                fileSizeText.setText(String.format(getString(R.string.total_size),
                        FileUtils.formatFileSize(transferHistory.getTotalSize())));
            }

            // Set direction icon
            if (transferDirectionIcon != null) {
                if ("send".equals(transferHistory.getTransferType())) {
                    transferDirectionIcon.setImageResource(R.drawable.ic_send);
                    transferDirectionIcon.setBackgroundResource(R.drawable.circle_background_blue);
                } else {
                    transferDirectionIcon.setImageResource(R.drawable.ic_receive);
                    transferDirectionIcon.setBackgroundResource(R.drawable.circle_background_green);
                }
            }

            // Set files in the adapter
            if (fileAdapter != null) {
                fileAdapter.submitList(transferHistory.getFiles());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying transfer details: " + e.getMessage());
        }
    }
}