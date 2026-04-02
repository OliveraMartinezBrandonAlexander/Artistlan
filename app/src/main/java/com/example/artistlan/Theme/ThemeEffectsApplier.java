package com.example.artistlan.Theme;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ThemeEffectsApplier {

    private ThemeEffectsApplier() {}

    public static void applyGlowIntensity(View glow, ThemeManager tm, String baseGlowKey) {
        if (glow == null || glow.getBackground() == null) return;

        int glowColor = tm.color(baseGlowKey);
        glow.getBackground().setColorFilter(glowColor, PorterDuff.Mode.SRC_ATOP);

        float alpha = alphaFromColor(tm.color(ThemeKeys.FX_GLOW_INTENSITY), 0.15f, 0.85f);
        glow.setAlpha(alpha);
    }

    public static void applyBarGloss(View target, ThemeManager tm) {
        if (target == null || target.getForeground() == null) return;
        target.getForeground().setColorFilter(tm.color(ThemeKeys.FX_BAR_GLOSS), PorterDuff.Mode.SRC_ATOP);
    }

    public static void applyTopLight(View lightView, ThemeManager tm) {
        if (lightView == null || lightView.getBackground() == null) return;
        lightView.getBackground().setColorFilter(tm.color(ThemeKeys.FX_TOP_LIGHT), PorterDuff.Mode.SRC_ATOP);
    }

    public static void applyPanelGlass(View panel, ThemeManager tm) {
        if (panel == null || panel.getBackground() == null) return;
        panel.getBackground().setColorFilter(tm.color(ThemeKeys.FX_GLASS_ALPHA), PorterDuff.Mode.SRC_ATOP);
    }

    public static void applyActiveBorder(View target, ThemeManager tm) {
        if (target == null || target.getBackground() == null) return;
        target.getBackground().setColorFilter(tm.color(ThemeKeys.FX_ACTIVE_BORDER), PorterDuff.Mode.SRC_ATOP);
    }

    public static float alphaFromColor(int color, float min, float max) {
        int alpha = (color >> 24) & 0xFF;
        float normalized = alpha / 255f;
        return min + ((max - min) * normalized);
    }
}