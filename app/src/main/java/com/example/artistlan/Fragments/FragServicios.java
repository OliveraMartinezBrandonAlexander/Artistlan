package com.example.artistlan.Fragments;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.List;


public class FragServicios extends Fragment{

    RecyclerView recyclerServicios;
    TarjetaTextoServicioAdapter adapter;
    List<TarjetaTextoServicioItem> listaServicios = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerServicios = view.findViewById(R.id.recyclerServicios);
        recyclerServicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Datos de prueba
        listaServicios.add(new TarjetaTextoServicioItem("Retratos digitales", "Hago ilustraciones personalizadas", "55-555-555", "Digital", "Art By Lua"));
        listaServicios.add(new TarjetaTextoServicioItem("Murales", "Murales en pared y negocios", "44-444-444", "Acrílico / Spray", "MurArt Studio"));

        adapter = new TarjetaTextoServicioAdapter(listaServicios, requireContext());
        recyclerServicios.setAdapter(adapter);
    }
}
