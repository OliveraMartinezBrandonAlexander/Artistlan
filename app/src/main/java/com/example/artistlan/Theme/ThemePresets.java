package com.example.artistlan.Theme;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ThemePresets {

    private ThemePresets() {}

    public static List<ThemePreset> build() {
        List<ThemePreset> presets = new ArrayList<>();

        // MODO CLARO: Más limpio y con acentos eléctricos
        presets.add(preset("Candy White", "Blanco puro con acentos vibrantes",
                "#FFFFFF", "#F0F3FF", "#E0E7FF", "#121212", "#4A5568", "#6366F1", "#EC4899"));

        // MODO OSCURO: Negro profundo con neones para que "salten" a la vista
        presets.add(preset("Cyber Night", "Negro profundo y neón eléctrico",
                "#050505", "#0F0F1A", "#050505", "#FFFFFF", "#A0AEC0", "#00F2FF", "#BC13FE"));

        // MODO AZUL: De azul marino a cian brillante
        presets.add(preset("Ocean Pop", "Azul eléctrico y espuma de mar",
                "#010B1A", "#021B3A", "#010B1A", "#E0F2FE", "#7DD3FC", "#38BDF8", "#34D399"));

        // FIESTA: Colores saturados al máximo (Estilo Retrowave)
        presets.add(preset("Super Fiesta", "Explosión de colores saturados",
                "#1A0033", "#2D0059", "#1A0033", "#FFFFFF", "#FFD6FF", "#FF007A", "#00FFD1"));

        // MORADO: Estilo galaxia vibrante
        presets.add(preset("Ultra Violet", "Morados intensos y eléctricos",
                "#120024", "#240046", "#120024", "#F5F3FF", "#C4B5FD", "#9D174D", "#A855F7"));

        // ROSA: Muy alegre y femenino (Estilo Bubblegum)
        presets.add(preset("Pink Punch", "Rosa intenso y energía",
                "#1F0010", "#3D0021", "#1F0010", "#FFF1F2", "#FDA4AF", "#FB7185", "#F472B6"));

        // VERDE: Neon Mint
        presets.add(preset("Electric Mint", "Verde neón sobre fondo oscuro",
                "#00120B", "#002416", "#00120B", "#F0FDF4", "#86EFAC", "#22C55E", "#00FF9D"));

        // DORADO: Más brillante, tipo "Sol"
        presets.add(preset("Golden Sun", "Oro brillante y cálido",
                "#1A0F00", "#2E1B00", "#1A0F00", "#FFFBEB", "#FDE68A", "#F59E0B", "#FACC15"));

        // NARANJA: Sunset vibrante
        presets.add(preset("Sunset Blast", "Naranja y rojo fuego",
                "#1C0900", "#361100", "#1C0900", "#FFF7ED", "#FFEDD5", "#F97316", "#EF4444"));

        // PREMIUM: El original pero con mucho más contraste y saturación
        presets.add(preset("Artistlan Vivid", "Nuestra marca con esteroides",
                "#0A001A", "#150035", "#0A001A", "#FFFFFF", "#E0E7FF", "#8B5CF6", "#F43F5E"));

        return presets;
    }

    private static ThemePreset preset(String name, String description, String bgTop, String bgMid, String bgBottom,
                                      String textPrimary, String textSecondary, String accentPrimary, String accentSecondary) {
        Map<String, Integer> c = new LinkedHashMap<>();
        c.put(ThemeKeys.BG_TOP, Color.parseColor(bgTop));
        c.put(ThemeKeys.BG_MID, Color.parseColor(bgMid));
        c.put(ThemeKeys.BG_BOTTOM, Color.parseColor(bgBottom));
        c.put(ThemeKeys.TEXT_PRIMARY, Color.parseColor(textPrimary));
        c.put(ThemeKeys.TEXT_SECONDARY, Color.parseColor(textSecondary));

        c.put(ThemeKeys.ACCENT_PRIMARY, Color.parseColor(accentPrimary));
        c.put(ThemeKeys.ACCENT_PRIMARY_LIGHT, shiftAlpha(Color.parseColor(accentPrimary), 210));
        c.put(ThemeKeys.ACCENT_SECONDARY, Color.parseColor(accentSecondary));
        c.put(ThemeKeys.ACCENT_SECONDARY_LIGHT, shiftAlpha(Color.parseColor(accentSecondary), 220));

        // Ajuste de barras para que no sean tan oscuras y se vea el color base
        c.put(ThemeKeys.MENU_TOPBAR, darken(Color.parseColor(bgMid), 0.92f));
        c.put(ThemeKeys.MENU_TOPBAR_2, darken(Color.parseColor(bgMid), 0.85f));
        c.put(ThemeKeys.MENU_BOTTOMBAR, darken(Color.parseColor(bgBottom), 0.90f));
        c.put(ThemeKeys.MENU_BOTTOMBAR_2, darken(Color.parseColor(bgBottom), 0.80f));
        c.put(ThemeKeys.MENU_DRAWER, shiftAlpha(darken(Color.parseColor(bgMid), 0.88f), 240));
        c.put(ThemeKeys.MENU_DRAWER_HEADER, shiftAlpha(darken(Color.parseColor(bgMid), 0.80f), 250));

        // Items activos más brillantes
        c.put(ThemeKeys.MENU_ITEM_ACTIVE, shiftAlpha(Color.parseColor(accentPrimary), 95));
        c.put(ThemeKeys.MENU_ITEM_ACTIVE_STROKE, Color.parseColor(accentPrimary));
        c.put(ThemeKeys.MENU_BADGE, Color.parseColor(accentSecondary));

        c.put(ThemeKeys.ICON_DEFAULT, shiftAlpha(Color.parseColor(textSecondary), 230));
        c.put(ThemeKeys.ICON_ACTIVE, Color.parseColor(accentPrimary)); // Ahora el icono activo brilla con el color primario
        c.put(ThemeKeys.ICON_TOPBAR, Color.parseColor(textPrimary));
        c.put(ThemeKeys.ICON_DRAWER, Color.parseColor(textPrimary));
        c.put(ThemeKeys.ICON_BOTTOM, shiftAlpha(Color.parseColor(textSecondary), 240));

        c.put(ThemeKeys.BUTTON_PRIMARY_BG, Color.parseColor(accentPrimary));
        c.put(ThemeKeys.BUTTON_SECONDARY_BG, Color.parseColor(accentSecondary));
        c.put(ThemeKeys.BUTTON_TEXT_DARK, chooseTextOn(Color.parseColor(accentPrimary)));
        c.put(ThemeKeys.BUTTON_TEXT_LIGHT, chooseTextOn(Color.parseColor(accentSecondary)));

        c.put(ThemeKeys.INPUT_BG, shiftAlpha(Color.parseColor(textPrimary), 35));
        c.put(ThemeKeys.INPUT_STROKE, shiftAlpha(Color.parseColor(textPrimary), 90));
        c.put(ThemeKeys.INPUT_HINT, shiftAlpha(Color.parseColor(textSecondary), 190));

        c.put(ThemeKeys.DIALOG_BG, darken(Color.parseColor(bgMid), 0.95f));
        c.put(ThemeKeys.DIALOG_TEXT, Color.parseColor(textPrimary));
        c.put(ThemeKeys.OVERLAY_BG, Color.parseColor("#99000000")); // Overlay un poco más oscuro para resaltar el diálogo

        // Glows (Resplandores) más intensos
        c.put(ThemeKeys.GLOW_PRIMARY, shiftAlpha(Color.parseColor(accentPrimary), 180));
        c.put(ThemeKeys.GLOW_SECONDARY, shiftAlpha(Color.parseColor(accentSecondary), 160));
        c.put(ThemeKeys.GLOW_TERTIARY, shiftAlpha(lighten(Color.parseColor(accentPrimary), 1.3f), 130));
        c.put(ThemeKeys.GLOW_DRAWER_PRIMARY, Color.parseColor(accentPrimary));
        c.put(ThemeKeys.GLOW_DRAWER_SECONDARY, Color.parseColor(accentSecondary));

        c.put(ThemeKeys.ACCOUNT_GLASS_PANEL, shiftAlpha(Color.parseColor(textPrimary), 45));
        c.put(ThemeKeys.ACCOUNT_GLASS_STROKE, shiftAlpha(Color.parseColor(textPrimary), 80));
        c.put(ThemeKeys.ACCOUNT_DIVIDER, Color.parseColor(accentPrimary));
        c.put(ThemeKeys.ACCOUNT_SHIMMER, Color.parseColor("#FFFFFF"));

        c.put(ThemeKeys.FX_GLOW_INTENSITY, Color.parseColor("#A0FFFFFF"));
        c.put(ThemeKeys.FX_BAR_GLOSS, shiftAlpha(Color.parseColor(textPrimary), 50));
        c.put(ThemeKeys.FX_PANEL_SHADOW, Color.parseColor("#77000000"));
        c.put(ThemeKeys.FX_GLASS_ALPHA, shiftAlpha(Color.parseColor(textPrimary), 65));
        c.put(ThemeKeys.FX_ACTIVE_BORDER, Color.parseColor(accentPrimary));
        c.put(ThemeKeys.FX_TOP_LIGHT, shiftAlpha(Color.parseColor(textPrimary), 40));

        return new ThemePreset(name, description, c);
    }

    private static int darken(int color, float factor) {
        int r = Math.max(0, (int) (Color.red(color) * factor));
        int g = Math.max(0, (int) (Color.green(color) * factor));
        int b = Math.max(0, (int) (Color.blue(color) * factor));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private static int lighten(int color, float factor) {
        int r = Math.min(255, (int) (Color.red(color) * factor));
        int g = Math.min(255, (int) (Color.green(color) * factor));
        int b = Math.min(255, (int) (Color.blue(color) * factor));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private static int shiftAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int chooseTextOn(int bg) {
        double luminance = (0.299 * Color.red(bg) + 0.587 * Color.green(bg) + 0.114 * Color.blue(bg)) / 255;
        // Si el fondo es brillante, el texto es negro profundo; si es oscuro, blanco puro.
        return luminance > 0.5 ? Color.parseColor("#000000") : Color.WHITE;
    }
}