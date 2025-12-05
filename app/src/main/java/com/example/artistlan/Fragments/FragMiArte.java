package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.artistlan.R;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMiArte extends Fragment {

    private RecyclerView recyclerMisObras;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_mi_arte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMisObras = view.findViewById(R.id.recyclerMiArte);
        recyclerMisObras.setLayoutManager(new LinearLayoutManager(requireContext()));

        cargarObrasDelUsuario();
    }

    private List<TarjetaTextoObraItem> convertirDTOaItem(List<ObraDTO> dtoList) {
        List<TarjetaTextoObraItem> items = new ArrayList<>();

        for (ObraDTO dto : dtoList) {
            items.add(new TarjetaTextoObraItem(
                    dto.getIdObra(),
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getEstado(),
                    dto.getPrecio(),
                    dto.getImagen1(),
                    dto.getImagen2(),
                    dto.getImagen3(),
                    dto.getTecnicas(),
                    dto.getMedidas(),
                    dto.getLikes() != null ? dto.getLikes() : 0,
                    dto.getNombreAutor(),
                    dto.getNombreCategoria(),
                    dto.getFotoPerfilAutor(),
                    false,
                    false
            ));
        }

        return items;
    }

    private void cargarObrasDelUsuario() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            Toast.makeText(requireContext(), "Error: usuario no logueado.", Toast.LENGTH_SHORT).show();
            return;
        }

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Call<List<ObraDTO>> call = api.obtenerObrasDeUsuario(idUsuario);

        Log.d("Retrofit_URL", "Llamando a: " + call.request().url().toString());

        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(Call<List<ObraDTO>> call, Response<List<ObraDTO>> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Error al cargar obras.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ObraDTO> dtos = response.body();
                if (dtos == null || dtos.isEmpty()) {
                    Toast.makeText(requireContext(), "No tienes obras creadas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<TarjetaTextoObraItem> items = convertirDTOaItem(dtos);

                recyclerMisObras.post(() -> {
                    TarjetaTextoObraAdapter adapter = new TarjetaTextoObraAdapter(items, requireContext());
                    recyclerMisObras.setAdapter(adapter);
                    recyclerMisObras.invalidate();
                });

                Toast.makeText(requireContext(), "Obras cargadas: " + items.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<ObraDTO>> call, Throwable t) {
                t.printStackTrace();
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}