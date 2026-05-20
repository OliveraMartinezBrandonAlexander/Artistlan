package com.example.artistlan.utils;

import android.graphics.PorterDuff;
import android.graphics.Color;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.artistlan.R;

public final class LikeUiHelper {

    private LikeUiHelper() {}

    public static void bind(
            @NonNull ImageButton button,
            @Nullable TextView counter,
            boolean liked,
            int likesCount,
            int activeColor,
            int inactiveColor
    ) {
        button.animate().cancel();
        button.setScaleX(1f);
        button.setScaleY(1f);
        button.setAlpha(1f);
        button.setImageResource(liked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
        button.setColorFilter(liked ? activeColor : inactiveColor, PorterDuff.Mode.SRC_IN);

        if (counter != null) {
            counter.setText(String.valueOf(Math.max(0, likesCount)));
        }
    }

    public static void animateChange(
            @NonNull ImageButton button,
            boolean liked,
            int activeColor,
            int inactiveColor
    ) {
        button.animate().cancel();
        button.setImageResource(liked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
        button.setColorFilter(liked ? activeColor : inactiveColor, PorterDuff.Mode.SRC_IN);
        button.setAlpha(liked ? 0.86f : 0.92f);
        button.setScaleX(liked ? 0.78f : 1.12f);
        button.setScaleY(liked ? 0.78f : 1.12f);

        button.animate()
                .alpha(1f)
                .scaleX(liked ? 1.22f : 0.96f)
                .scaleY(liked ? 1.22f : 0.96f)
                .setDuration(liked ? 135 : 90)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }

    public static void animatePress(@NonNull View view) {
        view.animate().cancel();
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(70)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                .start())
                .start();
    }

    public static void animateInstagramHeart(@Nullable ImageView heart) {
        animateInstagramHeart(heart, Color.WHITE);
    }

    public static void animateInstagramHeart(@Nullable ImageView heart, int color) {
        if (heart == null) {
            return;
        }

        heart.animate().cancel();
        heart.clearAnimation();
        heart.setImageResource(R.drawable.ic_like_filled);
        heart.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        heart.setVisibility(View.VISIBLE);
        heart.setAlpha(0.95f);
        heart.setScaleX(0.2f);
        heart.setScaleY(0.2f);

        heart.animate()
                .setStartDelay(0)
                .scaleX(1.18f)
                .scaleY(1.18f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> heart.animate()
                        .setStartDelay(0)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90)
                        .withEndAction(() -> heart.animate()
                                .alpha(0f)
                                .scaleX(1.32f)
                                .scaleY(1.32f)
                                .setStartDelay(260)
                                .setDuration(260)
                                .withEndAction(() -> {
                                    heart.setVisibility(View.GONE);
                                    heart.setAlpha(0f);
                                    heart.setScaleX(1f);
                                    heart.setScaleY(1f);
                                    heart.animate().setStartDelay(0);
                                })
                                .start())
                        .start())
                .start();
    }
}
