package com.example.artistlan.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.ObraDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ArtistaMiniObrasLoader {

    private ArtistaMiniObrasLoader() {
    }

    public interface MiniObrasCallback {
        void onResult(@NonNull List<String> miniObras);
    }

    public static void cargarMiniObrasPorUsuario(
            @NonNull ObraApi obraApi,
            @Nullable Integer idUsuario,
            @NonNull MiniObrasCallback callback
    ) {
        if (idUsuario == null || idUsuario <= 0) {
            callback.onResult(crearMiniObrasVacias());
            return;
        }

        obraApi.obtenerObrasDeUsuario(idUsuario).enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                List<String> miniObras = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    List<ObraDTO> obras = response.body();
                    for (int i = 0; i < Math.min(3, obras.size()); i++) {
                        miniObras.add(obras.get(i).getImagen1());
                    }
                }
                while (miniObras.size() < 3) {
                    miniObras.add(null);
                }
                callback.onResult(miniObras);
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                callback.onResult(crearMiniObrasVacias());
            }
        });
    }

    @NonNull
    private static List<String> crearMiniObrasVacias() {
        List<String> miniObras = new ArrayList<>();
        while (miniObras.size() < 3) {
            miniObras.add(null);
        }
        return miniObras;
    }
}