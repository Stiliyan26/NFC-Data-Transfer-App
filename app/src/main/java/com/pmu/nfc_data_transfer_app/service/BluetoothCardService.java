package com.pmu.nfc_data_transfer_app.service;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.content.SharedPreferences;
import java.nio.charset.StandardCharsets;

public class BluetoothCardService extends HostApduService {
    private static final byte[] STATUS_SUCCESS = {(byte)0x90, 0x00};
    private static final byte[] SELECT_HEADER = { 0x00, (byte)0xA4, 0x04, 0x00 };
    private static final byte[] AID = {
            (byte)0xD2, 0x76, 0x00, 0x00, (byte)0x85, 0x01, 0x01
    };
    private static final String PREFS = "nfc_prefs";

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        // Проверка за SELECT AID
        if (apdu.length < SELECT_HEADER.length + 1) return STATUS_SUCCESS;
        for (int i = 0; i < SELECT_HEADER.length; i++)
            if (apdu[i] != SELECT_HEADER[i]) return STATUS_SUCCESS;

        int lc = apdu[4];
        if (lc != AID.length) return STATUS_SUCCESS;
        for (int i = 0; i < AID.length; i++)
            if (apdu[5 + i] != AID[i]) return STATUS_SUCCESS;

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String mac = prefs.getString("bluetooth_mac", "");
        byte[] macBytes = mac.getBytes(StandardCharsets.UTF_8);

        byte[] response = new byte[macBytes.length + STATUS_SUCCESS.length];
        System.arraycopy(macBytes, 0, response, 0, macBytes.length);
        System.arraycopy(STATUS_SUCCESS, 0, response, macBytes.length, STATUS_SUCCESS.length);
        return response;
    }

    @Override
    public void onDeactivated(int reason) { /* няма нищо */ }
}
