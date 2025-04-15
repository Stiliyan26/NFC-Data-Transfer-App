package com.pmu.nfc_data_transfer_app.ui.main.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.pmu.nfc_data_transfer_app.data.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.main.helpers.FileAdapter;
import com.pmu.nfc_data_transfer_app.ui.main.helpers.MainViewModel;
import com.pmu.nfc_data_transfer_app.ui.util.Event;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileTransferActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private static final int REQUEST_CODE_PICK_FILES = 1;
    private static final int REQUEST_CODE_PERMISSION = 123;

    private MainViewModel viewModel;
    private FileAdapter fileAdapter;
    private BluetoothDevice device;
    private RecyclerView recyclerView;
    private TextView selectedCountTextView;
    private Button btnTransfer;
    private Button btnPickFiles;
    private ProgressBar loadingIndicator;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer);

        // --- Initialize Views ---
        recyclerView = findViewById(R.id.recyclerView);
        selectedCountTextView = findViewById(R.id.selectedCount);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnPickFiles = findViewById(R.id.btnPickFiles);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        emptyState = findViewById(R.id.emptyState);

        // Set toolbar background to black
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(Color.BLACK);
        }

        // Set the main content area background to white
        View mainContent = findViewById(android.R.id.content);
        mainContent.setBackgroundColor(Color.WHITE);

        // Style the "Add Files" button - rounded black button with white text
        btnPickFiles.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        btnPickFiles.setTextColor(Color.WHITE);

        // Set initial state of transfer button to gray
        btnTransfer.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        btnTransfer.setTextColor(Color.WHITE);
        btnTransfer.setEnabled(false);

        // --- Initialize ViewModel ---
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // --- Setup RecyclerView ---
        setupRecyclerView();

        // --- Setup Button Click Listeners ---
        btnPickFiles.setOnClickListener(v -> checkPermissionAndPickFiles());
        btnTransfer.setOnClickListener(v -> exportFiles());

        // --- Observe ViewModel LiveData ---
        observeViewModel();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this);
        recyclerView.setAdapter(fileAdapter);
    }

    private void observeViewModel() {
        // Observe file list changes
        viewModel.fileList.observe(this, fileItems -> {
            fileAdapter.submitList(fileItems);

            // Update empty state visibility
            emptyState.setVisibility(fileItems.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(fileItems.isEmpty() ? View.GONE : View.VISIBLE);

            // Update transfer button color based on file count
            updateTransferButtonAppearance(!fileItems.isEmpty());
        });

        // Observe selected count text
        viewModel.selectedCountText.observe(this, text -> selectedCountTextView.setText(text));

        // Observe transfer button enabled state
        viewModel.isTransferEnabled.observe(this, isEnabled -> {
            btnTransfer.setEnabled(isEnabled);
            updateTransferButtonAppearance(isEnabled);
        });

        // Observe toast messages
        viewModel.toastMessage.observe(this, new Event.EventObserver<>(message -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show()));

        // Observe loading state
        viewModel.isLoading.observe(this, isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Disable buttons while loading
            btnPickFiles.setEnabled(!isLoading);
            btnTransfer.setEnabled(!isLoading && viewModel.isTransferEnabled.getValue() != null && viewModel.isTransferEnabled.getValue());

            // Update button appearances
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
            // Permission granted or not needed (Tiramisu+ for picker)
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

    private BluetoothDevice processNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null && rawMsgs.length > 0) {
                NdefMessage message = (NdefMessage) rawMsgs[0];
                String macAddress = getTextFromMessage(message);
                Toast.makeText(this, "Received MAC: " + macAddress, Toast.LENGTH_LONG).show();

                if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                    // Use the MAC address to get the remote Bluetooth device
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    return bluetoothAdapter.getRemoteDevice(macAddress);
                }
            }
        }
        return null;
    }

    private String getTextFromMessage(NdefMessage message) {
        NdefRecord record = message.getRecords()[0];
        byte[] payload = record.getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0x3F;

        try {
            return new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void exportFiles() {
        // Create a list of TransferFileItem
        ArrayList<TransferFileItem> filesToTransfer = new ArrayList<>();

        // Convert FileItem objects to TransferFileItem objects
        if (viewModel.fileList.getValue() != null) {
            for (FileItem currentFile : viewModel.fileList.getValue()) {
                TransferFileItem item = new TransferFileItem(currentFile);
                filesToTransfer.add(item);
                // Add this debug to verify
                Log.d("FileTransfer", "Added TransferFileItem: " + item.getName() + ", class: " + item.getClass().getName());
            }
        }

        if (filesToTransfer.isEmpty()) {
            Toast.makeText(this, "Няма избрани файлове за трансфер", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d("FileTransfer", "Starting transfer with " + filesToTransfer.size() +
                    " TransferFileItems of type " + filesToTransfer.get(0).getClass().getName());
            // Start the transfer activity with TransferFileItem list
            TransferProgressActivity.start(this, filesToTransfer, device != null ? device.getAddress() : null);
        } catch (Exception e) {
            Log.e("FileTransfer", "Error starting transfer", e);
            e.printStackTrace();
            Toast.makeText(this, "Error starting transfer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;
        private final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private final MainViewModel viewModel;  // Reference to your ViewModel

        public ConnectThread(BluetoothDevice device, MainViewModel viewModel) {
            this.device = device;
            this.viewModel = viewModel;  // Initialize ViewModel
        }

        @Override
        public void run() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (ActivityCompat.checkSelfPermission(FileTransferActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            bluetoothAdapter.cancelDiscovery();

            try {
                socket = device.createRfcommSocketToServiceRecord(APP_UUID);
                socket.connect();

//                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();



                try {
                    // Wait for the result synchronously (blocks until the future is done)
                    Map<String, byte[]> files = viewModel.readFilesForTransfer().get();  // Blocking call (in background thread)

                    if (files != null && !files.isEmpty()) {
                        // Update UI on the main thread
                        viewModel.messageToast.postValue(new Event<>(files.size() + " files processed for transfer."));
                        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                            DataOutputStream dataOutputStream = getDataOutputStream(entry, outputStream);
                            dataOutputStream.flush();
                        }

                    } else {
                        viewModel.messageToast.postValue(new Event<>("Failed to read any files."));
                    }
                } catch (Exception e) {
                    viewModel.messageToast.postValue(new Event<>("Error during file preparation."));
                    e.printStackTrace();
                } finally {
                    viewModel.currentlyLoading.postValue(false);  // Update loading indicator in ViewModel
                }

            } catch (IOException e) {
                viewModel.messageToast.postValue(new Event<>("Connection failed: " + e.getMessage()));
                e.printStackTrace();

                try {
                    if (socket != null) socket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        }

        @NonNull
        private DataOutputStream getDataOutputStream(Map.Entry<String, byte[]> entry, OutputStream outputStream) throws IOException {
            String fileName = entry.getKey();
            byte[] fileData = entry.getValue();

            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            int fileNameLength = fileNameBytes.length;
            int fileSize = fileData.length;

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // 1. Send filename length
            dataOutputStream.writeInt(fileNameLength);

            // 2. Send filename
            dataOutputStream.write(fileNameBytes);

            // 3. Send file size
            dataOutputStream.writeInt(fileSize);

            // 4. Send file data
            dataOutputStream.write(fileData);
            return dataOutputStream;
        }

        public void cancel() {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFiles(); // Permission granted, proceed
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        this.device = processNfcIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = new ArrayList<>();
            if (data.getClipData() != null) { // Multiple files selected
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    uris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) { // Single file selected
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
} 