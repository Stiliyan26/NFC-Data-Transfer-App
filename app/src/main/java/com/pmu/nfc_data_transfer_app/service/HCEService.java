package com.pmu.nfc_data_transfer_app.service;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants;

import java.nio.charset.StandardCharsets;

public class HCEService extends HostApduService {
    private String responseString;
    private static final byte[] STATUS_SUCCESS = {(byte) 0x90, 0x00};

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        String expectedSelectApduHex = GlobalConstants.HCE_AID; // SELECT AID
        String apduHex = bytesToHex(apdu);

        if (apduHex.equalsIgnoreCase(expectedSelectApduHex)) {
            byte[] responseData = responseString.getBytes(StandardCharsets.UTF_8);
            return concatenate(responseData, STATUS_SUCCESS);
        }

        return STATUS_SUCCESS;
    }

    public void setResponseString(String macAddress) {
        this.responseString = macAddress;
    }

    @Override
    public void onDeactivated(int reason) {
        // Do nothing
    }

    private static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }
}

