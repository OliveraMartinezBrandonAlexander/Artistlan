package com.example.artistlan.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Carrusel.adapter.CarruselAdapter;
import com.example.artistlan.Carrusel.model.ObraCarruselItem;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.ReporteUiPermissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMain extends Fragment {
    private static final long LIKE_THROTTLE_MS = 500L;

    private ViewPager2 viewPager;
    private RecyclerView rvFeedPublicacionesMain;
    private RecyclerView rvConvocatoriasMain;
    private ProgressBar pbConvocatoriasMain;
    private TextView tvConvocatoriasMainEstado;

    private final Handler carruselHandler = new Handler(Looper.getMainLooper());
    private Runnable carruselRunnable;

    private Call<List<ObraDTO>> obrasFeedCall;
    private Call<List<ServicioDTO>> serviciosFeedCall;
    private Call<List<ObraDTO>> obrasCarruselCall;
    private FavoritosApi favoritosApi;
    private final Map<Integer, Long> ultimoToqueLikePorObra = new HashMap<>();
    private final Set<Integer> likesObraEnVuelo = new HashSet<>();
    private final Map<Integer, Long> ultimoToqueLikePorServicio = new HashMap<>();
    private final Set<Integer> likesServicioEnVuelo = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_frag_main, container, false);

        ThemeModuleStyler.styleFragment(this, root);
        new BotonesMenuSuperior(this);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);

        initViews(root);
        ocultarElementosNoVisibles(root);
        configurarCarrusel();
        animarEntradaCarrusel();
        configurarFeed();
        cargarFeedMixto();

        return root;
    }

    private void initViews(@NonNull View root) {
        viewPager = root.findViewById(R.id.viewPagerCarrusel);
        rvFeedPublicacionesMain = root.findViewById(R.id.rvFeedPublicacionesMain);
        rvConvocatoriasMain = root.findViewById(R.id.rvConvocatoriasMain);
        pbConvocatoriasMain = root.findViewById(R.id.pbConvocatoriasMain);
        tvConvocatoriasMainEstado = root.findViewById(R.id.tvConvocatoriasMainEstado);
    }

    private void ocultarElementosNoVisibles(@NonNull View root) {
        View btnIzquierdo = root.findViewById(R.id.btnCarruselIzquierdo);
        View btnDerecho = root.findViewById(R.id.btnCarruselDerecho);

        if (btnIzquierdo != null) {
            btnIzquierdo.setVisibility(View.GONE);
        }

        if (btnDerecho != null) {
            btnDerecho.setVisibility(View.GONE);
        }

        if (rvConvocatoriasMain != null) {
            rvConvocatoriasMain.setVisibility(View.GONE);
        }

        if (tvConvocatoriasMainEstado != null) {
            tvConvocatoriasMainEstado.setVisibility(View.GONE);
        }
    }

    private void configurarCarrusel() {
        if (viewPager == null) {
            return;
        }

        List<ObraCarruselItem> obras = new ArrayList<>();
        obras.add(new ObraCarruselItem(R.drawable.pin1, "Obra 1", "Descripción 1", "Superman", ""));
        obras.add(new ObraCarruselItem(R.drawable.pin2, "Obra 2", "Descripción 2", "Batman", ""));
        obras.add(new ObraCarruselItem(R.drawable.pin3, "Obra 3", "Descripción 3", "Wonder Woman", ""));

        CarruselAdapter adapter = new CarruselAdapter(obras, requireContext());
        adapter.setOnCarruselActionListener(new CarruselAdapter.OnCarruselActionListener() {
            @Override
            public void onOpen(ObraCarruselItem item, int position) {
                abrirDetalleObra(item.getIdObra());
            }

            @Override
            public void onLike(ObraCarruselItem item, int position) {
                toggleLikeCarrusel(item, position, adapter);
            }

            @Override
            public void onAuthor(ObraCarruselItem item, int position) {
                abrirPerfilPublico(item.getIdAutor());
            }
        });
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setPageTransformer((page, position) -> {
            float abs = Math.abs(position);
            page.setAlpha(0.86f + (1f - abs) * 0.14f);
            page.setScaleY(0.95f + (1f - abs) * 0.05f);
        });

        cargarObrasCarrusel(obras, adapter);
        iniciarAutoCarrusel(obras.size());
    }

    private void configurarFeed() {
        if (rvFeedPublicacionesMain == null) {
            return;
        }

        rvFeedPublicacionesMain.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeedPublicacionesMain.setNestedScrollingEnabled(false);
        rvFeedPublicacionesMain.setHasFixedSize(false);
        rvFeedPublicacionesMain.setItemAnimator(null);
        rvFeedPublicacionesMain.setClipToPadding(false);
    }

    private void cargarFeedMixto() {
        mostrarLoadingFeed(true);

        ObraApi obraApi = RetrofitClient.getClient().create(ObraApi.class);

        obrasFeedCall = obraApi.obtenerTodasLasObras();
        obrasFeedCall.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<ObraDTO>> call,
                    @NonNull Response<List<ObraDTO>> obraResponse
            ) {
                if (!isAdded()) {
                    return;
                }

                List<TarjetaTextoObraItem> obras = obraResponse.isSuccessful()
                        ? mapObras(obraResponse.body())
                        : new ArrayList<>();

                cargarServiciosParaFeed(obras);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ObraDTO>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded() || call.isCanceled()) {
                    return;
                }

                cargarServiciosParaFeed(new ArrayList<>());
            }
        });
    }

    private void cargarServiciosParaFeed(@NonNull List<TarjetaTextoObraItem> obras) {
        ServicioApi servicioApi = RetrofitClient.getClient().create(ServicioApi.class);

        serviciosFeedCall = servicioApi.obtenerTodos();
        serviciosFeedCall.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<ServicioDTO>> call,
                    @NonNull Response<List<ServicioDTO>> servicioResponse
            ) {
                if (!isAdded()) {
                    return;
                }

                mostrarLoadingFeed(false);

                List<TarjetaTextoServicioItem> servicios = servicioResponse.isSuccessful()
                        ? mapServicios(servicioResponse.body())
                        : new ArrayList<>();

                aplicarFeedMixto(obras, servicios);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ServicioDTO>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded() || call.isCanceled()) {
                    return;
                }

                mostrarLoadingFeed(false);
                aplicarFeedMixto(obras, new ArrayList<>());
            }
        });
    }

    private void aplicarFeedMixto(
            @NonNull List<TarjetaTextoObraItem> obras,
            @NonNull List<TarjetaTextoServicioItem> servicios
    ) {
        if (rvFeedPublicacionesMain == null) {
            return;
        }

        Collections.shuffle(obras);
        Collections.shuffle(servicios);

        List<RecyclerView.Adapter<? extends RecyclerView.ViewHolder>> bloques = new ArrayList<>();

        int idxObra = 0;
        int idxServicio = 0;

        while (idxObra < obras.size() || idxServicio < servicios.size()) {
            List<TarjetaTextoObraItem> bloqueObras = new ArrayList<>();

            for (int i = 0; i < 5 && idxObra < obras.size(); i++) {
                bloqueObras.add(obras.get(idxObra));
                idxObra++;
            }

            if (!bloqueObras.isEmpty()) {
                bloques.add(new TarjetaTextoObraAdapter(
                        bloqueObras,
                        requireContext(),
                        ModoTarjetaObra.EXPLORAR
                ));
            }

            if (idxServicio < servicios.size()) {
                List<TarjetaTextoServicioItem> bloqueServicio = new ArrayList<>();
                bloqueServicio.add(servicios.get(idxServicio));
                idxServicio++;

                bloques.add(new TarjetaTextoServicioAdapter(
                        bloqueServicio,
                        requireContext()
                ));
            }

            if (bloqueObras.isEmpty() && idxServicio < servicios.size()) {
                List<TarjetaTextoServicioItem> bloqueServicioExtra = new ArrayList<>();
                bloqueServicioExtra.add(servicios.get(idxServicio));
                idxServicio++;

                bloques.add(new TarjetaTextoServicioAdapter(
                        bloqueServicioExtra,
                        requireContext()
                ));
            }
        }

        for (RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter : bloques) {
            if (adapter instanceof TarjetaTextoObraAdapter) {
                TarjetaTextoObraAdapter obraAdapter = (TarjetaTextoObraAdapter) adapter;
                obraAdapter.setOnAuthorClickListener((obraItem, position) -> abrirPerfilPublico(obraItem.getIdAutor()));
                obraAdapter.setOnLikeClickListener((obraItem, position) -> toggleLikeObraEnFeed(obraItem, position, obraAdapter));
                obraAdapter.setOnPrimaryActionClickListener((obraItem, position) -> abrirDetalleObra(obraItem.getIdObra()));
            }
            if (adapter instanceof TarjetaTextoServicioAdapter) {
                TarjetaTextoServicioAdapter servicioAdapter = (TarjetaTextoServicioAdapter) adapter;
                servicioAdapter.setOnAuthorClickListener((servicioItem, position) -> abrirPerfilPublico(servicioItem.getIdUsuario()));
                servicioAdapter.setOnLikeClickListener((servicioItem, position) -> toggleLikeServicioEnFeed(servicioItem, position, servicioAdapter));
            }
        }
        rvFeedPublicacionesMain.setAdapter(new ConcatAdapter(bloques));

        if (rvFeedPublicacionesMain.getAlpha() < 1f) {
            rvFeedPublicacionesMain.animate().alpha(1f).setDuration(220).start();
        }

        boolean sinPublicaciones = obras.isEmpty() && servicios.isEmpty();

        if (tvConvocatoriasMainEstado != null) {
            tvConvocatoriasMainEstado.setVisibility(sinPublicaciones ? View.VISIBLE : View.GONE);

            if (sinPublicaciones) {
                tvConvocatoriasMainEstado.setText("No hay publicaciones aún.");
            }
        }
    }

    private List<TarjetaTextoObraItem> mapObras(@Nullable List<ObraDTO> dtos) {
        List<TarjetaTextoObraItem> items = new ArrayList<>();

        if (dtos == null) {
            return items;
        }

        for (ObraDTO dto : dtos) {
            if (dto == null) {
                continue;
            }

            TarjetaTextoObraItem item = new TarjetaTextoObraItem(
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
                    dto.getLikes() == null ? 0 : dto.getLikes(),
                    dto.getNombreAutor(),
                    dto.getNombreCategoria(),
                    dto.getFotoPerfilAutor(),
                    Boolean.TRUE.equals(dto.getEsFavorito()),
                    false
            );

            item.setIdAutor(dto.getIdUsuario());
            items.add(item);
        }

        return items;
    }

    private List<TarjetaTextoServicioItem> mapServicios(@Nullable List<ServicioDTO> dtos) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();

        if (dtos == null) {
            return items;
        }

        for (ServicioDTO dto : dtos) {
            if (dto == null) {
                continue;
            }

            TarjetaTextoServicioItem item = new TarjetaTextoServicioItem(
                    dto.getIdServicio(),
                    dto.getIdUsuario(),
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getContacto(),
                    dto.getTipoContacto(),
                    dto.getTecnicas(),
                    dto.getNombreUsuario(),
                    dto.getCategoria(),
                    dto.getFotoPerfilAutor(),
                    dto.getPrecioMin(),
                    dto.getPrecioMax(),
                    dto.getLikes() == null ? 0 : dto.getLikes(),
                    Boolean.TRUE.equals(dto.getEsFavorito()),
                    false
            );

            item.setIdAutor(dto.getIdUsuario());
            items.add(item);
        }

        return items;
    }


    private void animarEntradaCarrusel() {
        if (viewPager == null) {
            return;
        }
        viewPager.setAlpha(0f);
        viewPager.setTranslationY(24f);
        viewPager.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260)
                .start();
    }

    private void iniciarAutoCarrusel(int total) {
        detenerAutoCarrusel();

        if (viewPager == null || total <= 1) {
            return;
        }

        carruselRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || viewPager == null || total <= 1) {
                    return;
                }

                int siguiente = (viewPager.getCurrentItem() + 1) % total;
                viewPager.setCurrentItem(siguiente, true);
                carruselHandler.postDelayed(this, 3500);
            }
        };

        carruselHandler.postDelayed(carruselRunnable, 3500);
    }

    private void detenerAutoCarrusel() {
        if (carruselRunnable != null) {
            carruselHandler.removeCallbacks(carruselRunnable);
            carruselRunnable = null;
        }
    }

    private void cargarObrasCarrusel(
            @NonNull List<ObraCarruselItem> obras,
            @NonNull CarruselAdapter adapter
    ) {
        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);

        obrasCarruselCall = api.obtenerTodasLasObras();
        obrasCarruselCall.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<ObraDTO>> call,
                    @NonNull Response<List<ObraDTO>> response
            ) {
                if (!isAdded()
                        || !response.isSuccessful()
                        || response.body() == null
                        || response.body().isEmpty()) {
                    return;
                }

                List<ObraDTO> copia = new ArrayList<>(response.body());
                Collections.shuffle(copia);

                int reemplazos = Math.min(3, Math.min(copia.size(), obras.size()));

                for (int i = 0; i < reemplazos; i++) {
                    ObraDTO dto = copia.get(i);
                    ObraCarruselItem itemActual = obras.get(i);

                    ObraCarruselItem mapped = new ObraCarruselItem(
                            itemActual.getImagen(),
                            dto.getImagen1(),
                            dto.getTitulo(),
                            dto.getDescripcion(),
                            dto.getNombreAutor(),
                            "",
                            dto.getFotoPerfilAutor()
                    );
                    mapped.setIdObra(dto.getIdObra());
                    mapped.setIdAutor(dto.getIdUsuario());
                    mapped.setLikesCount(dto.getLikes() == null ? 0 : dto.getLikes());
                    mapped.setUserLiked(Boolean.TRUE.equals(dto.getEsFavorito()));
                    obras.set(i, mapped);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ObraDTO>> call,
                    @NonNull Throwable t
            ) {
                // Se mantiene carrusel local de respaldo.
            }
        });
    }

    private void mostrarLoadingFeed(boolean mostrar) {
        if (pbConvocatoriasMain != null) {
            pbConvocatoriasMain.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }

        if (rvFeedPublicacionesMain != null) {
            rvFeedPublicacionesMain.setAlpha(mostrar ? 0.6f : 1f);
        }

        if (tvConvocatoriasMainEstado != null && mostrar) {
            tvConvocatoriasMainEstado.setVisibility(View.GONE);
        }
    }
    private void abrirPerfilPublico(Integer idArtista) {
        if (!isAdded() || idArtista == null || idArtista <= 0) {
            Toast.makeText(requireContext(), "Perfil no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putInt("idArtista", idArtista);
        NavHostFragment.findNavController(this).navigate(R.id.fragVerPerfilPublico, args);
    }

    private void abrirDetalleObra(Integer idObra) {
        if (!isAdded() || idObra == null || idObra <= 0) {
            Toast.makeText(requireContext(), "Publicación no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        // No existe un fragment de detalle exclusivo de obra en el grafo; se reutiliza perfil público del autor.
        Toast.makeText(requireContext(), "Detalle completo no disponible en Home aún", Toast.LENGTH_SHORT).show();
    }

    private boolean puedeProcesarLikeObra(int idObra) {
        long ahora = SystemClock.elapsedRealtime();
        Long ultimoToque = ultimoToqueLikePorObra.get(idObra);
        if (ultimoToque != null && ahora - ultimoToque < LIKE_THROTTLE_MS) {
            return false;
        }
        if (likesObraEnVuelo.contains(idObra)) {
            return false;
        }
        ultimoToqueLikePorObra.put(idObra, ahora);
        likesObraEnVuelo.add(idObra);
        return true;
    }

    private boolean puedeProcesarLikeServicio(int idServicio) {
        long ahora = SystemClock.elapsedRealtime();
        Long ultimoToque = ultimoToqueLikePorServicio.get(idServicio);
        if (ultimoToque != null && ahora - ultimoToque < LIKE_THROTTLE_MS) {
            return false;
        }
        if (likesServicioEnVuelo.contains(idServicio)) {
            return false;
        }
        ultimoToqueLikePorServicio.put(idServicio, ahora);
        likesServicioEnVuelo.add(idServicio);
        return true;
    }

    private void toggleLikeObraEnFeed(TarjetaTextoObraItem obraItem, int position, @NonNull TarjetaTextoObraAdapter adapter) {
        if (obraItem == null || obraItem.getIdObra() <= 0 || favoritosApi == null || !isAdded()) {
            return;
        }
        Integer idUsuario = ReporteUiPermissions.resolveCurrentUserId(requireContext());
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(requireContext(), "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
            return;
        }

        int idObra = obraItem.getIdObra();
        if (!puedeProcesarLikeObra(idObra)) {
            return;
        }

        final boolean previo = obraItem.isUserLiked();
        final int likesPrevios = obraItem.getLikes();
        obraItem.setUserLiked(!previo);
        obraItem.setLikes(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        adapter.notifyLikeChangedPartial(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuario;
        dto.idObra = idObra;
        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                likesObraEnVuelo.remove(idObra);
                if (response.isSuccessful()) {
                    return;
                }
                if (!previo && response.code() == 409) {
                    obraItem.setUserLiked(true);
                    syncLikeCountObra(obraItem, position, adapter);
                    return;
                }
                obraItem.setUserLiked(previo);
                obraItem.setLikes(likesPrevios);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                likesObraEnVuelo.remove(idObra);
                obraItem.setUserLiked(previo);
                obraItem.setLikes(likesPrevios);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "Error de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLikeServicioEnFeed(TarjetaTextoServicioItem servicioItem, int position, @NonNull TarjetaTextoServicioAdapter adapter) {
        if (servicioItem == null || servicioItem.getIdServicio() == null || servicioItem.getIdServicio() <= 0 || favoritosApi == null || !isAdded()) {
            return;
        }
        Integer idUsuario = ReporteUiPermissions.resolveCurrentUserId(requireContext());
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(requireContext(), "Inicia sesion para dar like", Toast.LENGTH_SHORT).show();
            return;
        }
        Integer idServicio = servicioItem.getIdServicio();
        if (!puedeProcesarLikeServicio(idServicio)) {
            return;
        }

        final boolean previo = servicioItem.isFavorito();
        final int likesPrevios = servicioItem.getLikes();
        servicioItem.setFavorito(!previo);
        servicioItem.setLikes(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        adapter.notifyLikeChangedPartial(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuario;
        dto.idServicio = idServicio;
        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                likesServicioEnVuelo.remove(idServicio);
                if (response.isSuccessful()) {
                    return;
                }
                if (!previo && response.code() == 409) {
                    servicioItem.setFavorito(true);
                    syncLikeCountServicio(servicioItem, position, adapter);
                    return;
                }
                servicioItem.setFavorito(previo);
                servicioItem.setLikes(likesPrevios);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                likesServicioEnVuelo.remove(idServicio);
                servicioItem.setFavorito(previo);
                servicioItem.setLikes(likesPrevios);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "Error de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLikeCarrusel(ObraCarruselItem item, int position, CarruselAdapter adapter) {
        if (item == null || item.getIdObra() == null || item.getIdObra() <= 0 || favoritosApi == null || !isAdded()) {
            return;
        }
        Integer idUsuario = ReporteUiPermissions.resolveCurrentUserId(requireContext());
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(requireContext(), "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
            return;
        }
        Integer idObra = item.getIdObra();
        if (!puedeProcesarLikeObra(idObra)) {
            return;
        }
        final boolean previo = item.isUserLiked();
        final int likesPrevios = item.getLikesCount();
        item.setUserLiked(!previo);
        item.setLikesCount(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        adapter.notifyLikeChangedPartial(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuario;
        dto.idObra = idObra;
        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                likesObraEnVuelo.remove(idObra);
                if (response.isSuccessful()) {
                    return;
                }
                if (!previo && response.code() == 409) {
                    item.setUserLiked(true);
                    syncLikeCountCarrusel(item, position, adapter);
                    return;
                }
                item.setUserLiked(previo);
                item.setLikesCount(likesPrevios);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                likesObraEnVuelo.remove(idObra);
                item.setUserLiked(previo);
                item.setLikesCount(likesPrevios);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "Error de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncLikeCountObra(@NonNull TarjetaTextoObraItem item, int position, @NonNull TarjetaTextoObraAdapter adapter) {
        favoritosApi.likesObra(item.getIdObra()).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    item.setLikes(Math.max(0, response.body()));
                }
                adapter.notifyLikeChanged(position);
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                adapter.notifyLikeChanged(position);
            }
        });
    }

    private void syncLikeCountServicio(@NonNull TarjetaTextoServicioItem item, int position, @NonNull TarjetaTextoServicioAdapter adapter) {
        Integer idServicio = item.getIdServicio();
        if (idServicio == null) {
            adapter.notifyLikeChanged(position);
            return;
        }

        favoritosApi.likesServicio(idServicio).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    item.setLikes(Math.max(0, response.body()));
                }
                adapter.notifyLikeChanged(position);
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                adapter.notifyLikeChanged(position);
            }
        });
    }

    private void syncLikeCountCarrusel(@NonNull ObraCarruselItem item, int position, @NonNull CarruselAdapter adapter) {
        Integer idObra = item.getIdObra();
        if (idObra == null || idObra <= 0) {
            adapter.notifyLikeChanged(position);
            return;
        }

        favoritosApi.likesObra(idObra).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    item.setLikesCount(Math.max(0, response.body()));
                }
                adapter.notifyLikeChanged(position);
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                adapter.notifyLikeChanged(position);
            }
        });
    }

    @Override
    public void onDestroyView() {
        detenerAutoCarrusel();

        if (obrasFeedCall != null) {
            obrasFeedCall.cancel();
            obrasFeedCall = null;
        }

        if (serviciosFeedCall != null) {
            serviciosFeedCall.cancel();
            serviciosFeedCall = null;
        }

        if (obrasCarruselCall != null) {
            obrasCarruselCall.cancel();
            obrasCarruselCall = null;
        }

        likesObraEnVuelo.clear();
        likesServicioEnVuelo.clear();
        ultimoToqueLikePorObra.clear();
        ultimoToqueLikePorServicio.clear();

        if (rvFeedPublicacionesMain != null) {
            rvFeedPublicacionesMain.setAdapter(null);
        }

        if (viewPager != null) {
            viewPager.setAdapter(null);
        }

        viewPager = null;
        rvFeedPublicacionesMain = null;
        rvConvocatoriasMain = null;
        pbConvocatoriasMain = null;
        tvConvocatoriasMainEstado = null;

        super.onDestroyView();
    }
}
