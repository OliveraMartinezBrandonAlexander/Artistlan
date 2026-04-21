package com.example.artistlan.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import androidx.fragment.app.Fragment;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class FragExplorar extends Fragment {

    private static final int MENU_GROUP_FILTERS = 100;
    private static final int MENU_ID_CLEAR_FILTERS = 1000;

    private ChipGroup chipGroup;
    private SearchView searchView;
    private ImageButton btnFiltros;
    private boolean filtrosVisibles = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_explorar, container, false);
        ThemeModuleStyler.styleFragment(this, view);

        new com.example.artistlan.BotonesMenuSuperior(this);

        chipGroup = view.findViewById(R.id.chipGroupExplorar);
        searchView = view.findViewById(R.id.searchExplorar);
        btnFiltros = view.findViewById(R.id.btnFiltrosExplorar);
        btnFiltros.setVisibility(View.GONE);

        // Fragment inicial
        cargarFragment(new FragArte());

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            cerrarTeclado();
            searchView.setQuery("", false);
            searchView.clearFocus();

            Fragment fragment = null;

            searchView.animate()
                    .alpha(0.5f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        searchView.animate().alpha(1f).setDuration(100).start();
                    })
                    .start();

            if (checkedId == R.id.chipObras) {

                fragment = new FragArte();

            } else if (checkedId == R.id.chipServicios) {

                fragment = new FragServicios();

            } else if (checkedId == R.id.chipArtistas) {

                fragment = new FragArtistas();

            }

            if (fragment != null) {
                cargarFragment(fragment);
            }

        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                mostrarBotonFiltros(true);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mostrarBotonFiltros(newText != null && !newText.trim().isEmpty());

                Fragment fragmentActual = getChildFragmentManager()
                        .findFragmentById(R.id.fragmentContainerExplorar);

                if(fragmentActual instanceof FragArte){
                    ((FragArte) fragmentActual).filtrarBusqueda(newText);
                }

                if(fragmentActual instanceof FragServicios){
                    ((FragServicios) fragmentActual).filtrarBusqueda(newText);
                }

                if(fragmentActual instanceof FragArtistas){
                    ((FragArtistas) fragmentActual).filtrarBusqueda(newText);
                }

                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mostrarBotonFiltros(true);
            } else {
                CharSequence query = searchView.getQuery();
                mostrarBotonFiltros(query != null && query.length() > 0);
            }
        });

        btnFiltros.setOnClickListener(v -> mostrarMenuFiltros());

        return view;
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
        Fragment fragmentActual = getChildFragmentManager()
                .findFragmentById(R.id.fragmentContainerExplorar);

        if (!(fragmentActual instanceof FilterableExplorarFragment)) {
            return;
        }

        FilterableExplorarFragment filterableFragment = (FilterableExplorarFragment) fragmentActual;
        List<String> filtros = filterableFragment.getFilterOptions();

        PopupMenu popupMenu = new PopupMenu(requireContext(), btnFiltros);
        Menu menu = popupMenu.getMenu();
        String filtroActivo = filterableFragment.getActiveFilter();

        for (int i = 0; i < filtros.size(); i++) {
            String filtro = filtros.get(i);
            menu.add(MENU_GROUP_FILTERS, i, i, filtro)
                    .setCheckable(true)
                    .setChecked(filtro.equalsIgnoreCase(filtroActivo));
        }

        menu.setGroupCheckable(MENU_GROUP_FILTERS, true, true);

        menu.add(Menu.NONE, MENU_ID_CLEAR_FILTERS, filtros.size(), "Borrar filtros")
                .setEnabled(filtroActivo != null && !filtroActivo.isEmpty());

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
        InputMethodManager imm = (InputMethodManager) requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void cargarFragment(Fragment fragment){

        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainerExplorar, fragment)
                .commit();

    }
}
