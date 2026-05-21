package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.utils.CardThemeHelper;

import java.util.List;

public class FragExplorar extends Fragment {

    private static final String TAG_PERF = "ExplorarPerfDebug";
    private static final String TAG_INIT = "ExplorarInitDebug";
    private static final boolean ENABLE_EXPLORAR_DEBUG_LOGS = false;

    private SearchView searchView;
    private ImageButton btnFiltros;
    private View panelFiltros;
    private View segmentIndicator;
    private ViewGroup segmentContainer;
    private Button btnSegmentObras;
    private Button btnSegmentServicios;
    private Button btnSegmentArtistas;
    private ViewPager2 viewPager;
    private ExplorarPagerAdapter pagerAdapter;
    private PopupWindow filtrosPopup;

    private boolean filtrosVisibles = false;
    private boolean panelFiltrosVisible = true;
    private int currentTipoId = View.NO_ID;
    private boolean ignorarEventosBusqueda = false;
    private boolean limpiarBusquedaAlCambiarPagina = false;
    private ThemeManager themeManager;

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
        viewPager = view.findViewById(R.id.viewPagerExplorar);

        configurarBuscador();
        configurarBotonFiltros();
        configurarSelectorTipos();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.post(this::restaurarUiFiltros);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            view.post(this::restaurarUiFiltros);
        }
    }

    @Override
    public void onDestroyView() {
        if (filtrosPopup != null) {
            filtrosPopup.dismiss();
            filtrosPopup = null;
        }
        super.onDestroyView();
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

        btnFiltros.animate().cancel();
        btnFiltros.setVisibility(View.VISIBLE);
        btnFiltros.setAlpha(1f);
        btnFiltros.setTranslationX(0f);
        btnFiltros.setScaleX(1f);
        btnFiltros.setScaleY(1f);
        btnFiltros.setOnClickListener(v -> mostrarMenuFiltros());
    }

    private void configurarSelectorTipos() {
        if (segmentContainer == null
                || btnSegmentObras == null
                || btnSegmentServicios == null
                || btnSegmentArtistas == null
                || viewPager == null) {
            return;
        }

        if (pagerAdapter == null) {
            pagerAdapter = new ExplorarPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(3);
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    int tipoId = tipoForIndex(position);
                    currentTipoId = tipoId;
                    moverIndicadorTipo(tipoId, true);

                    if (limpiarBusquedaAlCambiarPagina) {
                        limpiarBusquedaAlCambiarPagina = false;
                        aplicarBusquedaAlFragmentActual("");
                    } else if (searchView != null) {
                        aplicarBusquedaAlFragmentActual(searchView.getQuery().toString());
                    }

                    actualizarVisibilidadBotonFiltros();
                    logPerf("Cambio tab swipe -> tipoId=" + currentTipoId + ", position=" + position);
                }
            });
        }

        aplicarTemaSelector();

        int tipoInicial = currentTipoId == View.NO_ID ? R.id.btnSegmentObras : currentTipoId;
        currentTipoId = tipoInicial;
        viewPager.setCurrentItem(indexForTipo(tipoInicial), false);
        actualizarVisibilidadBotonFiltros();

        btnSegmentObras.setOnClickListener(v -> seleccionarTipo(R.id.btnSegmentObras, true));
        btnSegmentServicios.setOnClickListener(v -> seleccionarTipo(R.id.btnSegmentServicios, true));
        btnSegmentArtistas.setOnClickListener(v -> seleccionarTipo(R.id.btnSegmentArtistas, true));

        segmentContainer.post(() -> moverIndicadorTipo(currentTipoId, false));
    }

    private void seleccionarTipo(int tipoId, boolean animar) {
        if (tipoId == View.NO_ID || viewPager == null) {
            return;
        }

        int targetIndex = indexForTipo(tipoId);
        if (viewPager.getCurrentItem() == targetIndex && currentTipoId == tipoId) {
            moverIndicadorTipo(currentTipoId, animar);
            return;
        }

        cerrarTeclado();
        limpiarBusquedaSinNotificar();
        limpiarBusquedaAlCambiarPagina = true;

        currentTipoId = tipoId;
        moverIndicadorTipo(tipoId, animar);
        actualizarVisibilidadBotonFiltros();
        viewPager.setCurrentItem(targetIndex, true);

        logInit("seleccionarTipo -> tipoId=" + tipoId + ", index=" + targetIndex);
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
        if (btnFiltros == null) {
            return;
        }

        mostrar = true;
        btnFiltros.animate().cancel();

        if (filtrosVisibles == mostrar) {
            aplicarEstadoFinalBotonFiltros(mostrar);
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
        FilterableExplorarFragment filterableFragment = obtenerFilterableActual();
        if (filterableFragment == null || btnFiltros == null) {
            return;
        }

        List<String> filtros = filterableFragment.getFilterOptions();

        if (filtros == null || filtros.isEmpty()) {
            return;
        }

        if (filtrosPopup != null && filtrosPopup.isShowing()) {
            filtrosPopup.dismiss();
            return;
        }

        String filtroActivo = filterableFragment.getActiveFilter();
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(12));
        container.setBackground(createFilterDialogBackground());
        container.setClickable(true);

        TextView title = new TextView(requireContext());
        title.setText("Filtrar");
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        ThemeApplier.applyTextPrimary(title, themeManager);
        container.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

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

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setFillViewport(false);
        scrollView.addView(radioGroup, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.min(dpToPx(300), getResources().getDisplayMetrics().heightPixels - dpToPx(260))
        ));

        Button clearButton = new Button(requireContext());
        clearButton.setText("Quitar filtro");
        clearButton.setAllCaps(false);
        clearButton.setEnabled(!TextUtils.isEmpty(filtroActivo));
        ThemeApplier.applySecondaryButton(clearButton, themeManager);
        container.addView(clearButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(44)
        ));

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId >= 0 && checkedId < filtros.size()) {
                filterableFragment.applyFilter(filtros.get(checkedId));
                if (filtrosPopup != null) {
                    filtrosPopup.dismiss();
                }
            }
        });

        clearButton.setOnClickListener(v -> {
            filterableFragment.clearFilter();
            if (filtrosPopup != null) {
                filtrosPopup.dismiss();
            }
        });

        int width = Math.min(dpToPx(280), getResources().getDisplayMetrics().widthPixels - dpToPx(72));
        filtrosPopup = new PopupWindow(container, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        filtrosPopup.setOutsideTouchable(true);
        filtrosPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filtrosPopup.setElevation(dpToPx(12));
        filtrosPopup.showAsDropDown(btnFiltros, btnFiltros.getWidth() - width, dpToPx(8));
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

    private void aplicarEstadoFinalBotonFiltros(boolean mostrar) {
        if (btnFiltros == null) {
            return;
        }
        btnFiltros.setVisibility(View.VISIBLE);
        btnFiltros.setAlpha(1f);
        btnFiltros.setTranslationX(0f);
        btnFiltros.setScaleX(1f);
        btnFiltros.setScaleY(1f);
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

    private void restaurarUiFiltros() {
        if (!isAdded()) {
            return;
        }
        if (themeManager == null) {
            themeManager = new ThemeManager(requireContext());
        }
        aplicarTemaSelector();
        if (btnFiltros != null) {
            btnFiltros.setOnClickListener(v -> mostrarMenuFiltros());
        }
        actualizarVisibilidadBotonFiltros();
        if (currentTipoId != View.NO_ID && segmentContainer != null) {
            segmentContainer.post(() -> moverIndicadorTipo(currentTipoId, false));
        }
    }

    @Nullable
    private Fragment obtenerFragmentActual() {
        if (viewPager == null) {
            return null;
        }

        int currentIndex = viewPager.getCurrentItem();
        if (pagerAdapter != null) {
            Fragment fragment = pagerAdapter.getFragmentForPosition(currentIndex);
            if (fragment != null) {
                return fragment;
            }
        }

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment == null) {
                continue;
            }
            if (currentIndex == 0 && fragment instanceof FragArte) {
                return fragment;
            }
            if (currentIndex == 1 && fragment instanceof FragServicios) {
                return fragment;
            }
            if (currentIndex == 2 && fragment instanceof FragArtistas) {
                return fragment;
            }
        }

        return null;
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

    private int tipoForIndex(int index) {
        if (index == 1) return R.id.btnSegmentServicios;
        if (index == 2) return R.id.btnSegmentArtistas;
        return R.id.btnSegmentObras;
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

    private boolean fragmentActualSoportaFiltros() {
        return obtenerFilterableActual() != null
                || currentTipoId == R.id.btnSegmentObras
                || currentTipoId == R.id.btnSegmentServicios
                || currentTipoId == R.id.btnSegmentArtistas;
    }

    @Nullable
    private FilterableExplorarFragment obtenerFilterableActual() {
        Fragment fragmentActual = obtenerFragmentActual();
        if (fragmentActual instanceof FilterableExplorarFragment) {
            return (FilterableExplorarFragment) fragmentActual;
        }
        return null;
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

    private static class ExplorarPagerAdapter extends FragmentStateAdapter {
        private final SparseArray<Fragment> fragmentsByPosition = new SparseArray<>();

        ExplorarPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            if (position == 1) {
                fragment = new FragServicios();
            } else if (position == 2) {
                fragment = new FragArtistas();
            } else {
                fragment = new FragArte();
            }
            fragmentsByPosition.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Nullable
        Fragment getFragmentForPosition(int position) {
            return fragmentsByPosition.get(position);
        }
    }
}
