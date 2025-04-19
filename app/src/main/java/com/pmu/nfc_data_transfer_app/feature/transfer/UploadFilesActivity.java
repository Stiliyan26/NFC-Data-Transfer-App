package com.pmu.nfc_data_transfer_app.feature.transfer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.adapters.FileAdapter;
import com.pmu.nfc_data_transfer_app.ui.viewholder.FileSelectionViewModel;
import com.pmu.nfc_data_transfer_app.util.Event;

import java.util.ArrayList;
import java.util.List;

public class UploadFilesActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {
    private static final int REQUEST_CODE_PICK_FILES = 1;
    private static final int REQUEST_CODE_PERMISSION = 123;
    private FileSelectionViewModel viewModel;
    private FileAdapter fileAdapter;

//    -- UNCOMMENT FOR REAL COMMUNICATION --
//    -----------------------------------------------------------------------------------
//    private final NfcService nfcService = new NfcService(NfcAdapter.getDefaultAdapter(this)); // prilojenieto zabiiva zaradi tozi red kogato e otkomentiran i se opitam da natisna izprashtane na failove ot glawnoto menu
//    private BluetoothService bluetoothService;
//    -----------------------------------------------------------------------------------
    private RecyclerView recyclerView;
    private TextView selectedCountTextView;
    private Button btnTransfer;
    private Button btnPickFiles;
    private ProgressBar loadingIndicator;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_files);

        // --- Initialize Views ---
        recyclerView = findViewById(R.id.recyclerView);
        selectedCountTextView = findViewById(R.id.selectedCount);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnPickFiles = findViewById(R.id.btnPickFiles);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        emptyState = findViewById(R.id.emptyState);

        btnPickFiles.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        btnPickFiles.setTextColor(Color.WHITE);

        btnTransfer.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        btnTransfer.setTextColor(Color.WHITE);
        btnTransfer.setEnabled(false);

        viewModel = new ViewModelProvider(this).get(FileSelectionViewModel.class);

        setupRecyclerView();

        btnPickFiles.setOnClickListener(v -> checkPermissionAndPickFiles());
        btnTransfer.setOnClickListener(v -> exportFiles());

        observeViewModel();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this);
        recyclerView.setAdapter(fileAdapter);
    }

    private void observeViewModel() {
        viewModel.fileList.observe(this, fileItems -> {
            fileAdapter.submitList(fileItems);

            emptyState.setVisibility(fileItems.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(fileItems.isEmpty() ? View.GONE : View.VISIBLE);

            updateTransferButtonAppearance(!fileItems.isEmpty());
        });

        viewModel.selectedCountText.observe(this, text -> selectedCountTextView.setText(text));

        viewModel.isTransferEnabled.observe(this, isEnabled -> {
            btnTransfer.setEnabled(isEnabled);
            updateTransferButtonAppearance(isEnabled);
        });

        viewModel.toastMessage.observe(this, new Event.EventObserver<>(message -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show()));

        viewModel.isLoading.observe(this, isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            btnPickFiles.setEnabled(!isLoading);
            btnTransfer.setEnabled(!isLoading
                            && viewModel.isTransferEnabled.getValue() != null
                            && viewModel.isTransferEnabled.getValue()
            );

            if (isLoading) {
                btnPickFiles.setAlpha(0.5f);
                btnTransfer.setAlpha(0.5f);
            } else {
                btnPickFiles.setAlpha(1.0f);
                updateTransferButtonAppearance(viewModel.isTransferEnabled.getValue() != null && viewModel.isTransferEnabled.getValue());
            }
        });
    }

    private void updateTransferButtonAppearance(boolean hasFiles) {
        if (hasFiles) {
            // Black button with white text when files are present
            btnTransfer.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            btnTransfer.setTextColor(Color.WHITE);
            btnTransfer.setAlpha(1.0f);
        } else {
            // Gray button when no files
            btnTransfer.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            btnTransfer.setTextColor(Color.WHITE);
            btnTransfer.setAlpha(0.5f);
        }
    }

    private void checkPermissionAndPickFiles() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        } else {
            pickFiles();
        }
    }

    private void pickFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_files)), REQUEST_CODE_PICK_FILES);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.install_file_manager), Toast.LENGTH_SHORT).show();
        }
    }

//    private BluetoothDevice processNfcIntent(Intent intent) {
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            if (rawMsgs != null && rawMsgs.length > 0) {
//                NdefMessage message = (NdefMessage) rawMsgs[0];
//                String macAddress = getTextFromMessage(message);
//                Toast.makeText(this, "Received MAC: " + macAddress, Toast.LENGTH_LONG).show();
//
//                if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
//                    // Use the MAC address to get the remote Bluetooth device
//                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//                    return bluetoothAdapter.getRemoteDevice(macAddress);
//                }
//            }
//        }
//        return null;
//    }
//
//    private String getTextFromMessage(NdefMessage message) {
//        NdefRecord record = message.getRecords()[0];
//        byte[] payload = record.getPayload();
//        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
//        int languageCodeLength = payload[0] & 0x3F;
//
//        try {
//            return new String(payload, languageCodeLength + 1,
//                    payload.length - languageCodeLength - 1, textEncoding);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    private void exportFiles() {
        ArrayList<TransferFileItem> filesToTransfer = new ArrayList<>();

        if (viewModel.fileList.getValue() != null) {
            filesToTransfer.addAll(viewModel.fileList.getValue());
        }

        if (filesToTransfer.isEmpty()) {
            Toast.makeText(this, "Няма избрани файлове за трансфер", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d("FileTransfer", "Starting transfer with " + filesToTransfer.size() +
                    " TransferFileItems of type " + filesToTransfer.get(0).getClass().getName());
            // Start the transfer activity with TransferFileItem list
            FileSendActivity.start(this, filesToTransfer, "1234:5678:9101:1213");

        } catch (Exception e) {
            Log.e("FileTransfer", "Error starting transfer", e);
            e.printStackTrace();
            Toast.makeText(this, "Error starting transfer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFiles();
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = new ArrayList<>();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    uris.add(data.getClipData().getItemAt(i).getUri());
                }

            } else if (data.getData() != null) {
                uris.add(data.getData());
            }

            if (!uris.isEmpty()) {
                viewModel.addFileUris(uris); // Delegate URI processing to ViewModel
            }
        }
    }

    @Override
    public void onFileRemoveClick(int position) {
        viewModel.removeFile(position); // Delegate removal to ViewModel
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

//  -- UNCOMMENT FOR REAL COMMUNICATION --
// ---------------------------------------------------------------------

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        setIntent(intent);
//        bluetoothService.setBluetoothDevice(nfcService.processNfcIntent(intent));
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
//        IntentFilter[] filters = new IntentFilter[]{
//                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
//        };
//
//        try {
//            filters[0].addDataType(GlobalConstants.MIME_TYPE);
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            e.printStackTrace();
//        }
//        nfcService.nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
//    }
// -----------------------------------------------------------------------
}
