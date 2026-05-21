package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.utils.LikeStateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMiArte extends Fragment {

    private static final long LIKE_THROTTLE_MS = 500L;
    private static final String TAG_CRUD = "ObraCrudDebug";
    private static final String TAG_REFRESH = "RefreshMiArteDebug";
    private RecyclerView recyclerMisObras;
    private TextView tvEmptyMiArte;
    private TarjetaTextoObraAdapter adapter;
    private FavoritosApi favoritosApi;
    private int idUsuarioLogueado = -1;
    private final Map<Integer, Long> lastLikeClickByObra = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();
    private final Set<Integer> obrasEnEliminacion = new HashSet<>();
    private boolean debeRecargarEnResume = true;
    private int requestToken = 0;
    private static final Map<Integer, List<TarjetaTextoObraItem>> obrasCachePorUsuario = new HashMap<>();
    private static final Map<Integer, Set<Integer>> ownedObrasCachePorUsuario = new HashMap<>();

    public static final String ARG_MODO_EDICION = "modo_edicion";
    public static final String ARG_OBRA_ID = "obra_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_mi_arte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        recyclerMisObras = view.findViewById(R.id.recyclerMiArte);
        tvEmptyMiArte = view.findViewById(R.id.tvEmptyMiArte);
        recyclerMisObras.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMisObras.setItemAnimator(null);
        ThemeApplier.applyTextPrimary(view.findViewById(R.id.tvTituloMiArte), new ThemeManager(requireContext()));
        ThemeApplier.applyTextSecondary(tvEmptyMiArte, new ThemeManager(requireContext()));
        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext(), ModoTarjetaObra.MIS_OBRAS);
        adapter.setEntryAnimationsEnabled(false);
        adapter.setOnLikeClickListener(this::toggleLikeObra);
        adapter.setOnEditClickListener(this::editarObra);
        adapter.setOnDeleteClickListener(this::confirmarEliminacionObra);
        recyclerMisObras.setAdapter(adapter);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        getParentFragmentManager().setFragmentResultListener(
                FragPortafolio.RESULT_KEY_PORTAFOLIO_REFRESH,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String target = result.getString(FragPortafolio.RESULT_EXTRA_TARGET, "");
                    boolean guardado = result.getBoolean(FragPortafolio.RESULT_EXTRA_GUARDADO, true);
                    String modo = result.getString(FragPortafolio.RESULT_EXTRA_MODO, "");
                    if (!FragPortafolio.TARGET_OBRAS.equals(target)) {
                        Log.d(TAG_REFRESH, "Resultado ignorado por obras target=" + target
                                + " guardado=" + guardado
                                + " modo=" + modo
                                + " serviciosNoSeTocan=true");
                        return;
                    }
                    Log.d(TAG_REFRESH, "Resultado recibido obras guardado=" + guardado
                            + " modo=" + modo
                            + " sizeAntes=" + (adapter != null ? adapter.getItemCount() : -1));
                    if (!guardado) {
                        debeRecargarEnResume = false;
                        boolean cacheRestaurada = restaurarObrasCacheSiExiste();
                        Log.d(TAG_REFRESH, "Regreso obras sin guardar cacheRestaurada=" + cacheRestaurada
                                + " sizeDespues=" + (adapter != null ? adapter.getItemCount() : -1));
                        return;
                    }
                    debeRecargarEnResume = true;
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        debeRecargarEnResume = false;
                        cargarObrasDelUsuario();
                    }
                }
        );
        if (debeRecargarEnResume) {
            debeRecargarEnResume = false;
            cargarObrasDelUsuario();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (debeRecargarEnResume && isAdded()) {
            debeRecargarEnResume = false;
            cargarObrasDelUsuario();
        }
    }

    private List<TarjetaTextoObraItem> convertirDTOaItem(List<ObraDTO> dtoList, Set<Integer> obrasFavoritas) {
        List<TarjetaTextoObraItem> items = new ArrayList<>();

        for (ObraDTO dto : dtoList) {
            int idObra = dto.getIdObra() != null ? dto.getIdObra() : -1;
            boolean esFavoritoReal = obrasFavoritas.contains(idObra) || Boolean.TRUE.equals(dto.getEsFavorito());
            int likesBackend = dto.getLikes() != null ? dto.getLikes() : 0;
            LikeStateManager.LikeState likeState = LikeStateManager.resolveObraState(
                    idObra,
                    esFavoritoReal,
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
                    false
            );
            item.setEditable(!Boolean.FALSE.equals(dto.getEditable()));
            item.setEliminable(!Boolean.FALSE.equals(dto.getEliminable()));
            item.setPuedeSolicitarCompra(Boolean.TRUE.equals(dto.getPuedeSolicitarCompra()));
            items.add(item);
        }

        return items;
    }

    private Set<Integer> extraerOwnedObraIds(List<ObraDTO> dtoList) {
        Set<Integer> ownedObraIds = new HashSet<>();
        if (dtoList == null) return ownedObraIds;

        for (ObraDTO dto : dtoList) {
            if (dto != null && dto.getIdObra() != null) {
                ownedObraIds.add(dto.getIdObra());
            }
        }
        return ownedObraIds;
    }

    private boolean isLikeActionBlocked(int idObra) {
        long now = SystemClock.elapsedRealtime();
        Long last = lastLikeClickByObra.get(idObra);
        if (last != null && now - last < LIKE_THROTTLE_MS) {
            return true;
        }
        lastLikeClickByObra.put(idObra, now);
        return false;
    }

    private void toggleLikeObra(TarjetaTextoObraItem obraItem, int position) {
        int idObra = obraItem.getIdObra();
        if (idUsuarioLogueado <= 0 || isLikeActionBlocked(idObra) || likesEnVuelo.contains(idObra)) return;
        if (!LikeStateManager.beginObraRequest(idObra)) return;
        likesEnVuelo.add(idObra);

        final boolean favoritoAnterior = obraItem.isUserLiked();
        final int likesAnterior = obraItem.getLikes();
        obraItem.setUserLiked(!favoritoAnterior);
        obraItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        LikeStateManager.setObraState(idObra, obraItem.isUserLiked(), obraItem.getLikes());
        adapter.updateLikeStateById(idObra, obraItem.isUserLiked(), obraItem.getLikes());

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = idObra;

        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnVuelo.remove(idObra);
                LikeStateManager.finishObraRequest(idObra);
                if (response.isSuccessful()) {
                    return;
                }

                if (!favoritoAnterior && response.code() == 409) {
                    obraItem.setUserLiked(true);
                    LikeStateManager.setObraState(idObra, true, obraItem.getLikes());
                    refreshLikeCount(obraItem, position);
                    return;
                }

                obraItem.setUserLiked(favoritoAnterior);
                obraItem.setLikes(likesAnterior);
                LikeStateManager.setObraState(idObra, favoritoAnterior, likesAnterior);
                adapter.updateLikeStateById(idObra, favoritoAnterior, likesAnterior);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnVuelo.remove(idObra);
                LikeStateManager.finishObraRequest(idObra);
                obraItem.setUserLiked(favoritoAnterior);
                obraItem.setLikes(likesAnterior);
                LikeStateManager.setObraState(idObra, favoritoAnterior, likesAnterior);
                adapter.updateLikeStateById(idObra, favoritoAnterior, likesAnterior);
                Toast.makeText(requireContext(), "Error de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshLikeCount(TarjetaTextoObraItem item, int position) {
        favoritosApi.likesObra(item.getIdObra()).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                item.setLikes(Math.max(0, response.body()));
                LikeStateManager.setObraState(item.getIdObra(), item.isUserLiked(), item.getLikes());
                adapter.updateLikeStateById(item.getIdObra(), item.isUserLiked(), item.getLikes());
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                // mantener valor optimista
            }
        });
    }

    private void refreshLikeCounts(List<TarjetaTextoObraItem> items) {
        for (int i = 0; i < items.size(); i++) {
            refreshLikeCount(items.get(i), i);
        }
    }

    private void editarObra(TarjetaTextoObraItem obraItem, int position) {
        if (!isAdded()) {
            return;
        }
        if (!obraItem.isEditable()) {
            Toast.makeText(requireContext(), "Esta obra no se puede editar", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG_CRUD, "Editar obra click usuarioId=" + idUsuarioLogueado
                + " idObra=" + obraItem.getIdObra()
                + " position=" + position
                + " titulo=" + obraItem.getTitulo());
        Bundle args = new Bundle();
        args.putBoolean(ARG_MODO_EDICION, true);
        args.putInt(ARG_OBRA_ID, obraItem.getIdObra());
        NavHostFragment.findNavController(this).navigate(R.id.fragSubirObra, args);
    }

    private void confirmarEliminacionObra(TarjetaTextoObraItem obraItem, int position) {
        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar obra")
                .setMessage("Esta acci\u00F3n eliminar\u00E1 la obra de forma permanente.\n\n"
                        + "Si hay solicitudes activas relacionadas, pueden cancelarse y "
                        + "se notificara a compradores afectados.\n\n"
                        + "Deseas continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarObra(obraItem, position))
                .show();
    }

    private void eliminarObra(TarjetaTextoObraItem obraItem, int position) {
        if (idUsuarioLogueado <= 0) {
            Toast.makeText(requireContext(), "Error de usuario.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!obraItem.isEliminable()) {
            Toast.makeText(requireContext(), "Esta obra no se puede eliminar", Toast.LENGTH_SHORT).show();
            return;
        }
        int idObra = obraItem.getIdObra();
        if (obrasEnEliminacion.contains(idObra)) {
            Toast.makeText(requireContext(), "Ya se esta procesando la eliminacion de esta obra", Toast.LENGTH_SHORT).show();
            return;
        }
        obrasEnEliminacion.add(idObra);
        Log.d(TAG_CRUD, "Borrar obra request DELETE obrasDeUsuario/{usuarioId}/{obraId}"
                + " usuarioId=" + idUsuarioLogueado
                + " idObra=" + idObra
                + " position=" + position
                + " sizeAntes=" + (adapter != null ? adapter.getItemCount() : -1));

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        api.eliminarObraDeUsuario(idUsuarioLogueado, idObra).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                obrasEnEliminacion.remove(idObra);
                if (!isAdded()) {
                    return;
                }

                int code = response.code();
                Log.d(TAG_CRUD, "Borrar obra response code=" + code
                        + " successful=" + response.isSuccessful()
                        + " idObra=" + idObra);
                if (response.isSuccessful()) {
                    eliminarObraDeCache(idObra);
                    if (adapter != null && position >= 0 && position < adapter.getItemCount()) {
                        adapter.removeItemAt(position);
                        actualizarEstadoVacio(adapter.getItemCount() == 0);
                    }
                    cargarObrasDelUsuario(true, idObra);
                    return;
                }
                if (code == 403) {
                    Toast.makeText(requireContext(), "No puedes eliminar esta obra", Toast.LENGTH_LONG).show();
                    return;
                }
                if (code == 404) {
                    Toast.makeText(requireContext(), "La obra ya no existe", Toast.LENGTH_LONG).show();
                    return;
                }
                if (code == 409) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se puede eliminar esta obra",
                            Toast.LENGTH_LONG).show();
                    cargarObrasDelUsuario();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Log.w(TAG_CRUD, "Borrar obra error code=" + code + " idObra=" + idObra + " error=" + backendMessage);
                String mensaje = backendMessage;
                if (mensaje == null || mensaje.trim().isEmpty() || "Error interno del servidor".equalsIgnoreCase(mensaje.trim())) {
                    mensaje = "No se pudo eliminar la obra por un error temporal del servidor";
                }
                Toast.makeText(requireContext(), mensaje + " (" + code + ")", Toast.LENGTH_LONG).show();
                cargarObrasDelUsuario();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                obrasEnEliminacion.remove(idObra);
                Log.e(TAG_CRUD, "Borrar obra failure idObra=" + idObra, t);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de conexi\u00F3n al eliminar la obra", Toast.LENGTH_LONG).show();
                    cargarObrasDelUsuario();
                }
            }
        });
    }


    private void cargarObrasDelUsuario() {
        cargarObrasDelUsuario(false);
    }

    private void cargarObrasDelUsuario(boolean permitirVaciarLista) {
        cargarObrasDelUsuario(permitirVaciarLista, null);
    }

    private void cargarObrasDelUsuario(boolean permitirVaciarLista, @Nullable Integer idEliminadoEsperado) {
        final int tokenLocal = ++requestToken;
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        LikeStateManager.setCurrentUserId(idUsuarioLogueado);
        int sizeAntes = adapter != null ? adapter.getItemCount() : -1;
        Log.d(TAG_REFRESH, "Cargar obras start token=" + tokenLocal
                + " usuarioId=" + idUsuarioLogueado
                + " permitirVaciar=" + permitirVaciarLista
                + " esperadoEliminado=" + idEliminadoEsperado
                + " sizeAntes=" + sizeAntes
                + " serviciosNoSeTocan=true");

        if (idUsuarioLogueado == -1) {
            Toast.makeText(requireContext(), "Error: usuario no logueado.", Toast.LENGTH_SHORT).show();
            debeRecargarEnResume = true;
            return;
        }

        restaurarObrasCacheSiExiste();

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Log.d(TAG_REFRESH, "GET obrasDeUsuario/{idUsuario}?usuarioIdConsulta=" + idUsuarioLogueado);
        Call<List<ObraDTO>> call = api.obtenerObrasDeUsuario(idUsuarioLogueado, idUsuarioLogueado);
        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                if (!isAdded()) return;
                if (tokenLocal != requestToken) {
                    Log.d(TAG_REFRESH, "Ignorada respuesta vieja obras token=" + tokenLocal
                            + " actual=" + requestToken
                            + " sizeActual=" + (adapter != null ? adapter.getItemCount() : -1));
                    return;
                }
                Log.d(TAG_REFRESH, "Cargar obras response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodySize=" + (response.body() != null ? response.body().size() : -1));

                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Error al cargar obras.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ObraDTO> dtos = response.body();
                if (dtos == null || dtos.isEmpty()) {
                    if (!permitirVaciarLista && restaurarObrasCacheSiExiste()) {
                        Log.w(TAG_REFRESH, "Respuesta obras vacia protegida con cache usuarioId=" + idUsuarioLogueado);
                        debeRecargarEnResume = false;
                        return;
                    }
                    adapter.actualizarLista(new ArrayList<>());
                    adapter.setOwnedObraIds(new HashSet<>());
                    obrasCachePorUsuario.remove(idUsuarioLogueado);
                    ownedObrasCachePorUsuario.remove(idUsuarioLogueado);
                    actualizarEstadoVacio(true);
                    if (idEliminadoEsperado != null) {
                        Log.d(TAG_CRUD, "Verificacion borrado obra id=" + idEliminadoEsperado
                                + " siguePresente=false sizeDespues=0");
                        Toast.makeText(requireContext(), "Obra eliminada correctamente", Toast.LENGTH_SHORT).show();
                    }
                    debeRecargarEnResume = false;
                    return;
                }
                cargarFavoritosObrasDeUsuario(obrasFavoritas -> {
                    if (!isAdded() || tokenLocal != requestToken) {
                        return;
                    }
                    List<TarjetaTextoObraItem> items = convertirDTOaItem(dtos, obrasFavoritas);
                    Set<Integer> ownedObraIds = extraerOwnedObraIds(dtos);
                    guardarObrasCache(items, ownedObraIds);
                    aplicarObrasEnUi(items, ownedObraIds);
                    if (idEliminadoEsperado != null) {
                        boolean siguePresente = contieneObra(items, idEliminadoEsperado);
                        Log.d(TAG_CRUD, "Verificacion borrado obra id=" + idEliminadoEsperado
                                + " siguePresente=" + siguePresente
                                + " sizeDespues=" + items.size());
                        Toast.makeText(requireContext(),
                                siguePresente
                                        ? "El servidor confirmó, pero la obra sigue apareciendo al recargar"
                                        : "Obra eliminada correctamente",
                                siguePresente ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
                    }
                    debeRecargarEnResume = false;
                    refreshLikeCounts(items);
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                if (isAdded() && tokenLocal == requestToken) {
                    debeRecargarEnResume = true;
                    Log.e(TAG_REFRESH, "Cargar obras failure token=" + tokenLocal + " usuarioId=" + idUsuarioLogueado, t);
                    Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean restaurarObrasCacheSiExiste() {
        List<TarjetaTextoObraItem> cachedItems = obrasCachePorUsuario.get(idUsuarioLogueado);
        if (cachedItems == null || cachedItems.isEmpty()) {
            return false;
        }
        Log.d(TAG_REFRESH, "Restaurando cache obras usuarioId=" + idUsuarioLogueado + " size=" + cachedItems.size());
        Set<Integer> cachedOwned = ownedObrasCachePorUsuario.get(idUsuarioLogueado);
        aplicarObrasEnUi(cachedItems, cachedOwned != null ? cachedOwned : new HashSet<>());
        return true;
    }

    private void guardarObrasCache(List<TarjetaTextoObraItem> items, Set<Integer> ownedObraIds) {
        obrasCachePorUsuario.put(idUsuarioLogueado, new ArrayList<>(items));
        ownedObrasCachePorUsuario.put(idUsuarioLogueado, new HashSet<>(ownedObraIds));
    }

    private void aplicarObrasEnUi(List<TarjetaTextoObraItem> items, Set<Integer> ownedObraIds) {
        adapter.actualizarLista(items != null ? new ArrayList<>(items) : new ArrayList<>());
        adapter.setOwnedObraIds(ownedObraIds != null ? new HashSet<>(ownedObraIds) : new HashSet<>());
        actualizarEstadoVacio(items == null || items.isEmpty());
        Log.d(TAG_REFRESH, "Aplicar obras UI size=" + (items != null ? items.size() : 0)
                + " emptyVisible=" + (items == null || items.isEmpty())
                + " serviciosNoSeTocan=true");
    }

    private boolean contieneObra(List<TarjetaTextoObraItem> items, int idObra) {
        if (items == null) return false;
        for (TarjetaTextoObraItem item : items) {
            if (item != null && item.getIdObra() == idObra) {
                return true;
            }
        }
        return false;
    }

    private void eliminarObraDeCache(int idObra) {
        List<TarjetaTextoObraItem> cachedItems = obrasCachePorUsuario.get(idUsuarioLogueado);
        if (cachedItems != null) {
            cachedItems.removeIf(item -> item != null && item.getIdObra() == idObra);
        }
        Set<Integer> cachedOwned = ownedObrasCachePorUsuario.get(idUsuarioLogueado);
        if (cachedOwned != null) {
            cachedOwned.remove(idObra);
        }
    }

    private void actualizarEstadoVacio(boolean mostrar) {
        if (tvEmptyMiArte == null || recyclerMisObras == null) {
            return;
        }
        tvEmptyMiArte.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        recyclerMisObras.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }
    private interface FavoritosObrasCallback {
        void onResult(Set<Integer> obrasFavoritas);
    }

    private void cargarFavoritosObrasDeUsuario(FavoritosObrasCallback callback) {
        favoritosApi.obtenerFavoritosUsuario(idUsuarioLogueado).enqueue(new Callback<List<FavoritoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoritoDTO>> call, @NonNull Response<List<FavoritoDTO>> response) {
                Set<Integer> obrasFavoritas = new HashSet<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (FavoritoDTO favorito : response.body()) {
                        if (favorito.idObra != null) {
                            obrasFavoritas.add(favorito.idObra);
                        }
                    }
                }
                callback.onResult(obrasFavoritas);
            }

            @Override
            public void onFailure(@NonNull Call<List<FavoritoDTO>> call, @NonNull Throwable t) {
                callback.onResult(new HashSet<>());
            }
        });
    }
}



