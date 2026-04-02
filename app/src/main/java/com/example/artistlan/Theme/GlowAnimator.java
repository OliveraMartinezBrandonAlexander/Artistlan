package com.example.artistlan.Theme;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class GlowAnimator {

    private GlowAnimator() {}

    public static ObjectAnimator create(View target, String property, float from, float to, long duration) {
        if (target == null) return null;
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, property, from, to);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    public static void start(ObjectAnimator... animators) {
        if (animators == null) return;
        for (ObjectAnimator a : animators) {
            if (a != null) a.start();
        }
    }

    public static void cancel(ObjectAnimator... animators) {
        if (animators == null) return;
        for (ObjectAnimator a : animators) {
            if (a != null) a.cancel();
        }
    }
}