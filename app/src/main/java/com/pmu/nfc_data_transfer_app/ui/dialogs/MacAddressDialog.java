package com.pmu.nfc_data_transfer_app.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.feature.transfer.FileReceiveActivity;

import java.util.regex.Pattern;

public class MacAddressDialog {

    private final Dialog dialog;
    private final AppCompatActivity activity;
    private EditText etMacAddress;
    private Button btnCancel;
    private Button btnConnect;

    public MacAddressDialog(AppCompatActivity activity) {
        this.activity = activity;

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_mac_address, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        initializeViews(view);
        setupListeners();
    }

    private void initializeViews(View view) {
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        etMacAddress = view.findViewById(R.id.etMacAddress);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConnect = view.findViewById(R.id.btnConnect);
        
        tvTitle.setText(R.string.enter_mac_address);
        tvMessage.setText(R.string.mac_address_description);
        
        // Set MAC address input filter for correct format
        InputFilter macFilter = new InputFilter() {
            final Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}:){0,5}[0-9A-Fa-f]{0,2}");
            
            @Override
            public CharSequence filter(CharSequence source, int start, int end, 
                                      Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dstart, dend, source.subSequence(start, end).toString());
                
                if (!pattern.matcher(builder.toString()).matches()) {
                    return "";
                }
                
                // Auto-insert colons after every 2 characters (except at the end)
                if (source.length() > 0 && dstart % 3 == 2 && dstart < 15 && source.charAt(0) != ':') {
                    return source + ":";
                }
                
                return null;
            }
        };
        
        etMacAddress.setFilters(new InputFilter[]{macFilter, new InputFilter.LengthFilter(17)});
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConnect.setOnClickListener(v -> {
            String macAddress = etMacAddress.getText().toString().trim();
            
            // Validate MAC address format (XX:XX:XX:XX:XX:XX)
            if (isValidMacAddress(macAddress)) {
                dialog.dismiss();
                // Start the FileReceiveActivity with the MAC address
                FileReceiveActivity.start(activity, macAddress);
            } else {
                Toast.makeText(activity, 
                        R.string.invalid_mac_address_format, 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidMacAddress(String macAddress) {
        // Check if the MAC address is in the correct format (XX:XX:XX:XX:XX:XX)
        return macAddress.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}");
    }

    public void show() {
        dialog.show();
    }
}