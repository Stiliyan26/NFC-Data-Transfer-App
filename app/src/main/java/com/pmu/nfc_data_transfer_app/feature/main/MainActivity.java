package com.pmu.nfc_data_transfer_app.feature.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.feature.about.AboutActivity;
import com.pmu.nfc_data_transfer_app.feature.history.TransferHistoryActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.UploadFilesActivity;
import com.pmu.nfc_data_transfer_app.ui.dialogs.MacAddressDialog;
import com.pmu.nfc_data_transfer_app.ui.util.NfcAnimationHelper;

public class MainActivity extends AppCompatActivity {

    private final String SEND = "send";
    private final String RECEIVE = "receive";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupUI();
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
        btnReceive.setOnClickListener(v -> showMacAddressDialog());

        btnHistory.setOnClickListener(v -> navigateToHistory());

        NfcAnimationHelper animationHelper = new NfcAnimationHelper(this);
        logoImage.setOnClickListener(v -> animationHelper.createNfcWaveEffect(logoImage));
    }

    private void showMacAddressDialog() {
        MacAddressDialog dialog = new MacAddressDialog(this);
        dialog.show();
    }

    private <T extends Activity> void navigateToFileTransfer(String mode, Class<T> activity) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("mode", mode);
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