package com.example.artistlan.Activitys;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.RenderMode;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

public class SplashActivity extends AppCompatActivity {

    private static final long ENTRY_DURATION_MS = 650L;
    private static final long VISIBLE_DURATION_MS = 2000L;
    private static final long FADE_DURATION_MS = 500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View root;
    private LottieAnimationView loading;
    private ThemeManager themeManager;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        themeManager = new ThemeManager(this);
        root = findViewById(R.id.SplashRoot);
        loading = findViewById(R.id.SplashLoading);

        applyThemeToSplash();
        prepareLoadingAnimation();
        playIntro();
    }

    private void applyThemeToSplash() {
        if (themeManager == null) {
            return;
        }

        if (root != null) {
            root.setBackgroundColor(themeManager.color(ThemeKeys.BG_BOTTOM));
        }

        if (loading != null) {
            int accentColor = themeManager.color(ThemeKeys.ACCENT_PRIMARY);
            loading.setRenderMode(RenderMode.AUTOMATIC);
            loading.addValueCallback(
                    new KeyPath("**"),
                    LottieProperty.COLOR_FILTER,
                    new LottieValueCallback<>(new PorterDuffColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP))
            );
        }
    }

    private void prepareLoadingAnimation() {
        if (loading == null) {
            return;
        }
        loading.setAlpha(0f);
        loading.setTranslationY(180f);
        loading.playAnimation();
    }

    private void playIntro() {
        if (loading != null) {
            loading.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(ENTRY_DURATION_MS)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        handler.postDelayed(this::fadeOutAndContinue, VISIBLE_DURATION_MS);
    }

    private void fadeOutAndContinue() {
        if (root == null) {
            openMainActivity();
            return;
        }

        root.animate()
                .alpha(0f)
                .setDuration(FADE_DURATION_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::openMainActivity)
                .start();
    }

    private void openMainActivity() {
        if (navigated) {
            return;
        }
        navigated = true;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (loading != null) {
            loading.cancelAnimation();
        }
    }
}
