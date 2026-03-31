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
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragServicios extends Fragment implements FilterableExplorarFragment {

    private RecyclerView recyclerServicios;
    private TarjetaTextoServicioAdapter adapter;
    private List<TarjetaTextoServicioItem> listaServicios = new ArrayList<>();
    private String tipoServicioFiltroActual = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_servicios, container, false);
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

        configurarServicios(view);
        cargarTodosLosServicios();
    }

    @Override
    public List<String> getFilterOptions() {
        return Arrays.asList(
                "Pintor", "Escultor", "Fotógrafo", "Ilustrador", "Diseñador gráfico",
                "Diseñador industrial", "Diseñador de moda", "Caricaturista", "Animador", "Artesano",
                "Ceramista", "Grabador", "Artista digital", "Artista plástico",
                "Maquetador", "Decorador", "Restaurador de arte", "Graffitero", "Modelador 3D"
        );
    }

    @Override
    public String getActiveFilter() {
        return tipoServicioFiltroActual;
    }

    @Override
    public void applyFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            clearFilter();
            return;
        }

        if (tipoServicioFiltroActual.equalsIgnoreCase(filter)) {
            tipoServicioFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();

            if (adapter != null) {
                adapter.actualizarLista(new ArrayList<>(listaServicios));
            }
            return;
        }

        tipoServicioFiltroActual = filter;
        Toast.makeText(getContext(), "Filtrando: " + filter, Toast.LENGTH_SHORT).show();
        filtrarServiciosLocalmente(filter);
    }

    @Override
    public void clearFilter() {
        tipoServicioFiltroActual = "";

        if (adapter != null) {
            adapter.actualizarLista(new ArrayList<>(listaServicios));
        }

        Toast.makeText(getContext(), "Filtros borrados", Toast.LENGTH_SHORT).show();
    }

    private void filtrarServiciosLocalmente(String tipoServicio) {
        List<TarjetaTextoServicioItem> serviciosFiltrados = new ArrayList<>();

        if (listaServicios.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para filtrar.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (TarjetaTextoServicioItem servicio : listaServicios) {
            String categoria = servicio.getCategoria();
            if (categoria != null && categoria.equalsIgnoreCase(tipoServicio)) {
                serviciosFiltrados.add(servicio);
            }
        }

        adapter.actualizarLista(serviciosFiltrados);
    }

    private void configurarServicios(View view) {
        recyclerServicios = view.findViewById(R.id.recyclerServicios);
        recyclerServicios.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        recyclerServicios.setAdapter(adapter);
    }

    private void cargarTodosLosServicios() {
        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Call<List<ServicioDTO>> call = api.obtenerTodos();

        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(Call<List<ServicioDTO>> call, Response<List<ServicioDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TarjetaTextoServicioItem> items = convertir(response.body());

                    listaServicios = new ArrayList<>(items);
                    if (tipoServicioFiltroActual.isEmpty()) {
                        adapter.actualizarLista(items);
                    } else {
                        filtrarServiciosLocalmente(tipoServicioFiltroActual);
                    }

                } else {
                    Toast.makeText(requireContext(), "Error al obtener servicios: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServicioDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red al cargar servicios.", Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private List<TarjetaTextoServicioItem> convertir(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> lista = new ArrayList<>();

        for (ServicioDTO dto : dtoList) {
            lista.add(new TarjetaTextoServicioItem(
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

        return lista;
    }
}
