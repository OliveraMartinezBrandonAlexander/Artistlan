package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.example.artistlan.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View btnInicioSesion, btnCrearCuenta;
    private View glowTopLeft, glowTopRight, glowBottomRight, glowBottomLeft;
    private View contentContainer, dividerShimmer;
    private TextView titulomain, texto1, texto2;

    private LottieAnimationView lottieLogin, lottieCrear;

    private final Handler pulseHandler = new Handler(Looper.getMainLooper());
    private boolean animateLogin = true;

    private ObjectAnimator glowTopLeftX, glowTopLeftY, glowTopLeftAlpha, glowTopLeftScaleX, glowTopLeftScaleY;
    private ObjectAnimator glowTopRightX, glowTopRightY, glowTopRightAlpha, glowTopRightScaleX, glowTopRightScaleY;
    private ObjectAnimator glowBottomRightX, glowBottomRightY, glowBottomRightAlpha, glowBottomRightScaleX, glowBottomRightScaleY;
    private ObjectAnimator glowBottomLeftX, glowBottomLeftY, glowBottomLeftAlpha, glowBottomLeftScaleX, glowBottomLeftScaleY;

    private ObjectAnimator dividerShimmerAnim;

    private final Runnable pulseRunnable = new Runnable() {
        @Override
        public void run() {
            if (animateLogin) {
                pulseBubble(btnInicioSesion);
            } else {
                pulseBubble(btnCrearCuenta);
            }

            animateLogin = !animateLogin;
            pulseHandler.postDelayed(this, 2200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnInicioSesion = findViewById(R.id.btnInicioSesion);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);

        glowTopLeft = findViewById(R.id.glowTopLeft);
        glowTopRight = findViewById(R.id.glowTopRight);
        glowBottomRight = findViewById(R.id.glowBottomRight);
        glowBottomLeft = findViewById(R.id.glowBottomLeft);

        contentContainer = findViewById(R.id.contentContainer);
        dividerShimmer = findViewById(R.id.dividerShimmer);

        titulomain = findViewById(R.id.titulomain);
        texto1 = findViewById(R.id.texto1);
        texto2 = findViewById(R.id.texto2);

        lottieLogin = findViewById(R.id.lottieLogin);
        lottieCrear = findViewById(R.id.lottieCrear);

        tintLottieWhite(lottieLogin);
        tintLottieWhite(lottieCrear);

        btnInicioSesion.setOnClickListener(this);
        btnCrearCuenta.setOnClickListener(this);

        setupPressAnimation(btnInicioSesion);
        setupPressAnimation(btnCrearCuenta);

        prepareIntroState();
        startGlowAnimations();
        startIntroAnimation();
    }

    private void tintLottieWhite(LottieAnimationView lottieView) {
        if (lottieView == null) return;

        lottieView.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                new LottieValueCallback<>(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP))
        );
    }

    private void prepareIntroState() {
        contentContainer.setAlpha(0f);
        contentContainer.setTranslationY(42f);

        titulomain.setAlpha(0f);
        titulomain.setScaleX(0.92f);
        titulomain.setScaleY(0.92f);

        texto1.setAlpha(0f);
        texto2.setAlpha(0f);

        btnInicioSesion.setAlpha(0f);
        btnCrearCuenta.setAlpha(0f);

        texto1.setTranslationY(18f);
        texto2.setTranslationY(18f);
        btnInicioSesion.setTranslationY(28f);
        btnCrearCuenta.setTranslationY(28f);
    }

    private void startIntroAnimation() {
        contentContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(650)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        titulomain.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        texto1.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(180)
                .setDuration(480)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        btnInicioSesion.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(300)
                .setDuration(520)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    startButtonPulseLoop();
                    startDividerShimmer();
                })
                .start();

        texto2.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(420)
                .setDuration(450)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        btnCrearCuenta.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(540)
                .setDuration(540)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void startButtonPulseLoop() {
        pulseHandler.removeCallbacks(pulseRunnable);
        pulseHandler.postDelayed(pulseRunnable, 1400);
    }

    private void pulseBubble(View view) {
        view.animate()
                .scaleX(1.06f)
                .scaleY(1.06f)
                .setDuration(260)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(260)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start())
                .start();
    }

    private void setupPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .setDuration(120)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start();
                    break;
            }
            return false;
        });
    }

    private void startGlowAnimations() {
        cancelGlowAnimationsOnly();

        glowTopLeftX = createInfiniteAnimator(glowTopLeft, "translationX", -120f, -10f, 5200);
        glowTopLeftY = createInfiniteAnimator(glowTopLeft, "translationY", -70f, 35f, 6100);
        glowTopLeftAlpha = createInfiniteAnimator(glowTopLeft, "alpha", 0.30f, 0.68f, 2600);
        glowTopLeftScaleX = createInfiniteAnimator(glowTopLeft, "scaleX", 0.88f, 1.15f, 4300);
        glowTopLeftScaleY = createInfiniteAnimator(glowTopLeft, "scaleY", 0.88f, 1.12f, 4700);

        glowTopRightX = createInfiniteAnimator(glowTopRight, "translationX", 95f, -35f, 5000);
        glowTopRightY = createInfiniteAnimator(glowTopRight, "translationY", 140f, 40f, 5600);
        glowTopRightAlpha = createInfiniteAnimator(glowTopRight, "alpha", 0.10f, 0.30f, 2400);
        glowTopRightScaleX = createInfiniteAnimator(glowTopRight, "scaleX", 0.92f, 1.20f, 3900);
        glowTopRightScaleY = createInfiniteAnimator(glowTopRight, "scaleY", 0.92f, 1.18f, 4300);

        glowBottomRightX = createInfiniteAnimator(glowBottomRight, "translationX", 120f, -8f, 5700);
        glowBottomRightY = createInfiniteAnimator(glowBottomRight, "translationY", 120f, -12f, 6500);
        glowBottomRightAlpha = createInfiniteAnimator(glowBottomRight, "alpha", 0.28f, 0.62f, 2900);
        glowBottomRightScaleX = createInfiniteAnimator(glowBottomRight, "scaleX", 0.90f, 1.18f, 4500);
        glowBottomRightScaleY = createInfiniteAnimator(glowBottomRight, "scaleY", 0.90f, 1.16f, 5000);

        glowBottomLeftX = createInfiniteAnimator(glowBottomLeft, "translationX", -80f, 45f, 5400);
        glowBottomLeftY = createInfiniteAnimator(glowBottomLeft, "translationY", 95f, -18f, 6000);
        glowBottomLeftAlpha = createInfiniteAnimator(glowBottomLeft, "alpha", 0.08f, 0.24f, 2500);
        glowBottomLeftScaleX = createInfiniteAnimator(glowBottomLeft, "scaleX", 0.85f, 1.22f, 4100);
        glowBottomLeftScaleY = createInfiniteAnimator(glowBottomLeft, "scaleY", 0.85f, 1.18f, 4600);

        startGlowSet();
    }

    private ObjectAnimator createInfiniteAnimator(View view, String property, float from, float to, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, property, from, to);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private void startGlowSet() {
        startAnimator(glowTopLeftX);
        startAnimator(glowTopLeftY);
        startAnimator(glowTopLeftAlpha);
        startAnimator(glowTopLeftScaleX);
        startAnimator(glowTopLeftScaleY);

        startAnimator(glowTopRightX);
        startAnimator(glowTopRightY);
        startAnimator(glowTopRightAlpha);
        startAnimator(glowTopRightScaleX);
        startAnimator(glowTopRightScaleY);

        startAnimator(glowBottomRightX);
        startAnimator(glowBottomRightY);
        startAnimator(glowBottomRightAlpha);
        startAnimator(glowBottomRightScaleX);
        startAnimator(glowBottomRightScaleY);

        startAnimator(glowBottomLeftX);
        startAnimator(glowBottomLeftY);
        startAnimator(glowBottomLeftAlpha);
        startAnimator(glowBottomLeftScaleX);
        startAnimator(glowBottomLeftScaleY);
    }

    private void startAnimator(ObjectAnimator animator) {
        if (animator != null) {
            animator.start();
        }
    }

    private void startDividerShimmer() {
        if (dividerShimmer == null) return;

        dividerShimmer.post(() -> {
            cancelAnimator(dividerShimmerAnim);

            float distance = 250f;
            dividerShimmerAnim = ObjectAnimator.ofFloat(dividerShimmer, "translationX", -distance, distance);
            dividerShimmerAnim.setDuration(1800);
            dividerShimmerAnim.setRepeatCount(ValueAnimator.INFINITE);
            dividerShimmerAnim.setRepeatMode(ValueAnimator.RESTART);
            dividerShimmerAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            dividerShimmerAnim.start();
        });
    }

    @Override
    public void onClick(View v) {
        int idClick = v.getId();
        Intent irActivity = null;

        if (idClick == R.id.btnInicioSesion) {
            irActivity = new Intent(this, ActIniciarSesion.class);
        } else if (idClick == R.id.btnCrearCuenta) {
            irActivity = new Intent(this, ActCrearCuenta.class);
        }

        if (irActivity != null) {
            startActivity(irActivity);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pulseHandler.removeCallbacks(pulseRunnable);
        cancelAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGlowAnimations();
        startButtonPulseLoop();
        startDividerShimmer();

        if (lottieLogin != null && !lottieLogin.isAnimating()) {
            lottieLogin.playAnimation();
        }
        if (lottieCrear != null && !lottieCrear.isAnimating()) {
            lottieCrear.playAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pulseHandler.removeCallbacks(pulseRunnable);
        cancelAnimations();
    }

    private void cancelAnimations() {
        cancelGlowAnimationsOnly();
        cancelAnimator(dividerShimmerAnim);

        if (lottieLogin != null) lottieLogin.cancelAnimation();
        if (lottieCrear != null) lottieCrear.cancelAnimation();
    }

    private void cancelGlowAnimationsOnly() {
        cancelAnimator(glowTopLeftX);
        cancelAnimator(glowTopLeftY);
        cancelAnimator(glowTopLeftAlpha);
        cancelAnimator(glowTopLeftScaleX);
        cancelAnimator(glowTopLeftScaleY);

        cancelAnimator(glowTopRightX);
        cancelAnimator(glowTopRightY);
        cancelAnimator(glowTopRightAlpha);
        cancelAnimator(glowTopRightScaleX);
        cancelAnimator(glowTopRightScaleY);

        cancelAnimator(glowBottomRightX);
        cancelAnimator(glowBottomRightY);
        cancelAnimator(glowBottomRightAlpha);
        cancelAnimator(glowBottomRightScaleX);
        cancelAnimator(glowBottomRightScaleY);

        cancelAnimator(glowBottomLeftX);
        cancelAnimator(glowBottomLeftY);
        cancelAnimator(glowBottomLeftAlpha);
        cancelAnimator(glowBottomLeftScaleX);
        cancelAnimator(glowBottomLeftScaleY);
    }

    private void cancelAnimator(ObjectAnimator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }
}