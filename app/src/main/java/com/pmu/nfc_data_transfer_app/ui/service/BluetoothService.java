package com.pmu.nfc_data_transfer_app.ui.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

//import com.pmu.nfc_data_transfer_app.Manifest;
import com.pmu.nfc_data_transfer_app.ui.activities.UploadFilesActivity;
import com.pmu.nfc_data_transfer_app.ui.util.Event;
import com.pmu.nfc_data_transfer_app.ui.viewmodels.MainViewModel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

public class BluetoothService {

    private BluetoothDevice bluetoothDevice;

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public String getBluetoothDeviceMacAddress() {
        return bluetoothDevice.getAddress();
    }

//    private class ConnectThread extends Thread {
//        private final BluetoothDevice device;
//        private BluetoothSocket socket;
//        private final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//        private final MainViewModel viewModel;  // Reference to your ViewModel
//
//        public ConnectThread(BluetoothDevice device, MainViewModel viewModel) {
//            this.device = device;
//            this.viewModel = viewModel;  // Initialize ViewModel
//        }
//
//        @Override
//        public void run() {
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//            if (ActivityCompat.checkSelfPermission(UploadFilesActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//
//            bluetoothAdapter.cancelDiscovery();
//
//            try {
//                socket = device.createRfcommSocketToServiceRecord(APP_UUID);
//                socket.connect();
//
////                InputStream inputStream = socket.getInputStream();
//                OutputStream outputStream = socket.getOutputStream();
//
//                try {
//                    // Wait for the result synchronously (blocks until the future is done)
//                    Map<String, byte[]> files = viewModel.readFilesForTransfer().get();  // Blocking call (in background thread)
//
//                    if (files != null && !files.isEmpty()) {
//                        // Update UI on the main thread
//                        viewModel.messageToast.postValue(new Event<>(files.size() + " files processed for transfer."));
//
//                        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
//                            DataOutputStream dataOutputStream = getDataOutputStream(entry, outputStream);
//                            dataOutputStream.flush();
//                        }
//
//                    } else {
//                        viewModel.messageToast.postValue(new Event<>("Failed to read any files."));
//                    }
//                } catch (Exception e) {
//                    viewModel.messageToast.postValue(new Event<>("Error during file preparation."));
//                    e.printStackTrace();
//                } finally {
//                    viewModel.currentlyLoading.postValue(false);  // Update loading indicator in ViewModel
//                }
//
//            } catch (IOException e) {
//                viewModel.messageToast.postValue(new Event<>("Connection failed: " + e.getMessage()));
//                e.printStackTrace();
//
//                try {
//                    if (socket != null) socket.close();
//                } catch (IOException closeException) {
//                    closeException.printStackTrace();
//                }
//            }
//        }
//
//        @NonNull
//        private DataOutputStream getDataOutputStream(Map.Entry<String, byte[]> entry, OutputStream outputStream) throws IOException {
//            String fileName = entry.getKey();
//            byte[] fileData = entry.getValue();
//
//            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
//            int fileNameLength = fileNameBytes.length;
//            int fileSize = fileData.length;
//
//            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
//
//            // 1. Send filename length
//            dataOutputStream.writeInt(fileNameLength);
//
//            // 2. Send filename
//            dataOutputStream.write(fileNameBytes);
//
//            // 3. Send file size
//            dataOutputStream.writeInt(fileSize);
//
//            // 4. Send file data
//            dataOutputStream.write(fileData);
//            return dataOutputStream;
//        }
//
//        public void cancel() {
//            try {
//                if (socket != null) socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
