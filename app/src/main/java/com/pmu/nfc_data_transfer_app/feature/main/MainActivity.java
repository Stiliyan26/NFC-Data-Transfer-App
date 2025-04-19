package com.pmu.nfc_data_transfer_app.feature.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.feature.about.AboutActivity;
import com.pmu.nfc_data_transfer_app.feature.history.TransferHistoryActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.FileReceiveActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.UploadFilesActivity;
import com.pmu.nfc_data_transfer_app.ui.dialogs.MacAddressDialog;
import com.pmu.nfc_data_transfer_app.ui.util.NfcAnimationHelper;
import com.pmu.nfc_data_transfer_app.util.AppPreferences;

public class MainActivity extends AppCompatActivity {

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

    private void setupUI() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        Button btnSend = findViewById(R.id.btnSend);
        Button btnReceive = findViewById(R.id.btnReceive);
        Button btnHistory = findViewById(R.id.btnHistory);

        final ImageView logoImage = findViewById(R.id.logo_image);

        // Click event listeners
        btnSend.setOnClickListener(v -> navigateToFileTransfer(SEND, UploadFilesActivity.class));
        btnReceive.setOnClickListener(v -> navigateToFileTransfer(RECEIVE, FileReceiveActivity.class));

        btnHistory.setOnClickListener(v -> navigateToHistory());

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
}