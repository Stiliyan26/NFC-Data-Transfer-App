package com.pmu.nfc_data_transfer_app.data.datasource;

import android.net.Uri;
import androidx.annotation.Nullable;
import java.io.IOException;

public interface FileDataSource {
    @Nullable
    String getFileName(Uri uri);

    long getFileSize(Uri uri);

    @Nullable
    String getMimeType(Uri uri);

    @Nullable
    byte[] getFileBytes(Uri uri) throws IOException;
}