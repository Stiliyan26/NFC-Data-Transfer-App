package com.pmu.nfc_data_transfer_app.service;

import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.CLA_NOT_SUPPORTED;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.DEFAULT_CLA;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.HCE_AID;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.INS_NOT_SUPPORTED;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.MIN_APDU_LENGTH;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.SELECT_INS;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.STATUS_FAILED;
import static com.pmu.nfc_data_transfer_app.core.constants.GlobalConstants.STATUS_SUCCESS;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import com.pmu.nfc_data_transfer_app.util.AppPreferences;


public class HCEService extends HostApduService {
    String TAG = "Host Card Emulator";
    private String responseString;
    private static final String HEX_CHARS = "0123456789ABCDEF";
    private static final char[] HEX_CHARS_ARRAY = HEX_CHARS.toCharArray();


    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        if (apdu == null) {
            return hexStringToByteArray(STATUS_FAILED);
        }

        String hexCommandApdu = toHex(apdu);
        if (hexCommandApdu.length() < MIN_APDU_LENGTH) {
            return hexStringToByteArray(STATUS_FAILED);
        }

        if (!hexCommandApdu.substring(0, 2).equals(DEFAULT_CLA)) {
            return hexStringToByteArray(CLA_NOT_SUPPORTED);
        }

        if (!hexCommandApdu.substring(2, 4).equals(SELECT_INS)) {
            return hexStringToByteArray(INS_NOT_SUPPORTED);
        }

        if (hexCommandApdu.substring(10, 24).equals(HCE_AID)) {
            return hexStringToByteArray(AppPreferences.getMacAddressWithoutColons(AppPreferences.getMacAddress(this)));
        } else {
            return hexStringToByteArray(STATUS_FAILED);
        }
    }

    public void setResponseString(String macAddress) {
        this.responseString = macAddress;
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
    }

    public static byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must be non-null and have even length");
        }

        int len = hex.length();
        byte[] result = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);

            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character: " + hex.substring(i, i + 2));
            }

            result[i / 2] = (byte) ((high << 4) + low);
        }

        return result;
    }

    public static String toHex(byte[] byteArray) {
        StringBuilder result = new StringBuilder();

        for (byte b : byteArray) {
            int octet = b & 0xFF;
            int firstIndex = (octet & 0xF0) >>> 4;
            int secondIndex = octet & 0x0F;
            result.append(HEX_CHARS_ARRAY[firstIndex]);
            result.append(HEX_CHARS_ARRAY[secondIndex]);
        }

        return result.toString();
    }
}

