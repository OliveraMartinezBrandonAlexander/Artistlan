package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Bundle;
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

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.PageResponseObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.utils.LikeStateManager;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragArte extends Fragment implements FilterableExplorarFragment {

    private static final String TAG_PAGINATION = "FragArtePagination";
    private static final boolean ENABLE_PAGINATION_DEBUG_LOGS = false;
    private static final long LIKE_THROTTLE_MS = 500L;
    private static final long SEARCH_DEBOUNCE_MS = 400L;
    private static final int PAGE_SIZE = 10;
    private static final int NEXT_PAGE_THRESHOLD = 5;
    private static final String SORT_DEFAULT = "idObra,desc";
    private static final int NESTED_SCROLL_BOTTOM_THRESHOLD_PX = 180;

    private NestedScrollView nestedScrollObras;
    private RecyclerView recyclerViewObras;
    private Button btnCargarMasObras;
    private LinearLayout loaderMasObras;
    private TarjetaTextoObraAdapter adapter;
    private String categoriaFiltroActual = "";
    private String textoBusquedaActual = "";
    private ObraApi obraApi;
    private FavoritosApi favoritosApi;
    private SolicitudesApi solicitudesApi;
    private int idUsuarioLogueado = -1;
    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean cargaInicialHecha = false;
    private int requestToken = 0;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private final List<TarjetaTextoObraItem> obrasAcumuladas = new ArrayList<>();
    private final Set<Integer> ownedObraIds = new HashSet<>();
    private final Map<Integer, Long> ultimoToqueLikePorObra = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_arte, container, false);
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        LikeStateManager.setCurrentUserId(idUsuarioLogueado);

        obraApi = RetrofitClient.getClient().create(ObraApi.class);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        solicitudesApi = RetrofitClient.getClient().create(SolicitudesApi.class);

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

        if (normalizarTextoFiltro(filter).equals(normalizarTextoFiltro(categoriaFiltroActual))) {
            categoriaFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();
        } else {
            categoriaFiltroActual = filter;
            Toast.makeText(getContext(), "Filtrando: " + filter, Toast.LENGTH_SHORT).show();
        }

        reiniciarYCargarPrimeraPagina();
    }

    @Override
    public void clearFilter() {
        categoriaFiltroActual = "";
        Toast.makeText(getContext(), "Filtros borrados", Toast.LENGTH_SHORT).show();
        reiniciarYCargarPrimeraPagina();
    }

    private void configurarObras(View view) {
        nestedScrollObras = view.findViewById(R.id.nestedScrollObras);
        recyclerViewObras = view.findViewById(R.id.recyclerObras);
        btnCargarMasObras = view.findViewById(R.id.btnCargarMasObras);
        loaderMasObras = view.findViewById(R.id.layoutLoaderMasObras);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewObras.setLayoutManager(layoutManager);
        recyclerViewObras.setItemAnimator(null);

        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext(), ModoTarjetaObra.EXPLORAR);
        adapter.setEntryAnimationsEnabled(false);
        adapter.setOnLikeClickListener(this::toggleLikeObra);
        adapter.setOnPrimaryActionClickListener(this::agregarObraAlCarrito);
        adapter.setOnAuthorClickListener(this::abrirPerfilPublico);
        adapter.setOnCardClickListener(this::abrirPerfilPublico);
        recyclerViewObras.setAdapter(adapter);
        if (btnCargarMasObras != null) {
            btnCargarMasObras.setOnClickListener(v -> {
                if (isLoading || isLastPage) {
                    return;
                }
                cargarSiguientePagina();
            });
        }

        recyclerViewObras.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoading || isLastPage) {
                    return;
                }
                int totalItems = layoutManager.getItemCount();
                int ultimoVisible = layoutManager.findLastVisibleItemPosition();
                if (totalItems > 0 && ultimoVisible >= totalItems - NEXT_PAGE_THRESHOLD) {
                    logPagination("Trigger scroll -> totalItems=" + totalItems
                            + ", ultimoVisible=" + ultimoVisible
                            + ", nextPageToLoad=" + nextPageToLoad
                            + ", isLoading=" + isLoading
                            + ", isLastPage=" + isLastPage);
                    cargarSiguientePagina();
                }
            }
        });

        if (nestedScrollObras != null) {
            nestedScrollObras.setOnScrollChangeListener((NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
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
                            + ", nextPageToLoad=" + nextPageToLoad
                            + ", isLoading=" + isLoading
                            + ", isLastPage=" + isLastPage);
                    cargarSiguientePagina();
                }
            });
        }

        if (!cargaInicialHecha) {
            cargaInicialHecha = true;
            reiniciarYCargarPrimeraPagina();
        }
    }

    private void agregarObraAlCarrito(TarjetaTextoObraItem obraItem, int position) {
        SolicitudCompraUiHelper.mostrarDialogoSolicitudCompra(
                this,
                idUsuarioLogueado,
                solicitudesApi,
                obraItem,
                this::reiniciarYCargarPrimeraPagina
        );
    }

    private void abrirPerfilPublico(TarjetaTextoObraItem obraItem, int position) {
        if (!isAdded() || obraItem == null || obraItem.getIdAutor() == null || obraItem.getIdAutor() <= 0) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("idArtista", obraItem.getIdAutor());
        NavHostFragment.findNavController(this).navigate(R.id.fragVerPerfilPublico, args);
    }

    private void toggleLikeObra(TarjetaTextoObraItem obraItem, int position) {
        if (idUsuarioLogueado <= 0) {
            return;
        }
        if (obraItem == null || obraItem.getIdObra() <= 0) {
            return;
        }
        Integer idObra = obraItem.getIdObra();
        long ahora = System.currentTimeMillis();
        Long ultimoToque = ultimoToqueLikePorObra.get(idObra);
        if (ultimoToque != null && ahora - ultimoToque < LIKE_THROTTLE_MS) {
            return;
        }
        if (likesEnVuelo.contains(idObra)) {
            return;
        }
        if (!LikeStateManager.beginObraRequest(idObra)) {
            return;
        }
        ultimoToqueLikePorObra.put(idObra, ahora);
        likesEnVuelo.add(idObra);

        final boolean favoritoAnterior = obraItem.isUserLiked();
        final int likesAnterior = obraItem.getLikes();
        obraItem.setUserLiked(!favoritoAnterior);
        obraItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        LikeStateManager.setObraState(idObra, obraItem.isUserLiked(), obraItem.getLikes());
        adapter.updateLikeStateById(idObra, obraItem.isUserLiked(), obraItem.getLikes());

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = obraItem.getIdObra();

        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnVuelo.remove(idObra);
                LikeStateManager.finishObraRequest(idObra);
                if (!response.isSuccessful()) {
                    if (!favoritoAnterior && response.code() == 409) {
                        obraItem.setUserLiked(true);
                        obraItem.setLikes(Math.max(0, likesAnterior + 1));
                        LikeStateManager.setObraState(idObra, true, obraItem.getLikes());
                        adapter.updateLikeStateById(idObra, true, obraItem.getLikes());
                        return;
                    }
                    obraItem.setUserLiked(favoritoAnterior);
                    obraItem.setLikes(likesAnterior);
                    LikeStateManager.setObraState(idObra, favoritoAnterior, likesAnterior);
                    adapter.updateLikeStateById(idObra, favoritoAnterior, likesAnterior);
                    Toast.makeText(getContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnVuelo.remove(idObra);
                LikeStateManager.finishObraRequest(idObra);
                obraItem.setUserLiked(favoritoAnterior);
                obraItem.setLikes(likesAnterior);
                LikeStateManager.setObraState(idObra, favoritoAnterior, likesAnterior);
                adapter.updateLikeStateById(idObra, favoritoAnterior, likesAnterior);
                Toast.makeText(getContext(), "Fallo de red al actualizar favorito", Toast.LENGTH_SHORT).show();
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

        obrasAcumuladas.clear();
        ownedObraIds.clear();
        adapter.actualizarLista(new ArrayList<>());
        adapter.setOwnedObraIds(new HashSet<>(), false);
        mostrarBotonCargarMas(false, false);
        mostrarLoaderMasObras(false);

        logPagination("Reset paginacion -> page=0, size=" + PAGE_SIZE
                + ", q=" + textoBusquedaActual
                + ", categoria=" + categoriaFiltroActual);
        cargarPagina(0);
    }

    private void cargarSiguientePagina() {
        if (isLoading || isLastPage) {
            return;
        }
        logPagination("Solicitando siguiente pagina -> page=" + nextPageToLoad
                + ", size=" + PAGE_SIZE
                + ", isLoading=" + isLoading
                + ", isLastPage=" + isLastPage);
        cargarPagina(nextPageToLoad);
    }

    private void cargarPagina(int pageObjetivo) {
        if (isLoading || (isLastPage && pageObjetivo > 0)) {
            return;
        }

        isLoading = true;
        mostrarLoaderMasObras(pageObjetivo > 0);
        if (pageObjetivo > 0) {
            mostrarBotonCargarMas(false, false);
        }
        final int tokenLocal = ++requestToken;

        Integer usuarioIdParam = idUsuarioLogueado > 0 ? idUsuarioLogueado : null;
        String queryParam = textoBusquedaActual.isEmpty() ? null : textoBusquedaActual;
        String categoriaParam = categoriaFiltroActual.isEmpty() ? null : categoriaFiltroActual;
        logPagination("Request pagina -> page=" + pageObjetivo
                + ", size=" + PAGE_SIZE
                + ", q=" + queryParam
                + ", categoria=" + categoriaParam
                + ", nextPageToLoad=" + nextPageToLoad
                + ", isLoading=" + isLoading
                + ", isLastPage=" + isLastPage);

        obraApi.obtenerObrasPaginadas(
                usuarioIdParam,
                queryParam,
                categoriaParam,
                null,
                pageObjetivo,
                PAGE_SIZE,
                SORT_DEFAULT
        ).enqueue(new Callback<PageResponseObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<PageResponseObraDTO> call, @NonNull Response<PageResponseObraDTO> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                mostrarLoaderMasObras(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                PageResponseObraDTO pageResponse = response.body();
                List<ObraDTO> obras = pageResponse.getContent();
                int recibidas = obras.size();
                List<TarjetaTextoObraItem> nuevosItems = new ArrayList<>();
                if (pageObjetivo == 0) {
                    obrasAcumuladas.clear();
                    ownedObraIds.clear();
                }

                for (ObraDTO dto : obras) {
                    if (dto.getIdUsuario() != null && dto.getIdUsuario() == idUsuarioLogueado && dto.getIdObra() != null) {
                        ownedObraIds.add(dto.getIdObra());
                    }
                    int idObra = dto.getIdObra() != null ? dto.getIdObra() : -1;
                    int likesBackend = dto.getLikes() != null ? dto.getLikes() : 0;
                    LikeStateManager.LikeState likeState = LikeStateManager.resolveObraState(
                            idObra,
                            Boolean.TRUE.equals(dto.getEsFavorito()),
                            likesBackend
                    );
                    TarjetaTextoObraItem item = new TarjetaTextoObraItem(
                            idObra,
                            dto.getTitulo(),
                            dto.getDescripcion(),
                            dto.getEstado(),
                            dto.getPrecio(),
                            dto.getImagen1(),
                            dto.getImagen2(),
                            dto.getImagen3(),
                            dto.getTecnicas(),
                            dto.getMedidas(),
                            likeState.getLikesCount(),
                            dto.getNombreAutor(),
                            dto.getNombreCategoria(),
                            dto.getFotoPerfilAutor(),
                            likeState.isLiked(),
                            false);
                    item.setIdAutor(dto.getIdUsuario());
                    item.setEditable(!Boolean.FALSE.equals(dto.getEditable()));
                    item.setEliminable(!Boolean.FALSE.equals(dto.getEliminable()));
                    item.setPuedeSolicitarCompra(Boolean.TRUE.equals(dto.getPuedeSolicitarCompra()));
                    obrasAcumuladas.add(item);
                    nuevosItems.add(item);
                }

                if (pageObjetivo == 0) {
                    adapter.actualizarLista(new ArrayList<>(obrasAcumuladas));
                    adapter.setOwnedObraIds(new HashSet<>(ownedObraIds), false);
                } else {
                    adapter.setOwnedObraIds(new HashSet<>(ownedObraIds), false);
                    adapter.agregarItems(nuevosItems);
                }

                nextPageToLoad = pageObjetivo + 1;
                isLastPage = pageResponse.isLast();
                mostrarBotonCargarMas(!isLastPage, false);
                logPagination("Response pagina -> page=" + pageObjetivo
                        + ", size=" + PAGE_SIZE
                        + ", recibidas=" + recibidas
                        + ", totalAcumuladas=" + obrasAcumuladas.size()
                        + ", last=" + pageResponse.isLast()
                        + ", totalPages=" + pageResponse.getTotalPages()
                        + ", nextPageToLoad=" + nextPageToLoad
                        + ", isLoading=" + isLoading
                        + ", isLastPage=" + isLastPage);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponseObraDTO> call, @NonNull Throwable t) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }
                isLoading = false;
                mostrarLoaderMasObras(false);
                if (pageObjetivo > 0) {
                    mostrarBotonCargarMas(!isLastPage, true);
                } else {
                    mostrarBotonCargarMas(false, false);
                }
                logPagination("Fallo pagina -> page=" + pageObjetivo
                        + ", size=" + PAGE_SIZE
                        + ", error=" + t.getMessage());
                Toast.makeText(getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLoaderMasObras(boolean mostrar) {
        if (loaderMasObras == null) {
            return;
        }
        loaderMasObras.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarBotonCargarMas(boolean mostrar, boolean reintento) {
        if (btnCargarMasObras == null) {
            return;
        }
        btnCargarMasObras.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            btnCargarMasObras.setText(reintento ? "Reintentar cargar m\u00E1s obras" : "Cargar m\u00E1s obras");
        }
    }

    private String normalizarTextoFiltro(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return limpio;
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
