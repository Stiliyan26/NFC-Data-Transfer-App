package com.pmu.nfc_data_transfer_app.ui.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pmu.nfc_data_transfer_app.R;

public class NfcAnimationHelper {
    private final Activity activity;
    private final int waveDuration = 1000;
    private final int bounceDuration = 800;
    private final int waveCount = 1;

    public NfcAnimationHelper(Activity activity) {
        this.activity = activity;
    }

    public void createNfcWaveEffect(ImageView sourceView) {
        try {
            View parentCardView = (View) sourceView.getParent();

            if (parentCardView == null) return;

            ViewGroup rootLayout = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootLayout == null) return; // Safety check

            sourceView.setClickable(true);
            sourceView.setBackground(null);

            final int cardWidth = parentCardView.getWidth();
            final int cardHeight = parentCardView.getHeight();
            if (cardWidth <= 0 || cardHeight <= 0) return;

            int[] cardLocation = new int[2];
            parentCardView.getLocationOnScreen(cardLocation);

            final int centerX = cardLocation[0] + cardWidth / 2;
            final int centerY = cardLocation[1] + cardHeight / 2;

            animateImageBounce(sourceView, parentCardView);

            for (int i = 0; i < waveCount; i++) {
                final int delay = i * (waveDuration / 3);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    animateImageBounce(sourceView, parentCardView);
                }, delay);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    createWave(rootLayout, centerX, centerY, cardWidth, cardHeight);
                }, delay);
            }
        } catch (Exception e) {
            Log.e("NfcAnimationHelper", "Animation error: " + e.getMessage());
        }
    }

    private void createWave(ViewGroup rootLayout, int centerX, int centerY, int cardWidth, int cardHeight) {
        try {
            final View waveView = new View(activity);
            waveView.setBackgroundResource(R.drawable.nfc_blue_wave_circle);

            rootLayout.addView(waveView, new ViewGroup.LayoutParams(cardWidth, cardHeight));

            waveView.setX(centerX - cardWidth / 2);
            waveView.setY(centerY - cardHeight / 2);
            waveView.setZ(5f);
            waveView.setAlpha(0.7f);

            ObjectAnimator scaleX = ObjectAnimator.ofFloat(waveView, "scaleX", 1.0f, 3.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(waveView, "scaleY", 1.0f, 3.0f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(waveView, "alpha", 0.7f, 0.0f);

            AnimatorSet waveAnim = new AnimatorSet();
            waveAnim.playTogether(scaleX, scaleY, fadeOut);
            waveAnim.setDuration(waveDuration);

            waveAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    try {
                        rootLayout.removeView(waveView);
                    } catch (Exception e) {
                        Log.e("NfcAnimationHelper", "Error removing wave: " + e.getMessage());
                    }
                }
            });

            waveAnim.start();
        } catch (Exception e) {
            Log.e("NfcAnimationHelper", "Wave creation error: " + e.getMessage());
        }
    }

    private void animateImageBounce(ImageView sourceView, View parentCardView) {
        try {
            ObjectAnimator bounceX = ObjectAnimator.ofFloat(sourceView, "scaleX",
                    1.0f, 1.15f, 0.95f, 1.05f, 1.0f);
            ObjectAnimator bounceY = ObjectAnimator.ofFloat(sourceView, "scaleY",
                    1.0f, 1.15f, 0.95f, 1.05f, 1.0f);

            ObjectAnimator wiggle = ObjectAnimator.ofFloat(sourceView, "rotation",
                    0f, 1f, -1f, 0.5f, 0f);

            AnimatorSet bounce = new AnimatorSet();

            bounce.playTogether(bounceX, bounceY, wiggle);
            bounce.setDuration(bounceDuration);
            bounce.setInterpolator(new android.view.animation.OvershootInterpolator(0.8f));

            ObjectAnimator pulseX = ObjectAnimator.ofFloat(parentCardView, "scaleX", 1.0f, 1.02f, 1.0f);
            ObjectAnimator pulseY = ObjectAnimator.ofFloat(parentCardView, "scaleY", 1.0f, 1.02f, 1.0f);
            AnimatorSet pulse = new AnimatorSet();
            pulse.playTogether(pulseX, pulseY);
            pulse.setDuration(bounceDuration);

            bounce.start();
            pulse.start();
        } catch (Exception e) {
            Log.e("NfcAnimationHelper", "Bounce animation error: " + e.getMessage());
        }
    }
}
