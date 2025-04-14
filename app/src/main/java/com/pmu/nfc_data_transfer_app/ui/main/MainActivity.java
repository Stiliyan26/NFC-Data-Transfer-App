package com.pmu.nfc_data_transfer_app.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
            // Create NFC wave effect without moving content
            createNfcWaveEffect(bouncingImage);
        });
    }
    private void createNfcWaveEffect(ImageView sourceView) {
        try {
            // Get the parent card view that contains the logo
            View parentCardView = (View) sourceView.getParent();
            if (parentCardView == null) return; // Safety check

            // Number of waves to create
            final int waveCount = 3;
            // Animation duration
            final int waveDuration = 2000; // 2 seconds per wave
            // Bounce duration (slower)
            final int bounceDuration = 800; // 800ms for a slower bounce

            // Use a safer approach to find the root layout
            ViewGroup rootLayout = (ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootLayout == null) return; // Safety check

            // Disable default touch feedback
            sourceView.setClickable(true);
            sourceView.setBackground(null);

            // Get card dimensions and location safely
            final int cardWidth = parentCardView.getWidth();
            final int cardHeight = parentCardView.getHeight();
            if (cardWidth <= 0 || cardHeight <= 0) return; // Safety check

            int[] cardLocation = new int[2];
            parentCardView.getLocationOnScreen(cardLocation);

            // Calculate center coordinates
            final int centerX = cardLocation[0] + cardWidth / 2;
            final int centerY = cardLocation[1] + cardHeight / 2;

            // Initial bounce animation
            animateImageBounce(sourceView, parentCardView, bounceDuration);

            // Create waves one by one with delay
            for (int i = 0; i < waveCount; i++) {
                final int waveIndex = i;
                final int delay = i * (waveDuration / 3);

                // Create a bounce animation for each wave
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Create another bounce with each wave
                    animateImageBounce(sourceView, parentCardView, bounceDuration);
                }, delay);

                // Create the wave
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        // Create wave view
                        final View waveView = new View(MainActivity.this);
                        waveView.setBackgroundResource(R.drawable.nfc_blue_wave_circle);

                        // Add to root layout
                        rootLayout.addView(waveView, new ViewGroup.LayoutParams(
                                cardWidth, cardHeight));

                        // Position wave
                        waveView.setX(centerX - cardWidth / 2);
                        waveView.setY(centerY - cardHeight / 2);
                        waveView.setZ(5f);
                        waveView.setAlpha(0.7f);

                        // Create animations
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(waveView, "scaleX", 1.0f, 3.0f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(waveView, "scaleY", 1.0f, 3.0f);
                        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(waveView, "alpha", 0.7f, 0.0f);

                        AnimatorSet waveAnim = new AnimatorSet();
                        waveAnim.playTogether(scaleX, scaleY, fadeOut);
                        waveAnim.setDuration(waveDuration);

                        // Remove wave when done
                        waveAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                try {
                                    rootLayout.removeView(waveView);
                                } catch (Exception e) {
                                    Log.e("MainActivity", "Error removing wave: " + e.getMessage());
                                }
                            }
                        });

                        // Start animation
                        waveAnim.start();
                    } catch (Exception e) {
                        Log.e("MainActivity", "Wave creation error: " + e.getMessage());
                    }
                }, delay);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Animation error: " + e.getMessage());
            // Don't crash the app if animation fails
        }
    }

    // Helper method to create a bounce animation
    private void animateImageBounce(ImageView sourceView, View parentCardView, int duration) {
        try {
            // Create a slower, more subtle bounce animation
            ObjectAnimator bounceX = ObjectAnimator.ofFloat(sourceView, "scaleX",
                    1.0f, 1.15f, 0.95f, 1.05f, 1.0f);
            ObjectAnimator bounceY = ObjectAnimator.ofFloat(sourceView, "scaleY",
                    1.0f, 1.15f, 0.95f, 1.05f, 1.0f);

            // Very subtle rotation for a gentler effect
            ObjectAnimator wiggle = ObjectAnimator.ofFloat(sourceView, "rotation",
                    0f, 1f, -1f, 0.5f, 0f);

            // Set up the animation set for the bounce
            AnimatorSet bounce = new AnimatorSet();
            bounce.playTogether(bounceX, bounceY, wiggle);
            bounce.setDuration(duration); // Slower animation
            bounce.setInterpolator(new android.view.animation.OvershootInterpolator(0.8f)); // Less dramatic overshoot

            // Very subtle pulse for the card
            ObjectAnimator pulseX = ObjectAnimator.ofFloat(parentCardView, "scaleX", 1.0f, 1.02f, 1.0f);
            ObjectAnimator pulseY = ObjectAnimator.ofFloat(parentCardView, "scaleY", 1.0f, 1.02f, 1.0f);
            AnimatorSet pulse = new AnimatorSet();
            pulse.playTogether(pulseX, pulseY);
            pulse.setDuration(duration);

            // Start both animations
            bounce.start();
            pulse.start();
        } catch (Exception e) {
            Log.e("MainActivity", "Bounce animation error: " + e.getMessage());
        }
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