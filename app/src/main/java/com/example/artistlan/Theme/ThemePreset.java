package com.example.artistlan.Theme;

import java.util.Map;

public class ThemePreset {

    private final String name;
    private final String description;
    private final Map<String, Integer> colors;

    public ThemePreset(String name, String description, Map<String, Integer> colors) {
        this.name = name;
        this.description = description;
        this.colors = colors;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getColors() {
        return colors;
    }
}