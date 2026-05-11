package com.example.artistlan.Conector.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ObrasLikesApi {

    @GET("obrasLikes/{obraId}")
    Call<Integer> obtenerLikes(@Path("obraId") int obraId);
}
