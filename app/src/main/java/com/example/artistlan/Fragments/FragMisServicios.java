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
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
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

public class FragMisServicios extends Fragment {

    private static final long LIKE_THROTTLE_MS = 500L;
    private static final String TAG_CRUD = "ServicioCrudDebug";
    private static final String TAG_REFRESH = "RefreshMiArteDebug";
    private RecyclerView recyclerMisServicios;
    private TextView tvEmptyMisServicios;
    private TarjetaTextoServicioAdapter adapter;
    private FavoritosApi favoritosApi;
    private int idUsuarioLogueado = -1;
    private final Map<Integer, Long> lastLikeClickByServicio = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();
    private boolean debeRecargarEnResume = true;
    private int requestToken = 0;
    private static final Map<Integer, List<TarjetaTextoServicioItem>> serviciosCachePorUsuario = new HashMap<>();

    public static final String ARG_MODO_EDICION = "modo_edicion";
    public static final String ARG_SERVICIO_ID = "servicio_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_mis_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        recyclerMisServicios = view.findViewById(R.id.recyclerMisServicios);
        tvEmptyMisServicios = view.findViewById(R.id.tvEmptyMisServicios);
        recyclerMisServicios.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMisServicios.setItemAnimator(null);
        ThemeApplier.applyTextPrimary(view.findViewById(R.id.tvTituloMisServicios), new ThemeManager(requireContext()));
        ThemeApplier.applyTextSecondary(tvEmptyMisServicios, new ThemeManager(requireContext()));
        adapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        adapter.setEntryAnimationsEnabled(false);
        adapter.setPortfolioHeaderEnabled(true);
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeServicio);
        adapter.setOnEditClickListener(this::editarServicio);
        adapter.setOnDeleteClickListener(this::confirmarEliminacionServicio);
        recyclerMisServicios.setAdapter(adapter);

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        getParentFragmentManager().setFragmentResultListener(
                FragPortafolio.RESULT_KEY_PORTAFOLIO_REFRESH,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String target = result.getString(FragPortafolio.RESULT_EXTRA_TARGET, "");
                    boolean guardado = result.getBoolean(FragPortafolio.RESULT_EXTRA_GUARDADO, true);
                    String modo = result.getString(FragPortafolio.RESULT_EXTRA_MODO, "");
                    if (!FragPortafolio.TARGET_SERVICIOS.equals(target)) {
                        Log.d(TAG_REFRESH, "Resultado ignorado por servicios target=" + target
                                + " guardado=" + guardado
                                + " modo=" + modo
                                + " obrasNoSeTocan=true");
                        return;
                    }
                    Log.d(TAG_REFRESH, "Resultado recibido servicios guardado=" + guardado
                            + " modo=" + modo
                            + " sizeAntes=" + (adapter != null ? adapter.getItemCount() : -1));
                    if (!guardado) {
                        debeRecargarEnResume = false;
                        boolean cacheRestaurada = restaurarServiciosCacheSiExiste();
                        Log.d(TAG_REFRESH, "Regreso servicios sin guardar cacheRestaurada=" + cacheRestaurada
                                + " sizeDespues=" + (adapter != null ? adapter.getItemCount() : -1));
                        return;
                    }
                    debeRecargarEnResume = true;
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        debeRecargarEnResume = false;
                        cargarServiciosDelUsuario();
                    }
                }
        );
        if (debeRecargarEnResume) {
            debeRecargarEnResume = false;
            cargarServiciosDelUsuario();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (debeRecargarEnResume && isAdded()) {
            debeRecargarEnResume = false;
            cargarServiciosDelUsuario();
        }
    }

    private List<TarjetaTextoServicioItem> convertirDTOaItem(List<ServicioDTO> dtoList, Set<Integer> serviciosFavoritos) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();

        for (ServicioDTO dto : dtoList) {
            Integer idServicio = dto.getIdServicio();
            boolean esFavoritoReal = idServicio != null && serviciosFavoritos.contains(idServicio);
            items.add(new TarjetaTextoServicioItem(
                    idServicio,
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
                    esFavoritoReal,
                    false
            ));
        }

        return items;
    }

    private boolean isLikeActionBlocked(int idServicio) {
        long now = SystemClock.elapsedRealtime();
        Long last = lastLikeClickByServicio.get(idServicio);
        if (last != null && now - last < LIKE_THROTTLE_MS) {
            return true;
        }
        lastLikeClickByServicio.put(idServicio, now);
        return false;
    }

    private void toggleLikeServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (idUsuarioLogueado <= 0 || servicioItem.getIdServicio() == null) return;
        if (isLikeActionBlocked(servicioItem.getIdServicio()) || likesEnVuelo.contains(servicioItem.getIdServicio())) return;
        likesEnVuelo.add(servicioItem.getIdServicio());

        final boolean favoritoAnterior = servicioItem.isFavorito();
        final int likesAnterior = servicioItem.getLikes();
        servicioItem.setFavorito(!favoritoAnterior);
        servicioItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        adapter.notifyLikeChangedPartial(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idServicio = servicioItem.getIdServicio();

        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnVuelo.remove(servicioItem.getIdServicio());
                if (response.isSuccessful()) {
                    return;
                }

                if (!favoritoAnterior && response.code() == 409) {
                    servicioItem.setFavorito(true);
                    refreshLikeCount(servicioItem, position);
                    return;
                }

                servicioItem.setFavorito(favoritoAnterior);
                servicioItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnVuelo.remove(servicioItem.getIdServicio());
                servicioItem.setFavorito(favoritoAnterior);
                servicioItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "Error de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshLikeCount(TarjetaTextoServicioItem item, int position) {
        Integer idServicio = item.getIdServicio();
        if (idServicio == null) return;

        favoritosApi.likesServicio(idServicio).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                item.setLikes(Math.max(0, response.body()));
                adapter.notifyLikeChanged(position);
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                // si falla, mantenemos valor optimista
            }
        });
    }

    private void refreshLikeCounts(List<TarjetaTextoServicioItem> items) {
        for (int i = 0; i < items.size(); i++) {
            refreshLikeCount(items.get(i), i);
        }
    }

    private void editarServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (!isAdded() || servicioItem.getIdServicio() == null) {
            return;
        }

        Log.d(TAG_CRUD, "Editar servicio click usuarioId=" + idUsuarioLogueado
                + " idServicio=" + servicioItem.getIdServicio()
                + " position=" + position
                + " titulo=" + servicioItem.getTitulo());
        Bundle args = new Bundle();
        args.putBoolean(ARG_MODO_EDICION, true);
        args.putInt(ARG_SERVICIO_ID, servicioItem.getIdServicio());
        NavHostFragment.findNavController(this).navigate(R.id.fragSubirServicio, args);
    }

    private void confirmarEliminacionServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar")
                .setMessage("¿Deseas eliminar este servicio?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarServicio(servicioItem, position))
                .show();
    }

    private void eliminarServicio(TarjetaTextoServicioItem servicioItem, int position) {
        Integer idServicio = servicioItem.getIdServicio();
        if (idUsuarioLogueado <= 0 || idServicio == null) {
            Toast.makeText(requireContext(), "Error de usuario.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG_CRUD, "Borrar servicio request DELETE portafolioPersonal/{usuarioId}/{idServicio}"
                + " usuarioId=" + idUsuarioLogueado
                + " idServicio=" + idServicio
                + " position=" + position
                + " sizeAntes=" + (adapter != null ? adapter.getItemCount() : -1));

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.eliminarServicioUsuario(idUsuarioLogueado, idServicio).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                int code = response.code();
                Log.d(TAG_CRUD, "Borrar servicio response code=" + code
                        + " successful=" + response.isSuccessful()
                        + " idServicio=" + idServicio);
                if (response.isSuccessful()) {
                    eliminarServicioDeCache(idServicio);
                    if (adapter != null && position >= 0 && position < adapter.getItemCount()) {
                        adapter.removeItemAt(position);
                        actualizarEstadoVacio(adapter.getItemCount() == 0);
                    }
                    cargarServiciosDelUsuario(true, idServicio);
                    return;
                }
                if (code == 403) {
                    Toast.makeText(requireContext(), "No puedes eliminar este servicio", Toast.LENGTH_LONG).show();
                    return;
                }
                if (code == 404) {
                    Toast.makeText(requireContext(), "El servicio ya no existe", Toast.LENGTH_LONG).show();
                    return;
                }
                if (code == 409) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se puede eliminar este servicio",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Log.w(TAG_CRUD, "Borrar servicio error code=" + code + " idServicio=" + idServicio + " error=" + backendMessage);
                Toast.makeText(requireContext(),
                        backendMessage != null ? backendMessage : "No se pudo eliminar el servicio (" + code + ")",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG_CRUD, "Borrar servicio failure idServicio=" + idServicio, t);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de conexión al eliminar el servicio", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cargarServiciosDelUsuario() {
        cargarServiciosDelUsuario(false);
    }

    private void cargarServiciosDelUsuario(boolean permitirVaciarLista) {
        cargarServiciosDelUsuario(permitirVaciarLista, null);
    }

    private void cargarServiciosDelUsuario(boolean permitirVaciarLista, @Nullable Integer idEliminadoEsperado) {
        final int tokenLocal = ++requestToken;
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (adapter != null) {
            adapter.setCurrentUserId(idUsuarioLogueado);
        }

        if (idUsuarioLogueado == -1) {
            debeRecargarEnResume = true;
            return;
        }

        int sizeAntes = adapter != null ? adapter.getItemCount() : -1;
        Log.d(TAG_REFRESH, "Cargar servicios start token=" + tokenLocal
                + " usuarioId=" + idUsuarioLogueado
                + " permitirVaciar=" + permitirVaciarLista
                + " esperadoEliminado=" + idEliminadoEsperado
                + " sizeAntes=" + sizeAntes
                + " obrasNoSeTocan=true");
        restaurarServiciosCacheSiExiste();

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Log.d(TAG_REFRESH, "GET portafolioPersonal/{usuarioId} usuarioId=" + idUsuarioLogueado);
        Call<List<ServicioDTO>> call = api.obtenerServiciosDeUsuario(idUsuarioLogueado);
        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ServicioDTO>> call, @NonNull Response<List<ServicioDTO>> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    if (tokenLocal != requestToken) {
                        Log.d(TAG_REFRESH, "Ignorada respuesta vieja servicios token=" + tokenLocal
                                + " actual=" + requestToken
                                + " sizeActual=" + (adapter != null ? adapter.getItemCount() : -1));
                    }
                    return;
                }
                Log.d(TAG_REFRESH, "Cargar servicios response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodySize=" + (response.body() != null ? response.body().size() : -1));
                if (response.code() == 204) {
                    if (!permitirVaciarLista && restaurarServiciosCacheSiExiste()) {
                        Log.w(TAG_REFRESH, "Respuesta servicios 204 protegida con cache usuarioId=" + idUsuarioLogueado);
                        debeRecargarEnResume = false;
                        return;
                    }
                    adapter.actualizarLista(new ArrayList<>());
                    serviciosCachePorUsuario.remove(idUsuarioLogueado);
                    actualizarEstadoVacio(true);
                    if (idEliminadoEsperado != null) {
                        Log.d(TAG_CRUD, "Verificacion borrado servicio id=" + idEliminadoEsperado
                                + " siguePresente=false sizeDespues=0 code=204");
                        Toast.makeText(requireContext(), "Servicio eliminado correctamente", Toast.LENGTH_SHORT).show();
                    }
                    debeRecargarEnResume = false;
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                List<ServicioDTO> dtos = response.body();
                if (dtos.isEmpty()) {
                    if (!permitirVaciarLista && restaurarServiciosCacheSiExiste()) {
                        Log.w(TAG_REFRESH, "Respuesta servicios vacia protegida con cache usuarioId=" + idUsuarioLogueado);
                        debeRecargarEnResume = false;
                        return;
                    }
                    adapter.actualizarLista(new ArrayList<>());
                    serviciosCachePorUsuario.remove(idUsuarioLogueado);
                    actualizarEstadoVacio(true);
                    if (idEliminadoEsperado != null) {
                        Log.d(TAG_CRUD, "Verificacion borrado servicio id=" + idEliminadoEsperado
                                + " siguePresente=false sizeDespues=0");
                        Toast.makeText(requireContext(), "Servicio eliminado correctamente", Toast.LENGTH_SHORT).show();
                    }
                    debeRecargarEnResume = false;
                    return;
                }
                cargarFavoritosServiciosDeUsuario(serviciosFavoritos -> {
                    if (!isAdded() || tokenLocal != requestToken) {
                        return;
                    }
                    List<TarjetaTextoServicioItem> items = convertirDTOaItem(dtos, serviciosFavoritos);
                    guardarServiciosCache(items);
                    aplicarServiciosEnUi(items);
                    if (idEliminadoEsperado != null) {
                        boolean siguePresente = contieneServicio(items, idEliminadoEsperado);
                        Log.d(TAG_CRUD, "Verificacion borrado servicio id=" + idEliminadoEsperado
                                + " siguePresente=" + siguePresente
                                + " sizeDespues=" + items.size());
                        Toast.makeText(requireContext(),
                                siguePresente
                                        ? "El servidor confirmó, pero el servicio sigue apareciendo al recargar"
                                        : "Servicio eliminado correctamente",
                                siguePresente ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
                    }
                    debeRecargarEnResume = false;
                    refreshLikeCounts(items);
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<ServicioDTO>> call, @NonNull Throwable t) {
                if (isAdded() && tokenLocal == requestToken) {
                    debeRecargarEnResume = true;
                    Log.e(TAG_REFRESH, "Cargar servicios failure token=" + tokenLocal + " usuarioId=" + idUsuarioLogueado, t);
                    Toast.makeText(requireContext(), "Error de red/API: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean restaurarServiciosCacheSiExiste() {
        List<TarjetaTextoServicioItem> cachedItems = serviciosCachePorUsuario.get(idUsuarioLogueado);
        if (cachedItems == null || cachedItems.isEmpty()) {
            return false;
        }
        Log.d(TAG_REFRESH, "Restaurando cache servicios usuarioId=" + idUsuarioLogueado + " size=" + cachedItems.size());
        aplicarServiciosEnUi(cachedItems);
        return true;
    }

    private void guardarServiciosCache(List<TarjetaTextoServicioItem> items) {
        serviciosCachePorUsuario.put(idUsuarioLogueado, new ArrayList<>(items));
    }

    private void aplicarServiciosEnUi(List<TarjetaTextoServicioItem> items) {
        adapter.actualizarLista(items != null ? new ArrayList<>(items) : new ArrayList<>());
        actualizarEstadoVacio(items == null || items.isEmpty());
        Log.d(TAG_REFRESH, "Aplicar servicios UI size=" + (items != null ? items.size() : 0)
                + " emptyVisible=" + (items == null || items.isEmpty())
                + " obrasNoSeTocan=true");
    }

    private boolean contieneServicio(List<TarjetaTextoServicioItem> items, int idServicio) {
        if (items == null) return false;
        for (TarjetaTextoServicioItem item : items) {
            if (item != null && item.getIdServicio() != null && item.getIdServicio() == idServicio) {
                return true;
            }
        }
        return false;
    }

    private void eliminarServicioDeCache(int idServicio) {
        List<TarjetaTextoServicioItem> cachedItems = serviciosCachePorUsuario.get(idUsuarioLogueado);
        if (cachedItems != null) {
            cachedItems.removeIf(item -> item != null && item.getIdServicio() != null && item.getIdServicio() == idServicio);
        }
    }

    private interface FavoritosServiciosCallback {
        void onResult(Set<Integer> serviciosFavoritos);
    }

    private void cargarFavoritosServiciosDeUsuario(FavoritosServiciosCallback callback) {
        favoritosApi.obtenerFavoritosUsuario(idUsuarioLogueado).enqueue(new Callback<List<FavoritoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoritoDTO>> call, @NonNull Response<List<FavoritoDTO>> response) {
                Set<Integer> serviciosFavoritos = new HashSet<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (FavoritoDTO favorito : response.body()) {
                        if (favorito.idServicio != null) {
                            serviciosFavoritos.add(favorito.idServicio);
                        }
                    }
                }
                callback.onResult(serviciosFavoritos);
            }
            @Override
            public void onFailure(@NonNull Call<List<FavoritoDTO>> call, @NonNull Throwable t) {
                callback.onResult(new HashSet<>());
                }
        });
    }

    private void actualizarEstadoVacio(boolean mostrar) {
        if (tvEmptyMisServicios == null || recyclerMisServicios == null) {
            return;
        }
        tvEmptyMisServicios.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        recyclerMisServicios.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }
}
