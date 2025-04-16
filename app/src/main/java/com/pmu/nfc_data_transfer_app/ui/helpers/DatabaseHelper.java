package com.pmu.nfc_data_transfer_app.ui.helpers;

import com.pmu.nfc_data_transfer_app.data.model.FileItem;
import com.pmu.nfc_data_transfer_app.data.model.TransferHistory;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "transferEvents.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRANSFERS = "transferEvents";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DEVICE_NAME = "deviceName";
    public static final String COLUMN_TRANSFER_DATE = "transferDate";
    public static final String COLUMN_TRANSFER_TYPE = "transferType";
    public static final String COLUMN_FILES  = "files";   // JSON representation of an List<FileItem>
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
                + COLUMN_FILES + " TEXT, " // Stores the list of FileItem objects in JSON format
                + COLUMN_TOTAL_SIZE + " INTEGER"
                + ");";

        db.execSQL(createTable);
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DEVICE_NAME,   "Xiaomi Mi 11");
        cv.put(COLUMN_TRANSFER_DATE,  System.currentTimeMillis() - 86_400_000L);
        cv.put(COLUMN_TRANSFER_TYPE,  "receive");
        cv.put(COLUMN_TOTAL_SIZE,     45_000_000);
        db.insert(TABLE_TRANSFERS, null, cv);

        cv.clear();
        cv.put(COLUMN_DEVICE_NAME,   "Samsung Galaxy S21");
        cv.put(COLUMN_TRANSFER_DATE,  System.currentTimeMillis());
        cv.put(COLUMN_TRANSFER_TYPE,  "send");
        cv.put(COLUMN_TOTAL_SIZE,     15_000_000);
        db.insert(TABLE_TRANSFERS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple upgrade strategy: Drop the existing tables and call onCreate()
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSFERS);
        onCreate(db);
    }

    private String convertFileItemsToJson(List<FileItem> fileItems) {
        JSONArray jsonArray = new JSONArray();
        for (FileItem item : fileItems) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("fileName", item.getFileName());
                jsonObject.put("fileSize", item.getFileSize());
                jsonObject.put("fileType", item.getFileType());
                jsonObject.put("fileUri", item.getFileUri().toString());
                jsonObject.put("isImage", item.isImage());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }

    private List<FileItem> convertJsonToFileItems(String json) {
        List<FileItem> fileItems = new ArrayList<>();

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
                Uri fileUri = Uri.parse(jsonObject.getString("fileUri"));

                fileItems.add(new FileItem(fileName, fileSize, fileType, fileUri, isImage));
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

                List<FileItem> fileItems = convertJsonToFileItems(fileItemsJson);

                TransferHistory event = new TransferHistory(id, deviceName, transferDate,
                        transferType, fileItems, totalSize);

                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return events;
    }

    public TransferHistory getTransferEvent(int eventId) {
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

            List<FileItem> fileItems = convertJsonToFileItems(fileItemsJson);

            event = new TransferHistory(id, deviceName, transferDate,
                    transferType, fileItems, totalSize);
        }

        cursor.close();
        db.close();

        return event;
    }
}
