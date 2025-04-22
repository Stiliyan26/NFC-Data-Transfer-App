package com.pmu.nfc_data_transfer_app.util;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.constants.FileConstants;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for file-related operations and formatting
 */
public class FileUtils {

    private static final Map<String, String> mimeToTypeMap = new HashMap<>();
    private static final Map<String, Integer> mimeToIconMap = new HashMap<>();

    private static final Map<String, String> extensionToMimeMap = new HashMap<>();

    static {
        // Initialize mime type mappings
        mimeToTypeMap.put(FileConstants.MIME_PDF, FileConstants.TYPE_PDF);
        mimeToTypeMap.put(FileConstants.MIME_DOC, FileConstants.TYPE_DOCUMENT);
        mimeToTypeMap.put(FileConstants.MIME_DOCX, FileConstants.TYPE_DOCUMENT);
        mimeToTypeMap.put(FileConstants.MIME_XLS, FileConstants.TYPE_SPREADSHEET);
        mimeToTypeMap.put(FileConstants.MIME_XLSX, FileConstants.TYPE_SPREADSHEET);
        mimeToTypeMap.put(FileConstants.MIME_PPT, FileConstants.TYPE_PRESENTATION);
        mimeToTypeMap.put(FileConstants.MIME_PPTX, FileConstants.TYPE_PRESENTATION);

        // Initialize mime type mappings for prefix types
        mimeToTypeMap.put(FileConstants.MIME_PREFIX_IMAGE, FileConstants.TYPE_IMAGE);
        mimeToTypeMap.put(FileConstants.MIME_PREFIX_VIDEO, FileConstants.TYPE_VIDEO);
        mimeToTypeMap.put(FileConstants.MIME_PREFIX_AUDIO, FileConstants.TYPE_AUDIO);
        mimeToTypeMap.put(FileConstants.MIME_PREFIX_TEXT, FileConstants.TYPE_TEXT);

        // Initialize mime to icon mappings
        mimeToIconMap.put(FileConstants.MIME_PDF, R.drawable.ic_pdf);
        mimeToIconMap.put(FileConstants.MIME_DOC, R.drawable.ic_document);
        mimeToIconMap.put(FileConstants.MIME_DOCX, R.drawable.ic_document);
        mimeToIconMap.put(FileConstants.MIME_XLS, R.drawable.ic_spreadsheet);
        mimeToIconMap.put(FileConstants.MIME_XLSX, R.drawable.ic_spreadsheet);
        mimeToIconMap.put(FileConstants.MIME_PPT, R.drawable.ic_presentation);
        mimeToIconMap.put(FileConstants.MIME_PPTX, R.drawable.ic_presentation);

        // Initialize mime to icon mappings for prefix types
        mimeToIconMap.put(FileConstants.MIME_PREFIX_VIDEO, R.drawable.ic_video);
        mimeToIconMap.put(FileConstants.MIME_PREFIX_AUDIO, R.drawable.ic_audio);
        mimeToIconMap.put(FileConstants.MIME_PREFIX_TEXT, R.drawable.ic_text);

        extensionToMimeMap.put("jpg", "image/jpeg");
        extensionToMimeMap.put("jpeg", "image/jpeg");
        extensionToMimeMap.put("png", "image/png");
        extensionToMimeMap.put("gif", "image/gif");
        extensionToMimeMap.put("pdf", FileConstants.MIME_PDF);
        extensionToMimeMap.put("doc", FileConstants.MIME_DOC);
        extensionToMimeMap.put("docx", FileConstants.MIME_DOCX);
        extensionToMimeMap.put("xls", FileConstants.MIME_XLS);
        extensionToMimeMap.put("xlsx", FileConstants.MIME_XLSX);
        extensionToMimeMap.put("ppt", FileConstants.MIME_PPT);
        extensionToMimeMap.put("pptx", FileConstants.MIME_PPTX);
        extensionToMimeMap.put("mp4", "video/mp4");
        extensionToMimeMap.put("avi", "video/avi");
        extensionToMimeMap.put("mov", "video/quicktime");
        extensionToMimeMap.put("mp3", "audio/mpeg");
        extensionToMimeMap.put("wav", "audio/wav");
        extensionToMimeMap.put("ogg", "audio/ogg");
        extensionToMimeMap.put("txt", "text/plain");
    }

    public static String formatFileSize(long size) {
        if (size <= 0) return FileConstants.SIZE_ZERO;

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat(FileConstants.SIZE_FORMAT_PATTERN)
                .format(size / Math.pow(1024, digitGroups))
                + " " + FileConstants.SIZE_UNITS[digitGroups];
    }

    public static String getFileTypeDescription(String mimeType) {
        if (mimeType == null) return FileConstants.TYPE_UNKNOWN;

        String exactType = mimeToTypeMap.get(mimeType);

        if (exactType != null) return exactType;

        for (Map.Entry<String, String> entry : mimeToTypeMap.entrySet()) {
            String key = entry.getKey();

            if (key.endsWith("/") && mimeType.startsWith(key)) {
                return entry.getValue();
            }
        }

        return FileConstants.TYPE_FILE;
    }

    public static int getIconForFileType(String mimeType) {
        if (mimeType == null) return R.drawable.ic_file;

        // Check exact matches first
        Integer exactIcon = mimeToIconMap.get(mimeType);

        if (exactIcon != null) return exactIcon;

        // Check prefix matches
        for (Map.Entry<String, Integer> entry : mimeToIconMap.entrySet()) {
            String key = entry.getKey();
            // Only check keys that end with "/"
            if (key.endsWith("/") && mimeType.startsWith(key)) {
                return entry.getValue();
            }
        }

        return R.drawable.ic_file;
    }

    public static String getMimeTypeFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "application/octet-stream";
        }

        // Extract the file extension
        int lastDotPosition = fileName.lastIndexOf('.');

        if (lastDotPosition > 0 && lastDotPosition < fileName.length() - 1) {
            String extension = fileName.substring(lastDotPosition + 1).toLowerCase();

            String mimeType = extensionToMimeMap.get(extension);

            if (mimeType != null) {
                return mimeType;
            }
        }

        return "application/octet-stream"; // Default MIME type
    }
}
