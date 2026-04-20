package com.example.artistlan.Theme;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public final class ThemeModuleStyler {

    private ThemeModuleStyler() {}

    public static void styleFragment(@NonNull Fragment fragment, View root) {
        if (root == null || fragment.getContext() == null) return;
        ThemeManager tm = new ThemeManager(fragment.requireContext());

        if (root.getBackground() != null) {
            root.getBackground().setColorFilter(tm.color(ThemeKeys.BG_MID), android.graphics.PorterDuff.Mode.SRC_ATOP);
        }

        applyRecursive(root, tm);
        root.setAlpha(0f);
        root.setTranslationY(18f);
        root.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private static void applyRecursive(View view, ThemeManager tm) {
        if (view instanceof TextView && !(view instanceof Button)) {
            ThemeApplier.applyTextSecondary((TextView) view, tm);
        }
        if (view instanceof EditText) {
            ThemeApplier.applyInput((EditText) view, tm);
        }
        if (view instanceof Button) {
            ThemeApplier.applyPrimaryButton(view, tm);
            ThemeApplier.animatePress(view);
        }
        if (view instanceof ImageButton) {
            ThemeApplier.animatePress(view);
        }
        if (view instanceof CardView) {
            ThemeApplier.applyCard((CardView) view, tm, ThemeKeys.ACCOUNT_GLASS_PANEL);
            ThemeApplier.animatePress(view);
        }
        if (view instanceof RecyclerView) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + 4, view.getPaddingRight(), view.getPaddingBottom() + 12);
            view.setClipToOutline(false);
        }
        if (view.getId() == android.R.id.title && view instanceof TextView) {
            ThemeApplier.applyTextPrimary((TextView) view, tm);
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyRecursive(vg.getChildAt(i), tm);
            }
        }
    }
}