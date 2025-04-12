package com.pmu.nfc_data_transfer_app.data.datasource;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AndroidFileDataSource implements FileDataSource {

    private static final String TAG = "AndroidFileDataSource";
    private final ContentResolver contentResolver;

    public AndroidFileDataSource(Context context) {
        this.contentResolver = context.getApplicationContext().getContentResolver();
    }

    @Override
    @Nullable
    public String getFileName(Uri uri) {
        String result = null;

        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = contentResolver.query(
                    uri,
                    new String[]{ OpenableColumns.DISPLAY_NAME}, null, null, null)
            ) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error getting file name for content URI: " + uri, e);
            }
        }

        if (result == null) {
            result = uri.getPath();

            if (result != null) {
                int cut = result.lastIndexOf('/');

                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            } else {
                result = "unknown_file"; // Fallback
            }
        }

        return result;
    }

    @Override
    public long getFileSize(Uri uri) {
        long size = -1;

        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = contentResolver.query(
                    uri,
                    new String[]{ OpenableColumns.SIZE }, null, null, null)
            ) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex);
                    } else {
                        Log.w(TAG, "Size column not found or is null for content URI: " + uri);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file size for content URI: " + uri, e);
            }
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            try {
                String path = uri.getPath();
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        size = file.length();
                    } else {
                        Log.w(TAG, "File does not exist for file URI: " + uri);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file size for file URI: " + uri, e);
            }
        } else {
            Log.w(TAG, "Unsupported URI scheme for getting size: " + uri.getScheme());
        }
        return size > 0 ? size : 0; // Return 0 if size is invalid
    }

    @Override
    @Nullable
    public String getMimeType(Uri uri) {
        return contentResolver.getType(uri);
    }

    @Override
    @Nullable
    public byte[] getFileBytes(Uri uri) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);

        if (inputStream == null) {
            Log.e(TAG, "Failed to open InputStream for URI: " + uri);
            return null;
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024 * 4;
        byte[] buffer = new byte[bufferSize];
        int len;
        long totalBytesRead = 0;

        try (InputStream is = inputStream) {
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
                totalBytesRead += len;
            }
        }

        byte[] result = byteBuffer.toByteArray();

        // Log the actual size we read
        Log.d(TAG, "Read " + totalBytesRead + " bytes from URI: " + uri);

        return result;
    }
    
    /**
     * Gets the file path from a URI.
     * For content URIs, this will attempt to get the actual file path if possible.
     * For file URIs, this will return the path directly.
     * 
     * @param uri The URI to get the path from
     * @return The file path, or null if it cannot be determined
     */
    @Nullable
    public String getFilePath(Uri uri) {
        if (uri == null) {
            return null;
        }
        
        // For file URIs, just return the path
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }
        
        // For content URIs, we need to try to get the actual file path
        // This is not always possible due to Android's security model
        try {
            // Try to get the file path using the _data column
            String[] projection = { "_data" };
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("_data");
                String path = cursor.getString(columnIndex);
                cursor.close();
                return path;
            }
            
            if (cursor != null) {
                cursor.close();
            }
            
            // If we couldn't get the path, return null
            Log.w(TAG, "Could not get file path for URI: " + uri);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting file path for URI: " + uri, e);
            return null;
        }
    }
}