package com.pmu.nfc_data_transfer_app.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.model.TransferHistory;
import com.pmu.nfc_data_transfer_app.ui.adapters.HistoryFileAdapter;
import com.pmu.nfc_data_transfer_app.ui.helpers.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TransferDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TransferDetailsActivity";
    private ImageView transferDirectionIcon;
    private TextView deviceNameText;
    private TextView transferTypeText;
    private TextView fileCountText;
    private TextView fileSizeText;
    private RecyclerView filesRecyclerView;
    private HistoryFileAdapter fileAdapter;

    private String transferId;
    private String deviceName;
    private String transferType;
    private Date transferDate;
    private long totalSize;

    private TransferHistory transferHistory;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_details);

        setupViews();
        setupToolbar();

        // Get data from intent
        transferId = getIntent().getStringExtra("TRANSFER_ID");
        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        transferType = getIntent().getStringExtra("TRANSFER_TYPE");

        long dateLong = getIntent().getLongExtra("TRANSFER_DATE", System.currentTimeMillis());
        transferDate = new Date(dateLong);

        totalSize = getIntent().getLongExtra("TOTAL_SIZE", 0);

        if (transferId != null) {
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
            // Generate sample files based on the device name
            List<TransferFileItem> sampleFiles = generateSampleFiles(deviceName);

            // Create a TransferHistory object with the data from the intent
            transferHistory = new TransferHistory(
                    deviceName,
                    transferDate,
                    transferType,
                    sampleFiles,
                    totalSize
            );

            displayTransferDetails();
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

    private List<TransferFileItem> generateSampleFiles(String deviceName) {
        if (deviceName != null) {
            if (deviceName.contains("Samsung")) {
                return createSamsungFiles();
            } else if (deviceName.contains("Xiaomi")) {
                return createXiaomiFiles();
            }
        }

        return createDefaultFiles();
    }

    private List<TransferFileItem> createSamsungFiles() {
        List<TransferFileItem> sampleFiles = new ArrayList<>();

        sampleFiles.add(createSampleFile("presentation.pptx", 5200000, "application/vnd.openxmlformats-officedocument.presentationml.presentation", false));
        sampleFiles.add(createSampleFile("document.docx", 1800000, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", false));
        sampleFiles.add(createSampleFile("report.pdf", 3500000, "application/pdf", false));
        sampleFiles.add(createSampleFile("data.xlsx", 900000, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", false));
        sampleFiles.add(createSampleFile("family_photo.jpg", 2800000, "image/jpeg", true));

        return sampleFiles;
    }

    private List<TransferFileItem> createXiaomiFiles() {
        List<TransferFileItem> sampleFiles = new ArrayList<>();

        sampleFiles.add(createSampleFile("vacation_photo1.jpg", 4500000, "image/jpeg", true));
        sampleFiles.add(createSampleFile("vacation_photo2.jpg", 5200000, "image/jpeg", true));
        sampleFiles.add(createSampleFile("vacation_photo3.jpg", 4800000, "image/jpeg", true));
        sampleFiles.add(createSampleFile("vacation_video.mp4", 18000000, "video/mp4", false));
        sampleFiles.add(createSampleFile("notes.txt", 150000, "text/plain", false));
        sampleFiles.add(createSampleFile("audio_recording.mp3", 8500000, "audio/mpeg", false));
        sampleFiles.add(createSampleFile("contacts.vcf", 85000, "text/vcard", false));

        return sampleFiles;
    }

    private List<TransferFileItem> createDefaultFiles() {
        List<TransferFileItem> sampleFiles = new ArrayList<>();

        sampleFiles.add(createSampleFile("document.pdf", 2500000, "application/pdf", false));
        sampleFiles.add(createSampleFile("image.jpg", 1800000, "image/jpeg", true));

        return sampleFiles;
    }

    private TransferFileItem createSampleFile(String fileName, long fileSize, String fileType, boolean isImage) {
        Uri dummyUri = Uri.parse("content://com.pmu.nfc_data_transfer_app/sample/" + UUID.randomUUID().toString());

        return new TransferFileItem(fileName, fileSize, fileType, dummyUri, isImage);
    }
}