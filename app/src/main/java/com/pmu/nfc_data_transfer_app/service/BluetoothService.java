package com.pmu.nfc_data_transfer_app.service;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.pmu.nfc_data_transfer_app.core.model.TransferFileItem;
import com.pmu.nfc_data_transfer_app.data.local.DatabaseHelper;
import com.pmu.nfc_data_transfer_app.data.source.AndroidFileDataSource;
import com.pmu.nfc_data_transfer_app.data.source.FileDataSource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BluetoothService {

    private final static String TAG = "BluetoothService";
    private final static int READY = 1000102221; // Random constant
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    //    private BluetoothSocket socket;
    private final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothServerSocket serverSocket;
    private FileDataSource fileDataSource;

    public BluetoothService(String bluetoothDeviceMacAddress, Application application) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Initialize adapter

        this.fileDataSource = new AndroidFileDataSource(application);

        if (bluetoothAdapter != null) {
            this.bluetoothDevice = bluetoothAdapter.getRemoteDevice(bluetoothDeviceMacAddress);
        } else {
            throw new IllegalStateException("Bluetooth not supported on this device");
        }
    }

    public BluetoothService() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Initialize adapter
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getBluetoothDeviceMacAddress() {
        return bluetoothDevice.getAddress();
    }


    public BluetoothSocket connectClient(Context context) {
        BluetoothSocket socket = null;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.'
            Log.i(TAG + "Connect client", "Bluethooth is not granted");
            // return null;
        }
        bluetoothAdapter.cancelDiscovery();

        try {
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(APP_UUID);
            socket.connect();

            return socket;
        } catch (IOException e) {
            e.printStackTrace();

            try {
                if (socket != null) socket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
        }
        return null;
    }

    // TFIL Stands for TransferFileItemList
    public void sendCountTFIL(BluetoothSocket socket, List<TransferFileItem> files) throws IOException {
        try {
            OutputStream outputStream = socket.getOutputStream();

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Send total files number
            dataOutputStream.writeInt(files.size());

            dataOutputStream.flush();
        } catch (Exception e) {
            Log.e(TAG, "Could not send file count through Bluetooth");
            e.printStackTrace();
        }
    }

    public int recieveCountTFIL(BluetoothSocket socket) throws IOException {
        int result = 0;

        try {
            InputStream inputStream = socket.getInputStream();

            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Recieve count of files of list
            result = dataInputStream.readInt();
        } catch (Exception e) {
            Log.e(TAG, "Could not read file count through Bluetooth");
            e.printStackTrace();
        }

        return result;
    }

    public void sendTotalSizeTFIL(BluetoothSocket socket, List<TransferFileItem> list) throws IOException {
        int totalSize = 0;

        for (TransferFileItem tr : list) {
            totalSize += (int) tr.getSize();
        }

        try {
            OutputStream outputStream = socket.getOutputStream();

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Send total files number
            dataOutputStream.writeInt(totalSize);

            dataOutputStream.flush();
        } catch (Exception e) {
            Log.e(TAG, "Could not send total file size through Bluetooth");
            e.printStackTrace();
        }
    }

    public int recieveTotalSizeTFIL(BluetoothSocket socket) throws IOException {
        int result = 0;

        try {
            InputStream inputStream = socket.getInputStream();

            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Recieve totalSize of list
            result = dataInputStream.readInt();
        } catch (Exception e) {
            Log.e(TAG, "Could not read file count through Bluetooth");
            e.printStackTrace();
        }

        return result;
    }

    public void sendMetadataTFIL(BluetoothSocket socket, List<TransferFileItem> files) throws IOException {
        OutputStream outputStream = socket.getOutputStream();

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            sendCountTFIL(socket, files);

            // Send each file's metadata
            for (int i = 0; i < files.size(); i++) {
                byte[] fileNameBytes = files.get(i).getName().getBytes(StandardCharsets.UTF_8);
                int fileNameLength = fileNameBytes.length;

                int fileSize = (int) files.get(i).getSize();

                byte[] mimeTypeBytes = files.get(i).getMimeType().getBytes(StandardCharsets.UTF_8);
                int mimeTypeLength = mimeTypeBytes.length;

                // 1. Send filename length
                dataOutputStream.writeInt(fileNameLength);

                // 2. Send filename
                dataOutputStream.write(fileNameBytes);

                // 3. Send file size
                dataOutputStream.writeInt(fileSize);

                // 4. Send mimeType length
                dataOutputStream.writeInt(mimeTypeLength);

                // 5. Send mimeType
                dataOutputStream.write(mimeTypeBytes);

                dataOutputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<TransferFileItem> recieveMetadataTFIL(BluetoothSocket socket) throws IOException {
        ArrayList<TransferFileItem> result = new ArrayList<TransferFileItem>();

        // Recieve files count
        int filesCount = recieveCountTFIL(socket);

        for (int i = 0; i < filesCount; i++) {
            result.add(recieveMetadataTFI(socket));
        }

        return result;
    }

    public TransferFileItem recieveMetadataTFI(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // 1. Read file name length
            int fileNameLength = dataInputStream.readInt(); // EOF will throw IOException


            // 2. Read file name
            byte[] fileNameBytes = new byte[fileNameLength];
            dataInputStream.readFully(fileNameBytes);
            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

            // 3. Read file size
            int fileSize = dataInputStream.readInt();


            Log.d("BluetoothService", "Metadata for file: " + fileName + " (" + fileSize + " bytes)");

            // 4. Read mimeType length
            int mimeTypeLength = dataInputStream.readInt(); // EOF will throw IOException

            // 5. Read mimeType
            byte[] mimeTypeBytes = new byte[mimeTypeLength];
            dataInputStream.readFully(mimeTypeBytes);
            String mimeType = new String(mimeTypeBytes, StandardCharsets.UTF_8);

            return new TransferFileItem(fileName, fileSize, mimeType, null, false);
        } catch (IOException e) {
            Log.e("BluetoothService", "Error managing socket connection", e);
            return null;
        }
    }

    public boolean sendFileDataTFI(BluetoothSocket socket, TransferFileItem file) throws IOException {
        OutputStream outputStream = socket.getOutputStream();

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            byte[] fileData = fileDataSource.getFileBytes(file.getUri());

            if (fileData == null) {
                Log.e(TAG, "Could ot load file data to transfer through Bluetooth!");
                return false;
            }

            // 1. Send file size
            dataOutputStream.writeInt(fileData.length);

            // 2. Send file data
            dataOutputStream.write(fileData);

            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public byte[]
    recieveFileDataTFI(BluetoothSocket socket, String fileName) {
        try {
            if (socket != null && socket.isConnected()) {

                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                // 1. Read file size
                int fileSize = dataInputStream.readInt();

                // 2. Read file data
                byte[] fileData = new byte[fileSize];

                dataInputStream.readFully(fileData);

                Log.d("BluetoothService", "Received file data for: " + fileName + " (" + fileSize + " bytes)");

                //saveFile(fileName, fileData, context);

                return fileData;
            } else {
                Log.e(TAG, "Bluetooth socket probably not connected");
            }
        } catch (IOException e) {
            Log.e("BluetoothService", "Error managing socket connection", e);
        }

        return null;
    }

    public boolean closeGracefully(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();

            int tries = 500; // Wait up to 5 seconds

            while (0 < inputStream.available() || 0 < tries) {
                Thread.sleep(10);

                tries--;
            }

            return tries != 0;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public BluetoothSocket connectServer(Context context) {

        BluetoothServerSocket tmp = null;
        BluetoothSocket socket = null;

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // This will be implemented for API level 31+. For API level 30 and below it will always fail so its commented

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                // Log.i(TAG + "server connect to client", "Bluethooth is not granted");

                // return null;
            }

            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothFileTransfer", APP_UUID);

        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }

        serverSocket = tmp;

        if (serverSocket == null) return null;

        try {
            socket = serverSocket.accept(); // Block until connection or exception
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Socket's accept() method failed", e);
        }

        return socket;
    }

    public void saveFile(String fileName, byte[] fileData, Context context) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outFile = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(outFile);

            fos.write(fileData);
            fos.close();

            Log.d("BluetoothService", "Saved file to: " + outFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("BluetoothService", "Failed to save file", e);
        }
    }

}
