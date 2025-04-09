package com.pmu.nfc_data_transfer_app.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider; // Use ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View; // Import View
import android.widget.Button;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import com.pmu.nfc_data_transfer_app.R; // Import R
import com.pmu.nfc_data_transfer_app.ui.util.Event; // Import Event

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private static final int REQUEST_CODE_PICK_FILES = 1;
    private static final int REQUEST_CODE_PERMISSION = 123;

    private MainViewModel viewModel; // ViewModel instance
    private FileAdapter fileAdapter;
    private RecyclerView recyclerView;
    private TextView selectedCountTextView;
    private Button btnTransfer;
    private Button btnPickFiles;
    private ProgressBar loadingIndicator; // Add ProgressBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Initialize ViewModel ---
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // --- Initialize Views ---
        recyclerView = findViewById(R.id.recyclerView);
        selectedCountTextView = findViewById(R.id.selectedCount);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnPickFiles = findViewById(R.id.btnPickFiles);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // --- Setup RecyclerView ---
        setupRecyclerView();

        // --- Setup Button Click Listeners ---
        btnPickFiles.setOnClickListener(v -> checkPermissionAndPickFiles());
        btnTransfer.setOnClickListener(v -> viewModel.transferFiles());

        // --- Observe ViewModel LiveData ---
        observeViewModel();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this); // Pass the listener (this activity)
        recyclerView.setAdapter(fileAdapter);
    }

    private void observeViewModel() {
        // Observe file list changes
        viewModel.fileList.observe(this, fileItems -> {
            fileAdapter.submitList(fileItems);
            // Optional: Scroll to bottom when items are added (might need smarter logic)
            // if (!fileItems.isEmpty()) {
            //    recyclerView.post(() -> recyclerView.smoothScrollToPosition(fileItems.size() - 1));
            // }
        });

        // Observe selected count text
        viewModel.selectedCountText.observe(this, text -> {
            selectedCountTextView.setText(text);
        });

        // Observe transfer button enabled state
        viewModel.isTransferEnabled.observe(this, isEnabled -> {
            btnTransfer.setEnabled(isEnabled);
        });

        // Observe toast messages
        viewModel.toastMessage.observe(this, new Event.EventObserver<>(message -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }));

        // Observe loading state
        viewModel.isLoading.observe(this, isLoading -> {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // Optionally disable buttons while loading
            btnPickFiles.setEnabled(!isLoading);
            btnTransfer.setEnabled(!isLoading && viewModel.isTransferEnabled.getValue() != null && viewModel.isTransferEnabled.getValue());
        });
    }

    // --- File Picking and Permissions (Remains in Activity) ---

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
            startActivityForResult(Intent.createChooser(intent, "Select Files"), REQUEST_CODE_PICK_FILES);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFiles(); // Permission granted, proceed
            } else {
                Toast.makeText(this, "Read Storage permission is required to select files.", Toast.LENGTH_LONG).show();
            }
        }
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

    // --- FileAdapter.OnFileClickListener Implementation ---

    @Override
    public void onFileRemoveClick(int position) {
        viewModel.removeFile(position); // Delegate removal to ViewModel
    }
}