package com.pmu.nfc_data_transfer_app.core.constants;

public final class GlobalConstants {
    public static final String MIME_TYPE = "application/vnd.com.pmu.nfc_data_transfer_app";
    public static final String HCE_AID = "A0000002471001";

    public static final String  TAG = "Host Card Emulator";
    public static final String STATUS_SUCCESS = "9000";
    public static final String STATUS_FAILED = "6F00";
    public static final String CLA_NOT_SUPPORTED = "6E00";
    public static final String INS_NOT_SUPPORTED = "6D00";
    public static final String SELECT_INS = "A4";
    public static final String DEFAULT_CLA = "00";
    public static final int MIN_APDU_LENGTH = 12;
}
