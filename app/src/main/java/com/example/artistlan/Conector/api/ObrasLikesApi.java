package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ObraDTO;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ObrasLikesApi {

    // GET - obtener likes
    @GET("/api/obrasLikes/{obraId}")
    Call<Integer> obtenerLikes(@Path("obraId") int obraId);

    // POST - dar like
    @POST("/api/obrasLikes/{obraId}")
    Call<ObraDTO> darLike(@Path("obraId") int obraId);

    // DELETE - quitar like
    @DELETE("/api/obrasLikes/{obraId}")
    Call<ObraDTO> quitarLike(@Path("obraId") int obraId);
}