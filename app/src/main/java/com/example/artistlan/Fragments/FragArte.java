package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragArte extends Fragment implements FilterableExplorarFragment {

    private RecyclerView recyclerViewObras;
    private TarjetaTextoObraAdapter adapter;
    private String categoriaFiltroActual = "";
    private ObraApi obraApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_arte, container, false);
    }

    public void filtrarBusqueda(String texto) {
        if (adapter != null) {
            adapter.filtrar(texto);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this);

        obraApi = RetrofitClient.getClient().create(ObraApi.class);

        configurarObras(view);
    }

    @Override
    public List<String> getFilterOptions() {
        return Arrays.asList(
                "Pintura", "Dibujo", "Escultura", "Fotografía", "Digital",
                "Acuarela", "Óleo", "Acrílico", "Grabado", "Cerámica",
                "Arte textil", "Collage", "Ilustración", "Mural",
                "Arte abstracto", "Retrato", "Paisaje", "Arte conceptual"
        );
    }

    @Override
    public String getActiveFilter() {
        return categoriaFiltroActual;
    }

    @Override
    public void applyFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            clearFilter();
            return;
        }

        if (filter.equalsIgnoreCase(categoriaFiltroActual)) {
            categoriaFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();
        } else {
            categoriaFiltroActual = filter;
            Toast.makeText(getContext(), "Filtrando: " + filter, Toast.LENGTH_SHORT).show();
        }

        obtenerObrasDeAPI();
    }

    @Override
    public void clearFilter() {
        categoriaFiltroActual = "";
        Toast.makeText(getContext(), "Filtros borrados", Toast.LENGTH_SHORT).show();
        obtenerObrasDeAPI();
    }

    private void configurarObras(View view) {
        recyclerViewObras = view.findViewById(R.id.recyclerObras);
        recyclerViewObras.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
        recyclerViewObras.setAdapter(adapter);

        obtenerObrasDeAPI();
    }

    private void obtenerObrasDeAPI() {

        Call<List<ObraDTO>> call = obraApi.obtenerTodasLasObras();

        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                List<TarjetaTextoObraItem> items = new ArrayList<>();

                for (ObraDTO dto : response.body()) {
                    if (!categoriaFiltroActual.isEmpty() &&
                            (dto.getNombreCategoria() == null || !categoriaFiltroActual.equalsIgnoreCase(dto.getNombreCategoria()))) {
                        continue;
                    }

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

                adapter.actualizarLista(items);
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
