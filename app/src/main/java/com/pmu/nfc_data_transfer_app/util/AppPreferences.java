package com.pmu.nfc_data_transfer_app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing application preferences including globally stored values
 */
public class AppPreferences {
    private static final String PREFERENCES_NAME = "NfcTransferPrefs";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    /**
     * Save MAC address to shared preferences
     */
    public static void saveMacAddress(Context context, String macAddress) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_MAC_ADDRESS, macAddress);
        editor.apply();
    }

    /**
     * Retrieve saved MAC address
     */
    public static String getMacAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MAC_ADDRESS, "");
    }

    public static String getOtherDeviceMacAddress(Context context) {
        return "04:B4:29:30:B5:0A"; // TODO: Temporary, remove
    }

    /**
     * Check if MAC address has been saved
     */
    public static boolean hasMacAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String macAddress = prefs.getString(KEY_MAC_ADDRESS, "");
        return prefs.contains(KEY_MAC_ADDRESS) && !macAddress.isEmpty();
    }
}