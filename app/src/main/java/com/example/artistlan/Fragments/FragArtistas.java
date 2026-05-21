package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.ArtistaResumenDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.PageResponseArtistaDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoArtista.adapter.TarjetaTextoArtistaAdapter;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;
import com.example.artistlan.Theme.ThemeModuleStyler;

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

public class FragArtistas extends Fragment implements FilterableExplorarFragment {

    private static final String TAG_PAGINATION = "FragArtistasPagination";
    private static final boolean ENABLE_PAGINATION_DEBUG_LOGS = false;
    private static final long LIKE_THROTTLE_MS = 500L;
    private static final long SEARCH_DEBOUNCE_MS = 400L;
    private static final int PAGE_SIZE = 10;
    private static final String SORT_DEFAULT = "idUsuario,desc";
    private static final int NESTED_SCROLL_BOTTOM_THRESHOLD_PX = 180;

    private NestedScrollView nestedScrollArtistas;
    private RecyclerView recyclerViewArtistas;
    private Button btnCargarMasArtistas;
    private LinearLayout layoutLoaderMasArtistas;
    private TarjetaTextoArtistaAdapter adapter;
    private UsuarioApi usuarioApi;
    private FavoritosApi favoritosApi;
    private String profesionFiltroActual = "";
    private String textoBusquedaActual = "";
    private int idUsuarioLogueado = -1;
    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean cargaInicialHecha = false;
    private int requestToken = 0;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private final List<TarjetaTextoArtistaItem> artistasAcumulados = new ArrayList<>();
    private final Map<Integer, Long> ultimoToqueLikePorArtista = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_artistas, container, false);
    }

    public void filtrarBusqueda(String texto) {
        String nuevoTexto = texto != null ? texto.trim() : "";
        if (nuevoTexto.equals(textoBusquedaActual)) {
            return;
        }
        textoBusquedaActual = nuevoTexto;
        programarRecargaDebounce();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        new BotonesMenuSuperior(this);
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        configurarArtistas(view);
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
        reiniciarYCargarPrimeraPagina();
    }

    @Override
    public void clearFilter() {
        profesionFiltroActual = "";
        reiniciarYCargarPrimeraPagina();
    }

    private void configurarArtistas(View view) {
        nestedScrollArtistas = view.findViewById(R.id.nestedScrollArtistas);
        recyclerViewArtistas = view.findViewById(R.id.recyclerArtistas);
        btnCargarMasArtistas = view.findViewById(R.id.btnCargarMasArtistas);
        layoutLoaderMasArtistas = view.findViewById(R.id.layoutLoaderMasArtistas);

        recyclerViewArtistas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewArtistas.setItemAnimator(null);
        adapter = new TarjetaTextoArtistaAdapter(new ArrayList<>(), requireContext());
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeArtista);
        adapter.setOnVisitarClickListener(this::abrirPerfilPublico);
        recyclerViewArtistas.setAdapter(adapter);

        if (btnCargarMasArtistas != null) {
            btnCargarMasArtistas.setOnClickListener(v -> {
                if (isLoading || isLastPage) {
                    return;
                }
                cargarSiguientePagina();
            });
        }

        if (nestedScrollArtistas != null) {
            nestedScrollArtistas.setOnScrollChangeListener((NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
                if (scrollY <= oldScrollY || isLoading || isLastPage) {
                    return;
                }
                View contenido = v.getChildAt(0);
                if (contenido == null) {
                    return;
                }
                int distanciaAlFinal = contenido.getBottom() - (v.getHeight() + v.getScrollY());
                if (distanciaAlFinal <= NESTED_SCROLL_BOTTOM_THRESHOLD_PX) {
                    logPagination("Trigger nested scroll -> distanciaAlFinal=" + distanciaAlFinal
                            + ", nextPageToLoad=" + nextPageToLoad);
                    cargarSiguientePagina();
                }
            });
        }

        if (!cargaInicialHecha) {
            cargaInicialHecha = true;
            reiniciarYCargarPrimeraPagina();
        }
    }

    private void abrirPerfilPublico(TarjetaTextoArtistaItem artistaItem, int position) {
        if (!isAdded() || artistaItem.getIdArtista() == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("idArtista", artistaItem.getIdArtista());
        NavHostFragment.findNavController(this).navigate(R.id.fragVerPerfilPublico, args);
    }

    private void toggleLikeArtista(TarjetaTextoArtistaItem artistaItem, int position) {
        if (idUsuarioLogueado <= 0 || artistaItem.getIdArtista() == null) {
            return;
        }
        Integer idArtistaTarget = artistaItem.getIdArtista();
        long ahora = System.currentTimeMillis();
        Long ultimoToque = ultimoToqueLikePorArtista.get(idArtistaTarget);
        if (ultimoToque != null && ahora - ultimoToque < LIKE_THROTTLE_MS) {
            return;
        }
        if (likesEnVuelo.contains(idArtistaTarget)) {
            return;
        }
        ultimoToqueLikePorArtista.put(idArtistaTarget, ahora);
        likesEnVuelo.add(idArtistaTarget);

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
                likesEnVuelo.remove(idArtistaTarget);
                if (!response.isSuccessful()) {
                    artistaItem.setFavorito(favoritoAnterior);
                    artistaItem.setLikes(likesAnterior);
                    adapter.notifyLikeChanged(position);
                    Toast.makeText(getContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnVuelo.remove(idArtistaTarget);
                artistaItem.setFavorito(favoritoAnterior);
                artistaItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(getContext(), "No se pudo actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void programarRecargaDebounce() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        pendingSearchRunnable = this::reiniciarYCargarPrimeraPagina;
        searchHandler.postDelayed(pendingSearchRunnable, SEARCH_DEBOUNCE_MS);
    }

    private void reiniciarYCargarPrimeraPagina() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }

        requestToken++;
        nextPageToLoad = 0;
        isLastPage = false;
        isLoading = false;

        artistasAcumulados.clear();
        adapter.actualizarLista(new ArrayList<>());
        mostrarBotonCargarMas(false, false);
        mostrarLoaderMasArtistas(false);

        logPagination("Reset paginacion -> page=0, size=" + PAGE_SIZE
                + ", q=" + textoBusquedaActual
                + ", categoria=" + profesionFiltroActual);
        cargarPagina(0);
    }

    private void cargarSiguientePagina() {
        if (isLoading || isLastPage) {
            return;
        }
        logPagination("Solicitando siguiente pagina -> page=" + nextPageToLoad + ", size=" + PAGE_SIZE);
        cargarPagina(nextPageToLoad);
    }

    private void cargarPagina(int pageObjetivo) {
        if (isLoading || (isLastPage && pageObjetivo > 0)) {
            return;
        }

        isLoading = true;
        mostrarLoaderMasArtistas(pageObjetivo > 0);
        if (pageObjetivo > 0) {
            mostrarBotonCargarMas(false, false);
        }

        final int tokenLocal = ++requestToken;
        Integer usuarioIdParam = idUsuarioLogueado > 0 ? idUsuarioLogueado : null;
        String qParam = textoBusquedaActual.isEmpty() ? null : textoBusquedaActual;
        String categoriaParam = profesionFiltroActual.isEmpty() ? null : profesionFiltroActual;

        usuarioApi.getArtistasPaginados(
                usuarioIdParam,
                qParam,
                categoriaParam,
                null,
                pageObjetivo,
                PAGE_SIZE,
                SORT_DEFAULT
        ).enqueue(new Callback<PageResponseArtistaDTO>() {
            @Override
            public void onResponse(@NonNull Call<PageResponseArtistaDTO> call, @NonNull Response<PageResponseArtistaDTO> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                mostrarLoaderMasArtistas(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            "Error al obtener artistas: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                PageResponseArtistaDTO pageResponse = response.body();
                List<TarjetaTextoArtistaItem> nuevosItems = convertir(pageResponse.getContent());

                if (pageObjetivo == 0) {
                    artistasAcumulados.clear();
                }
                artistasAcumulados.addAll(nuevosItems);

                if (pageObjetivo == 0) {
                    adapter.actualizarLista(new ArrayList<>(artistasAcumulados));
                } else {
                    adapter.agregarItems(nuevosItems);
                }

                nextPageToLoad = pageObjetivo + 1;
                isLastPage = pageResponse.isLast();
                mostrarBotonCargarMas(!isLastPage, false);

                logPagination("Response pagina -> page=" + pageObjetivo
                        + ", size=" + PAGE_SIZE
                        + ", recibidas=" + nuevosItems.size()
                        + ", totalAcumuladas=" + artistasAcumulados.size()
                        + ", last=" + pageResponse.isLast()
                        + ", totalPages=" + pageResponse.getTotalPages()
                        + ", nextPageToLoad=" + nextPageToLoad);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponseArtistaDTO> call, @NonNull Throwable t) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }
                isLoading = false;
                mostrarLoaderMasArtistas(false);
                if (pageObjetivo > 0) {
                    mostrarBotonCargarMas(!isLastPage, true);
                } else {
                    mostrarBotonCargarMas(false, false);
                }
                Toast.makeText(getContext(),
                        "Error de conexión al cargar artistas",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLoaderMasArtistas(boolean mostrar) {
        if (layoutLoaderMasArtistas == null) {
            return;
        }
        layoutLoaderMasArtistas.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarBotonCargarMas(boolean mostrar, boolean reintento) {
        if (btnCargarMasArtistas == null) {
            return;
        }
        btnCargarMasArtistas.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            btnCargarMasArtistas.setText(reintento ? "Reintentar cargar más artistas" : "Cargar más artistas");
        }
    }

    private List<TarjetaTextoArtistaItem> convertir(List<ArtistaResumenDTO> dtoList) {
        List<TarjetaTextoArtistaItem> lista = new ArrayList<>();
        if (dtoList == null) {
            return lista;
        }
        for (ArtistaResumenDTO dto : dtoList) {
            List<String> miniObras = new ArrayList<>(dto.getMiniObras());
            while (miniObras.size() < 3) {
                miniObras.add(null);
            }
            lista.add(new TarjetaTextoArtistaItem(
                    dto.getIdUsuario(),
                    dto.getUsuario(),
                    dto.getCategoria(),
                    dto.getDescripcion(),
                    dto.getFotoPerfil(),
                    miniObras,
                    dto.getLikes() != null ? dto.getLikes() : 0,
                    Boolean.TRUE.equals(dto.getEsFavorito())
            ));
        }
        return lista;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
    }

    private void logPagination(String message) {
        if (!ENABLE_PAGINATION_DEBUG_LOGS) {
            return;
        }
        Context context = getContext();
        if (context != null
                && context.getApplicationInfo() != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG_PAGINATION, message);
        }
    }
}
