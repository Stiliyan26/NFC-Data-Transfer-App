package com.pmu.nfc_data_transfer_app;

import android.app.Application;
import android.content.res.Configuration;
import java.util.Locale;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set Bulgarian as the default language
        Locale locale = new Locale("bg");
        Locale.setDefault(locale);
        
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
} 