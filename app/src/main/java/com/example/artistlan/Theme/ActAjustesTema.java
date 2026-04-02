package com.example.artistlan.Theme;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ActAjustesTema extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnReset;
    private RecyclerView recyclerSections;

    private View previewTopbar;
    private View previewBottomBar;
    private View previewDrawer;
    private View previewGlow1;
    private View previewGlow2;
    private TextView previewTitle;
    private TextView previewSubtitle;

    private ThemePrefsManager prefsManager;
    private ThemeManager themeManager;
    private ThemeSectionAdapter sectionAdapter;
    private final List<ThemeSection> sections = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_ajustes_tema);

        prefsManager = new ThemePrefsManager(this);
        themeManager = new ThemeManager(this);

        initViews();
        initRecycler();
        loadSections();
        renderPreview();

        btnBack.setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            prefsManager.resetAll();
            loadSections();
            renderPreview();
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.themeBtnBack);
        btnReset = findViewById(R.id.themeBtnReset);
        recyclerSections = findViewById(R.id.recyclerThemeSections);

        previewTopbar = findViewById(R.id.previewTopbar);
        previewBottomBar = findViewById(R.id.previewBottomBar);
        previewDrawer = findViewById(R.id.previewDrawer);
        previewGlow1 = findViewById(R.id.previewGlow1);
        previewGlow2 = findViewById(R.id.previewGlow2);
        previewTitle = findViewById(R.id.previewTitle);
        previewSubtitle = findViewById(R.id.previewSubtitle);
    }

    private void initRecycler() {
        sectionAdapter = new ThemeSectionAdapter(sections, this::openColorPicker);
        recyclerSections.setLayoutManager(new LinearLayoutManager(this));
        recyclerSections.setNestedScrollingEnabled(false);
        recyclerSections.setHasFixedSize(false);
        recyclerSections.setItemAnimator(null);
        recyclerSections.setAdapter(sectionAdapter);
    }

    private void loadSections() {
        sections.clear();

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_CUENTA,
                "Login, registro, panel glass y bienvenida.",
                buildCuentaItems(),
                true
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_TEMAS,
                "Fondos y textos base de toda la identidad.",
                buildTemaItems(),
                false
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_MENU,
                "Top bar, bottom nav, drawer y estados activos.",
                buildMenuItems(),
                false
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_GLOWS,
                "Esferas y glows principales del sistema.",
                buildGlowItems(),
                false
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_ICONOS,
                "Colores de iconos activos, inactivos y de barras.",
                buildIconItems(),
                false
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_BOTONES,
                "Botones principales y secundarios.",
                buildButtonItems(),
                false
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_INPUTS,
                "Inputs, hints y bordes.",
                buildInputItems(),
                false
        ));
        sections.add(new ThemeSection(
                "Apariencia avanzada",
                "Glow, brillo, sombras, glass y bordes.",
                buildEffectsItems(),
                false
        ));
        sections.add(new ThemeSection(
                ThemeKeys.SECTION_DIALOGOS,
                "Overlays y diálogos.",
                buildDialogItems(),
                false
        ));

        sectionAdapter.notifyDataSetChanged();
    }

    private List<ThemeItem> buildTemaItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.BG_TOP, "Fondo superior", "Parte superior del fondo principal"));
        list.add(item(ThemeKeys.BG_MID, "Fondo medio", "Capa media de pantallas"));
        list.add(item(ThemeKeys.BG_BOTTOM, "Fondo inferior", "Base del fondo general"));
        list.add(item(ThemeKeys.TEXT_PRIMARY, "Texto principal", "Títulos y textos fuertes"));
        list.add(item(ThemeKeys.TEXT_SECONDARY, "Texto secundario", "Subtítulos y ayudas"));
        return list;
    }

    private List<ThemeItem> buildEffectsItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.FX_GLOW_INTENSITY, "Intensidad glow", "Controla qué tanto brillan las esferas"));
        list.add(item(ThemeKeys.FX_BAR_GLOSS, "Brillo de barra", "Luz superior de top bar y bottom nav"));
        list.add(item(ThemeKeys.FX_PANEL_SHADOW, "Sombra de panel", "Sombras decorativas de paneles"));
        list.add(item(ThemeKeys.FX_GLASS_ALPHA, "Transparencia glass", "Nivel visual de los paneles translúcidos"));
        list.add(item(ThemeKeys.FX_ACTIVE_BORDER, "Borde activo", "Borde del módulo o cápsula activa"));
        list.add(item(ThemeKeys.FX_TOP_LIGHT, "Luz superior", "Refuerzo de luz en la parte alta"));
        return list;
    }

    private List<ThemeItem> buildCuentaItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.ACCOUNT_GLASS_PANEL, "Panel glass", "Contenedor de login / registro"));
        list.add(item(ThemeKeys.ACCOUNT_GLASS_STROKE, "Borde glass", "Borde del panel principal"));
        list.add(item(ThemeKeys.ACCOUNT_DIVIDER, "Divider glow", "Línea decorativa"));
        list.add(item(ThemeKeys.ACCOUNT_SHIMMER, "Shimmer", "Brillo de la línea"));
        return list;
    }

    private List<ThemeItem> buildMenuItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.MENU_TOPBAR, "Top bar", "Color principal barra superior"));
        list.add(item(ThemeKeys.MENU_TOPBAR_2, "Top bar degradado", "Segundo tono superior"));
        list.add(item(ThemeKeys.MENU_BOTTOMBAR, "Bottom nav", "Color principal barra inferior"));
        list.add(item(ThemeKeys.MENU_BOTTOMBAR_2, "Bottom nav degradado", "Segundo tono inferior"));
        list.add(item(ThemeKeys.MENU_DRAWER, "Drawer", "Fondo del menú lateral"));
        list.add(item(ThemeKeys.MENU_DRAWER_HEADER, "Header drawer", "Cabecera del lateral"));
        list.add(item(ThemeKeys.MENU_ITEM_ACTIVE, "Item activo", "Cápsula del módulo seleccionado"));
        list.add(item(ThemeKeys.MENU_ITEM_ACTIVE_STROKE, "Borde activo", "Borde del item activo"));
        list.add(item(ThemeKeys.MENU_BADGE, "Badge notificaciones", "Punto de la campana"));
        return list;
    }

    private List<ThemeItem> buildGlowItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.GLOW_PRIMARY, "Glow principal", "Esfera/glow principal"));
        list.add(item(ThemeKeys.GLOW_SECONDARY, "Glow secundario", "Esfera/glow secundaria"));
        list.add(item(ThemeKeys.GLOW_TERTIARY, "Glow terciario", "Esfera/glow terciaria"));
        list.add(item(ThemeKeys.GLOW_DRAWER_PRIMARY, "Glow drawer 1", "Glow del header lateral"));
        list.add(item(ThemeKeys.GLOW_DRAWER_SECONDARY, "Glow drawer 2", "Segundo glow del lateral"));
        return list;
    }

    private List<ThemeItem> buildIconItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.ICON_DEFAULT, "Ícono default", "Íconos inactivos"));
        list.add(item(ThemeKeys.ICON_ACTIVE, "Ícono activo", "Íconos seleccionados"));
        list.add(item(ThemeKeys.ICON_TOPBAR, "Ícono top bar", "Campana y acciones superiores"));
        list.add(item(ThemeKeys.ICON_DRAWER, "Ícono drawer", "Íconos del menú lateral"));
        list.add(item(ThemeKeys.ICON_BOTTOM, "Ícono bottom nav", "Íconos inferiores"));
        return list;
    }

    private List<ThemeItem> buildButtonItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.BUTTON_PRIMARY_BG, "Botón primario", "Fondo principal de botones"));
        list.add(item(ThemeKeys.BUTTON_SECONDARY_BG, "Botón secundario", "Fondo secundario de botones"));
        list.add(item(ThemeKeys.BUTTON_TEXT_DARK, "Texto botón oscuro", "Texto sobre botón claro"));
        list.add(item(ThemeKeys.BUTTON_TEXT_LIGHT, "Texto botón claro", "Texto sobre botón fuerte"));
        return list;
    }

    private List<ThemeItem> buildInputItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.INPUT_BG, "Input fondo", "Fondo de inputs"));
        list.add(item(ThemeKeys.INPUT_STROKE, "Input borde", "Borde de inputs"));
        list.add(item(ThemeKeys.INPUT_HINT, "Input hint", "Texto hint"));
        return list;
    }

    private List<ThemeItem> buildDialogItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.DIALOG_BG, "Diálogo fondo", "Fondo del selector y diálogos"));
        list.add(item(ThemeKeys.DIALOG_TEXT, "Diálogo texto", "Texto interno del diálogo"));
        list.add(item(ThemeKeys.OVERLAY_BG, "Overlay", "Capa oscura de fondo"));
        return list;
    }

    private ThemeItem item(String key, String title, String description) {
        return new ThemeItem(
                key,
                title,
                description,
                "",
                prefsManager.getResolvedColor(key)
        );
    }

    private void openColorPicker(ThemeItem item) {
        new AmbilWarnaDialog(
                this,
                item.getColor(),
                true,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) { }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        prefsManager.setColor(item.getKey(), color);
                        item.setColor(color);
                        sectionAdapter.notifyDataSetChanged();
                        renderPreview();
                    }
                }
        ).show();
    }

    private void renderPreview() {
        previewTopbar.setBackgroundColor(themeManager.color(ThemeKeys.MENU_TOPBAR));
        previewBottomBar.setBackgroundColor(themeManager.color(ThemeKeys.MENU_BOTTOMBAR));
        previewDrawer.setBackgroundColor(themeManager.color(ThemeKeys.MENU_DRAWER));
        previewGlow1.setBackgroundColor(themeManager.color(ThemeKeys.GLOW_PRIMARY));
        previewGlow2.setBackgroundColor(themeManager.color(ThemeKeys.GLOW_SECONDARY));
        previewTitle.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
        previewSubtitle.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
    }
}