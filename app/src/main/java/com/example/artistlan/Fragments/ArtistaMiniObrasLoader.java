package com.example.artistlan.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.PerfilPublicoArtistaDTO;

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
            @NonNull UsuarioApi usuarioApi,
            @Nullable Integer idUsuarioConsulta,
            @Nullable Integer idArtista,
            @NonNull MiniObrasCallback callback
    ) {
        if (idArtista == null || idArtista <= 0) {
            callback.onResult(crearMiniObrasVacias());
            return;
        }

        usuarioApi.obtenerPerfilPublicoArtista(idArtista, idUsuarioConsulta).enqueue(new Callback<PerfilPublicoArtistaDTO>() {
            @Override
            public void onResponse(@NonNull Call<PerfilPublicoArtistaDTO> call, @NonNull Response<PerfilPublicoArtistaDTO> response) {
                List<String> miniObras = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    List<ObraDTO> obras = response.body().getObras();
                    if (obras == null) {
                        obras = new ArrayList<>();
                    }
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
            public void onFailure(@NonNull Call<PerfilPublicoArtistaDTO> call, @NonNull Throwable t) {
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
