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
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.ArtistaDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoArtista.adapter.TarjetaTextoArtistaAdapter;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragArtistas extends Fragment implements FilterableExplorarFragment {

    private RecyclerView recyclerViewArtistas;
    private TarjetaTextoArtistaAdapter adapter;
    private String profesionFiltroActual = "";
    private List<TarjetaTextoArtistaItem> listaArtistas = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        Toast.makeText(getContext(), "Filtros borrados", Toast.LENGTH_SHORT).show();
        cargarArtistas();
    }

    private void configurarArtistas(View view) {
        recyclerViewArtistas = view.findViewById(R.id.recyclerArtistas);
        recyclerViewArtistas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TarjetaTextoArtistaAdapter(listaArtistas, requireContext());
        recyclerViewArtistas.setAdapter(adapter);
    }

    private void cargarArtistas() {
        UsuarioApi api = RetrofitClient.getClient().create(UsuarioApi.class);
        Call<List<ArtistaDTO>> call = api.getArtistas();

        listaArtistas.clear();
        adapter.actualizarLista(new ArrayList<>());

        call.enqueue(new Callback<List<ArtistaDTO>>() {
            @Override
            public void onResponse(Call<List<ArtistaDTO>> call,
                                   Response<List<ArtistaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ArtistaDTO> artistas = response.body();

                    int artistasQueCumplenFiltro = 0;

                    for (ArtistaDTO artista : artistas) {
                        if (!profesionFiltroActual.isEmpty()) {
                            String profesionArtista = artista.getCategoria();
                            if (profesionArtista == null ||
                                    !profesionFiltroActual.equalsIgnoreCase(profesionArtista)) {
                                continue;
                            }
                        }

                        artistasQueCumplenFiltro++;
                        obtenerMiniObras(artista);
                    }

                    if (artistasQueCumplenFiltro == 0) {
                        adapter.actualizarLista(new ArrayList<>());
                    }

                } else {
                    Toast.makeText(getContext(),
                            "Error al obtener artistas: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ArtistaDTO>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(),
                        "Error de conexión al cargar artistas",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void obtenerMiniObras(ArtistaDTO artista) {
        ObraApi obraApi = RetrofitClient.getClient().create(ObraApi.class);
        Call<List<ObraDTO>> call = obraApi.obtenerObrasDeUsuario(artista.getIdUsuario());

        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(Call<List<ObraDTO>> call, Response<List<ObraDTO>> response) {
                List<String> miniObras = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    List<ObraDTO> obras = response.body();
                    for (int i = 0; i < Math.min(3, obras.size()); i++) {
                        miniObras.add(obras.get(i).getImagen1());
                    }
                }
                while (miniObras.size() < 3) {
                    miniObras.add(null);
                }

                TarjetaTextoArtistaItem item = new TarjetaTextoArtistaItem(
                        artista.getUsuario(),
                        artista.getCategoria(),
                        artista.getDescripcion(),
                        artista.getFotoPerfil(),
                        miniObras
                );

                listaArtistas.add(item);
                adapter.actualizarLista(new ArrayList<>(listaArtistas));
            }

            @Override
            public void onFailure(Call<List<ObraDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
