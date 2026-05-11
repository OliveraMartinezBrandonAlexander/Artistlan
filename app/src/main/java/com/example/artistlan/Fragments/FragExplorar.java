package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class FragExplorar extends Fragment {

    private static final String TAG_PERF = "ExplorarPerfDebug";
    private static final String TAG_INIT = "ExplorarInitDebug";
    private static final boolean ENABLE_EXPLORAR_DEBUG_LOGS = false;
    private static final long MIN_REPLACE_INTERVAL_MS = 450L;
    private static final int MENU_GROUP_FILTERS = 100;
    private static final int MENU_ID_CLEAR_FILTERS = 1000;

    private ChipGroup chipGroup;
    private SearchView searchView;
    private ImageButton btnFiltros;
    private boolean filtrosVisibles = false;
    private int currentChipId = View.NO_ID;
    private int lastLoadedChipId = View.NO_ID;
    private long lastReplaceTimestampMs = 0L;
    private boolean ignorarEventosBusqueda = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_explorar, container, false);
        ThemeModuleStyler.styleFragment(this, view);

        new com.example.artistlan.BotonesMenuSuperior(this);

        chipGroup = view.findViewById(R.id.chipGroupExplorar);
        searchView = view.findViewById(R.id.searchExplorar);
        btnFiltros = view.findViewById(R.id.btnFiltrosExplorar);

        configurarBuscador();
        configurarBotonFiltros();
        configurarChips();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logInit("onViewCreated -> checkedChipId=" + (chipGroup != null ? chipGroup.getCheckedChipId() : View.NO_ID)
                + ", currentChipId=" + currentChipId
                + ", fragmentActual=" + nombreFragment(obtenerFragmentActual()));
        asegurarTabActualCargado("onViewCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        logInit("onResume -> checkedChipId=" + (chipGroup != null ? chipGroup.getCheckedChipId() : View.NO_ID)
                + ", currentChipId=" + currentChipId
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

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> actualizarVisibilidadBotonFiltros());

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

    private void configurarChips() {
        if (chipGroup == null) return;

        int chipInicial = chipGroup.getCheckedChipId();
        if (chipInicial == View.NO_ID) {
            chipInicial = R.id.chipObras;
            chipGroup.check(chipInicial);
        }

        Fragment actual = getChildFragmentManager().findFragmentById(R.id.fragmentContainerExplorar);
        if (actual == null) {
            Fragment inicial = crearFragmentPorChip(chipInicial);
            if (inicial != null) {
                logInit("configurarChips -> carga inicial, chip=" + chipInicial
                        + ", fragment=" + inicial.getClass().getSimpleName());
                cargarFragment(inicial, chipInicial, "configurarChips-inicial");
            }
        } else if (coincideFragmentConChip(actual, chipInicial)) {
            lastLoadedChipId = chipInicial;
        }
        currentChipId = chipInicial;
        actualizarVisibilidadBotonFiltros();
        logPerf("Tab inicial -> chipId=" + currentChipId);
        logInit("configurarChips -> currentChipId=" + currentChipId
                + ", fragmentActual=" + nombreFragment(actual));

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID || checkedId == currentChipId) {
                return;
            }

            cerrarTeclado();
            limpiarBusquedaSinNotificar();
            actualizarVisibilidadBotonFiltros();

            Fragment fragment = crearFragmentPorChip(checkedId);
            if (fragment != null) {
                logInit("onCheckedChange -> checkedId=" + checkedId
                        + ", currentChipId(before)=" + currentChipId
                        + ", fragmentNuevo=" + fragment.getClass().getSimpleName());
                cargarFragment(fragment, checkedId, "onCheckedChange");
                currentChipId = checkedId;
                logPerf("Cambio tab -> chipId=" + currentChipId
                        + ", fragment=" + fragment.getClass().getSimpleName());
                logInit("onCheckedChange -> currentChipId(after)=" + currentChipId);
                actualizarVisibilidadBotonFiltros();
            }
        });
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

        PopupMenu popupMenu = new PopupMenu(requireContext(), btnFiltros);
        Menu menu = popupMenu.getMenu();
        String filtroActivo = filterableFragment.getActiveFilter();

        for (int i = 0; i < filtros.size(); i++) {
            String filtro = filtros.get(i);
            menu.add(MENU_GROUP_FILTERS, i, i, filtro)
                    .setCheckable(true)
                    .setChecked(filtroActivo != null && filtro.equalsIgnoreCase(filtroActivo));
        }

        menu.setGroupCheckable(MENU_GROUP_FILTERS, true, true);

        menu.add(Menu.NONE, MENU_ID_CLEAR_FILTERS, filtros.size(), "Borrar filtros")
                .setEnabled(!TextUtils.isEmpty(filtroActivo));

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == MENU_ID_CLEAR_FILTERS) {
                filterableFragment.clearFilter();
                return true;
            }

            if (item.getGroupId() == MENU_GROUP_FILTERS) {
                filterableFragment.applyFilter(item.getTitle().toString());
                return true;
            }

            return false;
        });

        popupMenu.show();
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

    private void cargarFragment(@NonNull Fragment fragment, int chipId, @NonNull String motivo) {
        lastLoadedChipId = chipId;
        lastReplaceTimestampMs = SystemClock.elapsedRealtime();
        logInit("cargarFragment(" + motivo + ") -> chipId=" + chipId
                + ", fragment=" + fragment.getClass().getSimpleName()
                + ", currentChipId=" + currentChipId);
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
    private Fragment crearFragmentPorChip(int chipId) {
        if (chipId == R.id.chipObras) {
            return new FragArte();
        }
        if (chipId == R.id.chipServicios) {
            return new FragServicios();
        }
        if (chipId == R.id.chipArtistas) {
            return new FragArtistas();
        }
        return null;
    }

    private void asegurarTabActualCargado(@NonNull String motivo) {
        if (!isAdded() || chipGroup == null) {
            return;
        }
        int chipSeleccionado = chipGroup.getCheckedChipId();
        if (chipSeleccionado == View.NO_ID) {
            chipSeleccionado = R.id.chipObras;
            chipGroup.check(chipSeleccionado);
        }

        Fragment actual = obtenerFragmentActual();
        boolean coincide = coincideFragmentConChip(actual, chipSeleccionado);
        boolean necesitaCarga = actual == null || !coincide || actual.getView() == null;
        boolean replaceRecienteMismoChip = chipSeleccionado == lastLoadedChipId
                && (SystemClock.elapsedRealtime() - lastReplaceTimestampMs) < MIN_REPLACE_INTERVAL_MS;

        logInit("asegurarTabActualCargado(" + motivo + ") -> chip=" + chipSeleccionado
                + ", currentChipId=" + currentChipId
                + ", fragmentActual=" + nombreFragment(actual)
                + ", coincide=" + coincide
                + ", viewNull=" + (actual == null || actual.getView() == null)
                + ", necesitaCarga=" + necesitaCarga
                + ", replaceRecienteMismoChip=" + replaceRecienteMismoChip);

        if (!necesitaCarga) {
            currentChipId = chipSeleccionado;
            actualizarVisibilidadBotonFiltros();
            return;
        }

        if (replaceRecienteMismoChip) {
            logInit("asegurarTabActualCargado(" + motivo + ") -> omitido replace reciente");
            return;
        }

        Fragment nuevo = crearFragmentPorChip(chipSeleccionado);
        if (nuevo == null) {
            return;
        }

        cargarFragment(nuevo, chipSeleccionado, "asegurarTabActualCargado:" + motivo);
        currentChipId = chipSeleccionado;
        actualizarVisibilidadBotonFiltros();
        logInit("asegurarTabActualCargado(" + motivo + ") -> recargado "
                + nuevo.getClass().getSimpleName());
    }

    private boolean coincideFragmentConChip(@Nullable Fragment fragment, int chipId) {
        if (fragment == null) {
            return false;
        }
        if (chipId == R.id.chipObras) {
            return fragment instanceof FragArte;
        }
        if (chipId == R.id.chipServicios) {
            return fragment instanceof FragServicios;
        }
        if (chipId == R.id.chipArtistas) {
            return fragment instanceof FragArtistas;
        }
        return false;
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
