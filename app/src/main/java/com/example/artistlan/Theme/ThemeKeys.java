package com.example.artistlan.Theme;

import java.util.Arrays;
import java.util.List;

public final class ThemeKeys {

    private ThemeKeys() {}

    // Secciones
    public static final String SECTION_CUENTA = "Cuenta";
    public static final String SECTION_TEMAS = "Temas";
    public static final String SECTION_MENU = "Menú";
    public static final String SECTION_GLOWS = "Esferas / Glows";
    public static final String SECTION_ICONOS = "Íconos";
    public static final String SECTION_BOTONES = "Botones";
    public static final String SECTION_INPUTS = "Inputs";
    public static final String SECTION_DIALOGOS = "Diálogos";

    // Fondo / tema general
    public static final String BG_TOP = "bg_top";
    public static final String BG_MID = "bg_mid";
    public static final String BG_BOTTOM = "bg_bottom";

    // Textos globales
    public static final String TEXT_PRIMARY = "text_primary";
    public static final String TEXT_SECONDARY = "text_secondary";

    // Cuenta / bienvenida / login / registro
    public static final String ACCOUNT_GLASS_PANEL = "account_glass_panel";
    public static final String ACCOUNT_GLASS_STROKE = "account_glass_stroke";
    public static final String ACCOUNT_DIVIDER = "account_divider";
    public static final String ACCOUNT_SHIMMER = "account_shimmer";

    // Menú
    public static final String MENU_TOPBAR = "menu_topbar";
    public static final String MENU_TOPBAR_2 = "menu_topbar_2";
    public static final String MENU_BOTTOMBAR = "menu_bottombar";
    public static final String MENU_BOTTOMBAR_2 = "menu_bottombar_2";
    public static final String MENU_DRAWER = "menu_drawer";
    public static final String MENU_DRAWER_HEADER = "menu_drawer_header";
    public static final String MENU_ITEM_ACTIVE = "menu_item_active";
    public static final String MENU_ITEM_ACTIVE_STROKE = "menu_item_active_stroke";
    public static final String MENU_BADGE = "menu_badge";

    // Acentos
    public static final String ACCENT_PRIMARY = "accent_primary";
    public static final String ACCENT_PRIMARY_LIGHT = "accent_primary_light";
    public static final String ACCENT_SECONDARY = "accent_secondary";
    public static final String ACCENT_SECONDARY_LIGHT = "accent_secondary_light";

    // Íconos
    public static final String ICON_DEFAULT = "icon_default";
    public static final String ICON_ACTIVE = "icon_active";
    public static final String ICON_TOPBAR = "icon_topbar";
    public static final String ICON_DRAWER = "icon_drawer";
    public static final String ICON_BOTTOM = "icon_bottom";

    // Botones
    public static final String BUTTON_PRIMARY_BG = "button_primary_bg";
    public static final String BUTTON_SECONDARY_BG = "button_secondary_bg";
    public static final String BUTTON_TEXT_DARK = "button_text_dark";
    public static final String BUTTON_TEXT_LIGHT = "button_text_light";

    // Inputs
    public static final String INPUT_BG = "input_bg";
    public static final String INPUT_STROKE = "input_stroke";
    public static final String INPUT_HINT = "input_hint";

    // Diálogos
    public static final String DIALOG_BG = "dialog_bg";
    public static final String DIALOG_TEXT = "dialog_text";
    public static final String OVERLAY_BG = "overlay_bg";

    // Glows
    public static final String GLOW_PRIMARY = "glow_primary";
    public static final String GLOW_SECONDARY = "glow_secondary";
    public static final String GLOW_TERTIARY = "glow_tertiary";
    public static final String GLOW_DRAWER_PRIMARY = "glow_drawer_primary";
    public static final String GLOW_DRAWER_SECONDARY = "glow_drawer_secondary";

    // Efectos visuales
    public static final String FX_GLOW_INTENSITY = "fx_glow_intensity";
    public static final String FX_BAR_GLOSS = "fx_bar_gloss";
    public static final String FX_PANEL_SHADOW = "fx_panel_shadow";
    public static final String FX_GLASS_ALPHA = "fx_glass_alpha";
    public static final String FX_ACTIVE_BORDER = "fx_active_border";
    public static final String FX_TOP_LIGHT = "fx_top_light";

    public static List<String> sections() {
        return Arrays.asList(
                SECTION_CUENTA,
                SECTION_TEMAS,
                SECTION_MENU,
                SECTION_GLOWS,
                SECTION_ICONOS,
                SECTION_BOTONES,
                SECTION_INPUTS,
                SECTION_DIALOGOS
        );
    }
}