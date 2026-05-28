package com.example.artistlan.Theme;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ThemeApplier {

    private ThemeApplier() {}

    public static void applySystemBars(Activity activity, ThemeManager tm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(tm.color(ThemeKeys.MENU_TOPBAR));
            activity.getWindow().setNavigationBarColor(tm.color(ThemeKeys.MENU_BOTTOMBAR));
        }
    }

    public static void applyTextPrimary(TextView tv, ThemeManager tm) {
        if (tv != null) tv.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
    }

    public static void applyTextSecondary(TextView tv, ThemeManager tm) {
        if (tv != null) tv.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
    }

    public static void applyIcon(ImageView iv, ThemeManager tm, String key) {
        if (iv != null) {
            iv.setColorFilter(tm.color(key), PorterDuff.Mode.SRC_IN);
        }
    }

    public static void applyGlow(View glow, int color) {
        if (glow == null) return;
        if (glow.getBackground() != null) {
            glow.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void applyFragmentBackground(View root, ThemeManager tm, View primaryGlow, View tertiaryGlow, View secondaryGlow) {
        if (root != null) {
            GradientDrawable background = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            ColorUtils.setAlphaComponent(tm.color(ThemeKeys.BG_TOP), 255),
                            ColorUtils.setAlphaComponent(tm.color(ThemeKeys.BG_MID), 255),
                            ColorUtils.setAlphaComponent(tm.color(ThemeKeys.BG_BOTTOM), 255)
                    }
            );
            root.setBackground(background);
        }

        applyThemedBubble(primaryGlow, tm.color(ThemeKeys.GLOW_PRIMARY), 0.46f);
        applyThemedBubble(tertiaryGlow, tm.color(ThemeKeys.GLOW_TERTIARY), 0.24f);
        applyThemedBubble(secondaryGlow, tm.color(ThemeKeys.GLOW_SECONDARY), 0.34f);
    }

    private static void applyThemedBubble(View glow, int color, float maxAlpha) {
        if (glow == null) return;
        int center = ColorUtils.setAlphaComponent(color, Math.min(Color.alpha(color), 178));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawable.setGradientRadius(dp(glow, 170));
        drawable.setColors(new int[]{center, Color.TRANSPARENT});
        glow.setBackground(drawable);
        glow.setAlpha(Math.min(maxAlpha, Math.max(0.12f, Color.alpha(color) / 255f * maxAlpha)));
    }

    public static void applyPrimaryButton(View button, ThemeManager tm) {
        if (button == null || button.getBackground() == null) return;
        button.getBackground().setColorFilter(tm.color(ThemeKeys.BUTTON_PRIMARY_BG), PorterDuff.Mode.SRC_ATOP);
        if (button instanceof TextView) {
            ((TextView) button).setTextColor(tm.color(ThemeKeys.BUTTON_TEXT_DARK));
        }
    }

    public static void applySecondaryButton(View button, ThemeManager tm) {
        if (button == null || button.getBackground() == null) return;
        button.getBackground().setColorFilter(tm.color(ThemeKeys.BUTTON_SECONDARY_BG), PorterDuff.Mode.SRC_ATOP);
        if (button instanceof TextView) {
            ((TextView) button).setTextColor(tm.color(ThemeKeys.BUTTON_TEXT_LIGHT));
        }
    }

    public static void applyInput(EditText et, ThemeManager tm) {
        if (et == null) return;
        et.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        et.setHintTextColor(tm.color(ThemeKeys.INPUT_HINT));

        if (et.getBackground() instanceof GradientDrawable) {
            GradientDrawable bg = (GradientDrawable) et.getBackground().mutate();
            bg.setColor(tm.color(ThemeKeys.INPUT_BG));
            bg.setStroke(2, tm.color(ThemeKeys.INPUT_STROKE));
        } else if (et.getBackground() != null) {
            et.getBackground().setColorFilter(tm.color(ThemeKeys.INPUT_BG), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void applyTopBar(View topBar, ThemeManager tm) {
        if (topBar == null || topBar.getBackground() == null) return;
        topBar.getBackground().setColorFilter(tm.color(ThemeKeys.MENU_TOPBAR), PorterDuff.Mode.SRC_ATOP);
    }

    public static void applyBottomNav(BottomNavigationView bottomBar, ThemeManager tm) {
        if (bottomBar == null) return;
        if (bottomBar.getBackground() != null) {
            bottomBar.getBackground().setColorFilter(tm.color(ThemeKeys.MENU_BOTTOMBAR), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void applyDrawer(NavigationView navigationView, ThemeManager tm) {
        if (navigationView == null) return;
        if (navigationView.getBackground() != null) {
            navigationView.getBackground().setColorFilter(tm.color(ThemeKeys.MENU_DRAWER), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void applyCard(CardView card, ThemeManager tm, String fillKey) {
        if (card == null) return;
        card.setCardBackgroundColor(tm.color(fillKey));
    }

    public static void applyPreviewPanel(View panel, ThemeManager tm) {
        if (panel == null || panel.getBackground() == null) return;
        panel.getBackground().setColorFilter(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), PorterDuff.Mode.SRC_ATOP);
    }

    // ✅ MÉTODO FUERA (CORRECTO)
    public static void animatePress(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.985f).scaleY(0.985f).setDuration(90)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(130)
                            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    break;
            }
            return false;
        });
    }

    // ✅ MÉTODO FUERA (CORRECTO)
    public static void applyCardContainer(View view, ThemeManager tm) {
        if (view == null || view.getBackground() == null) return;
        view.getBackground().setColorFilter(
                tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                PorterDuff.Mode.SRC_ATOP
        );
        animatePress(view);
    }

    private static float dp(View view, int value) {
        return value * view.getResources().getDisplayMetrics().density;
    }
}
