package com.example.artistlan.Theme;

public class ThemeColorRole {

    private final String key;
    private final String title;
    private final String description;
    private int color;

    public ThemeColorRole(String key, String title, String description, int color) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}