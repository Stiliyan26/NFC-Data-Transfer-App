package com.pmu.nfc_data_transfer_app.ui.main.helpers;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.data.model.FileItem;

import java.text.DecimalFormat;

/**
 * Utility class for file-related operations and formatting
 */
public class FileUtils {

    // Formats file size into human-readable format
    public static String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    // Returns a human-readable description of the file type based on MIME type
    public static String getFileTypeDescription(FileItem fileItem) {
        String mimeType = fileItem.getFileType();
        if (mimeType == null) return "Unknown";

        if (mimeType.startsWith("image/")) return "Image";
        if (mimeType.startsWith("video/")) return "Video";
        if (mimeType.startsWith("audio/")) return "Audio";
        if (mimeType.startsWith("text/")) return "Text";
        if (mimeType.equals("application/pdf")) return "PDF";
        if (mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            return "Document";
        if (mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            return "Spreadsheet";
        if (mimeType.equals("application/vnd.ms-powerpoint") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
            return "Presentation";

        return "File";
    }

    // Resource ID for the icon
    public static int getIconForFileType(String mimeType) {
        if (mimeType.startsWith("video/")) {
            return R.drawable.ic_video;
        } else if (mimeType.startsWith("audio/")) {
            return R.drawable.ic_audio;
        } else if (mimeType.startsWith("text/")) {
            return R.drawable.ic_text;
        } else if (mimeType.equals("application/pdf")) {
            return R.drawable.ic_pdf;
        } else if (mimeType.equals("application/msword") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return R.drawable.ic_document;
        } else if (mimeType.equals("application/vnd.ms-excel") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return R.drawable.ic_spreadsheet;
        } else if (mimeType.equals("application/vnd.ms-powerpoint") ||
                mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
            return R.drawable.ic_presentation;
        }

        return R.drawable.ic_file;
    }
}