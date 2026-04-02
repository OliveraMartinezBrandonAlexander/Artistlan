package com.example.artistlan.Theme;

import android.content.Context;

public class ThemeManager {

    private final ThemePrefsManager prefs;

    public ThemeManager(Context context) {
        prefs = new ThemePrefsManager(context);
    }

    public int color(String key) {
        return prefs.getResolvedColor(key);
    }

    public ThemePrefsManager prefs() {
        return prefs;
    }
}