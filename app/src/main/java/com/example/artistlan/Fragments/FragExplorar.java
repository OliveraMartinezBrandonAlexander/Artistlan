package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogThemeHelper;

import java.util.List;

public class FragExplorar extends Fragment {

    private static final String TAG_PERF = "ExplorarPerfDebug";
    private static final String TAG_INIT = "ExplorarInitDebug";
    private static final boolean ENABLE_EXPLORAR_DEBUG_LOGS = false;
    private static final long MIN_REPLACE_INTERVAL_MS = 450L;
    private static final int MENU_GROUP_FILTERS = 100;
    private static final int MENU_ID_CLEAR_FILTERS = 1000;

    private SearchView searchView;
    private ImageButton btnFiltros;
    private View panelFiltros;
    private View segmentIndicator;
    private ViewGroup segmentContainer;
    private Button btnSegmentObras;
    private Button btnSegmentServicios;
    private Button btnSegmentArtistas;
    private boolean filtrosVisibles = false;
    private boolean panelFiltrosVisible = true;
    private int currentTipoId = View.NO_ID;
    private int lastLoadedTipoId = View.NO_ID;
    private long lastReplaceTimestampMs = 0L;
    private boolean ignorarEventosBusqueda = false;
    private ThemeManager themeManager;
    private float swipeStartX = 0f;
    private float swipeStartY = 0f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_explorar, container, false);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());

        new com.example.artistlan.BotonesMenuSuperior(this);

        searchView = view.findViewById(R.id.searchExplorar);
        btnFiltros = view.findViewById(R.id.btnFiltrosExplorar);
        panelFiltros = view.findViewById(R.id.panelFiltrosExplorar);
        segmentIndicator = view.findViewById(R.id.segmentIndicatorExplorar);
        segmentContainer = view.findViewById(R.id.segmentContainerExplorar);
        btnSegmentObras = view.findViewById(R.id.btnSegmentObras);
        btnSegmentServicios = view.findViewById(R.id.btnSegmentServicios);
        btnSegmentArtistas = view.findViewById(R.id.btnSegmentArtistas);

        configurarBuscador();
        configurarBotonFiltros();
        configurarSelectorTipos();
        configurarDeslizamientoEntreTipos(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logInit("onViewCreated -> currentTipoId=" + currentTipoId
                + ", fragmentActual=" + nombreFragment(obtenerFragmentActual()));
        asegurarTabActualCargado("onViewCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        logInit("onResume -> currentTipoId=" + currentTipoId
                + ", fragmentActual=" + nombreFragment(obtenerFragmentActual()));
        asegurarTabActualCargado("onResume");
    }

    private void configurarBuscador() {
        if (searchView == null) return;

        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setQueryHint("Buscar en Artistlan");

        int searchIconId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView searchIcon = searchView.findViewById(searchIconId);
        if (searchIcon != null) {
            searchIcon.setImageResource(R.drawable.ic_nav_explorar_artistlan);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (ignorarEventosBusqueda) {
                    return true;
                }
                aplicarBusquedaAlFragmentActual(query);
                actualizarVisibilidadBotonFiltros();
                cerrarTeclado();
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (ignorarEventosBusqueda) {
                    return true;
                }
                aplicarBusquedaAlFragmentActual(newText);
                actualizarVisibilidadBotonFiltros();
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarPanelFiltros(true);
            }
            actualizarVisibilidadBotonFiltros();
        });
        searchView.setOnClickListener(v -> mostrarPanelFiltros(true));

        searchView.setOnCloseListener(() -> {
            limpiarBusquedaSinNotificar();
            aplicarBusquedaAlFragmentActual("");
            actualizarVisibilidadBotonFiltros();
            return false;
        });
    }

    private void configurarBotonFiltros() {
        if (btnFiltros == null) return;

        btnFiltros.setVisibility(View.GONE);
        btnFiltros.setAlpha(0f);
        btnFiltros.setOnClickListener(v -> mostrarMenuFiltros());
    }

    private void configurarSelectorTipos() {
        if (segmentContainer == null
                || btnSegmentObras == null
                || btnSegmentServicios == null
                || btnSegmentArtistas == null) {
            return;
        }

        aplicarTemaSelector();

        int tipoInicial = currentTipoId == View.NO_ID ? R.id.btnSegmentObras : currentTipoId;
        Fragment actual = getChildFragmentManager().findFragmentById(R.id.fragmentContainerExplorar);
        if (actual == null) {
            Fragment inicial = crearFragmentPorTipo(tipoInicial);
            if (inicial != null) {
                logInit("configurarSelectorTipos -> carga inicial, tipo=" + tipoInicial
                        + ", fragment=" + inicial.getClass().getSimpleName());
                cargarFragment(inicial, tipoInicial, "configurarSelectorTipos-inicial");
            }
        } else if (coincideFragmentConTipo(actual, tipoInicial)) {
            lastLoadedTipoId = tipoInicial;
        }
        currentTipoId = tipoInicial;
        actualizarVisibilidadBotonFiltros();
        logPerf("Tab inicial -> tipoId=" + currentTipoId);
        logInit("configurarSelectorTipos -> currentTipoId=" + currentTipoId
                + ", fragmentActual=" + nombreFragment(actual));

        btnSegmentObras.setOnClickListener(v -> seleccionarTipo(R.id.btnSegmentObras, true));
        btnSegmentServicios.setOnClickListener(v -> seleccionarTipo(R.id.btnSegmentServicios, true));
        btnSegmentArtistas.setOnClickListener(v -> seleccionarTipo(R.id.btnSegmentArtistas, true));

        segmentContainer.post(() -> moverIndicadorTipo(currentTipoId, false));
    }

    private void seleccionarTipo(int tipoId, boolean animar) {
        if (tipoId == View.NO_ID || tipoId == currentTipoId) {
            moverIndicadorTipo(currentTipoId, animar);
            return;
        }

        cerrarTeclado();
        limpiarBusquedaSinNotificar();

        Fragment fragment = crearFragmentPorTipo(tipoId);
        if (fragment == null) {
            return;
        }

        logInit("seleccionarTipo -> tipoId=" + tipoId
                + ", currentTipoId(before)=" + currentTipoId
                + ", fragmentNuevo=" + fragment.getClass().getSimpleName());
        cargarFragment(fragment, tipoId, "seleccionarTipo");
        currentTipoId = tipoId;
        moverIndicadorTipo(tipoId, animar);
        actualizarVisibilidadBotonFiltros();
        logPerf("Cambio tab -> tipoId=" + currentTipoId
                + ", fragment=" + fragment.getClass().getSimpleName());
    }

    private void aplicarBusquedaAlFragmentActual(String texto) {
        Fragment fragmentActual = obtenerFragmentActual();

        if (fragmentActual instanceof FragArte) {
            ((FragArte) fragmentActual).filtrarBusqueda(texto);
        } else if (fragmentActual instanceof FragServicios) {
            ((FragServicios) fragmentActual).filtrarBusqueda(texto);
        } else if (fragmentActual instanceof FragArtistas) {
            ((FragArtistas) fragmentActual).filtrarBusqueda(texto);
        }
    }

    private void mostrarBotonFiltros(boolean mostrar) {
        if (btnFiltros == null || filtrosVisibles == mostrar) {
            return;
        }

        filtrosVisibles = mostrar;

        if (mostrar) {
            btnFiltros.setVisibility(View.VISIBLE);
            btnFiltros.setTranslationX(18f);
            btnFiltros.setScaleX(0.88f);
            btnFiltros.setScaleY(0.88f);
            btnFiltros.setAlpha(0f);

            btnFiltros.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
            return;
        }

        btnFiltros.animate()
                .alpha(0f)
                .translationX(14f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(170)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> {
                    if (btnFiltros != null && !filtrosVisibles) {
                        btnFiltros.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void mostrarMenuFiltros() {
        Fragment fragmentActual = obtenerFragmentActual();

        if (!(fragmentActual instanceof FilterableExplorarFragment)) {
            return;
        }

        FilterableExplorarFragment filterableFragment = (FilterableExplorarFragment) fragmentActual;
        List<String> filtros = filterableFragment.getFilterOptions();

        if (filtros == null || filtros.isEmpty()) {
            return;
        }

        String filtroActivo = filterableFragment.getActiveFilter();
        RadioGroup radioGroup = new RadioGroup(requireContext());
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        radioGroup.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));

        for (int i = 0; i < filtros.size(); i++) {
            String filtro = filtros.get(i);
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setId(i);
            radioButton.setText(filtro);
            radioButton.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
            radioButton.setButtonTintList(android.content.res.ColorStateList.valueOf(themeManager.color(ThemeKeys.ACCENT_PRIMARY)));
            radioButton.setPadding(0, dpToPx(4), 0, dpToPx(4));
            radioButton.setChecked(filtroActivo != null && filtro.equalsIgnoreCase(filtroActivo));
            radioGroup.addView(radioButton, new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(4));
        container.setBackground(createFilterDialogBackground());
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setFillViewport(false);
        scrollView.addView(radioGroup, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.min(dpToPx(420), getResources().getDisplayMetrics().heightPixels - dpToPx(220))
        ));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Filtrar")
                .setView(container)
                .setNegativeButton("Cerrar", null)
                .setNeutralButton("Borrar filtros", (d, which) -> filterableFragment.clearFilter())
                .create();

        dialog.setOnShowListener(d -> {
            DialogThemeHelper.styleAlertDialog(dialog, requireContext());
            Button clearButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (clearButton != null) {
                clearButton.setEnabled(!TextUtils.isEmpty(filtroActivo));
            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId >= 0 && checkedId < filtros.size()) {
                filterableFragment.applyFilter(filtros.get(checkedId));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void configurarDeslizamientoEntreTipos(@NonNull View root) {
        View target = root.findViewById(R.id.fragmentContainerExplorar);
        View.OnTouchListener listener = (v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    swipeStartX = event.getX();
                    swipeStartY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - swipeStartX;
                    float dy = event.getY() - swipeStartY;
                    if (Math.abs(dx) > dpToPx(72) && Math.abs(dx) > Math.abs(dy) * 1.35f) {
                        seleccionarTipoPorDeslizamiento(dx < 0 ? 1 : -1);
                    }
                    break;
                default:
                    break;
            }
            return false;
        };
        root.setOnTouchListener(listener);
        if (target != null) {
            target.setOnTouchListener(listener);
        }
    }

    private void seleccionarTipoPorDeslizamiento(int direction) {
        int nextIndex = Math.max(0, Math.min(2, indexForTipo(currentTipoId) + direction));
        seleccionarTipo(tipoForIndex(nextIndex), true);
    }

    private int tipoForIndex(int index) {
        if (index == 1) return R.id.btnSegmentServicios;
        if (index == 2) return R.id.btnSegmentArtistas;
        return R.id.btnSegmentObras;
    }

    private GradientDrawable createFilterDialogBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(18));
        drawable.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.FILTER_BUTTON_BG), 88));
        drawable.setStroke(dpToPx(1), themeManager.color(ThemeKeys.FILTER_BUTTON_STROKE));
        return drawable;
    }

    private void mostrarPanelFiltros(boolean mostrar) {
        if (panelFiltros == null || panelFiltrosVisible == mostrar) {
            return;
        }

        panelFiltrosVisible = mostrar;
        panelFiltros.animate().cancel();

        if (mostrar) {
            panelFiltros.setVisibility(View.VISIBLE);
            panelFiltros.setAlpha(0f);
            panelFiltros.setTranslationY(-12f);
            panelFiltros.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(220)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
            if (currentTipoId != View.NO_ID && segmentContainer != null) {
                segmentContainer.post(() -> moverIndicadorTipo(currentTipoId, false));
            }
            return;
        }

        panelFiltros.animate()
                .alpha(0f)
                .translationY(-10f)
                .setDuration(180)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> {
                    if (panelFiltros != null && !panelFiltrosVisible) {
                        panelFiltros.setVisibility(View.GONE);
                        panelFiltros.setTranslationY(0f);
                    }
                })
                .start();
    }

    private void cerrarTeclado() {
        if (!isAdded()) return;

        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        View focusedView = requireActivity().getCurrentFocus();
        if (imm != null && focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void cargarFragment(@NonNull Fragment fragment, int tipoId, @NonNull String motivo) {
        lastLoadedTipoId = tipoId;
        lastReplaceTimestampMs = SystemClock.elapsedRealtime();
        logInit("cargarFragment(" + motivo + ") -> tipoId=" + tipoId
                + ", fragment=" + fragment.getClass().getSimpleName()
                + ", currentTipoId=" + currentTipoId);
        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerExplorar, fragment)
                .commit();
    }

    private void limpiarBusquedaSinNotificar() {
        if (searchView == null) {
            return;
        }
        ignorarEventosBusqueda = true;
        searchView.setQuery("", false);
        searchView.clearFocus();
        ignorarEventosBusqueda = false;
    }

    private void actualizarVisibilidadBotonFiltros() {
        mostrarBotonFiltros(fragmentActualSoportaFiltros());
    }

    @Nullable
    private Fragment obtenerFragmentActual() {
        return getChildFragmentManager().findFragmentById(R.id.fragmentContainerExplorar);
    }

    @Nullable
    private Fragment crearFragmentPorTipo(int tipoId) {
        if (tipoId == R.id.btnSegmentObras) {
            return new FragArte();
        }
        if (tipoId == R.id.btnSegmentServicios) {
            return new FragServicios();
        }
        if (tipoId == R.id.btnSegmentArtistas) {
            return new FragArtistas();
        }
        return null;
    }

    private void asegurarTabActualCargado(@NonNull String motivo) {
        if (!isAdded() || segmentContainer == null) {
            return;
        }
        int tipoSeleccionado = currentTipoId == View.NO_ID ? R.id.btnSegmentObras : currentTipoId;
        if (currentTipoId == View.NO_ID) {
            currentTipoId = tipoSeleccionado;
        }

        Fragment actual = obtenerFragmentActual();
        boolean coincide = coincideFragmentConTipo(actual, tipoSeleccionado);
        boolean necesitaCarga = actual == null || !coincide || actual.getView() == null;
        boolean replaceRecienteMismoTipo = tipoSeleccionado == lastLoadedTipoId
                && (SystemClock.elapsedRealtime() - lastReplaceTimestampMs) < MIN_REPLACE_INTERVAL_MS;

        logInit("asegurarTabActualCargado(" + motivo + ") -> tipo=" + tipoSeleccionado
                + ", currentTipoId=" + currentTipoId
                + ", fragmentActual=" + nombreFragment(actual)
                + ", coincide=" + coincide
                + ", viewNull=" + (actual == null || actual.getView() == null)
                + ", necesitaCarga=" + necesitaCarga
                + ", replaceRecienteMismoTipo=" + replaceRecienteMismoTipo);

        if (!necesitaCarga) {
            currentTipoId = tipoSeleccionado;
            actualizarVisibilidadBotonFiltros();
            if (segmentContainer != null) {
                segmentContainer.post(() -> moverIndicadorTipo(currentTipoId, false));
            }
            return;
        }

        if (replaceRecienteMismoTipo) {
            logInit("asegurarTabActualCargado(" + motivo + ") -> omitido replace reciente");
            return;
        }

        Fragment nuevo = crearFragmentPorTipo(tipoSeleccionado);
        if (nuevo == null) {
            return;
        }

        cargarFragment(nuevo, tipoSeleccionado, "asegurarTabActualCargado:" + motivo);
        currentTipoId = tipoSeleccionado;
        if (segmentContainer != null) {
            segmentContainer.post(() -> moverIndicadorTipo(currentTipoId, false));
        }
        actualizarVisibilidadBotonFiltros();
        logInit("asegurarTabActualCargado(" + motivo + ") -> recargado "
                + nuevo.getClass().getSimpleName());
    }

    private boolean coincideFragmentConTipo(@Nullable Fragment fragment, int tipoId) {
        if (fragment == null) {
            return false;
        }
        if (tipoId == R.id.btnSegmentObras) {
            return fragment instanceof FragArte;
        }
        if (tipoId == R.id.btnSegmentServicios) {
            return fragment instanceof FragServicios;
        }
        if (tipoId == R.id.btnSegmentArtistas) {
            return fragment instanceof FragArtistas;
        }
        return false;
    }

    private void aplicarTemaSelector() {
        if (themeManager == null) return;
        limpiarFondoBotonSegmento(btnSegmentObras);
        limpiarFondoBotonSegmento(btnSegmentServicios);
        limpiarFondoBotonSegmento(btnSegmentArtistas);
        tintBackground(segmentContainer, themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL));
        tintBackground(segmentIndicator, themeManager.color(ThemeKeys.ACCENT_PRIMARY));
        CardThemeHelper.applyFilterButton(btnFiltros, themeManager);
        aplicarColoresBotonesTipo();
    }

    private void limpiarFondoBotonSegmento(@Nullable Button button) {
        if (button != null) {
            button.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void tintBackground(@Nullable View view, int color) {
        if (view != null && view.getBackground() != null) {
            view.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void moverIndicadorTipo(int tipoId, boolean animar) {
        if (segmentContainer == null || segmentIndicator == null) return;
        int width = segmentContainer.getWidth();
        if (width <= 0) return;

        int innerWidth = width - segmentContainer.getPaddingLeft() - segmentContainer.getPaddingRight();
        int segmentWidth = innerWidth / 3;
        int index = indexForTipo(tipoId);
        float targetX = segmentContainer.getPaddingLeft() + (segmentWidth * index);

        ViewGroup.LayoutParams params = segmentIndicator.getLayoutParams();
        if (params.width != segmentWidth) {
            params.width = segmentWidth;
            segmentIndicator.setLayoutParams(params);
        }

        segmentIndicator.animate().cancel();
        if (animar) {
            segmentIndicator.animate()
                    .x(targetX)
                    .setDuration(220)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        } else {
            segmentIndicator.setX(targetX);
        }

        aplicarColoresBotonesTipo();
    }

    private int indexForTipo(int tipoId) {
        if (tipoId == R.id.btnSegmentServicios) return 1;
        if (tipoId == R.id.btnSegmentArtistas) return 2;
        return 0;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarColoresBotonesTipo() {
        if (themeManager == null) return;
        int selected = themeManager.color(ThemeKeys.TEXT_PRIMARY);
        int unselected = themeManager.color(ThemeKeys.TEXT_SECONDARY);
        if (btnSegmentObras != null) {
            btnSegmentObras.setTextColor(currentTipoId == R.id.btnSegmentObras ? selected : unselected);
        }
        if (btnSegmentServicios != null) {
            btnSegmentServicios.setTextColor(currentTipoId == R.id.btnSegmentServicios ? selected : unselected);
        }
        if (btnSegmentArtistas != null) {
            btnSegmentArtistas.setTextColor(currentTipoId == R.id.btnSegmentArtistas ? selected : unselected);
        }
    }

    @NonNull
    private String nombreFragment(@Nullable Fragment fragment) {
        return fragment == null ? "null" : fragment.getClass().getSimpleName();
    }

    private boolean fragmentActualSoportaFiltros() {
        return obtenerFragmentActual() instanceof FilterableExplorarFragment;
    }

    private void logPerf(String message) {
        if (!ENABLE_EXPLORAR_DEBUG_LOGS) {
            return;
        }
        Context context = getContext();
        if (context != null
                && context.getApplicationInfo() != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG_PERF, message);
        }
    }

    private void logInit(String message) {
        if (!ENABLE_EXPLORAR_DEBUG_LOGS) {
            return;
        }
        Context context = getContext();
        if (context != null
                && context.getApplicationInfo() != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG_INIT, message);
        }
    }
}
