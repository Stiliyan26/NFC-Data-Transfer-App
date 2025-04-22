package com.pmu.nfc_data_transfer_app.ui.viewholder;

import static android.text.TextUtils.TruncateAt.MIDDLE;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pmu.nfc_data_transfer_app.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.ui.adapters.FileAdapter;
import com.pmu.nfc_data_transfer_app.util.FileUtils;

public class FileViewHolder extends RecyclerView.ViewHolder {
    private final ImageView fileIcon;
    private final ImageView fileIconBackground;
    private final TextView fileName;
    private final TextView fileInfo;
    private final ImageButton btnRemove;
    private final FileAdapter.OnFileClickListener listener;
    private final Context context;

    // Cache references to resources to avoid repeated lookups
    private final int fileIconPadding;

    public FileViewHolder(@NonNull View itemView, FileAdapter.OnFileClickListener listener) {
        super(itemView);
        
        this.listener = listener;
        this.context = itemView.getContext();
        
        fileIcon = itemView.findViewById(R.id.fileIcon);
        fileIconBackground = itemView.findViewById(R.id.fileIconBackground);
        fileName = itemView.findViewById(R.id.fileName);
        fileInfo = itemView.findViewById(R.id.fileInfo);
        btnRemove = itemView.findViewById(R.id.btnRemove);
        
        // Cache the dimension resource for better performance
        fileIconPadding = context.getResources().getDimensionPixelSize(R.dimen.file_icon_padding);
    }

    public void bind(TransferFileItem fileItem) {
        // Set file name with ellipsize if too long
        fileName.setText(fileItem.getName());
        fileName.setEllipsize(MIDDLE);
        
        // Format file size and type info
        String size = FileUtils.formatFileSize(fileItem.getSize());
        String type = FileUtils.getFileTypeDescription(fileItem.getMimeType());
        
        // Combine size and type
        fileInfo.setText(String.format("%s • %s", size, type));

        // Set file icon based on file type
        setFileIcon(fileItem);

        // Set remove button click listener
        btnRemove.setOnClickListener(v -> {
            int position = getAdapterPosition();
            
            if (position != RecyclerView.NO_POSITION) {
                listener.onFileRemoveClick(position);
            }
        });
    }

    private void setFileIcon(TransferFileItem fileItem) {
        // Clear any previous image loading
        Glide.with(context).clear(fileIcon);
        fileIcon.setColorFilter(null);
        fileIcon.setPadding(0, 0, 0, 0);

        String mimeType = fileItem.getMimeType();

        if (mimeType == null) {
            setupFileIcon(R.drawable.ic_file, true); // Apply black filter
            return;
        }

        if (mimeType.startsWith("image/")) {
            // For images, hide background and load actual image
            fileIconBackground.setVisibility(View.GONE);
            fileIcon.setPadding(0, 0, 0, 0);
            fileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Load image thumbnail using Glide with improved caching and transitions
            Glide.with(context)
                    .load(fileItem.getUri())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_file)
                            .error(R.drawable.ic_file)
                            .centerCrop()
                            .override(200, 200))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(fileIcon);
        } else if (mimeType.equals("application/pdf")) {
            // For PDF files, show the icon in its original color (red)
            setupFileIcon(FileUtils.getIconForFileType(mimeType), false); // Don't apply black filter
        } else {
            // Reset image view state
            fileIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            // For other file types, show background and icon
            setupFileIcon(FileUtils.getIconForFileType(mimeType), true); // Apply black filter
        }
    }

    private void setupFileIcon(int iconResId, boolean applyBlackFilter) {
        // Clear any previous image loading
        Glide.with(context).clear(fileIcon);

        fileIconBackground.setVisibility(View.VISIBLE);
        fileIcon.setPadding(fileIconPadding, fileIconPadding, fileIconPadding, fileIconPadding);
        fileIcon.setImageResource(iconResId);

        if (applyBlackFilter) {
            fileIcon.setColorFilter(Color.BLACK);
        } else {
            fileIcon.setColorFilter(null); // Remove any color filter
        }

        fileIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
}
