package com.pmu.nfc_data_transfer_app.core.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for file items during transfer process
 */
public class TransferFileItem implements Parcelable {
    private final String name;
    private final long size;
    private final String mimeType;
    private final Uri uri;
    private final boolean isImage;
    private FileTransferStatus status = FileTransferStatus.PENDING;
    private int progress = 0;

    public TransferFileItem(
            String name,
            long size,
            String mimeType,
            Uri uri,
            boolean isImage
    ) {
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.uri = uri;
        this.isImage = isImage;
    }

    protected TransferFileItem(Parcel in) {
        name = in.readString();
        size = in.readLong();
        mimeType = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        status = FileTransferStatus.valueOf(in.readString());
        isImage = in.readByte() != 0;
        progress = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(mimeType);
        dest.writeParcelable(uri, flags);
        dest.writeString(status.name());
        dest.writeByte((byte) (isImage ? 1 : 0));
        dest.writeInt(progress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransferFileItem> CREATOR = new Creator<TransferFileItem>() {
        @Override
        public TransferFileItem createFromParcel(Parcel in) {
            return new TransferFileItem(in);
        }

        @Override
        public TransferFileItem[] newArray(int size) {
            return new TransferFileItem[size];
        }
    };

    // Getters
    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Uri getUri() {
        return uri;
    }

    public boolean isImage() {
        return isImage;
    }

    public FileTransferStatus getStatus() {
        return status;
    }

    public void setStatus(FileTransferStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TransferFileItem that = (TransferFileItem) o;

        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
