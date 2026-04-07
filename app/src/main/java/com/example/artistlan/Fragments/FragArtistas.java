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
import androidx.navigation.fragment.NavHostFragment;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.ArtistaDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoArtista.adapter.TarjetaTextoArtistaAdapter;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragArtistas extends Fragment implements FilterableExplorarFragment {

    private RecyclerView recyclerViewArtistas;
    private TarjetaTextoArtistaAdapter adapter;
    private String profesionFiltroActual = "";
    private List<TarjetaTextoArtistaItem> listaArtistas = new ArrayList<>();
    private int idUsuarioLogueado = -1;
    private FavoritosApi favoritosApi;
    private ObraApi obraApi;
    private int cargaArtistasToken = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_artistas, container, false);
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
        obraApi = RetrofitClient.getClient().create(ObraApi.class);
        configurarArtistas(view);
        cargarArtistas();
    }

    @Override
    public List<String> getFilterOptions() {
        return Arrays.asList(
                "Pintor", "Escultor", "Fotógrafo", "Ilustrador", "Diseñador gráfico",
                "Diseñador industrial", "Diseñador de moda", "Caricaturista", "Animador", "Ceramista",
                "Grabador", "Artista digital", "Artista plástico", "Maquetador",
                "Decorador", "Restaurador de arte", "Graffitero", "Modelador 3D"
        );
    }

    @Override
    public String getActiveFilter() {
        return profesionFiltroActual;
    }

    @Override
    public void applyFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            clearFilter();
            return;
        }

        if (filter.equalsIgnoreCase(profesionFiltroActual)) {
            profesionFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();
        } else {
            profesionFiltroActual = filter;
            Toast.makeText(getContext(), "Filtrando: " + filter, Toast.LENGTH_SHORT).show();
        }

        cargarArtistas();
    }

    @Override
    public void clearFilter() {
        profesionFiltroActual = "";
        cargarArtistas();
    }

    private void configurarArtistas(View view) {
        recyclerViewArtistas = view.findViewById(R.id.recyclerArtistas);
        recyclerViewArtistas.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TarjetaTextoArtistaAdapter(listaArtistas, requireContext());
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeArtista);
        adapter.setOnVisitarClickListener(this::abrirPerfilPublico);
        recyclerViewArtistas.setAdapter(adapter);
    }
    private void abrirPerfilPublico(TarjetaTextoArtistaItem artistaItem, int position) {
        if (!isAdded() || artistaItem.getIdArtista() == null) return;
        Bundle args = new Bundle();
        args.putInt("idArtista", artistaItem.getIdArtista());
        NavHostFragment.findNavController(this).navigate(R.id.fragVerPerfilPublico, args);
    }
    private void toggleLikeArtista(TarjetaTextoArtistaItem artistaItem, int position) {
        if (idUsuarioLogueado <= 0 || artistaItem.getIdArtista() == null) return;
        final boolean favoritoAnterior = artistaItem.isFavorito();
        final int likesAnterior = artistaItem.getLikes();
        artistaItem.setFavorito(!favoritoAnterior);
        artistaItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        adapter.notifyLikeChanged(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idArtista = artistaItem.getIdArtista();
        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    artistaItem.setFavorito(favoritoAnterior);
                    artistaItem.setLikes(likesAnterior);
                    adapter.notifyLikeChanged(position);
                    Toast.makeText(getContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                artistaItem.setFavorito(favoritoAnterior);
                artistaItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(getContext(), "No se pudo actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarArtistas() {
        final int tokenActual = ++cargaArtistasToken;
        UsuarioApi api = RetrofitClient.getClient().create(UsuarioApi.class);
        api.getArtistas(idUsuarioLogueado > 0 ? idUsuarioLogueado : null).enqueue(new Callback<List<ArtistaDTO>>() {
            @Override
            public void onResponse(Call<List<ArtistaDTO>> call, Response<List<ArtistaDTO>> response) {
                if (!isAdded() || tokenActual != cargaArtistasToken) return;
                if (response.code() == 204) { adapter.actualizarLista(new ArrayList<>()); return; }
                if (response.isSuccessful() && response.body() != null) {
                    List<ArtistaDTO> artistasFiltrados = new ArrayList<>();
                    for (ArtistaDTO artista : response.body()) {
                        if (!profesionFiltroActual.isEmpty()) {
                            String profesionArtista = artista.getCategoria();
                            if (profesionArtista == null ||
                                    !profesionFiltroActual.equalsIgnoreCase(profesionArtista)) {
                                continue;
                            }
                        }
                        artistasFiltrados.add(artista);
                    }

                    if (artistasFiltrados.isEmpty()) {
                        listaArtistas.clear();
                        adapter.actualizarLista(new ArrayList<>());
                        return;
                    }

                    List<TarjetaTextoArtistaItem> nuevaLista = new ArrayList<>();
                    AtomicInteger pendientes = new AtomicInteger(artistasFiltrados.size());
                    for (ArtistaDTO artista : artistasFiltrados) {
                        obtenerMiniObras(artista, miniObras -> {
                            if (!isAdded() || tokenActual != cargaArtistasToken) return;
                            TarjetaTextoArtistaItem item = new TarjetaTextoArtistaItem(
                                    artista.getIdUsuario(),
                                    artista.getUsuario(),
                                    artista.getCategoria(),
                                    artista.getDescripcion(),
                                    artista.getFotoPerfil(),
                                    miniObras,
                                    artista.getLikes() != null ? artista.getLikes() : 0,
                                    Boolean.TRUE.equals(artista.getEsFavorito())
                            );
                            nuevaLista.add(item);

                            if (pendientes.decrementAndGet() == 0) {
                                listaArtistas.clear();
                                listaArtistas.addAll(nuevaLista);
                                adapter.actualizarLista(new ArrayList<>(listaArtistas));
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Error al obtener artistas: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ArtistaDTO>> call, Throwable t) {
                if (!isAdded() || tokenActual != cargaArtistasToken) return;
                t.printStackTrace();
                Toast.makeText(getContext(),
                        "Error de conexión al cargar artistas",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void obtenerMiniObras(ArtistaDTO artista, ArtistaMiniObrasLoader.MiniObrasCallback callback) {
        ArtistaMiniObrasLoader.cargarMiniObrasPorUsuario(obraApi, artista.getIdUsuario(), callback);
    }
}
