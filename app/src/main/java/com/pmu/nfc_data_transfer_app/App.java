package com.pmu.nfc_data_transfer_app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;

public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        // Set Bulgarian locale
        Locale locale = new Locale("bg");
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        Context context = base.createConfigurationContext(config);

        super.attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
} 