package com.example.artistlan.Theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
public class ThemePrefsManager {

    private static final String PREFS_NAME = "artistlan_theme_prefs";

    private final SharedPreferences prefs;

    public ThemePrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getColor(String key, int defaultColor) {
        return prefs.getInt(key, defaultColor);
    }

    public void setColor(String key, int color) {
        prefs.edit().putInt(key, color).apply();
    }

    public void resetAll() {
        prefs.edit().clear().apply();
    }

    public void setColors(Map<String, Integer> colors) {
        if (colors == null || colors.isEmpty()) return;
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                editor.putInt(entry.getKey(), entry.getValue());
            }
        }
        editor.apply();
    }

    public boolean hasAnyCustomValue(Set<String> keys) {
        if (keys == null || keys.isEmpty()) return false;
        for (String key : keys) {
            if (prefs.contains(key)) return true;
        }
        return false;
    }

    public int getResolvedColor(String key) {
        Integer color = getDefaults().get(key);
        return prefs.getInt(key, color != null ? color : Color.WHITE);
    }

    public Map<String, Integer> getDefaults() {
        Map<String, Integer> d = new LinkedHashMap<>();

        d.put(ThemeKeys.BG_TOP, Color.parseColor("#12091D"));
        d.put(ThemeKeys.BG_MID, Color.parseColor("#1E1030"));
        d.put(ThemeKeys.BG_BOTTOM, Color.parseColor("#0B0612"));

        d.put(ThemeKeys.TEXT_PRIMARY, Color.parseColor("#FFFFFFFF"));
        d.put(ThemeKeys.TEXT_SECONDARY, Color.parseColor("#D0D4E6"));

        d.put(ThemeKeys.ACCOUNT_GLASS_PANEL, Color.parseColor("#2BFFFFFF"));
        d.put(ThemeKeys.ACCOUNT_GLASS_STROKE, Color.parseColor("#33FFFFFF"));
        d.put(ThemeKeys.ACCOUNT_DIVIDER, Color.parseColor("#80B28DFF"));
        d.put(ThemeKeys.ACCOUNT_SHIMMER, Color.parseColor("#F0FFFFFF"));

        d.put(ThemeKeys.MENU_TOPBAR, Color.parseColor("#2E1B47"));
        d.put(ThemeKeys.MENU_TOPBAR_2, Color.parseColor("#40215E"));
        d.put(ThemeKeys.MENU_BOTTOMBAR, Color.parseColor("#26163D"));
        d.put(ThemeKeys.MENU_BOTTOMBAR_2, Color.parseColor("#351D52"));
        d.put(ThemeKeys.MENU_DRAWER, Color.parseColor("#EE101722"));
        d.put(ThemeKeys.MENU_DRAWER_HEADER, Color.parseColor("#E61B2432"));
        d.put(ThemeKeys.MENU_ITEM_ACTIVE, Color.parseColor("#40A97CFF"));
        d.put(ThemeKeys.MENU_ITEM_ACTIVE_STROKE, Color.parseColor("#88A97CFF"));
        d.put(ThemeKeys.MENU_BADGE, Color.parseColor("#FF5CA8"));

        d.put(ThemeKeys.ACCENT_PRIMARY, Color.parseColor("#B28DFF"));
        d.put(ThemeKeys.ACCENT_PRIMARY_LIGHT, Color.parseColor("#D6C2FF"));
        d.put(ThemeKeys.ACCENT_SECONDARY, Color.parseColor("#FF85D0"));
        d.put(ThemeKeys.ACCENT_SECONDARY_LIGHT, Color.parseColor("#FFC4E9"));

        d.put(ThemeKeys.ICON_DEFAULT, Color.parseColor("#D7C8F7"));
        d.put(ThemeKeys.ICON_ACTIVE, Color.parseColor("#FFFFFFFF"));
        d.put(ThemeKeys.ICON_TOPBAR, Color.parseColor("#FFFFFFFF"));
        d.put(ThemeKeys.ICON_DRAWER, Color.parseColor("#E6D6FF"));
        d.put(ThemeKeys.ICON_BOTTOM, Color.parseColor("#D7C8F7"));

        d.put(ThemeKeys.BUTTON_PRIMARY_BG, Color.parseColor("#B28DFF"));
        d.put(ThemeKeys.BUTTON_SECONDARY_BG, Color.parseColor("#FF85D0"));
        d.put(ThemeKeys.BUTTON_TEXT_DARK, Color.parseColor("#1A1026"));
        d.put(ThemeKeys.BUTTON_TEXT_LIGHT, Color.parseColor("#2A1020"));

        d.put(ThemeKeys.INPUT_BG, Color.parseColor("#26FFFFFF"));
        d.put(ThemeKeys.INPUT_STROKE, Color.parseColor("#33FFFFFF"));
        d.put(ThemeKeys.INPUT_HINT, Color.parseColor("#9FB1C7"));

        d.put(ThemeKeys.DIALOG_BG, Color.parseColor("#2A2238"));
        d.put(ThemeKeys.DIALOG_TEXT, Color.parseColor("#FFFFFFFF"));
        d.put(ThemeKeys.OVERLAY_BG, Color.parseColor("#73000000"));

        d.put(ThemeKeys.GLOW_PRIMARY, Color.parseColor("#7A7C3AED"));
        d.put(ThemeKeys.GLOW_SECONDARY, Color.parseColor("#66FF6FD8"));
        d.put(ThemeKeys.GLOW_TERTIARY, Color.parseColor("#4D6DA8FF"));
        d.put(ThemeKeys.GLOW_DRAWER_PRIMARY, Color.parseColor("#8E7C3AED"));
        d.put(ThemeKeys.GLOW_DRAWER_SECONDARY, Color.parseColor("#66FF6FD8"));

        d.put(ThemeKeys.FX_GLOW_INTENSITY, Color.parseColor("#66FFFFFF"));
        d.put(ThemeKeys.FX_BAR_GLOSS, Color.parseColor("#26FFFFFF"));
        d.put(ThemeKeys.FX_PANEL_SHADOW, Color.parseColor("#55000000"));
        d.put(ThemeKeys.FX_GLASS_ALPHA, Color.parseColor("#33FFFFFF"));
        d.put(ThemeKeys.FX_ACTIVE_BORDER, Color.parseColor("#96B18CFF"));
        d.put(ThemeKeys.FX_TOP_LIGHT, Color.parseColor("#1AFFFFFF"));
        return d;
    }
}