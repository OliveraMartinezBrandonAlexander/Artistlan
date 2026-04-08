package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragServicios extends Fragment implements FilterableExplorarFragment {

    private static final long LIKE_THROTTLE_MS = 500L;

    private RecyclerView recyclerServicios;
    private TarjetaTextoServicioAdapter adapter;
    private List<TarjetaTextoServicioItem> listaServicios = new ArrayList<>();
    private String tipoServicioFiltroActual = "";
    private int idUsuarioLogueado = -1;
    private FavoritosApi favoritosApi;
    private final Map<Integer, Long> ultimoToqueLikePorServicio = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);

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
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeServicio);
        recyclerServicios.setAdapter(adapter);
    }

    private void toggleLikeServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (idUsuarioLogueado <= 0 || servicioItem.getIdServicio() == null) return;
        Integer idServicio = servicioItem.getIdServicio();
        long ahora = System.currentTimeMillis();
        Long ultimoToque = ultimoToqueLikePorServicio.get(idServicio);
        if (ultimoToque != null && ahora - ultimoToque < LIKE_THROTTLE_MS) {
            return;
        }
        if (likesEnVuelo.contains(idServicio)) {
            return;
        }
        ultimoToqueLikePorServicio.put(idServicio, ahora);
        likesEnVuelo.add(idServicio);

        final boolean favoritoAnterior = servicioItem.isFavorito();
        final int likesAnterior = servicioItem.getLikes();
        servicioItem.setFavorito(!favoritoAnterior);
        servicioItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        adapter.notifyLikeChanged(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idServicio = servicioItem.getIdServicio();
        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnVuelo.remove(idServicio);
                if (!response.isSuccessful()) {
                    servicioItem.setFavorito(favoritoAnterior);
                    servicioItem.setLikes(likesAnterior);
                    adapter.notifyLikeChanged(position);
                    Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnVuelo.remove(idServicio);
                servicioItem.setFavorito(favoritoAnterior);
                servicioItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarTodosLosServicios() {
        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.obtenerTodos(idUsuarioLogueado > 0 ? idUsuarioLogueado : null).enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(Call<List<ServicioDTO>> call, Response<List<ServicioDTO>> response) {
                if (response.code() == 204) { adapter.actualizarLista(new ArrayList<>()); return; }
                if (response.isSuccessful() && response.body() != null) {
                    List<TarjetaTextoServicioItem> items = convertir(response.body());
                    listaServicios = new ArrayList<>(items);
                    if (tipoServicioFiltroActual.isEmpty()) adapter.actualizarLista(items);
                    else filtrarServiciosLocalmente(tipoServicioFiltroActual);

                } else {
                    Toast.makeText(requireContext(), "Error al obtener servicios: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServicioDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red al cargar servicios.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<TarjetaTextoServicioItem> convertir(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> lista = new ArrayList<>();
        for (ServicioDTO dto : dtoList) {
            lista.add(new TarjetaTextoServicioItem(dto.getIdServicio(), dto.getIdUsuario(), dto.getTitulo(), dto.getDescripcion(), dto.getContacto(), dto.getTipoContacto(), dto.getTecnicas(), dto.getNombreUsuario(), dto.getCategoria(), dto.getFotoPerfilAutor(), dto.getPrecioMin(), dto.getPrecioMax(), dto.getLikes() != null ? dto.getLikes() : 0, Boolean.TRUE.equals(dto.getEsFavorito()), false));
        }

        return lista;
    }
}
