package com.pmu.nfc_data_transfer_app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for managing application preferences including globally stored values
 */
public class AppPreferences {
    private static final String PREFERENCES_NAME = "NfcTransferPrefs";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_OTHER_DEVICE_MAC_ADDRESS = "other_device_mac_address";
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
     * Save other device MAC address to shared preferences
     */
    public static void saveOtherDeviceMacAddress(Context context, String macAddress) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_OTHER_DEVICE_MAC_ADDRESS, macAddress);
        editor.apply();
    }

    /**
     * Retrieve saved MAC address
     */
    public static String getMacAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MAC_ADDRESS, "00:00:00:00:00:02");
    }

    public static String getOtherDeviceMacAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_OTHER_DEVICE_MAC_ADDRESS, "04:B4:29:30:B5:0A"); // Default value if not set
    }

    /**
     * Format MAC address by removing colons
     */
    public static String getMacAddressWithoutColons(String macAddress) {
        if (macAddress == null) {
            return "";
        }

        return macAddress.replace(":", "");
    }

    /**
     * Format MAC address by adding colons between each pair of characters
     */
    public static String formatMacAddressWithColons(String macAddressWithoutColons) {
        if (macAddressWithoutColons == null) {
            return "";
        }

        if (macAddressWithoutColons.length() != 12) {
            return macAddressWithoutColons; // Return as is if not exactly 12 characters
        }

        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < macAddressWithoutColons.length(); i += 2) {

            if (i > 0) {
                formatted.append(":");
            }

            formatted.append(macAddressWithoutColons.substring(i, i + 2));
        }

        return formatted.toString();
    }

    /**
     * Check if MAC address has been saved
     */
    public static boolean hasMacAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String macAddress = prefs.getString(KEY_MAC_ADDRESS, "");
        return prefs.contains(KEY_MAC_ADDRESS) && !macAddress.isEmpty();
    }

    /**
     * Check if other device MAC address has been saved
     */
    public static boolean hasOtherDeviceMacAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String macAddress = prefs.getString(KEY_OTHER_DEVICE_MAC_ADDRESS, "");
        return prefs.contains(KEY_OTHER_DEVICE_MAC_ADDRESS) && !macAddress.isEmpty();
    }
}