package com.pmu.nfc_data_transfer_app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.OpenableColumns;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LinearLayout imageContainer;
    private TextView tvSelectedCount;
    private Button btnSendViaNfc;
    private List<Uri> selectedUris = new ArrayList<>();

    // Register the launcher for image picking
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleImageSelection(result.getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageContainer = findViewById(R.id.imageContainer);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnSendViaNfc = findViewById(R.id.btnSendViaNfc);

        findViewById(R.id.btnSelectImages).setOnClickListener(v -> openImagePicker());
        btnSendViaNfc.setOnClickListener(v -> prepareNfcTransfer());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void handleImageSelection(@Nullable Intent data) {
        selectedUris.clear();
        imageContainer.removeAllViews();

        if (data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedUris.add(data.getData());
            }
            updateImagePreviews();
        }
    }

    private void updateImagePreviews() {
        imageContainer.removeAllViews();

        for (Uri uri : selectedUris) {
            View previewView = LayoutInflater.from(this).inflate(R.layout.item_image_preview, imageContainer, false);
            ImageView imagePreview = previewView.findViewById(R.id.imagePreview);
            TextView imageName = previewView.findViewById(R.id.imageName);

            // Load image with Glide
            Glide.with(this)
                    .load(uri)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(imagePreview);

            // Set image name
            String name = getFileName(uri);
            imageName.setText(name);

            // Add click effect
            previewView.setOnClickListener(v -> {
                v.animate()
                        .scaleX(0.9f).scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).start())
                        .start();
            });

            imageContainer.addView(previewView);
        }

        // Update count text
        String countText = selectedUris.size() + (selectedUris.size() == 1 ? " image selected" : " images selected");
        tvSelectedCount.setText(countText);
        btnSendViaNfc.setEnabled(!selectedUris.isEmpty());
    }

    private String getFileName(Uri uri) {
        String result = null;

        if (uri.getScheme().equals("content")) {
            String[] projection = { MediaStore.Images.Media.DISPLAY_NAME};

            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
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

    private void prepareNfcTransfer() {
        // Your NFC transfer implementation
    }
}