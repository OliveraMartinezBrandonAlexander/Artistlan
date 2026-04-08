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
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

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
    private RecyclerView recyclerMisObras;
    private TarjetaTextoObraAdapter adapter;
    private FavoritosApi favoritosApi;
    private int idUsuarioLogueado = -1;
    private final Map<Integer, Long> lastLikeClickByObra = new HashMap<>();
    private boolean debeRecargarEnResume = false;

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

        recyclerMisObras = view.findViewById(R.id.recyclerMiArte);
        recyclerMisObras.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext(), ModoTarjetaObra.MIS_OBRAS);
        adapter.setOnLikeClickListener(this::toggleLikeObra);
        adapter.setOnEditClickListener(this::editarObra);
        adapter.setOnDeleteClickListener(this::confirmarEliminacionObra);
        recyclerMisObras.setAdapter(adapter);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        cargarObrasDelUsuario();
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
            int idObra = dto.getIdObra();
            boolean esFavoritoReal = obrasFavoritas.contains(idObra);
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
                    dto.getLikes() != null ? dto.getLikes() : 0,
                    dto.getNombreAutor(),
                    dto.getNombreCategoria(),
                    dto.getFotoPerfilAutor(),
                    esFavoritoReal,
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
        if (idUsuarioLogueado <= 0 || isLikeActionBlocked(idObra)) return;

        final boolean favoritoAnterior = obraItem.isUserLiked();
        final int likesAnterior = obraItem.getLikes();
        obraItem.setUserLiked(!favoritoAnterior);
        obraItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        adapter.notifyLikeChanged(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = idObra;

        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    refreshLikeCount(obraItem, position);
                    return;
                }

                if (!favoritoAnterior && response.code() == 409) {
                    obraItem.setUserLiked(true);
                    adapter.notifyLikeChanged(position);
                    refreshLikeCount(obraItem, position);
                    return;
                }

                obraItem.setUserLiked(favoritoAnterior);
                obraItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                obraItem.setUserLiked(favoritoAnterior);
                obraItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
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
                adapter.notifyLikeChanged(position);
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
        debeRecargarEnResume = true;
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
                .setMessage("Esta accion eliminara la obra de forma permanente.\n\n"
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

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        api.eliminarObraDeUsuario(idUsuarioLogueado, obraItem.getIdObra()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                int code = response.code();
                if (code == 204) {
                    adapter.removeItemAt(position);
                    Toast.makeText(requireContext(), "Obra eliminada correctamente", Toast.LENGTH_SHORT).show();
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
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Toast.makeText(requireContext(),
                        backendMessage != null ? backendMessage : "No se pudo eliminar la obra (" + code + ")",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de conexion al eliminar la obra", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void cargarObrasDelUsuario() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);

        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        if (idUsuarioLogueado == -1) {
            Toast.makeText(requireContext(), "Error: usuario no logueado.", Toast.LENGTH_SHORT).show();
            return;
        }

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Call<List<ObraDTO>> call = api.obtenerObrasDeUsuario(idUsuarioLogueado, idUsuarioLogueado);
        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Error al cargar obras.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ObraDTO> dtos = response.body();
                if (dtos == null || dtos.isEmpty()) {
                    adapter.actualizarLista(new ArrayList<>());
                    adapter.setOwnedObraIds(new HashSet<>());
                    return;
                }
                cargarFavoritosObrasDeUsuario(obrasFavoritas -> {
                    List<TarjetaTextoObraItem> items = convertirDTOaItem(dtos, obrasFavoritas);
                    Set<Integer> ownedObraIds = extraerOwnedObraIds(dtos);
                    adapter.actualizarLista(items);
                    adapter.setOwnedObraIds(ownedObraIds);
                    refreshLikeCounts(items);
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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



