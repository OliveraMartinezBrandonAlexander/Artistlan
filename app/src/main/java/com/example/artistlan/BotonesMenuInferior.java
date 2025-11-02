package com.example.artistlan;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.artistlan.Activitys.MainActivity;


public class BotonesMenuInferior implements AdapterView.OnItemSelectedListener {

    private final Fragment fragmento;
    private Spinner SpinnerMenu;

    public BotonesMenuInferior(Fragment fragmento, View ruta) {
        this.fragmento = fragmento;
        inicializarSpinner(ruta);
    }

    private void inicializarSpinner(View ruta) {
        SpinnerMenu = ruta.findViewById(R.id.SpinnerMenu);
        if (SpinnerMenu != null) {
            String[] opciones = {"Menu ≡", "Favoritos", "(Próximamente)", "(Próximamente)", "Cerrar Sesión"};

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    fragmento.getContext(),
                    android.R.layout.simple_spinner_item,
                    opciones
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            SpinnerMenu.setAdapter(adapter);
            SpinnerMenu.setOnItemSelectedListener(this);
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (position == 1) {
                Navigation.findNavController(fragmento.requireView()).navigate(R.id.fragFavoritos);
            } else if (position == 2 || position == 3) {
                Toast.makeText(fragmento.getContext(), "Próximamente...", Toast.LENGTH_SHORT).show();
            } else if (position == 4) {
                Intent irActivity = new Intent(fragmento.requireContext(), MainActivity.class);
                fragmento.startActivity(irActivity);
            }

            if (SpinnerMenu != null) {
                SpinnerMenu.setSelection(0);
            }
        } catch (Exception e) {
            Toast.makeText(fragmento.getContext(), "Error en spinner", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No hacer nada
    }
}