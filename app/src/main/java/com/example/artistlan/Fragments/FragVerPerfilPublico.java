package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.PerfilPublicoArtistaDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragVerPerfilPublico extends Fragment {
    private static final long LIKE_THROTTLE_MS = 500L;

    private View root;
    private View cardPerfil;
    private View expandedInfo;
    private View tabsContainer;
    private View indicator;
    private ImageView imgPerfil;
    private TextView tvUsuario;
    private TextView tvDescripcion;
    private TextView tvNombreCompleto;
    private TextView tvRedes;
    private TextView tvFecha;
    private TextView tvCategorias;
    private TextView tvUbicacion;
    private TextView tvVacio;
    private TextView btnTabObras;
    private TextView btnTabServicios;
    private RecyclerView recyclerPublico;

    private boolean expandido = false;
    private boolean mostrandoObras = true;
    private int idUsuarioLogueado = -1;
    private int idArtista = -1;

    private TarjetaTextoObraAdapter obraAdapter;
    private TarjetaTextoServicioAdapter servicioAdapter;
    private FavoritosApi favoritosApi;
    private SolicitudesApi solicitudesApi;
    private UsuarioApi usuarioApi;

    private List<TarjetaTextoObraItem> obras = new ArrayList<>();
    private List<TarjetaTextoServicioItem> servicios = new ArrayList<>();
    private final Map<String, Long> ultimoToqueLike = new HashMap<>();
    private final Set<String> likesEnCurso = new HashSet<>();

    public FragVerPerfilPublico() {
        super(R.layout.fragment_frag_ver_perfil_publico);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        new BotonesMenuSuperior(this);
        root = view;

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        idArtista = getArguments() != null ? getArguments().getInt("idArtista", -1) : -1;

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        solicitudesApi = RetrofitClient.getClient().create(SolicitudesApi.class);
        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        bindViews();
        setupExpandCollapse();
        setupTabs();
        setupRecycler();
        cargarPerfilPublico();
    }

    private void bindViews() {
        cardPerfil = root.findViewById(R.id.cardPerfilPublico);
        expandedInfo = root.findViewById(R.id.expandedInfoPublico);
        imgPerfil = root.findViewById(R.id.imgPerfilPublico);
        tvUsuario = root.findViewById(R.id.tvNombrePublico);
        tvDescripcion = root.findViewById(R.id.tvDescripcionPublico);
        tvNombreCompleto = root.findViewById(R.id.tvNombreCompletoPublico);
        tvRedes = root.findViewById(R.id.tvRedesPublico);
        tvFecha = root.findViewById(R.id.tvFecNacPublico);
        tvCategorias = root.findViewById(R.id.tvCategoriaPublico);
        tvUbicacion = root.findViewById(R.id.tvUbicacionPublico);
        tabsContainer = root.findViewById(R.id.tabsContainer);
        indicator = root.findViewById(R.id.tabIndicator);
        btnTabObras = root.findViewById(R.id.btnTabObras);
        btnTabServicios = root.findViewById(R.id.btnTabServicios);
        recyclerPublico = root.findViewById(R.id.recyclerPublico);
        tvVacio = root.findViewById(R.id.tvPublicoVacio);
    }

    private void setupRecycler() {
        recyclerPublico.setLayoutManager(new LinearLayoutManager(requireContext()));
        obraAdapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
        servicioAdapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        servicioAdapter.setCurrentUserId(idUsuarioLogueado);
        obraAdapter.setOnLikeClickListener(this::toggleLikeObra);
        obraAdapter.setOnPrimaryActionClickListener(this::solicitarCompraDesdePerfilPublico);
        servicioAdapter.setOnLikeClickListener(this::toggleLikeServicio);
    }

    private void solicitarCompraDesdePerfilPublico(TarjetaTextoObraItem obraItem, int position) {
        SolicitudCompraUiHelper.mostrarDialogoSolicitudCompra(
                this,
                idUsuarioLogueado,
                solicitudesApi,
                obraItem,
                this::cargarPerfilPublico
        );
    }

    private void toggleLikeObra(TarjetaTextoObraItem item, int position) {
        if (item == null || item.getIdObra() <= 0) {
            return;
        }
        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = item.getIdObra();
        cambiarFavoritoObra(item, position, dto);
    }

    private void cambiarFavoritoObra(TarjetaTextoObraItem item, int pos, FavoritoDTO dto) {
        String llave = "obra:" + item.getIdObra();
        if (!puedeProcesarLike(llave)) {
            return;
        }

        boolean previo = item.isUserLiked();
        int likesPrevios = item.getLikes();
        item.setUserLiked(!previo);
        item.setLikes(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        obraAdapter.notifyLikeChanged(pos);

        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnCurso.remove(llave);
                if (!response.isSuccessful()) {
                    revertir();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnCurso.remove(llave);
                revertir();
            }

            private void revertir() {
                item.setUserLiked(previo);
                item.setLikes(likesPrevios);
                obraAdapter.notifyLikeChanged(pos);
            }
        });
    }

    private void toggleLikeServicio(TarjetaTextoServicioItem item, int position) {
        if (item.getIdServicio() == null) {
            return;
        }
        String llave = "servicio:" + item.getIdServicio();
        if (!puedeProcesarLike(llave)) {
            return;
        }

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idServicio = item.getIdServicio();

        boolean previo = item.isFavorito();
        int likesPrevios = item.getLikes();
        item.setFavorito(!previo);
        item.setLikes(Math.max(0, likesPrevios + (previo ? -1 : 1)));
        servicioAdapter.notifyLikeChanged(position);

        Call<Void> call = previo ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnCurso.remove(llave);
                if (!response.isSuccessful()) {
                    revertir();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnCurso.remove(llave);
                revertir();
            }

            private void revertir() {
                item.setFavorito(previo);
                item.setLikes(likesPrevios);
                servicioAdapter.notifyLikeChanged(position);
            }
        });
    }

    private boolean puedeProcesarLike(@NonNull String llave) {
        long ahora = System.currentTimeMillis();
        Long ultimo = ultimoToqueLike.get(llave);
        if (ultimo != null && (ahora - ultimo) < LIKE_THROTTLE_MS) {
            return false;
        }
        if (likesEnCurso.contains(llave)) {
            return false;
        }
        ultimoToqueLike.put(llave, ahora);
        likesEnCurso.add(llave);
        return true;
    }

    private void cargarPerfilPublico() {
        if (idArtista <= 0) {
            Toast.makeText(getContext(), "Artista invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioApi.obtenerPerfilPublicoArtista(idArtista, idUsuarioLogueado > 0 ? idUsuarioLogueado : null)
                .enqueue(new Callback<PerfilPublicoArtistaDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<PerfilPublicoArtistaDTO> call, @NonNull Response<PerfilPublicoArtistaDTO> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), "No se pudo cargar el perfil publico", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        renderizarPerfil(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<PerfilPublicoArtistaDTO> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Error de red al cargar perfil", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void renderizarPerfil(PerfilPublicoArtistaDTO perfil) {
        if (perfil.getIdUsuario() != null && perfil.getIdUsuario() > 0) {
            idArtista = perfil.getIdUsuario();
        }

        tvUsuario.setText(safe(perfil.getUsuario(), "usuario"));
        tvDescripcion.setText(safe(perfil.getDescripcion(), "Sin descripcion"));
        tvNombreCompleto.setText("Nombre: " + safe(perfil.getNombreCompleto(), "No disponible"));
        tvRedes.setText("Redes: " + safe(perfil.getRedesSociales(), "Sin redes"));
        tvFecha.setText("Fecha nac.: " + safe(perfil.getFechaNacimiento(), "No disponible"));
        tvUbicacion.setText("Ubicacion: " + safe(perfil.getUbicacion(), "No especificada"));
        tvCategorias.setText("Ocupacion: " + safe(perfil.getOcupacion(), "Sin ocupacion"));
        cargarOcupacionUsuarioConsultado();

        Glide.with(this)
                .load(perfil.getFotoPerfil())
                .placeholder(R.drawable.fotoperfilprueba)
                .error(R.drawable.fotoperfilprueba)
                .into(imgPerfil);

        obras = convertirObras(perfil.getObras());
        servicios = convertirServicios(perfil.getServicios());
        mostrarObras();
    }

    private void cargarOcupacionUsuarioConsultado() {
        if (idArtista <= 0 || usuarioApi == null) {
            return;
        }
        usuarioApi.obtenerUsuarioPorId(idArtista, idUsuarioLogueado > 0 ? idUsuarioLogueado : null)
                .enqueue(new Callback<UsuariosDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UsuariosDTO> call, @NonNull Response<UsuariosDTO> response) {
                        if (!isAdded() || !response.isSuccessful() || response.body() == null) {
                            return;
                        }
                        String ocupacion = safe(response.body().getCategoria(), "Sin ocupacion");
                        tvCategorias.setText("Ocupacion: " + ocupacion);
                    }

                    @Override
                    public void onFailure(@NonNull Call<UsuariosDTO> call, @NonNull Throwable t) {
                        // fallback: ya hay un valor renderizado.
                    }
                });
    }

    private List<TarjetaTextoObraItem> convertirObras(List<ObraDTO> dtoList) {
        List<TarjetaTextoObraItem> items = new ArrayList<>();
        if (dtoList == null) {
            return items;
        }
        for (ObraDTO dto : dtoList) {
            TarjetaTextoObraItem item = new TarjetaTextoObraItem(
                    dto.getIdObra() != null ? dto.getIdObra() : -1,
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
                    Boolean.TRUE.equals(dto.getEsFavorito()),
                    false
            );
            item.setEditable(!Boolean.FALSE.equals(dto.getEditable()));
            item.setEliminable(!Boolean.FALSE.equals(dto.getEliminable()));
            item.setPuedeSolicitarCompra(Boolean.TRUE.equals(dto.getPuedeSolicitarCompra()));
            items.add(item);
        }
        return items;
    }

    private List<TarjetaTextoServicioItem> convertirServicios(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();
        if (dtoList == null) {
            return items;
        }
        for (ServicioDTO dto : dtoList) {
            items.add(new TarjetaTextoServicioItem(
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
                    dto.getLikes() != null ? dto.getLikes() : 0,
                    Boolean.TRUE.equals(dto.getEsFavorito()),
                    false
            ));
        }
        return items;
    }

    private void setupExpandCollapse() {
        cardPerfil.setOnClickListener(v -> toggleExpand());
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && expandido) {
                Rect rect = new Rect();
                cardPerfil.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    colapsar();
                    return true;
                }
            }
            return false;
        });
    }

    private void toggleExpand() {
        if (expandido) {
            colapsar();
        } else {
            expandir();
        }
    }

    private void expandir() {
        expandido = true;
        expandedInfo.setVisibility(View.VISIBLE);
    }

    private void colapsar() {
        expandido = false;
        expandedInfo.setVisibility(View.GONE);
    }

    private void setupTabs() {
        tabsContainer.post(() -> {
            int w = tabsContainer.getWidth();
            if (w <= 0) {
                return;
            }
            indicator.getLayoutParams().width = w / 2;
            indicator.requestLayout();
            moverIndicador(true, false);
        });

        btnTabObras.setOnClickListener(v -> {
            if (!mostrandoObras) {
                mostrarObras();
            }
        });
        btnTabServicios.setOnClickListener(v -> {
            if (mostrandoObras) {
                mostrarServicios();
            }
        });
    }

    private void mostrarObras() {
        mostrandoObras = true;
        moverIndicador(true, true);
        btnTabObras.setTextColor(0xFFFFFFFF);
        btnTabServicios.setTextColor(0xFF2F6FED);
        obraAdapter.actualizarLista(obras);
        recyclerPublico.setAdapter(obraAdapter);
        tvVacio.setVisibility(obras.isEmpty() ? View.VISIBLE : View.GONE);
        tvVacio.setText("Este artista aun no tiene obras publicas.");
    }

    private void mostrarServicios() {
        mostrandoObras = false;
        moverIndicador(false, true);
        btnTabServicios.setTextColor(0xFFFFFFFF);
        btnTabObras.setTextColor(0xFF2F6FED);
        servicioAdapter.actualizarLista(servicios);
        recyclerPublico.setAdapter(servicioAdapter);
        tvVacio.setVisibility(servicios.isEmpty() ? View.VISIBLE : View.GONE);
        tvVacio.setText("Este artista aun no tiene servicios publicos.");
    }

    private void moverIndicador(boolean aObras, boolean animar) {
        int w = tabsContainer.getWidth();
        float targetX = aObras ? 0f : (w / 2f);
        if (!animar) {
            indicator.setTranslationX(targetX);
            return;
        }
        indicator.animate().translationX(targetX).setDuration(220).start();
    }

    private String safe(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }
}
