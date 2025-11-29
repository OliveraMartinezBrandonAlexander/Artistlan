package com.example.artistlan.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.artistlan.R;

public class FragSubirObra extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_subir_obra, container, false);

        Spinner spinnerCategoria = view.findViewById(R.id.categoria);

        String[] categorias = {
                "Seleccione una opción",
                "Pintura",
                "Dibujo",
                "Escultura",
                "Fotografía",
                "Digital",
                "Acuarela",
                "Óleo",
                "Acrílico",
                "Grabado",
                "Cerámica",
                "Arte textil",
                "Collage",
                "Ilustración",
                "Mural",
                "Arte abstracto",
                "Retrato",
                "Paisaje",
                "Arte conceptual",
                "Otros"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categorias
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        return view;
    }
}
