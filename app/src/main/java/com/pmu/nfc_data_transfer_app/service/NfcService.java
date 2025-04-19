package com.pmu.nfc_data_transfer_app.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.util.Log;

import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

import com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


public class NfcService {

    private final String TAG = "NfcService";
    private final NfcAdapter nfcAdapter;

    public NfcService(NfcAdapter nfcAdapter) {
        this.nfcAdapter = nfcAdapter;
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }

    public BluetoothDevice processNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null && rawMsgs.length > 0) {

                NdefMessage message = (NdefMessage) rawMsgs[0];
                String macAddress = getTextFromMessage(message);

                if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                    // Use the MAC address to get the remote Bluetooth device
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    return bluetoothAdapter.getRemoteDevice(macAddress);
                }
            }
        }
        return null;
    }

    // https://berlin.ccc.de/~starbug/felica/NFCForum-TS-RTD_Text_1.0.pdf#:~:text=The%20Text%20Record%20Type%20Description%20defines%20an%20NFC,as%20a%20normative%20reference%20to%20the%20Text%20RTD.
    private String getTextFromMessage(NdefMessage message) {
        NdefRecord record = message.getRecords()[0];
        byte[] payload = record.getPayload();
        String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0x3F;

        try {
            return new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // no UTF-8, really?
            return null;
        }
    }

    public NdefMessage createMimeMessage(String macAddress) {
        try {
            byte[] mimeBytes = GlobalConstants.MIME_TYPE.getBytes(StandardCharsets.US_ASCII);
            byte[] macAddressBytes = macAddress.getBytes(StandardCharsets.UTF_8);

            NdefRecord record = new NdefRecord(
                    NdefRecord.TNF_MIME_MEDIA, // Type Name Format (MIME media)
                    mimeBytes,                 // Type field (GlobalConstants.MIME_TYPE)
                    new byte[0],               // ID field (unused)
                    macAddressBytes                  // Payload Mac address
            );


            return new NdefMessage(new NdefRecord[]{record});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Writes the Bluetooth device address to an NFC tag.
     *
     * @param activity The current activity
     * @param tag The discovered NFC tag
     * @param bluetoothDeviceAddress The Bluetooth MAC address to write
     * @return true if writing was successful, false otherwise
     */
    public boolean writeBluetoothAddressToTag(Activity activity, Tag tag, String bluetoothDeviceAddress) {
        try {
            // Create an NdefMessage containing the Bluetooth MAC address
            NdefMessage ndefMessage = createMimeMessage(bluetoothDeviceAddress);

            if (ndefMessage == null) {
                Log.e(TAG, "Failed to create NDEF message");
                return false;
            }

            // Try to get an Ndef instance for this tag
            Ndef ndef = Ndef.get(tag);

            if (ndef != null) {
                // Tag is already NDEF formatted
                return writeToNdefTag(ndef, ndefMessage);
            } else {
                // Tag is not NDEF formatted, try to format it
                NdefFormatable ndefFormatable = NdefFormatable.get(tag);

                if (ndefFormatable != null) {
                    return formatAndWriteToTag(ndefFormatable, ndefMessage);
                } else {
                    Log.e(TAG, "Tag doesn't support NDEF format");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error writing to NFC tag", e);
            return false;
        }
    }

    /**
     * Writes NDEF message to an already formatted NDEF tag
     */
    private boolean writeToNdefTag(Ndef ndef, NdefMessage ndefMessage) {
        try {
            // Check if the tag is writable
            if (!ndef.isWritable()) {
                Log.e(TAG, "Tag is read-only");
                return false;
            }

            // Check if there's enough space on the tag
            int size = ndefMessage.toByteArray().length;

            if (ndef.getMaxSize() < size) {
                Log.e(TAG, "Tag capacity is too small, need " + size + " bytes, available " + ndef.getMaxSize() + " bytes");
                return false;
            }

            // Connect to the tag
            ndef.connect();

            // Write the message
            ndef.writeNdefMessage(ndefMessage);

            // Close the connection
            ndef.close();

            Log.d(TAG, "Successfully wrote Bluetooth address to tag");
            return true;
        } catch (IOException | SecurityException | FormatException e) {
            Log.e(TAG, "Failed to write to NDEF tag", e);
            return false;
        }
    }

    /**
     * Formats an unformatted tag and writes NDEF message to it
     */
    private boolean formatAndWriteToTag(NdefFormatable ndefFormatable, NdefMessage ndefMessage) {
        try {
            // Connect to the tag
            ndefFormatable.connect();

            // Format and write in one step
            ndefFormatable.format(ndefMessage);

            // Close the connection
            ndefFormatable.close();

            Log.d(TAG, "Successfully formatted and wrote Bluetooth address to tag");
            return true;
        } catch (IOException | SecurityException | FormatException e) {
            Log.e(TAG, "Failed to format and write to NDEF tag", e);
            return false;
        }
    }
}
