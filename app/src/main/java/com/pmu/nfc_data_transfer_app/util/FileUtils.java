package com.pmu.nfc_data_transfer_app.util;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.core.constants.FileConstants;

import java.text.DecimalFormat;

/**
 * Utility class for file-related operations and formatting
 */
public class FileUtils {

    public static String formatFileSize(long size) {
        if (size <= 0) return FileConstants.SIZE_ZERO;

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat(FileConstants.SIZE_FORMAT_PATTERN)
                .format(size / Math.pow(1024, digitGroups))
                + " " + FileConstants.SIZE_UNITS[digitGroups];
    }
    public static String getFileTypeDescription(String mimeType) {
        if (mimeType == null) return FileConstants.TYPE_UNKNOWN;

        if (mimeType.startsWith(FileConstants.MIME_PREFIX_IMAGE)) return FileConstants.TYPE_IMAGE;
        if (mimeType.startsWith(FileConstants.MIME_PREFIX_VIDEO)) return FileConstants.TYPE_VIDEO;
        if (mimeType.startsWith(FileConstants.MIME_PREFIX_AUDIO)) return FileConstants.TYPE_AUDIO;
        if (mimeType.startsWith(FileConstants.MIME_PREFIX_TEXT)) return FileConstants.TYPE_TEXT;

        switch (mimeType) {
            case FileConstants.MIME_PDF:
                return FileConstants.TYPE_PDF;

            case FileConstants.MIME_DOC:
            case FileConstants.MIME_DOCX:
                return FileConstants.TYPE_DOCUMENT;

            case FileConstants.MIME_XLS:
            case FileConstants.MIME_XLSX:
                return FileConstants.TYPE_SPREADSHEET;

            case FileConstants.MIME_PPT:
            case FileConstants.MIME_PPTX:
                return FileConstants.TYPE_PRESENTATION;
        }

        return FileConstants.TYPE_FILE;
    }

    public static int getIconForFileType(String mimeType) {
        if (mimeType.startsWith(FileConstants.MIME_PREFIX_VIDEO)) {
            return R.drawable.ic_video;
        } else if (mimeType.startsWith(FileConstants.MIME_PREFIX_AUDIO)) {
            return R.drawable.ic_audio;
        } else if (mimeType.startsWith(FileConstants.MIME_PREFIX_TEXT)) {
            return R.drawable.ic_text;
        } else if (mimeType.equals(FileConstants.MIME_PDF)) {
            return R.drawable.ic_pdf;
        } else if (mimeType.equals(FileConstants.MIME_DOC) ||
                mimeType.equals(FileConstants.MIME_DOCX)) {
            return R.drawable.ic_document;
        } else if (mimeType.equals(FileConstants.MIME_XLS) ||
                mimeType.equals(FileConstants.MIME_XLSX)) {
            return R.drawable.ic_spreadsheet;
        } else if (mimeType.equals(FileConstants.MIME_PPT) ||
                mimeType.equals(FileConstants.MIME_PPTX)) {
            return R.drawable.ic_presentation;
        }

        return R.drawable.ic_file;
    }
}