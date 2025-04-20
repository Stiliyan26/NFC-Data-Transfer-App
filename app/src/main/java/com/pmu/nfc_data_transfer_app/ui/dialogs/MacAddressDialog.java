package com.pmu.nfc_data_transfer_app.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pmu.nfc_data_transfer_app.R;

import java.util.regex.Pattern;

public class MacAddressDialog {

    private final Dialog dialog;
    private final AppCompatActivity activity;
    private final boolean isRequiredOnInit;
    private final OnMacAddressSubmittedListener listener;
    private EditText etMacAddress;
    private Button btnCancel;
    private Button btnSubmit;
    private TextView tvTitle;
    private TextView tvMessage;

    public interface OnMacAddressSubmittedListener {
        void onMacAddressSubmitted(String macAddress);
    }

    public MacAddressDialog(AppCompatActivity activity, boolean isRequiredOnInit,
                            OnMacAddressSubmittedListener listener) {
        Log.d("MacAddressDialog", "Creating dialog with isRequiredOnInit=" + isRequiredOnInit);
        this.activity = activity;
        this.isRequiredOnInit = isRequiredOnInit;
        this.listener = listener;

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(!isRequiredOnInit);
        dialog.setCanceledOnTouchOutside(!isRequiredOnInit);

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_mac_address, null);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initializeViews(view);
        setupListeners();
    }

    private void initializeViews(View view) {
        Log.d("MacAddressDialog", "Initializing views");
        tvTitle = view.findViewById(R.id.tvDialogTitle);
        tvMessage = view.findViewById(R.id.tvDialogMessage);
        etMacAddress = view.findViewById(R.id.etMacAddress);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSubmit = view.findViewById(R.id.btnConnect);

        tvTitle.setText(R.string.enter_mac_address);
        tvMessage.setText(R.string.mac_address_description);

        // Hide cancel button if dialog is required
        if (isRequiredOnInit) {
            Log.d("MacAddressDialog", "Dialog is required, hiding cancel button");
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
        }

        // Change button text from "Connect" to "Submit"
        btnSubmit.setText("Submit");

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

    /**
     * Set a custom title for the dialog
     * @param title the custom title to set
     */
    public void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    /**
     * Set a custom message for the dialog
     * @param message the custom message to set
     */
    public void setMessage(String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
    }

    /**
     * Pre-fill the MAC address field with an existing value
     * @param macAddress the MAC address to pre-fill
     */
    public void setMacAddress(String macAddress) {
        if (etMacAddress != null && macAddress != null && !macAddress.isEmpty()) {
            etMacAddress.setText(macAddress);
        }
    }

    private void setupListeners() {
        Log.d("MacAddressDialog", "Setting up listeners");
        btnCancel.setOnClickListener(v -> {
            Log.d("MacAddressDialog", "Cancel button clicked");
            if (!isRequiredOnInit) {
                dialog.dismiss();
            } else {
                Toast.makeText(activity,
                        "You must enter a valid MAC address",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String macAddress = etMacAddress.getText().toString().trim();
            Log.d("MacAddressDialog", "Submit button clicked with MAC: " + macAddress);

            // Validate MAC address format (XX:XX:XX:XX:XX:XX)
            if (isValidMacAddress(macAddress)) {
                Log.d("MacAddressDialog", "MAC address is valid, calling listener");
                if (listener != null) {
                    listener.onMacAddressSubmitted(macAddress);
                }
                // Show a toast confirming successful save
                Toast.makeText(activity,
                        "MAC address saved: " + macAddress,
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else {
                Log.d("MacAddressDialog", "Invalid MAC address format");
                Toast.makeText(activity,
                        R.string.invalid_mac_address_format,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidMacAddress(String macAddress) {
        // Check if the MAC address is in the correct format (XX:XX:XX:XX:XX:XX)
        boolean isValid = macAddress.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}");
        Log.d("MacAddressDialog", "MAC address validation: " + macAddress + " is " + (isValid ? "valid" : "invalid"));
        return isValid;
    }

    public void show() {
        Log.d("MacAddressDialog", "Showing dialog");
        dialog.show();
    }
}