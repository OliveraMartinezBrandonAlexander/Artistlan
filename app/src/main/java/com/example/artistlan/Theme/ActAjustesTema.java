package com.example.artistlan.Theme;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;
import com.example.artistlan.utils.ArtistlanDialogFactory;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogConfig;
import com.example.artistlan.utils.TermsDialogHelper;

import java.util.ArrayList;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ActAjustesTema extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnReset;
    private RecyclerView recyclerSections;
    private GridLayout optionsGrid;
    private View rootView;
    private View presetsPanel;
    private TextView screenTitle;

    private View previewTopbar;
    private View previewBottomBar;
    private View previewDrawer;
    private View previewGlow1;
    private View previewGlow2;
    private View previewCard;
    private View previewInput;
    private View previewButton;
    private View themeGlowTop;
    private View themeGlowCenter;
    private View themeGlowBottom;
    private TextView previewTitle;
    private TextView previewSubtitle;
    private TextView previewBody;

    private LinearLayout presetsContainer;

    private ThemePrefsManager prefsManager;
    private ThemeManager themeManager;
    private ThemeSectionAdapter sectionAdapter;
    private final List<ThemeSection> sections = new ArrayList<>();
    private final List<ThemePreset> presets = ThemePresets.build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_ajustes_tema);

        prefsManager = new ThemePrefsManager(this);
        themeManager = new ThemeManager(this);

        initViews();
        initRecycler();
        buildOptionsGrid();
        loadSections();
        buildPresetsUi();
        renderPreview();

        btnBack.setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            prefsManager.resetAll();
            loadSections();
            buildPresetsUi();
            renderPreview();
        });
    }

    private void initViews() {
        rootView = findViewById(R.id.themeRoot);
        btnBack = findViewById(R.id.themeBtnBack);
        btnReset = findViewById(R.id.themeBtnReset);
        recyclerSections = findViewById(R.id.recyclerThemeSections);
        optionsGrid = findViewById(R.id.themeOptionsGrid);
        presetsPanel = findViewById(R.id.themePresetsPanel);
        screenTitle = findViewById(R.id.themeTitle);

        previewTopbar = findViewById(R.id.previewTopbar);
        previewBottomBar = findViewById(R.id.previewBottomBar);
        previewDrawer = findViewById(R.id.previewDrawer);
        previewGlow1 = findViewById(R.id.previewGlow1);
        previewGlow2 = findViewById(R.id.previewGlow2);
        previewCard = findViewById(R.id.previewCard);
        previewInput = findViewById(R.id.previewInput);
        previewButton = findViewById(R.id.previewButton);
        previewTitle = findViewById(R.id.previewTitle);
        previewSubtitle = findViewById(R.id.previewSubtitle);
        previewBody = findViewById(R.id.previewBody);
        themeGlowTop = findViewById(R.id.themeGlowTop);
        themeGlowCenter = findViewById(R.id.themeGlowCenter);
        themeGlowBottom = findViewById(R.id.themeGlowBottom);
        presetsContainer = findViewById(R.id.themePresetsContainer);
    }

    private void buildOptionsGrid() {
        if (optionsGrid == null) {
            return;
        }
        optionsGrid.removeAllViews();
        addOption("Perfil", "Datos de cuenta", R.drawable.ic_nav_perfil_artistlan,
                () -> showInfoDialog("Perfil", "Gestiona tus datos desde la pantalla de perfil."));
        addOption("Tema", "Colores Artistlan", R.drawable.ic_nav_config_artistlan,
                () -> presetsPanel.requestFocus());
        addOption("Seguridad", "Cuenta protegida", R.drawable.ic_eye,
                () -> showInfoDialog("Seguridad", "Las opciones de seguridad se gestionan desde tu perfil cuando estan disponibles."));
        addOption("2FA", "Verificacion", R.drawable.ic_notificaciones_artistlan,
                () -> showInfoDialog("2FA", "La verificacion en dos pasos se activa desde el flujo de seguridad de tu cuenta."));
        addOption("Privacidad", "Datos y visibilidad", R.drawable.ic_nav_cerrar_artistlan,
                () -> showInfoDialog("Privacidad", "Artistlan usa tus datos para autenticacion, perfiles, publicaciones e interacciones dentro de la plataforma."));
        addOption("Notificaciones", "Avisos", R.drawable.ic_notificaciones_artistlan,
                () -> showInfoDialog("Notificaciones", "Revisa tus avisos y solicitudes desde el menu lateral."));
        addOption("Mensajes", "Bandeja", R.drawable.ic_nav_mensajes_artistlan,
                () -> showInfoDialog("Mensajes", "Consulta conversaciones, solicitudes y notificaciones desde el centro de mensajes."));
        addOption("Portafolio", "Obras y servicios", R.drawable.ic_nav_portafolio_artistlan,
                () -> showInfoDialog("Portafolio", "Administra tus obras y servicios publicados desde la seccion de portafolio."));
        addOption("Soporte", "Ayuda", R.drawable.ic_help_artistlan,
                () -> showInfoDialog("Soporte", "Consulta la seccion de ayuda desde el menu lateral."));
        addOption("Términos", "Uso de Artistlan", R.drawable.ic_nav_historia_artistlan_story,
                () -> TermsDialogHelper.show(this));
        addOption("Acerca de", "Artistlan", R.drawable.ic_nav_historia_arte_artistlan,
                () -> showInfoDialog("Acerca de Artistlan", "Artistlan es una plataforma para publicar, explorar y gestionar arte, servicios, portafolios y convocatorias."));
        addOption("Cerrar sesion", "Menu lateral", R.drawable.ic_nav_cerrar_artistlan,
                () -> showInfoDialog("Cerrar sesion", "Usa la opcion Cerrar sesion del menu lateral para salir de tu cuenta."));
    }

    private void addOption(String title, String subtitle, int iconRes, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> action.run());
        CardThemeHelper.applyThemedSurface(card, themeManager, 16);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(8));
        card.setLayoutParams(params);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(themeManager.color(ThemeKeys.ICON_ACTIVE)));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        card.addView(icon, iconParams);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
        titleView.setTextSize(15f);
        titleView.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(8);
        card.addView(titleView, titleParams);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        subtitleView.setTextSize(12f);
        subtitleView.setMaxLines(2);
        card.addView(subtitleView);

        optionsGrid.addView(card);
    }

    private void showInfoDialog(String title, String message) {
        ArtistlanDialogFactory.show(this, DialogConfig.builder()
                .setType(DialogConfig.Type.INFO)
                .setTitle(title)
                .setMessage(message)
                .setPositiveText("Entendido")
                .build());
    }

    private void initRecycler() {
        sectionAdapter = new ThemeSectionAdapter(sections, this::openColorPicker);
        recyclerSections.setLayoutManager(new LinearLayoutManager(this));
        recyclerSections.setNestedScrollingEnabled(false);
        recyclerSections.setHasFixedSize(false);
        recyclerSections.setItemAnimator(null);
        recyclerSections.setAdapter(sectionAdapter);
    }

    private void buildPresetsUi() {
        presetsContainer.removeAllViews();
        LinearLayout row = null;
        for (ThemePreset preset : presets) {
            if (row == null || row.getChildCount() == 2) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.bottomMargin = dp(8);
                presetsContainer.addView(row, rowParams);
            }

            Button button = new Button(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0,
                    dp(48),
                    1f
            );
            lp.setMargins(dp(4), 0, dp(4), 0);
            button.setLayoutParams(lp);
            button.setAllCaps(false);
            button.setText(preset.getName());
            button.setBackgroundResource(R.drawable.bg_btn_bubble_glass_secondary);
            button.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
            button.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL), PorterDuff.Mode.SRC_ATOP);

            button.setOnClickListener(v -> {
                prefsManager.setColors(preset.getColors());
                loadSections();
                renderPreview();
                stylePresetButtons(preset.getName());
            });
            row.addView(button);
        }
        stylePresetButtons(null);
    }

    private void stylePresetButtons(@Nullable String activePreset) {
        for (int i = 0; i < presetsContainer.getChildCount(); i++) {
            View row = presetsContainer.getChildAt(i);
            if (!(row instanceof LinearLayout)) continue;
            LinearLayout rowLayout = (LinearLayout) row;
            for (int j = 0; j < rowLayout.getChildCount(); j++) {
                View child = rowLayout.getChildAt(j);
                if (!(child instanceof Button)) continue;
                Button b = (Button) child;
                boolean active = activePreset != null && b.getText().toString().equals(activePreset);
                b.setTextColor(active ? themeManager.color(ThemeKeys.BUTTON_TEXT_DARK) : themeManager.color(ThemeKeys.TEXT_PRIMARY));
                if (b.getBackground() != null) {
                    b.getBackground().setColorFilter(
                            active ? themeManager.color(ThemeKeys.BUTTON_PRIMARY_BG) : themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                            PorterDuff.Mode.SRC_ATOP
                    );
                }
            }
        }
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
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
                "Fondo, títulos y textos principales.",
                buildTemaItems(),
                false
        ));

        sections.add(new ThemeSection(
                ThemeKeys.SECTION_MENU,
                "Menú superior, botón menú, drawer y estados activos.",
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
                ThemeKeys.SECTION_FICHAS,
                "Bordes, chips, likes y filtros de fichas.",
                buildCardItems(),
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
        list.add(item(ThemeKeys.MENU_TITLE, "Título Artistlan", "Texto principal del menú superior"));
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

    private List<ThemeItem> buildCardItems() {
        List<ThemeItem> list = new ArrayList<>();
        list.add(item(ThemeKeys.CARD_BORDER, "Borde de fichas", "Marco cuadrado de obras, artistas, servicios e historia"));
        list.add(item(ThemeKeys.CARD_CHIP_BG, "Fondo chips", "Etiquetas dentro de fichas"));
        list.add(item(ThemeKeys.CARD_CHIP_TEXT, "Texto chips", "Texto de etiquetas dentro de fichas"));
        list.add(item(ThemeKeys.LIKE_ACTIVE, "Like activo", "Corazón activo y doble tap"));
        list.add(item(ThemeKeys.FILTER_BUTTON_BG, "Filtro fondo", "Fondo del botón de filtros"));
        list.add(item(ThemeKeys.FILTER_BUTTON_STROKE, "Filtro borde", "Borde e ícono del botón de filtros"));
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
        themeManager = new ThemeManager(this);
        ThemeApplier.applyFragmentBackground(rootView, themeManager, themeGlowTop, themeGlowCenter, themeGlowBottom);
        ThemeApplier.applyTextPrimary(screenTitle, themeManager);
        buildOptionsGrid();
        previewTopbar.setBackgroundColor(themeManager.color(ThemeKeys.MENU_TOPBAR));
        previewBottomBar.setBackgroundColor(themeManager.color(ThemeKeys.MENU_BOTTOMBAR));
        previewDrawer.setBackgroundColor(themeManager.color(ThemeKeys.MENU_DRAWER));
        previewGlow1.setBackgroundColor(themeManager.color(ThemeKeys.GLOW_PRIMARY));
        previewGlow2.setBackgroundColor(themeManager.color(ThemeKeys.GLOW_SECONDARY));
        previewTitle.setText("Artistlan");
        previewTitle.setTextColor(themeManager.color(ThemeKeys.MENU_TITLE));
        previewSubtitle.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        previewBody.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        CardThemeHelper.applyFilterButton(btnBack, themeManager);
        ThemeApplier.applySecondaryButton(btnReset, themeManager);
        CardThemeHelper.applyFlatCard(previewCard, themeManager);

        if (previewInput.getBackground() != null) {
            previewInput.getBackground().setColorFilter(themeManager.color(ThemeKeys.INPUT_BG), PorterDuff.Mode.SRC_ATOP);
        }
        if (previewButton.getBackground() != null) {
            previewButton.getBackground().setColorFilter(themeManager.color(ThemeKeys.BUTTON_PRIMARY_BG), PorterDuff.Mode.SRC_ATOP);
        }
    }
}
