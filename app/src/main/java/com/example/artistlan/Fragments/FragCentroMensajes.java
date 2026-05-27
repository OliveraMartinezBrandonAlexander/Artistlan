package com.example.artistlan.Fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragCentroMensajes extends Fragment {

    public static final String ARG_TAB_INICIAL = "tab_inicial";
    public static final String ARG_SOLICITUDES_MODO = "solicitudes_modo";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View menuInferior;
    private View layoutTabs;
    private ImageButton btnAtras;
    private ImageButton btnMenuOpciones;
    private TextView tvResumenContador;
    private TextView tvTituloCentroMensajes;
    private ThemeManager themeManager;
    private int notificacionesNoLeidas = 0;
    private int solicitudesPendientes = 0;
    private long ultimoRefreshResumenMs = 0L;
    private boolean refrescoResumenEnCurso = false;
    private boolean tabsThemeListenerAttached = false;
    private static final long RESUMEN_REFRESH_MIN_INTERVAL_MS = 800L;
    private CentroMensajesPagerAdapter pagerAdapter;
    private int modoSolicitudesPendiente = FragSolicitudesMensajes.MODO_RECIBIDAS;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_centro_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());

        new BotonesMenuSuperior(this);

        menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        tabLayout = view.findViewById(R.id.tabLayoutCentroMensajes);
        viewPager = view.findViewById(R.id.viewPagerCentroMensajes);
        layoutTabs = view.findViewById(R.id.layoutCentroMensajesTabs);
        btnAtras = view.findViewById(R.id.btnCentroMensajesAtras);
        btnMenuOpciones = view.findViewById(R.id.btnCentroMensajesMenu);
        tvResumenContador = view.findViewById(R.id.tvCentroMensajesResumenContador);
        tvTituloCentroMensajes = view.findViewById(R.id.tvTituloCentroMensajes);
        btnAtras.setOnClickListener(v -> navegarAtrasSeguro());
        btnMenuOpciones.setOnClickListener(this::mostrarMenuAcciones);

        pagerAdapter = new CentroMensajesPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                notificarDrawerTabActivo(position);
                if (position == 1) {
                    FragSolicitudesMensajes solicitudes = getSolicitudesFragmentActual();
                    if (solicitudes != null) {
                        solicitudes.seleccionarModoExterno(modoSolicitudesPendiente);
                    }
                }
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(position == 0 ? "Mensajes (0)" : "Solicitudes (0)")
        ).attach();
        configurarTabsTematicos();

        int tabInicial = 0;
        Bundle args = getArguments();
        if (args != null) {
            tabInicial = args.getInt(ARG_TAB_INICIAL, 0);
            modoSolicitudesPendiente = args.getInt(ARG_SOLICITUDES_MODO, FragSolicitudesMensajes.MODO_RECIBIDAS);
        }
        seleccionarTab(tabInicial);
        refrescarResumenContadores(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            themeManager = new ThemeManager(requireContext());
            aplicarTemaTabsCentro();
        }
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
        if (getActivity() instanceof com.example.artistlan.Activitys.ActFragmentoPrincipal) {
            ((com.example.artistlan.Activitys.ActFragmentoPrincipal) getActivity()).refrescarBadgeMensajes();
        }
        notificarDrawerTabActivo(getTabActivoActual());
        refrescarResumenContadores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        pageChangeCallback = null;
        if (viewPager != null) {
            viewPager.setAdapter(null);
        }
        pagerAdapter = null;
        tabLayout = null;
        viewPager = null;
        btnAtras = null;
        btnMenuOpciones = null;
        tvResumenContador = null;
        tvTituloCentroMensajes = null;
        layoutTabs = null;
        themeManager = null;
        tabsThemeListenerAttached = false;
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
        menuInferior = null;
    }

    public void seleccionarTab(int tabIndex) {
        if (viewPager == null) {
            return;
        }
        int safeIndex = Math.max(0, Math.min(1, tabIndex));
        notificarDrawerTabActivo(safeIndex);
        viewPager.setCurrentItem(safeIndex, false);
    }

    public void seleccionarModoSolicitudes(int modo) {
        modoSolicitudesPendiente = (modo == FragSolicitudesMensajes.MODO_ENVIADAS)
                ? FragSolicitudesMensajes.MODO_ENVIADAS
                : FragSolicitudesMensajes.MODO_RECIBIDAS;
        FragSolicitudesMensajes solicitudes = getSolicitudesFragmentActual();
        if (solicitudes != null) {
            solicitudes.seleccionarModoExterno(modoSolicitudesPendiente);
        }
    }

    public void refrescarResumenContadores() {
        refrescarResumenContadores(false);
    }

    public void refrescarResumenContadores(boolean forzar) {
        if (!isAdded()) {
            return;
        }
        long ahora = SystemClock.elapsedRealtime();
        if (!forzar) {
            if (refrescoResumenEnCurso) {
                return;
            }
            if (ahora - ultimoRefreshResumenMs < RESUMEN_REFRESH_MIN_INTERVAL_MS) {
                return;
            }
        }
        refrescoResumenEnCurso = true;
        ultimoRefreshResumenMs = ahora;

        android.content.SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", android.content.Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        MensajesBadgeManager.refrescarBadgeDetalle(idUsuario, detalle -> {
            refrescoResumenEnCurso = false;
            if (!isAdded()) {
                return;
            }
            notificacionesNoLeidas = detalle.getNotificacionesNoLeidas();
            solicitudesPendientes = detalle.getSolicitudesPendientes();
            ultimoRefreshResumenMs = SystemClock.elapsedRealtime();
            aplicarResumenContadores();
        });
    }

    private void aplicarResumenContadores() {
        if (tvResumenContador != null) {
            tvResumenContador.setText(
                    "Pendientes: " + notificacionesNoLeidas
                            + " mensajes no leidos + "
                            + solicitudesPendientes
                            + " solicitudes"
            );
        }
        if (tabLayout == null || tabLayout.getTabCount() < 2) {
            return;
        }
        com.google.android.material.tabs.TabLayout.Tab bandeja = tabLayout.getTabAt(0);
        com.google.android.material.tabs.TabLayout.Tab solicitudes = tabLayout.getTabAt(1);
        if (bandeja != null) {
            actualizarTextoTab(bandeja, "Mensajes (" + notificacionesNoLeidas + ")");
        }
        if (solicitudes != null) {
            actualizarTextoTab(solicitudes, "Solicitudes (" + solicitudesPendientes + ")");
        }
    }

    private void configurarTabsTematicos() {
        if (tabLayout == null || themeManager == null) {
            return;
        }
        aplicarTemaTabsCentro();
        if (tabsThemeListenerAttached) {
            return;
        }
        tabsThemeListenerAttached = true;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                aplicarEstadoTabCentro(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                aplicarEstadoTabCentro(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                aplicarEstadoTabCentro(tab, true);
            }
        });
    }

    private void aplicarTemaTabsCentro() {
        if (tabLayout == null || themeManager == null) {
            return;
        }
        if (layoutTabs != null) {
            layoutTabs.setBackground(crearFondoRedondeado(
                    ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 220),
                    ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE), 185),
                    2,
                    16
            ));
        }
        View root = getView();
        if (root != null) {
            View appbar = root.findViewById(R.id.appbarCentroMensajes);
            View header = root.findViewById(R.id.headerCentroMensajes);
            View pager = root.findViewById(R.id.viewPagerCentroMensajes);
            if (appbar != null) {
                appbar.setBackgroundColor(themeManager.color(ThemeKeys.BG_MID));
            }
            if (header != null) {
                header.setBackgroundColor(themeManager.color(ThemeKeys.BG_MID));
            }
            if (pager != null) {
                pager.setBackgroundColor(themeManager.color(ThemeKeys.BG_MID));
            }
        }
        CardThemeHelper.applyFilterButton(btnAtras, themeManager);
        CardThemeHelper.applyFilterButton(btnMenuOpciones, themeManager);
        if (tvTituloCentroMensajes != null) {
            tvTituloCentroMensajes.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
        }
        if (tvResumenContador != null) {
            tvResumenContador.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        }
        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        tabLayout.setTabRippleColor(ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.ACCENT_PRIMARY), 34)
        ));

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                CharSequence text = tab.getText();
                tab.setCustomView(crearVistaTab(text != null ? text.toString() : ""));
            }
        }
        aplicarEstadoTabsCentro();
    }

    @NonNull
    private TextView crearVistaTab(@NonNull String texto) {
        TextView tabView = new TextView(requireContext());
        tabView.setText(texto);
        tabView.setGravity(Gravity.CENTER);
        tabView.setSingleLine(true);
        tabView.setTextSize(13);
        tabView.setTypeface(tabView.getTypeface(), Typeface.BOLD);
        tabView.setBackgroundTintList(null);
        tabView.setMinHeight(dpToPx(42));
        tabView.setIncludeFontPadding(false);
        tabView.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
        return tabView;
    }

    private void actualizarTextoTab(@NonNull TabLayout.Tab tab, @NonNull String texto) {
        tab.setText(texto);
        View customView = tab.getCustomView();
        if (customView instanceof TextView) {
            ((TextView) customView).setText(texto);
            aplicarEstadoTabCentro(tab, tab.getPosition() == tabLayout.getSelectedTabPosition());
        }
    }

    private void aplicarEstadoTabsCentro() {
        if (tabLayout == null) {
            return;
        }
        int selected = tabLayout.getSelectedTabPosition();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            aplicarEstadoTabCentro(tabLayout.getTabAt(i), i == selected);
        }
    }

    private void aplicarEstadoTabCentro(@Nullable TabLayout.Tab tab, boolean seleccionado) {
        if (tab == null || themeManager == null) {
            return;
        }
        View customView = tab.getCustomView();
        if (!(customView instanceof TextView)) {
            return;
        }
        TextView tabView = (TextView) customView;
        tabView.animate().cancel();
        if (seleccionado) {
            tabView.setTextColor(resolverColorTextoSobre(themeManager.color(ThemeKeys.ACCENT_PRIMARY)));
            tabView.setBackground(crearFondoTabActivo());
            tabView.setAlpha(1f);
            tabView.setScaleX(1f);
            tabView.setScaleY(1f);
            tabView.animate().scaleX(1.02f).scaleY(1.02f).setDuration(110)
                    .withEndAction(() -> tabView.animate().scaleX(1f).scaleY(1f).setDuration(110).start())
                    .start();
        } else {
            tabView.setTextColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
            tabView.setBackground(crearFondoTabInactivo());
            tabView.setAlpha(0.88f);
            tabView.setScaleX(0.98f);
            tabView.setScaleY(0.98f);
        }
    }

    private GradientDrawable crearFondoTabActivo() {
        int base = themeManager.color(ThemeKeys.ACCENT_PRIMARY);
        int bottom = ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.BG_BOTTOM), 255);
        int mid = themeManager.color(ThemeKeys.BG_MID);
        int surface = Color.alpha(mid) < 255 ? ColorUtils.compositeColors(mid, bottom) : ColorUtils.setAlphaComponent(mid, 255);
        if (Color.alpha(base) < 255) {
            base = ColorUtils.compositeColors(base, surface);
        }
        int end = ColorUtils.blendARGB(base, themeManager.color(ThemeKeys.BG_BOTTOM), 0.18f);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{base, end}
        );
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(13));
        drawable.setStroke(dpToPx(1), ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 170));
        return drawable;
    }

    private GradientDrawable crearFondoTabInactivo() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 180));
        drawable.setCornerRadius(dpToPx(13));
        drawable.setStroke(dpToPx(1), ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE), 95));
        return drawable;
    }

    private GradientDrawable crearFondoRedondeado(int fillColor, int strokeColor, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dpToPx(radiusDp));
        drawable.setStroke(dpToPx(strokeDp), strokeColor);
        return drawable;
    }

    private int resolverColorTextoSobre(int backgroundColor) {
        int darkText = themeManager.color(ThemeKeys.BUTTON_TEXT_DARK);
        int lightText = themeManager.color(ThemeKeys.BUTTON_TEXT_LIGHT);
        double darkContrast = ColorUtils.calculateContrast(darkText, backgroundColor);
        double lightContrast = ColorUtils.calculateContrast(lightText, backgroundColor);
        return darkContrast >= lightContrast ? darkText : lightText;
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void onMarcarTodoClick() {
        if (viewPager == null || viewPager.getCurrentItem() != 0) {
            return;
        }
        FragBandejaMensajes bandeja = getBandejaFragmentActual();
        if (bandeja != null) {
            bandeja.marcarTodasDesdeHeader();
        }
    }

    private void onActualizarClick() {
        if (viewPager == null) {
            return;
        }
        if (viewPager.getCurrentItem() == 0) {
            FragBandejaMensajes bandeja = getBandejaFragmentActual();
            if (bandeja != null) {
                bandeja.recargarDesdeHeader();
            }
        } else {
            FragSolicitudesMensajes solicitudes = getSolicitudesFragmentActual();
            if (solicitudes != null) {
                solicitudes.recargarDesdeHeader();
            }
        }
    }

    private void mostrarMenuAcciones(View anchor) {
        if (!isAdded()) {
            return;
        }
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        final int MENU_MARCAR_TODO = 1;
        final int MENU_ACTUALIZAR = 2;

        boolean enMensajes = viewPager != null && viewPager.getCurrentItem() == 0;
        if (enMensajes) {
            popup.getMenu().add(0, MENU_MARCAR_TODO, 0, "Marcar todo como leido");
        }
        popup.getMenu().add(0, MENU_ACTUALIZAR, 1, "Actualizar");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == MENU_MARCAR_TODO) {
                onMarcarTodoClick();
                return true;
            }
            if (id == MENU_ACTUALIZAR) {
                onActualizarClick();
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Nullable
    private FragBandejaMensajes getBandejaFragmentActual() {
        for (Fragment child : getChildFragmentManager().getFragments()) {
            if (child instanceof FragBandejaMensajes) {
                return (FragBandejaMensajes) child;
            }
        }
        return null;
    }

    @Nullable
    private FragSolicitudesMensajes getSolicitudesFragmentActual() {
        for (Fragment child : getChildFragmentManager().getFragments()) {
            if (child instanceof FragSolicitudesMensajes) {
                return (FragSolicitudesMensajes) child;
            }
        }
        return null;
    }

    private void navegarAtrasSeguro() {
        if (!isAdded()) {
            return;
        }

        NavController navController = null;
        try {
            navController = NavHostFragment.findNavController(this);
            if (navController.popBackStack()) {
                return;
            }
        } catch (Exception ignored) {
            // fallback abajo
        }

        if (getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).navegarDesdeCentroMensajes(R.id.fragExplorar, null);
            return;
        }

        try {
            if (navController != null && navController.navigateUp()) {
                return;
            }
        } catch (Exception ignored) {
            // fallback final
        }

        requireActivity().finish();
    }

    private int getTabActivoActual() {
        if (viewPager == null) {
            return 0;
        }
        int current = viewPager.getCurrentItem();
        return Math.max(0, Math.min(1, current));
    }

    private void notificarDrawerTabActivo(int tabActivo) {
        if (getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).onCentroMensajesTabChanged(tabActivo);
        }
    }

    private static class CentroMensajesPagerAdapter extends FragmentStateAdapter {

        CentroMensajesPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new FragBandejaMensajes() : new FragSolicitudesMensajes();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
