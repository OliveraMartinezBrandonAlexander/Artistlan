package com.example.artistlan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class BotonesMenuInferior implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //Fragmento desde el que se este llamando a la clase (fragArte,fragArtista,fragServicios,etc.)
    private Fragment fragmento;

    // Constructor que recibe el fragmento actual y la vista del layout
    public BotonesMenuInferior(Fragment fragmento, View ruta){
        this.fragmento = fragmento;

        Button btnInicio,btnServicios,btnArtistas,btnArte,btnPerfil;
        Spinner SpinnerMenu;
        View view;

        //Vista que contiene el id del menú inferior
        view = ruta.findViewById(R.id.MenuInferior);

        // Valida si se encuentra el menú
        if (view == null) return;

        btnInicio = view.findViewById(R.id.btnInicio);
        btnServicios = view.findViewById(R.id.btnServicios);
        btnArtistas = view.findViewById(R.id.btnArtistas);
        btnArte = view.findViewById(R.id.btnArte);
        btnPerfil = view.findViewById(R.id.btnPerfil);

        btnInicio.setOnClickListener(this);
        btnServicios.setOnClickListener(this);
        btnArtistas.setOnClickListener(this);
        btnArte.setOnClickListener(this);
        btnPerfil.setOnClickListener(this);

        //Logica del spinner:
        SpinnerMenu = view.findViewById(R.id.SpinnerMenu);
        SpinnerMenu.setOnItemSelectedListener(this);
        String opciones[] = {"≡","Favoritos","(Proximamente)","(Proximamente)"};
        ArrayAdapter SpinnerAdapter;
        SpinnerAdapter = new ArrayAdapter(fragmento.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                opciones);
        SpinnerMenu.setAdapter(SpinnerAdapter);
    }
    @Override
    public void onClick(View v) {
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
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                break;
            case 1:
                Navigation.findNavController(view).navigate(R.id.fragFavoritos);
                break;
            case 2:
                Toast.makeText(fragmento.getContext(), "Próximamente...", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(fragmento.getContext(), "Próximamente...", Toast.LENGTH_SHORT).show();
                break;
        }
        // vuelve a mostrar "≡" despues de seleccionar algo
        Spinner spinnerMenu = (Spinner) parent;
        spinnerMenu.setSelection(0);

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}