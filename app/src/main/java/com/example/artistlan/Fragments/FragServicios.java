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

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.PageResponseServicioDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
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

    private static final String TAG_PAGINATION = "FragServiciosPagination";
    private static final boolean ENABLE_PAGINATION_DEBUG_LOGS = false;
    private static final long LIKE_THROTTLE_MS = 500L;
    private static final long SEARCH_DEBOUNCE_MS = 400L;
    private static final int PAGE_SIZE = 10;
    private static final String SORT_DEFAULT = "idServicio,desc";
    private static final int NEXT_PAGE_THRESHOLD = 5;
    private static final int NESTED_SCROLL_BOTTOM_THRESHOLD_PX = 180;

    private NestedScrollView nestedScrollServicios;
    private RecyclerView recyclerServicios;
    private Button btnCargarMasServicios;
    private LinearLayout layoutLoaderMasServicios;
    private TarjetaTextoServicioAdapter adapter;
    private ServicioApi servicioApi;
    private String tipoServicioFiltroActual = "";
    private String textoBusquedaActual = "";
    private int idUsuarioLogueado = -1;
    private FavoritosApi favoritosApi;
    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private boolean cargaInicialHecha = false;
    private int requestToken = 0;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private final List<TarjetaTextoServicioItem> serviciosAcumulados = new ArrayList<>();
    private final Map<Integer, Long> ultimoToqueLikePorServicio = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_servicios, container, false);
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
        servicioApi = RetrofitClient.getClient().create(ServicioApi.class);

        configurarServicios(view);
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
        } else {
            tipoServicioFiltroActual = filter;
            Toast.makeText(getContext(), "Filtrando: " + filter, Toast.LENGTH_SHORT).show();
        }

        reiniciarYCargarPrimeraPagina();
    }

    @Override
    public void clearFilter() {
        tipoServicioFiltroActual = "";
        Toast.makeText(getContext(), "Filtros borrados", Toast.LENGTH_SHORT).show();
        reiniciarYCargarPrimeraPagina();
    }

    private void configurarServicios(View view) {
        nestedScrollServicios = view.findViewById(R.id.nestedScrollServicios);
        recyclerServicios = view.findViewById(R.id.recyclerServicios);
        btnCargarMasServicios = view.findViewById(R.id.btnCargarMasServicios);
        layoutLoaderMasServicios = view.findViewById(R.id.layoutLoaderMasServicios);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerServicios.setLayoutManager(layoutManager);
        recyclerServicios.setItemAnimator(null);
        adapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        adapter.setEntryAnimationsEnabled(false);
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeServicio);
        adapter.setOnAuthorClickListener(this::abrirPerfilPublico);
        adapter.setOnCardClickListener(this::abrirPerfilPublico);
        recyclerServicios.setAdapter(adapter);
        ThemeManager tm = new ThemeManager(requireContext());
        ThemeApplier.applySecondaryButton(btnCargarMasServicios, tm);
        ThemeApplier.applySecondaryButton(layoutLoaderMasServicios, tm);

        if (btnCargarMasServicios != null) {
            btnCargarMasServicios.setOnClickListener(v -> {
                if (isLoading || isLastPage) {
                    return;
                }
                cargarSiguientePagina();
            });
        }

        recyclerServicios.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoading || isLastPage) {
                    return;
                }
                int totalItems = layoutManager.getItemCount();
                int ultimoVisible = layoutManager.findLastVisibleItemPosition();
                if (totalItems > 0 && ultimoVisible >= totalItems - NEXT_PAGE_THRESHOLD) {
                    logPagination("Trigger recycler scroll -> totalItems=" + totalItems
                            + ", ultimoVisible=" + ultimoVisible
                            + ", nextPageToLoad=" + nextPageToLoad);
                    cargarSiguientePagina();
                }
            }
        });

        if (nestedScrollServicios != null) {
            nestedScrollServicios.setOnScrollChangeListener((NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
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

    private void abrirPerfilPublico(TarjetaTextoServicioItem servicioItem, int position) {
        if (!isAdded() || servicioItem == null || servicioItem.getIdUsuario() == null || servicioItem.getIdUsuario() <= 0) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("idArtista", servicioItem.getIdUsuario());
        NavHostFragment.findNavController(this).navigate(R.id.fragVerPerfilPublico, args);
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

        serviciosAcumulados.clear();
        adapter.actualizarLista(new ArrayList<>());
        mostrarBotonCargarMas(false, false);
        mostrarLoaderMasServicios(false);

        logPagination("Reset paginacion -> page=0, size=" + PAGE_SIZE
                + ", q=" + textoBusquedaActual
                + ", categoria=" + tipoServicioFiltroActual);
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
        mostrarLoaderMasServicios(pageObjetivo > 0);
        if (pageObjetivo > 0) {
            mostrarBotonCargarMas(false, false);
        }

        final int tokenLocal = ++requestToken;
        Integer usuarioIdParam = idUsuarioLogueado > 0 ? idUsuarioLogueado : null;
        String qParam = textoBusquedaActual.isEmpty() ? null : textoBusquedaActual;
        String categoriaParam = tipoServicioFiltroActual.isEmpty() ? null : tipoServicioFiltroActual;

        servicioApi.obtenerServiciosPaginados(
                usuarioIdParam,
                qParam,
                categoriaParam,
                null,
                pageObjetivo,
                PAGE_SIZE,
                SORT_DEFAULT
        ).enqueue(new Callback<PageResponseServicioDTO>() {
            @Override
            public void onResponse(@NonNull Call<PageResponseServicioDTO> call, @NonNull Response<PageResponseServicioDTO> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                mostrarLoaderMasServicios(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Error al obtener servicios: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                PageResponseServicioDTO pageResponse = response.body();
                List<ServicioDTO> servicios = pageResponse.getContent();
                List<TarjetaTextoServicioItem> nuevosItems = convertir(servicios);

                if (pageObjetivo == 0) {
                    serviciosAcumulados.clear();
                }
                serviciosAcumulados.addAll(nuevosItems);

                if (pageObjetivo == 0) {
                    adapter.actualizarLista(new ArrayList<>(serviciosAcumulados));
                } else {
                    adapter.agregarItems(nuevosItems);
                }

                nextPageToLoad = pageObjetivo + 1;
                isLastPage = pageResponse.isLast();
                mostrarBotonCargarMas(!isLastPage, false);

                logPagination("Response pagina -> page=" + pageObjetivo
                        + ", size=" + PAGE_SIZE
                        + ", recibidas=" + nuevosItems.size()
                        + ", totalAcumuladas=" + serviciosAcumulados.size()
                        + ", last=" + pageResponse.isLast()
                        + ", totalPages=" + pageResponse.getTotalPages()
                        + ", nextPageToLoad=" + nextPageToLoad);
            }

            @Override
            public void onFailure(@NonNull Call<PageResponseServicioDTO> call, @NonNull Throwable t) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }
                isLoading = false;
                mostrarLoaderMasServicios(false);
                if (pageObjetivo > 0) {
                    mostrarBotonCargarMas(!isLastPage, true);
                } else {
                    mostrarBotonCargarMas(false, false);
                }
                Toast.makeText(requireContext(), "Error de red al cargar servicios.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarLoaderMasServicios(boolean mostrar) {
        if (layoutLoaderMasServicios == null) {
            return;
        }
        layoutLoaderMasServicios.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarBotonCargarMas(boolean mostrar, boolean reintento) {
        if (btnCargarMasServicios == null) {
            return;
        }
        btnCargarMasServicios.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            btnCargarMasServicios.setText(reintento ? "Reintentar cargar m\u00E1s servicios" : "Cargar m\u00E1s servicios");
        }
    }

    private List<TarjetaTextoServicioItem> convertir(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> lista = new ArrayList<>();
        for (ServicioDTO dto : dtoList) {
            lista.add(new TarjetaTextoServicioItem(dto.getIdServicio(), dto.getIdUsuario(), dto.getTitulo(), dto.getDescripcion(), dto.getContacto(), dto.getTipoContacto(), dto.getTecnicas(), dto.getNombreUsuario(), dto.getCategoria(), dto.getFotoPerfilAutor(), dto.getPrecioMin(), dto.getPrecioMax(), dto.getLikes() != null ? dto.getLikes() : 0, Boolean.TRUE.equals(dto.getEsFavorito()), false));
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
