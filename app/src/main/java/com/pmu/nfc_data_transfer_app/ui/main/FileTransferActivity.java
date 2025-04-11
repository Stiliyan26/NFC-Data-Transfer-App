package com.pmu.nfc_data_transfer_app.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.ui.util.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileTransferActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private static final int REQUEST_CODE_PICK_FILES = 1;
    private static final int REQUEST_CODE_PERMISSION = 123;

    private MainViewModel viewModel;
    private FileAdapter fileAdapter;
    private RecyclerView recyclerView;
    private TextView selectedCountTextView;
    private Button btnTransfer;
    private Button btnPickFiles;
    private ProgressBar loadingIndicator;
    private View emptyState;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Set Bulgarian locale
        Locale locale = new Locale("bg");
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Bulgarian locale
        Locale locale = new Locale("bg");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        
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
        btnTransfer.setOnClickListener(v -> viewModel.transferFiles());

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
        viewModel.selectedCountText.observe(this, text -> {
            selectedCountTextView.setText(text);
        });

        // Observe transfer button enabled state
        viewModel.isTransferEnabled.observe(this, isEnabled -> {
            btnTransfer.setEnabled(isEnabled);
            updateTransferButtonAppearance(isEnabled);
        });

        // Observe toast messages
        viewModel.toastMessage.observe(this, new Event.EventObserver<>(message -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }));

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