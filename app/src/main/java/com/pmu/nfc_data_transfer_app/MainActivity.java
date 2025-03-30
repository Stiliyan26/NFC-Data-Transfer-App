package com.pmu.nfc_data_transfer_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private static final int REQUEST_CODE_PICK_FILES = 1;
    private static final int REQUEST_CODE_PERMISSION = 123;

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileItem> fileList = new ArrayList<>();
    private TextView selectedCount;
    private Button btnTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        selectedCount = findViewById(R.id.selectedCount);
        btnTransfer = findViewById(R.id.btnTransfer);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileAdapter = new FileAdapter(this, fileList, this);
        recyclerView.setAdapter(fileAdapter);

        // Set up file picker button
        Button btnPickFiles = findViewById(R.id.btnPickFiles);
        btnPickFiles.setOnClickListener(v -> checkPermissionAndPickFiles());

        // Set up transfer button
        btnTransfer.setOnClickListener(v -> transferFiles());

        updateSelectedCount();
    }

    private void updateSelectedCount() {
        selectedCount.setText(String.format("%d files selected", fileList.size()));
        btnTransfer.setEnabled(!fileList.isEmpty());
    }

    private void transferFiles() {
        if (fileList.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
            return;
        }
        // Implement your transfer logic here
        Toast.makeText(this,
                "Transferring " + fileList.size() + " files",
                Toast.LENGTH_SHORT).show();
    }

    private void checkPermissionAndPickFiles() {
        // For devices below Android Tiramisu, we still need to check READ_EXTERNAL_STORAGE permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION
                );
            } else {
                pickFiles();
            }
        } else {
            // For Android Tiramisu and above, if you only need to pick files via the picker,
            // you might not need extra permission since the picker grants temporary access.
            pickFiles();
        }
    }

    // Updated method using ACTION_GET_CONTENT for better multi-select support
    private void pickFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // Note: ACTION_GET_CONTENT does not support persistent URI permissions,
        // so we don't add FLAG_GRANT_PERSISTABLE_URI_PERMISSION here.
        Intent chooser = Intent.createChooser(intent, "Select Files");
        startActivityForResult(chooser, REQUEST_CODE_PICK_FILES);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFiles();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILES && resultCode == RESULT_OK && data != null) {
            // For multi-select, check if ClipData is returned.
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    addFileToList(uri);
                }
            } else if (data.getData() != null) {
                // Single file selected
                Uri uri = data.getData();
                addFileToList(uri);
            }

            fileAdapter.notifyDataSetChanged();
            updateSelectedCount();

            // Scroll to show new files
            recyclerView.post(() -> recyclerView.smoothScrollToPosition(fileList.size() - 1));
        }
    }

    private void addFileToList(Uri uri) {
        for (FileItem item : fileList) {
            if (item.getFileUri().equals(uri)) {
                Toast.makeText(this, "File already selected", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String mimeType = getContentResolver().getType(uri);
        String name = getFileName(uri);
        long size = getFileSize(uri);
        boolean isImage = mimeType != null && mimeType.startsWith("image/");

        fileList.add(new FileItem(name, size, mimeType, uri, isImage));
        Toast.makeText(this, "Added " + name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileRemoveClick(int position) {
        fileList.remove(position);
        fileAdapter.notifyItemRemoved(position);
        updateSelectedCount();
        Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (uri.getScheme().equals("file")) {
            try {
                java.io.File file = new java.io.File(uri.getPath());
                size = file.length();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }
}
