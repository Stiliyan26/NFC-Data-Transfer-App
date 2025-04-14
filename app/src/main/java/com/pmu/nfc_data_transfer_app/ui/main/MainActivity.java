package com.pmu.nfc_data_transfer_app.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.animation.ObjectAnimator;
import android.view.animation.BounceInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.animation.AnimatorSet;
import android.util.Log;

import com.pmu.nfc_data_transfer_app.R;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Set Bulgarian locale
        Locale locale = new Locale("bg");
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        Context context = newBase.createConfigurationContext(config);
        
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set Bulgarian locale
        Locale locale = new Locale("bg");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference to the ImageView from the layout
        final ImageView bouncingImage = findViewById(R.id.activity_main_logo);

        // Check if the view was found to avoid NullPointerExceptions
        if (bouncingImage == null) {
            Log.e("MainActivity", "ImageView with ID 'activity_main_logo' not found in the layout.");
            return;
        }

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the main content area background to white
        View mainContent = findViewById(android.R.id.content);
        mainContent.setBackgroundColor(Color.WHITE);

        // Initialize buttons
        Button btnSend = findViewById(R.id.btnSend);
        Button btnReceive = findViewById(R.id.btnReceive);

        // Set up click listeners
        btnSend.setOnClickListener(v -> {
            Intent intent = new Intent(this, FileTransferActivity.class);
            intent.putExtra("mode", "send");
            startActivity(intent);
        });

        btnReceive.setOnClickListener(v -> {
            Intent intent = new Intent(this, FileTransferActivity.class);
            intent.putExtra("mode", "receive");
            startActivity(intent);
        });

        bouncingImage.setOnClickListener(v -> {
                // Create ObjectAnimators for scaling the view in X and Y directions
                ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(bouncingImage, "scaleX", 1.0f, 1.5f, 1.0f);
                ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(bouncingImage, "scaleY", 1.0f, 1.5f, 1.0f);

                // Set the duration for the animations
                scaleXAnimator.setDuration(600);
                scaleYAnimator.setDuration(600);

                // Create an AnimatorSet to play the scale animations together
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(scaleXAnimator, scaleYAnimator);

                // Set a BounceInterpolator for a bouncy feel
                animatorSet.setInterpolator(new BounceInterpolator());

                // Start the animation
                animatorSet.start();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            // Navigate to About page
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}