package com.pmu.nfc_data_transfer_app.data.local;

import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.core.model.TransferHistory;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "transferEvents.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_TRANSFERS = "transferEvents";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DEVICE_NAME = "deviceName";
    public static final String COLUMN_TRANSFER_DATE = "transferDate";
    public static final String COLUMN_TRANSFER_TYPE = "transferType";
    public static final String COLUMN_FILES  = "files";
    public static final String COLUMN_TOTAL_SIZE  = "totalSize";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TRANSFERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DEVICE_NAME + " TEXT, "
                + COLUMN_TRANSFER_DATE + " INTEGER, "
                + COLUMN_TRANSFER_TYPE + " TEXT, "
                + COLUMN_FILES + " TEXT, "
                + COLUMN_TOTAL_SIZE + " INTEGER"
                + ");";

        db.execSQL(createTable);

        ContentValues cv = new ContentValues();

        //// Xiaomi device
        List<TransferFileItem> xiaomiFiles = createXiaomiFiles();
        long xiaomiTotalSize = calculateTotalSize(xiaomiFiles);

        cv.put(COLUMN_DEVICE_NAME,   "Xiaomi Mi 11");
        cv.put(COLUMN_TRANSFER_DATE,  System.currentTimeMillis() - 86_400_000L);
        cv.put(COLUMN_TRANSFER_TYPE,  "receive");
        cv.put(COLUMN_FILES, convertFileItemsToJson(xiaomiFiles));
        cv.put(COLUMN_TOTAL_SIZE, xiaomiTotalSize);

        db.insert(TABLE_TRANSFERS, null, cv);

        // Samsung device
        List<TransferFileItem> samsungFiles = createSamsungFiles();
        long samsungTotalSize = calculateTotalSize(samsungFiles);

        cv.clear();

        cv.put(COLUMN_DEVICE_NAME,   "Samsung Galaxy S21");
        cv.put(COLUMN_TRANSFER_DATE,  System.currentTimeMillis());
        cv.put(COLUMN_TRANSFER_TYPE,  "send");
        cv.put(COLUMN_FILES, convertFileItemsToJson(samsungFiles));
        cv.put(COLUMN_TOTAL_SIZE,     samsungTotalSize);

        db.insert(TABLE_TRANSFERS, null, cv);

        // Google Pixel device
        List<TransferFileItem> pixelFiles = createPixelFiles();
        long pixelTotalSize = calculateTotalSize(pixelFiles);

        cv.clear();
        cv.put(COLUMN_DEVICE_NAME, "Google Pixel 7");
        cv.put(COLUMN_TRANSFER_DATE, System.currentTimeMillis() - 43_200_000L);
        cv.put(COLUMN_TRANSFER_TYPE, "send");
        cv.put(COLUMN_FILES, convertFileItemsToJson(pixelFiles));
        cv.put(COLUMN_TOTAL_SIZE, pixelTotalSize);
        db.insert(TABLE_TRANSFERS, null, cv);
    }

    public List<TransferHistory> getAllDevicesInfo() {
        List<TransferHistory> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " +
                COLUMN_ID + ", " +
                COLUMN_DEVICE_NAME + ", " +
                COLUMN_TRANSFER_DATE + ", " +
                COLUMN_TRANSFER_TYPE + ", " +
                COLUMN_TOTAL_SIZE +
                " FROM " + TABLE_TRANSFERS;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_NAME));
                long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSFER_DATE));
                Date transferDate = new Date(dateLong);
                String transferType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSFER_TYPE));
                long totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SIZE));

                TransferHistory event = new TransferHistory(id, deviceName, transferDate,
                        transferType, new ArrayList<>(), totalSize);

                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return events;
    }

    private long calculateTotalSize(List<TransferFileItem> files) {
        long totalSize = 0;

        for (TransferFileItem file : files) {
            totalSize += file.getSize();
        }
        return totalSize;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSFERS);
        onCreate(db);
    }

    private String convertFileItemsToJson(List<TransferFileItem> fileItems) {
        JSONArray jsonArray = new JSONArray();

        for (TransferFileItem item : fileItems) {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("fileName", item.getName());
                jsonObject.put("fileSize", item.getSize());
                jsonObject.put("fileType", item.getMimeType());
                if (item.getUri() != null) {
                    jsonObject.put("fileUri", item.getUri().toString());
                }
                jsonObject.put("isImage", item.isImage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }

        return jsonArray.toString();
    }

    private List<TransferFileItem> convertJsonToFileItems(String json) {
        List<TransferFileItem> fileItems = new ArrayList<>();

        if(null == json){
            return fileItems;
        }

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String fileName = jsonObject.getString("fileName");
                long fileSize = jsonObject.getLong("fileSize");
                String fileType = jsonObject.getString("fileType");
                boolean isImage = jsonObject.getBoolean("isImage");
                Uri fileUri = Uri.parse("");
                if (jsonObject.has("fileUri")) {
                    fileUri = Uri.parse(jsonObject.getString("fileUri"));
                }
                fileItems.add(new TransferFileItem(fileName, fileSize, fileType, fileUri, isImage));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fileItems;
    }

    public long addTransferEventToDatabase(TransferHistory tr)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_DEVICE_NAME, tr.getDeviceName());
        values.put(COLUMN_TRANSFER_DATE, tr.getTransferDate().getTime());
        values.put(COLUMN_TRANSFER_TYPE, tr.getTransferType());
        values.put(COLUMN_FILES, convertFileItemsToJson(tr.getFiles()));
        values.put(COLUMN_TOTAL_SIZE, tr.getTotalSize());

        long id = db.insert(TABLE_TRANSFERS, null, values);

        db.close();

        return id;
    }

    public List<TransferHistory> getAllTransferEvents() {
        List<TransferHistory> events = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSFERS;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));

                String deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_NAME));

                long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSFER_DATE));
                Date transferDate = new Date(dateLong);

                String fileItemsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILES));

                String transferType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSFER_TYPE));

                long totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SIZE));

                List<TransferFileItem> fileItems = convertJsonToFileItems(fileItemsJson);

                TransferHistory event = new TransferHistory(id, deviceName, transferDate,
                        transferType, fileItems, totalSize);

                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return events;
    }

    public TransferHistory getTransferredFilesByTransferId(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSFERS + " WHERE " + COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(eventId)});

        TransferHistory event = null;

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));

            String deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEVICE_NAME));

            long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSFER_DATE));
            Date transferDate = new Date(dateLong);

            String fileItemsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILES));

            String transferType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSFER_TYPE));

            long totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SIZE));

            List<TransferFileItem> fileItems = convertJsonToFileItems(fileItemsJson);

            event = new TransferHistory(id, deviceName, transferDate,
                    transferType, fileItems, totalSize);
        }

        cursor.close();
        db.close();

        return event;
    }

    private List<TransferFileItem> createSamsungFiles() {
        List<TransferFileItem> sampleFiles = new ArrayList<>();

        sampleFiles.add(createSampleFile("presentation.pptx", 5200000, "application/vnd.openxmlformats-officedocument.presentationml.presentation", false));
        sampleFiles.add(createSampleFile("document.docx", 1800000, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", false));
        sampleFiles.add(createSampleFile("report.pdf", 3500000, "application/pdf", false));
        sampleFiles.add(createSampleFile("data.xlsx", 900000, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", false));
        sampleFiles.add(createSampleFile("family_photo.jpg", 2800000, "image/jpeg", true));

        return sampleFiles;
    }

    // TODO: delete when done testing
    private List<TransferFileItem> createXiaomiFiles() {
        List<TransferFileItem> sampleFiles = new ArrayList<>();

        sampleFiles.add(createSampleFile("vacation_photo1.jpg", 4500000, "image/jpeg", true));
        sampleFiles.add(createSampleFile("vacation_photo2.jpg", 5200000, "image/jpeg", true));
        sampleFiles.add(createSampleFile("vacation_photo3.jpg", 4800000, "image/jpeg", true));
        sampleFiles.add(createSampleFile("vacation_video.mp4", 18000000, "video/mp4", false));
        sampleFiles.add(createSampleFile("notes.txt", 150000, "text/plain", false));
        sampleFiles.add(createSampleFile("audio_recording.mp3", 8500000, "audio/mpeg", false));
        sampleFiles.add(createSampleFile("contacts.vcf", 85000, "text/vcard", false));

        return sampleFiles;
    }

    private List<TransferFileItem> createPixelFiles() {
        List<TransferFileItem> sampleFiles = new ArrayList<>();

        sampleFiles.add(createSampleFile("project_proposal.pdf", 4200000, "application/pdf", false));
        sampleFiles.add(createSampleFile("screenshot_1.png", 1500000, "image/png", true));
        sampleFiles.add(createSampleFile("screenshot_2.png", 1800000, "image/png", true));
        sampleFiles.add(createSampleFile("app_backup.zip", 12500000, "application/zip", false));
        sampleFiles.add(createSampleFile("meeting_notes.txt", 75000, "text/plain", false));
        sampleFiles.add(createSampleFile("budget.xlsx", 950000, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", false));

        return sampleFiles;
    }

    private TransferFileItem createSampleFile(String fileName, long fileSize, String fileType, boolean isImage) {
        Uri dummyUri = Uri.parse(
                "content://com.pmu.nfc_data_transfer_app/sample/" + UUID.randomUUID().toString()
        );

        return new TransferFileItem(fileName, fileSize, fileType, dummyUri, isImage);
    }
    // TODO: delete when done testing END
    public boolean transferExists(int transferId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String countQuery = "SELECT COUNT(*) FROM " + TABLE_TRANSFERS +
                " WHERE " + COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(transferId)});

        boolean exists = false;

        if (cursor.moveToFirst()) {
            exists = (cursor.getInt(0) > 0);
        }

        cursor.close();
        db.close();

        return exists;
    }

    public boolean addFileToTransfer(int transferId, TransferFileItem newFile) {
        try {
            TransferHistory currentTransfer = getTransferredFilesByTransferId(transferId);

            if (currentTransfer == null) {
                Log.e("Database", "Cannot find transfer with ID: " + transferId);
                return false;
            }

            List<TransferFileItem> updatedFiles = new ArrayList<>(currentTransfer.getFiles());
            updatedFiles.add(newFile);

            // Calculate new total size
            long newTotalSize = currentTransfer.getTotalSize() + newFile.getSize();

            // Create updated transfer history
            TransferHistory updatedTransfer = new TransferHistory(
                    transferId,
                    currentTransfer.getDeviceName(),
                    currentTransfer.getTransferDate(),
                    currentTransfer.getTransferType(),
                    updatedFiles,
                    newTotalSize
            );

            // Update database
            boolean success = updateTransferEvent(transferId, updatedTransfer);

            if (success) {
                Log.d("Database", "Successfully added file to transfer: " + newFile.getName());
            } else {
                Log.e("Database", "Failed to add file to transfer: " + newFile.getName());
            }

            return success;

        } catch (Exception e) {
            Log.e("Database", "Error adding file to transfer", e);
            return false;
        }
    }

    public boolean updateTransferEvent(int transferId, TransferHistory updatedTransfer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_NAME, updatedTransfer.getDeviceName());
        values.put(COLUMN_TRANSFER_DATE, updatedTransfer.getTransferDate().getTime());
        values.put(COLUMN_TRANSFER_TYPE, updatedTransfer.getTransferType());
        values.put(COLUMN_FILES, convertFileItemsToJson(updatedTransfer.getFiles()));
        values.put(COLUMN_TOTAL_SIZE, updatedTransfer.getTotalSize());

        // Update the record
        int rowsAffected = db.update(
                TABLE_TRANSFERS,
                values,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(transferId) }
        );

        db.close();

        return rowsAffected > 0;
    }
}
