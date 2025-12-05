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
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.model.ServicioDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMisServicios extends Fragment {

    private RecyclerView recyclerMisServicios;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_mis_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMisServicios = view.findViewById(R.id.recyclerMisServicios);
        recyclerMisServicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        cargarServiciosDelUsuario();
    }


    private List<TarjetaTextoServicioItem> convertirDTOaItem(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();

        for (ServicioDTO dto : dtoList) {
            items.add(new TarjetaTextoServicioItem(
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getContacto(),
                    dto.getTecnicas(),
                    dto.getNombreUsuario(),
                    dto.getCategoria(),
                    dto.getFotoPerfilAutor(),
                    false
            ));
        }

        return items;
    }

    private void cargarServiciosDelUsuario() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            return;
        }

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Call<List<ServicioDTO>> call = api.obtenerServiciosDeUsuario(idUsuario);

        Log.d("Retrofit_URL", "Llamando a: " + call.request().url().toString());

        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(Call<List<ServicioDTO>> call, Response<List<ServicioDTO>> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) {
                    return;
                }

                List<ServicioDTO> dtos = response.body();
                if (dtos.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay servicios disponibles.", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<TarjetaTextoServicioItem> items = convertirDTOaItem(dtos);

                recyclerMisServicios.post(() -> {

                    TarjetaTextoServicioAdapter adapter = new TarjetaTextoServicioAdapter(items, requireContext());
                    recyclerMisServicios.setAdapter(adapter);

                    recyclerMisServicios.invalidate();
                });

                Toast.makeText(requireContext(), "Servicios cargados: " + items.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<ServicioDTO>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(requireContext(), "Error de red/API: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}