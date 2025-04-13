package com.pmu.nfc_data_transfer_app.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pmu.nfc_data_transfer_app.R;

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

        // Initialize buttons
        Button btnSend = findViewById(R.id.btnSend);
        Button btnReceive = findViewById(R.id.btnReceive);

        // Click event listeners for buttons
        btnSend.setOnClickListener(v -> navigateToFileTransfer(SEND));
        btnReceive.setOnClickListener(v -> navigateToFileTransfer(RECEIVE));
    }

    private void navigateToFileTransfer(String mode) {
        Intent intent = new Intent(this, FileTransferActivity.class);
        intent.putExtra("mode", mode);
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