package com.pmu.nfc_data_transfer_app.feature.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pmu.nfc_data_transfer_app.R;
import com.pmu.nfc_data_transfer_app.feature.about.AboutActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.FileReceiveActivity;
import com.pmu.nfc_data_transfer_app.feature.history.TransferHistoryActivity;
import com.pmu.nfc_data_transfer_app.feature.transfer.UploadFilesActivity;

public class MainActivity extends AppCompatActivity {

    private final String SEND = "send";
    private final String RECEIVE = "receive";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setupUI();
    }

    private void setupUI() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        Button btnSend = findViewById(R.id.btnSend);
        Button btnReceive = findViewById(R.id.btnReceive);
        Button btnHistory = findViewById(R.id.btnHistory);

        final ImageView logoImage = findViewById(R.id.logo_image);

        // Click event listeners
        btnSend.setOnClickListener(v -> navigateToFileTransfer(SEND, UploadFilesActivity.class));
        btnReceive.setOnClickListener(v -> navigateToFileTransfer(RECEIVE, FileReceiveActivity.class));
        btnHistory.setOnClickListener(v -> navigateToHistory());

        logoImage.setOnClickListener(v -> createNfcWaveEffect(logoImage));
    }

    private <T extends Activity> void navigateToFileTransfer(String mode, Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("mode", mode);
        startActivity(intent);
    }

    private void navigateToHistory() {
        Intent intent = new Intent(this, TransferHistoryActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true; // true to display the menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true; // true to indicate that the event was handled
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNfcWaveEffect(ImageView sourceView) {
        try {
            // Get the parent card view that contains the logo
            View parentCardView = (View) sourceView.getParent();
            if (parentCardView == null) return; // Safety check

            // Number of waves to create
            final int waveCount = 3;
            // Animation duration
            final int waveDuration = 3000; // 2 seconds per wave
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
}