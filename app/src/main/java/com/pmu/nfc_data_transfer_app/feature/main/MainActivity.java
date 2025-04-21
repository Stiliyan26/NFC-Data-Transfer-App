package com.pmu.nfc_data_transfer_app.feature.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.feature.about.AboutActivity;
import com.pmu.nfc_data_transfer_app.feature.history.TransferHistoryActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.FileReceiveActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.UploadFilesActivity;
import com.pmu.nfc_data_transfer_app.ui.dialogs.MacAddressDialog;
import com.pmu.nfc_data_transfer_app.ui.util.NfcAnimationHelper;
import com.pmu.nfc_data_transfer_app.util.AppPreferences;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;

    private final String SEND = "send";
    private final String RECEIVE = "receive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupUI();

        // Add debug logging to check current MAC address status
        boolean hasMacAddress = AppPreferences.hasMacAddress(this);
        String currentMacAddress = AppPreferences.getMacAddress(this);
        android.util.Log.d("MainActivity", "Has MAC Address: " + hasMacAddress);
        android.util.Log.d("MainActivity", "Current MAC Address: '" + currentMacAddress + "'");

        // Check if MAC address is already saved, if not show the dialog
        if (!hasMacAddress) {
            android.util.Log.d("MainActivity", "Showing MAC Address dialog on init");
            showMacAddressDialogOnInit();
        } else {
            android.util.Log.d("MainActivity", "MAC Address already exists, not showing dialog");
        }

        checkBluetoothPermissions();
    }

    private void showMacAddressDialogOnInit() {
        MacAddressDialog dialog = new MacAddressDialog(this, true, macAddress -> {
            // Save the MAC address globally
            AppPreferences.saveMacAddress(this, macAddress);
            // Log to verify the MAC address was saved
            android.util.Log.d("MainActivity", "MAC Address saved to preferences: " + macAddress);

            // Verify it was actually saved by reading it back
            String savedMacAddress = AppPreferences.getMacAddress(this);
            android.util.Log.d("MainActivity", "MAC Address retrieved from preferences: " + savedMacAddress);

            // Additional verification in UI
            Toast.makeText(this,
                    "MAC Address saved: " + savedMacAddress,
                    Toast.LENGTH_LONG).show();
        });
        dialog.show();
    }

    /**
     * Show dialog to change MAC address
     */
    private void showChangeMacAddressDialog() {
        // Get current MAC address to display in toast
        String currentMacAddress = AppPreferences.getMacAddress(this);

        // Create the dialog with isRequiredOnInit=false since this is a manual change
        MacAddressDialog dialog = new MacAddressDialog(this, false, macAddress -> {
            // Save the new MAC address
            AppPreferences.saveMacAddress(this, macAddress);
            // Log the change
            android.util.Log.d("MainActivity", "MAC Address changed from: " + currentMacAddress + " to: " + macAddress);

            // Show confirmation toast
            Toast.makeText(this,
                    "MAC Address changed to: " + macAddress,
                    Toast.LENGTH_LONG).show();
        });

        // Customize the dialog for changing MAC address
        dialog.setTitle("Смени своя адрес");
        dialog.setMessage("Въведете новия MAC адрес на устройството");

        // Pre-fill with current MAC address if available
        if (currentMacAddress != null && !currentMacAddress.isEmpty()) {
            dialog.setMacAddress(currentMacAddress);
        }

        dialog.show();
    }

    private void setupUI() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        Button btnSend = findViewById(R.id.btnSend);
        Button btnReceive = findViewById(R.id.btnReceive);
        Button btnHistory = findViewById(R.id.btnHistory);
        Button btnChangeMacAddress = findViewById(R.id.btnChangeMacAddress);

        final ImageView logoImage = findViewById(R.id.logo_image);

        // Click event listeners
        btnSend.setOnClickListener(v -> navigateToFileTransfer(SEND, UploadFilesActivity.class));
        btnReceive.setOnClickListener(v -> navigateToFileTransfer(RECEIVE, FileReceiveActivity.class));
        btnHistory.setOnClickListener(v -> navigateToHistory());
        btnChangeMacAddress.setOnClickListener(v -> showChangeMacAddressDialog());

        NfcAnimationHelper animationHelper = new NfcAnimationHelper(this);
        logoImage.setOnClickListener(v -> animationHelper.createNfcWaveEffect(logoImage));
    }

    private <T extends Activity> void navigateToFileTransfer(String mode, Class<T> activity) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("mode", mode);

        // If it's receive mode, get the saved MAC address and pass it to the activity
        if (mode.equals(RECEIVE) && activity == FileReceiveActivity.class) {
            String macAddress = AppPreferences.getMacAddress(this);
            intent.putExtra("mac_address", macAddress);
        }

        startActivity(intent);
    }

    private void navigateToHistory() {
        Intent intent = new Intent(this, TransferHistoryActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true; // true to display the menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true; // true to indicate that the event was handled
        }

        return super.onOptionsItemSelected(item);
    }


    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            List<String> permissionsToRequest = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        permissionsToRequest.toArray(new String[0]),
                        REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show();
                // Start Bluetooth service or proceed with connection
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}