package com.example.artistlan;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.Activitys.MainActivity;

public class BotonesMenuInferior implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final Fragment fragmento;
    private Spinner SpinnerMenu;

    public BotonesMenuInferior(Fragment fragmento, View ruta){
        this.fragmento = fragmento;
        inicializarBotones(ruta);
    }

    private void inicializarBotones(View ruta) {
        View view = ruta.findViewById(R.id.MenuInferior);
        if (view == null) return;

        // Botones inferiores
        ImageButton btnInicio    = view.findViewById(R.id.btnInicio);
        ImageButton btnServicios = view.findViewById(R.id.btnServicios);
        ImageButton btnArtistas  = view.findViewById(R.id.btnArtistas);
        ImageButton btnArte      = view.findViewById(R.id.btnArte);
        ImageButton btnPerfil    = view.findViewById(R.id.btnPerfil);

        // Set listeners solo si existen, para evitar NullPointerException
        if (btnInicio    != null) btnInicio.setOnClickListener(this);
        if (btnServicios != null) btnServicios.setOnClickListener(this);
        if (btnArtistas  != null) btnArtistas.setOnClickListener(this);
        if (btnArte      != null) btnArte.setOnClickListener(this);
        if (btnPerfil    != null) btnPerfil.setOnClickListener(this);

        // Spinner del menú superior
        SpinnerMenu = ruta.findViewById(R.id.SpinnerMenu);
        if (SpinnerMenu != null) {
            configurarSpinner();
        }
    }

    private void configurarSpinner() {
        String[] opciones = {"Menu ≡", "Favoritos", "(Próximamente)", "(Próximamente)","Cerrar Sesión"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                fragmento.getContext(),
                android.R.layout.simple_spinner_item,
                opciones
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SpinnerMenu.setAdapter(adapter);
        SpinnerMenu.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            int id = v.getId();

            if (id == R.id.btnInicio) {
                Navigation.findNavController(v).navigate(R.id.fragMain);
            } else if (id == R.id.btnServicios) {
                Navigation.findNavController(v).navigate(R.id.fragServicios);
            } else if (id == R.id.btnArtistas) {
                Navigation.findNavController(v).navigate(R.id.fragArtistas);
            } else if (id == R.id.btnArte) {
                Navigation.findNavController(v).navigate(R.id.fragArte);
            } else if (id == R.id.btnPerfil) {
                Navigation.findNavController(v).navigate(R.id.fragVerPerfil);
            }
        } catch (Exception e) {
            Toast.makeText(fragmento.getContext(), "Error en navegación", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            switch (position) {
                case 0:
                    // "≡" (no hacer nada)
                    break;
                case 1:
                    // Favoritos
                    Navigation.findNavController(parent.getRootView().findViewById(R.id.MenuInferior))
                            .navigate(R.id.fragFavoritos);
                    break;
                case 2:
                    Toast.makeText(fragmento.getContext(), "Próximamente...", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(fragmento.getContext(), "Próximamente...", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Intent irActivity = new Intent(fragmento.requireContext(), MainActivity.class);
                    fragmento.startActivity(irActivity);
                    break;
            }

            // Reset spinner visualmente a "≡"
            if (SpinnerMenu != null) {
                SpinnerMenu.setSelection(0);
            }
        } catch (Exception e) {
            Toast.makeText(fragmento.getContext(), "Error en spinner", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // nada
    }
}
