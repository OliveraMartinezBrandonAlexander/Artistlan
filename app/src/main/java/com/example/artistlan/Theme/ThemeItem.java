package com.example.artistlan.Theme;

public class ThemeItem {

    private final String key;
    private final String title;
    private final String description;
    private final String section;
    private int color;

    public ThemeItem(String key, String title, String description, String section, int color) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.section = section;
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

    public String getSection() {
        return section;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}