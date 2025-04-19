package com.pmu.nfc_data_transfer_app.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;


import com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants;

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
}
