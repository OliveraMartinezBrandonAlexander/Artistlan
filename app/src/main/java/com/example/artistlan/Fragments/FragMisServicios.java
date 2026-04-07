package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
    private RecyclerView recyclerMisServicios;
    private TarjetaTextoServicioAdapter adapter;
    private FavoritosApi favoritosApi;
    private int idUsuarioLogueado = -1;
    private final Map<Integer, Long> lastLikeClickByServicio = new HashMap<>();
    private boolean debeRecargarEnResume = false;

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

        recyclerMisServicios = view.findViewById(R.id.recyclerMisServicios);
        recyclerMisServicios.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeServicio);
        adapter.setOnEditClickListener(this::editarServicio);
        adapter.setOnDeleteClickListener(this::confirmarEliminacionServicio);
        recyclerMisServicios.setAdapter(adapter);

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        cargarServiciosDelUsuario();
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
        if (isLikeActionBlocked(servicioItem.getIdServicio())) return;

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
                if (response.isSuccessful()) {
                    refreshLikeCount(servicioItem, position);
                    return;
                }

                if (!favoritoAnterior && response.code() == 409) {
                    servicioItem.setFavorito(true);
                    adapter.notifyLikeChanged(position);
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

        debeRecargarEnResume = true;
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

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.eliminarServicioUsuario(idUsuarioLogueado, idServicio).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                int code = response.code();
                if (code == 204) {
                    adapter.removeItemAt(position);
                    Toast.makeText(requireContext(), "Servicio eliminado correctamente", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(),
                        backendMessage != null ? backendMessage : "No se pudo eliminar el servicio (" + code + ")",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de conexión al eliminar el servicio", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cargarServiciosDelUsuario() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (adapter != null) {
            adapter.setCurrentUserId(idUsuarioLogueado);
        }

        if (idUsuarioLogueado == -1) {
            return;
        }

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Call<List<ServicioDTO>> call = api.obtenerServiciosDeUsuario(idUsuarioLogueado);
        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ServicioDTO>> call, @NonNull Response<List<ServicioDTO>> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.code() == 204) {
                    adapter.actualizarLista(new ArrayList<>());
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                List<ServicioDTO> dtos = response.body();
                if (dtos.isEmpty()) {
                    adapter.actualizarLista(new ArrayList<>());
                    return;
                }
                cargarFavoritosServiciosDeUsuario(serviciosFavoritos -> {
                    List<TarjetaTextoServicioItem> items = convertirDTOaItem(dtos, serviciosFavoritos);
                    adapter.actualizarLista(items);
                    refreshLikeCounts(items);
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<ServicioDTO>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red/API: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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
}
