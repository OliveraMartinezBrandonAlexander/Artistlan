package com.example.artistlan.Theme;

import java.util.List;

public class ThemeSection {

    private final String title;
    private final String subtitle;
    private final List<ThemeItem> items;
    private boolean expanded;

    public ThemeSection(String title, String subtitle, List<ThemeItem> items, boolean expanded) {
        this.title = title;
        this.subtitle = subtitle;
        this.items = items;
        this.expanded = expanded;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<ThemeItem> getItems() {
        return items;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggle() {
        expanded = !expanded;
    }
}