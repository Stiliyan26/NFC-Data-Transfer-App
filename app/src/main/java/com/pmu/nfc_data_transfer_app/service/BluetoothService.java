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
import java.util.List;
import java.util.UUID;


public class BluetoothService {

    private final static String TAG = "BluetoothService";

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
            return null;
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

    private void getDataOutputStream(BluetoothSocket socket, List<TransferFileItem> files) throws IOException {
        OutputStream outputStream = socket.getOutputStream();

        try {
            byte[] fileBytes = new byte[files.size()];

            if (files == null || files.isEmpty()) {
                return;
            }

            for (TransferFileItem entry : files) {

                byte[] fileNameBytes = entry.getName().getBytes(StandardCharsets.UTF_8);
                int fileNameLength = fileNameBytes.length;
                byte[] fileData = fileDataSource.getFileBytes(entry.getUri());
                int fileSize = fileData.length;

                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                // 1. Send filename length
                dataOutputStream.writeInt(fileNameLength);

                // 2. Send filename
                dataOutputStream.write(fileNameBytes);

                // 3. Send file size
                dataOutputStream.writeInt(fileSize);

                // 4. Send file data
                dataOutputStream.write(fileData);
                dataOutputStream.flush();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public BluetoothSocket serverConnectingToClient(Context context) {

        BluetoothServerSocket tmp = null;
        BluetoothSocket socket;

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG + "server connect to client", "Bluethooth is not granted");

                return null;
            }

            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothFileTransfer", APP_UUID);

        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }

        serverSocket = tmp;

        if (serverSocket == null) return null;

        while (true) {
            try {
                socket = serverSocket.accept(); // Block until connection or exception
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            return socket;
        }
        return null;
    }

    private void getDataInputStream(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // 1. Read file name length
            int fileNameLength;

            try {
                fileNameLength = dataInputStream.readInt(); // EOF will throw IOException
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            // 2. Read file name
            byte[] fileNameBytes = new byte[fileNameLength];

            dataInputStream.readFully(fileNameBytes);

            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

            // 3. Read file size
            int fileSize = dataInputStream.readInt();

            // 4. Read file data
            byte[] fileData = new byte[fileSize];

            dataInputStream.readFully(fileData);

            Log.d("BluetoothService", "Received file: " + fileName + " (" + fileSize + " bytes)");

            saveFile(fileName, fileData);


            socket.close();

        } catch (IOException e) {
            Log.e("BluetoothService", "Error managing socket connection", e);
        }
    }

    private void saveFile(String fileName, byte[] fileData) {
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
