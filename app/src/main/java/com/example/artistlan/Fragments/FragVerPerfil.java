package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Activitys.ActActualizarDatos;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.TarjetaTextoArtista.adapter.TarjetaTextoArtistaAdapter;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragVerPerfil extends Fragment implements View.OnClickListener {

    private TextView tvNombre, tvUsuario, tvCorreo, tvDescripcion, tvTelefono, tvRedes, tvFecNac, tvCategoria, tvUbicacion, tvFavoritosVacio;
    private ImageView imgFotoPerfil;
    private ImageButton btnEditarPefil;
    private CardView cardPerfilInfo;
    private View expandedSectionPerfil;
    private RecyclerView recyclerFavoritos;
    private TabLayout tabFavoritos;

    private int idUsuarioLogueado = -1;
    private String rolUsuario = "USER";

    private FavoritosApi favoritosApi;
    private ObraApi obraApi;
    private ServicioApi servicioApi;
    private SolicitudesApi solicitudesApi;
    private UsuarioApi usuarioApi;

    private TarjetaTextoObraAdapter obraAdapter;
    private TarjetaTextoServicioAdapter servicioAdapter;
    private TarjetaTextoArtistaAdapter artistaAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_ver_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        new BotonesMenuSuperior(this);

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        obraApi = RetrofitClient.getClient().create(ObraApi.class);
        servicioApi = RetrofitClient.getClient().create(ServicioApi.class);
        solicitudesApi = RetrofitClient.getClient().create(SolicitudesApi.class);
        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        View root = view.findViewById(R.id.rootPerfil);
        root.setOnClickListener(v -> colapsarFicha());

        cardPerfilInfo = view.findViewById(R.id.cardPerfilInfo);
        expandedSectionPerfil = view.findViewById(R.id.expanded_section_perfil);
        cardPerfilInfo.setOnClickListener(v -> toggleFicha());

        btnEditarPefil = view.findViewById(R.id.btnEditarPefil);
        btnEditarPefil.setOnClickListener(this);

        tvNombre = view.findViewById(R.id.VrpTxvNombre);
        tvUsuario = view.findViewById(R.id.VrpTxvUsuario);
        tvCorreo = view.findViewById(R.id.VrpTxvCorreo);
        tvDescripcion = view.findViewById(R.id.VrpTxvDescripcion);
        tvTelefono = view.findViewById(R.id.VrpTxvTelefono);
        tvRedes = view.findViewById(R.id.VrpTxvRedes);
        tvFecNac = view.findViewById(R.id.VrpTxvFecNac);
        tvCategoria = view.findViewById(R.id.VrpTxvCategoria);
        tvUbicacion = view.findViewById(R.id.VrpTxvUbicacion);
        imgFotoPerfil = view.findViewById(R.id.imgPerfil);
        recyclerFavoritos = view.findViewById(R.id.recyclerFavoritosPerfil);
        tvFavoritosVacio = view.findViewById(R.id.tvFavoritosVacio);
        tabFavoritos = view.findViewById(R.id.tabFavoritosPerfil);

        recyclerFavoritos.setLayoutManager(new LinearLayoutManager(requireContext()));
        obraAdapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
        servicioAdapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        artistaAdapter = new TarjetaTextoArtistaAdapter(new ArrayList<>(), requireContext());
        servicioAdapter.setCurrentUserId(idUsuarioLogueado);
        artistaAdapter.setCurrentUserId(idUsuarioLogueado);

        obraAdapter.setOnLikeClickListener(this::eliminarFavoritoObra);
        obraAdapter.setOnPrimaryActionClickListener(this::solicitarCompraDesdeFavoritos);
        servicioAdapter.setOnLikeClickListener(this::eliminarFavoritoServicio);
        artistaAdapter.setOnLikeClickListener(this::eliminarFavoritoArtista);
        artistaAdapter.setOnVisitarClickListener(this::abrirPerfilPublicoDesdeFavoritos);

        setupTabs();
        cargarDatosUsuario();
        refrescarDatosPerfilDesdeApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosUsuario();
        refrescarDatosPerfilDesdeApi();
        cargarFavoritosPorTab(0);
    }

    private void setupTabs() {
        tabFavoritos.removeAllTabs();
        tabFavoritos.addTab(tabFavoritos.newTab().setText("Obras"));
        tabFavoritos.addTab(tabFavoritos.newTab().setText("Servicios"));
        tabFavoritos.addTab(tabFavoritos.newTab().setText("Usuarios"));
        tabFavoritos.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                cargarFavoritosPorTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                cargarFavoritosPorTab(tab.getPosition());
            }
        });
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        rolUsuario = prefs.getString("rol", "USER");
        if (servicioAdapter != null) {
            servicioAdapter.setCurrentUserId(idUsuarioLogueado);
        }
        if (artistaAdapter != null) {
            artistaAdapter.setCurrentUserId(idUsuarioLogueado);
        }

        String nombre = prefs.getString("nombreCompleto", "Nombre no disponible");
        String usuario = prefs.getString("usuario", "usuario");
        String correo = prefs.getString("correo", "correo no disponible");
        String descripcion = prefs.getString("descripcion", "");
        String telefono = prefs.getString("telefono", "");
        String redes = prefs.getString("redes", "");
        String fechaNac = prefs.getString("fechaNac", "");
        String categoria = prefs.getString("ocupacion", prefs.getString("categoria", "Sin ocupación"));
        String ubicacion = prefs.getString("ubicacion", "");

        tvNombre.setText(usuario.isEmpty() ? "usuario" : usuario);
        tvUsuario.setText(nombre.isEmpty() ? "Nombre no disponible" : nombre);
        tvCorreo.setText(correo.isEmpty() ? "correo no disponible" : correo);
        tvDescripcion.setText(descripcion.isEmpty() ? "Sin descripcion" : descripcion);
        tvTelefono.setText(telefono.isEmpty() ? "No disponible" : telefono);
        tvRedes.setText(redes.isEmpty() ? "Sin redes" : redes);
        tvFecNac.setText(fechaNac.isEmpty() ? "Sin fecha" : fechaNac);
        tvCategoria.setText(categoria.isEmpty() ? "Sin ocupación" : categoria);
        tvUbicacion.setText(ubicacion.isEmpty() ? "No disponible" : ubicacion);

        String fotoPerfil = prefs.getString("fotoPerfil", null);
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(this)
                    .load(fotoPerfil)
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgFotoPerfil);
        } else {
            imgFotoPerfil.setImageResource(R.drawable.fotoperfilprueba);
        }
    }

    private void refrescarDatosPerfilDesdeApi() {
        if (idUsuarioLogueado <= 0 || usuarioApi == null) {
            return;
        }

        usuarioApi.obtenerUsuarioPorId(idUsuarioLogueado, idUsuarioLogueado).enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(@NonNull Call<UsuariosDTO> call, @NonNull Response<UsuariosDTO> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) {
                    return;
                }

                UsuariosDTO user = response.body();
                String usuario = textoSeguro(user.getUsuario(), "usuario");
                String nombreCompleto = textoSeguro(user.getNombreCompleto(), "Nombre no disponible");
                String correo = textoSeguro(user.getCorreo(), "correo no disponible");
                String descripcion = textoSeguro(user.getDescripcion(), "Sin descripcion");
                String telefono = textoSeguro(user.getTelefono(), "No disponible");
                String redes = textoSeguro(user.getRedesSociales(), "Sin redes");
                String fechaNac = textoSeguro(user.getFechaNacimiento(), "Sin fecha");
                String ocupacion = textoSeguro(user.getCategoria(), "Sin ocupación");
                String ubicacion = textoSeguro(user.getUbicacion(), "No disponible");

                tvNombre.setText(usuario);
                tvUsuario.setText(nombreCompleto);
                tvCorreo.setText(correo);
                tvDescripcion.setText(descripcion);
                tvTelefono.setText(telefono);
                tvRedes.setText(redes);
                tvFecNac.setText(fechaNac);
                tvCategoria.setText(ocupacion);
                tvUbicacion.setText(ubicacion);

                SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("usuario", usuario)
                        .putString("nombreCompleto", nombreCompleto)
                        .putString("correo", correo)
                        .putString("descripcion", descripcion)
                        .putString("telefono", user.getTelefono() != null ? user.getTelefono() : "")
                        .putString("redes", user.getRedesSociales() != null ? user.getRedesSociales() : "")
                        .putString("fechaNac", user.getFechaNacimiento() != null ? user.getFechaNacimiento() : "")
                        .putString("categoria", ocupacion)
                        .putString("ocupacion", ocupacion)
                        .putString("ubicacion", user.getUbicacion() != null ? user.getUbicacion() : "")
                        .apply();

                String fotoPerfil = user.getFotoPerfil();
                if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                    Glide.with(FragVerPerfil.this)
                            .load(fotoPerfil)
                            .placeholder(R.drawable.fotoperfilprueba)
                            .error(R.drawable.fotoperfilprueba)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imgFotoPerfil);
                } else {
                    imgFotoPerfil.setImageResource(R.drawable.fotoperfilprueba);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuariosDTO> call, @NonNull Throwable t) {
                // si falla red, mantenemos los datos ya renderizados desde preferencias.
            }
        });
    }

    private String textoSeguro(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private void eliminarFavoritoObra(TarjetaTextoObraItem item, int position) {
        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = item.getIdObra();
        eliminarFavoritoDesdePerfil(dto, position, () -> obraAdapter.removeItemAt(position));
    }

    private void solicitarCompraDesdeFavoritos(TarjetaTextoObraItem item, int position) {
        SolicitudCompraUiHelper.mostrarDialogoSolicitudCompra(
                this,
                idUsuarioLogueado,
                solicitudesApi,
                item,
                () -> cargarFavoritosPorTab(
                        tabFavoritos != null && tabFavoritos.getSelectedTabPosition() >= 0
                                ? tabFavoritos.getSelectedTabPosition()
                                : 0
                )
        );
    }

    private void eliminarFavoritoServicio(TarjetaTextoServicioItem item, int position) {
        if (item.getIdServicio() == null) return;
        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idServicio = item.getIdServicio();
        eliminarFavoritoDesdePerfil(dto, position, () -> servicioAdapter.removeItemAt(position));
    }

    private void eliminarFavoritoArtista(TarjetaTextoArtistaItem item, int position) {
        if (item.getIdArtista() == null) return;
        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idArtista = item.getIdArtista();
        eliminarFavoritoDesdePerfil(dto, position, () -> artistaAdapter.removeItemAt(position));
    }

    private void abrirPerfilPublicoDesdeFavoritos(TarjetaTextoArtistaItem artistaItem, int position) {
        if (!isAdded() || artistaItem == null || artistaItem.getIdArtista() == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putInt("idArtista", artistaItem.getIdArtista());
        NavHostFragment.findNavController(this).navigate(R.id.fragVerPerfilPublico, args);
    }

    private void eliminarFavoritoDesdePerfil(FavoritoDTO dto, int position, Runnable removeAction) {
        favoritosApi.eliminarFavorito(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo eliminar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                animateItemRemoval(position, removeAction);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error de red al eliminar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animateItemRemoval(int position, Runnable removeAction) {
        RecyclerView.ViewHolder viewHolder = recyclerFavoritos.findViewHolderForAdapterPosition(position);
        if (viewHolder == null || viewHolder.itemView == null) {
            removeAction.run();
            verificarFavoritosVacios();
            return;
        }

        viewHolder.itemView.animate()
                .alpha(0f)
                .translationX(-viewHolder.itemView.getWidth() * 0.2f)
                .setDuration(180)
                .withEndAction(() -> {
                    removeAction.run();
                    verificarFavoritosVacios();
                })
                .start();
    }

    private void verificarFavoritosVacios() {
        RecyclerView.Adapter<?> current = recyclerFavoritos.getAdapter();
        tvFavoritosVacio.setVisibility(current == null || current.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }


    private void cargarFavoritosPorTab(int tabIndex) {
        if (idUsuarioLogueado <= 0) return;
        favoritosApi.obtenerFavoritosUsuario(idUsuarioLogueado).enqueue(new Callback<List<FavoritoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoritoDTO>> call, @NonNull Response<List<FavoritoDTO>> response) {
                if (response.code() == 204 || response.body() == null || response.body().isEmpty()) {
                    recyclerFavoritos.setAdapter(null);
                    tvFavoritosVacio.setVisibility(View.VISIBLE);
                    return;
                }
                tvFavoritosVacio.setVisibility(View.GONE);
                List<FavoritoDTO> all = response.body();
                if (tabIndex == 0) cargarObrasFavoritas(all);
                else if (tabIndex == 1) cargarServiciosFavoritos(all);
                else cargarUsuariosFavoritos(all);
            }

            @Override
            public void onFailure(@NonNull Call<List<FavoritoDTO>> call, @NonNull Throwable t) {
                tvFavoritosVacio.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cargarObrasFavoritas(List<FavoritoDTO> favoritos) {
        List<TarjetaTextoObraItem> items = new ArrayList<>();
        Set<Integer> obrasPropias = new HashSet<>();
        final int[] total = {0};
        final int[] done = {0};
        for (FavoritoDTO fav : favoritos) if (fav.idObra != null) total[0]++;
        if (total[0] == 0) {
            obraAdapter.actualizarLista(new ArrayList<>());
            obraAdapter.setOwnedObraIds(new HashSet<>());
            recyclerFavoritos.setAdapter(obraAdapter);
            tvFavoritosVacio.setVisibility(View.VISIBLE);
            return;
        }
        for (FavoritoDTO fav : favoritos) {
            if (fav.idObra == null) continue;
            obraApi.obtenerObraPorId(fav.idObra, idUsuarioLogueado).enqueue(new Callback<ObraDTO>() {
                @Override
                public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ObraDTO dto = response.body();
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
                                dto.getLikes() != null ? dto.getLikes() : 0,
                                dto.getNombreAutor(),
                                dto.getNombreCategoria(),
                                dto.getFotoPerfilAutor(),
                                true,
                                false
                        );
                        item.setEditable(!Boolean.FALSE.equals(dto.getEditable()));
                        item.setEliminable(!Boolean.FALSE.equals(dto.getEliminable()));
                        item.setPuedeSolicitarCompra(Boolean.TRUE.equals(dto.getPuedeSolicitarCompra()));
                        items.add(item);
                        if (dto.getIdUsuario() != null && dto.getIdUsuario() == idUsuarioLogueado && dto.getIdObra() != null) {
                            obrasPropias.add(dto.getIdObra());
                        }
                    }
                    done[0]++;
                    if (done[0] == total[0]) {
                        obraAdapter.actualizarLista(items);
                        obraAdapter.setOwnedObraIds(obrasPropias);
                        recyclerFavoritos.setAdapter(obraAdapter);
                        tvFavoritosVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                    done[0]++;
                }
            });
        }
    }

    private void cargarServiciosFavoritos(List<FavoritoDTO> favoritos) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();
        final int[] total = {0};
        final int[] done = {0};
        for (FavoritoDTO fav : favoritos) if (fav.idServicio != null) total[0]++;
        if (total[0] == 0) {
            servicioAdapter.actualizarLista(new ArrayList<>());
            recyclerFavoritos.setAdapter(servicioAdapter);
            tvFavoritosVacio.setVisibility(View.VISIBLE);
            return;
        }
        for (FavoritoDTO fav : favoritos) {
            if (fav.idServicio == null) continue;
            servicioApi.obtenerPorId(fav.idServicio, idUsuarioLogueado).enqueue(new Callback<ServicioDTO>() {
                @Override
                public void onResponse(@NonNull Call<ServicioDTO> call, @NonNull Response<ServicioDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ServicioDTO dto = response.body();
                        items.add(new TarjetaTextoServicioItem(dto.getIdServicio(), dto.getIdUsuario(), dto.getTitulo(), dto.getDescripcion(), dto.getContacto(), dto.getTipoContacto(), dto.getTecnicas(), dto.getNombreUsuario(), dto.getCategoria(), dto.getFotoPerfilAutor(), dto.getPrecioMin(), dto.getPrecioMax(), dto.getLikes() != null ? dto.getLikes() : 0, true, false));
                    }
                    done[0]++;
                    if (done[0] == total[0]) {
                        servicioAdapter.actualizarLista(items);
                        recyclerFavoritos.setAdapter(servicioAdapter);
                        tvFavoritosVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ServicioDTO> call, @NonNull Throwable t) {
                    done[0]++;
                }
            });
        }
    }

    private void cargarUsuariosFavoritos(List<FavoritoDTO> favoritos) {
        List<TarjetaTextoArtistaItem> items = new ArrayList<>();
        final int[] total = {0};
        final int[] done = {0};
        for (FavoritoDTO fav : favoritos) if (fav.idArtista != null) total[0]++;
        if (total[0] == 0) {
            artistaAdapter.actualizarLista(new ArrayList<>());
            recyclerFavoritos.setAdapter(artistaAdapter);
            tvFavoritosVacio.setVisibility(View.VISIBLE);
            return;
        }
        for (FavoritoDTO fav : favoritos) {
            if (fav.idArtista == null) continue;
            usuarioApi.obtenerUsuarioPorId(fav.idArtista, idUsuarioLogueado).enqueue(new Callback<com.example.artistlan.Conector.model.UsuariosDTO>() {
                @Override
                public void onResponse(@NonNull Call<com.example.artistlan.Conector.model.UsuariosDTO> call, @NonNull Response<com.example.artistlan.Conector.model.UsuariosDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.artistlan.Conector.model.UsuariosDTO dto = response.body();
                        ArtistaMiniObrasLoader.cargarMiniObrasPorUsuario(obraApi, dto.getIdUsuario(), miniObras -> {
                            items.add(new TarjetaTextoArtistaItem(
                                    dto.getIdUsuario(),
                                    dto.getUsuario(),
                                    dto.getCategoria(),
                                    dto.getDescripcion(),
                                    dto.getFotoPerfil(),
                                    miniObras,
                                    dto.getLikes() != null ? dto.getLikes() : 0,
                                    true
                            ));
                            done[0]++;
                            if (done[0] == total[0]) {
                                artistaAdapter.actualizarLista(items);
                                recyclerFavoritos.setAdapter(artistaAdapter);
                                tvFavoritosVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                        });
                        return;
                    }
                    done[0]++;
                    if (done[0] == total[0]) {
                        artistaAdapter.actualizarLista(items);
                        recyclerFavoritos.setAdapter(artistaAdapter);
                        tvFavoritosVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<com.example.artistlan.Conector.model.UsuariosDTO> call, @NonNull Throwable t) {
                    done[0]++;
                }
            });
        }
    }

    private void toggleFicha() {
        if (expandedSectionPerfil.getVisibility() == View.VISIBLE) animarExpand(expandedSectionPerfil, false);
        else animarExpand(expandedSectionPerfil, true);
    }

    private void colapsarFicha() {
        if (expandedSectionPerfil != null && expandedSectionPerfil.getVisibility() == View.VISIBLE)
            animarExpand(expandedSectionPerfil, false);
    }

    private void animarExpand(View v, boolean expandir) {
        if (expandir) {
            if (v.getVisibility() == View.VISIBLE) return;
            v.setVisibility(View.VISIBLE);
            v.setAlpha(0f);
            v.setScaleY(0f);
            v.animate().alpha(1f).scaleY(1f).setDuration(120).start();
        } else {
            if (v.getVisibility() == View.GONE) return;
            v.animate().alpha(0f).scaleY(0f).setDuration(150)
                    .withEndAction(() -> v.setVisibility(View.GONE))
                    .start();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnEditarPefil) {
            Intent intent = new Intent(v.getContext(), ActActualizarDatos.class);
            v.getContext().startActivity(intent);
        }
    }
}
