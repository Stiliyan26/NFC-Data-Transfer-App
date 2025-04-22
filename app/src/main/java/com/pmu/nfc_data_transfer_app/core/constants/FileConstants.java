package com.pmu.nfc_data_transfer_app.core.constants;

/**
 * Constants related to file operations and types
 */
public class FileConstants {
    // Size related constants
    public static final String SIZE_ZERO = "0 B";
    public static final String SIZE_FORMAT_PATTERN = "#,##0.#";
    public static final String[] SIZE_UNITS = new String[]{"B", "KB", "MB", "GB", "TB"};
    
    // File type descriptions
    public static final String TYPE_UNKNOWN = "Unknown";
    public static final String TYPE_IMAGE = "Image";
    public static final String TYPE_VIDEO = "Video";
    public static final String TYPE_AUDIO = "Audio";
    public static final String TYPE_TEXT = "Text";
    public static final String TYPE_PDF = "PDF";
    public static final String TYPE_DOCUMENT = "Document";
    public static final String TYPE_SPREADSHEET = "Spreadsheet";
    public static final String TYPE_PRESENTATION = "Presentation";
    public static final String TYPE_FILE = "File";
    
    // MIME type prefixes
    public static final String MIME_PREFIX_IMAGE = "image/";
    public static final String MIME_PREFIX_VIDEO = "video/";
    public static final String MIME_PREFIX_AUDIO = "audio/";
    public static final String MIME_PREFIX_TEXT = "text/";
    
    // MIME types
    public static final String MIME_PDF = "application/pdf";
    public static final String MIME_DOC = "application/msword";
    public static final String MIME_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIME_XLS = "application/vnd.ms-excel";
    public static final String MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MIME_PPT = "application/vnd.ms-powerpoint";
    public static final String MIME_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
}
