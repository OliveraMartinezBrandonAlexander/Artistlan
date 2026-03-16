package com.example.artistlan.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;

import com.example.artistlan.R;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.search.SearchBar;

public class FragExplorar extends Fragment {

    private ChipGroup chipGroup;
    SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_explorar, container, false);

        new com.example.artistlan.BotonesMenuSuperior(this);

        chipGroup = view.findViewById(R.id.chipGroupExplorar);
        searchView = view.findViewById(R.id.searchExplorar);

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

        searchView = view.findViewById(R.id.searchExplorar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

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

        return view;
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