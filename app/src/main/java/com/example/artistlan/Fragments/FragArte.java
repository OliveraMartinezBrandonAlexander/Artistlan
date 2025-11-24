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

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.List;

public class FragArte extends Fragment {

    private RecyclerView recyclerView;
    private TarjetaTextoObraAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_arte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        recyclerView = view.findViewById(R.id.recyclerObras);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<TarjetaTextoObraItem> listaObras = new ArrayList<>();
        listaObras.add(new TarjetaTextoObraItem(
                "Amanecer en la Playa",
                "Obra inspirada en un amanecer",
                "Disponible",
                3500.0,
                "url_img1",
                "url_img2",
                "url_img3",
                "Óleo",
                "30x50cm",
                "Naturaleza",
                120,
                1
        ));

        listaObras.add(new TarjetaTextoObraItem(
                "Retrato Azul",
                "Retrato expresivo",
                "Vendido",
                4200.0,
                "img1",
                "img2",
                "img3",
                "Acrílico",
                "50x70cm",
                "Retrato",
                200,
                3
        ));

        adapter = new TarjetaTextoObraAdapter(listaObras, requireContext());
        recyclerView.setAdapter(adapter);
    }
}
