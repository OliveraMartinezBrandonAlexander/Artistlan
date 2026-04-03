package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.FavoritoDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritosApi {

    @POST("favoritos")
    Call<Void> agregarFavorito(@Body FavoritoDTO favoritoDTO);

    @HTTP(method = "DELETE", path = "favoritos", hasBody = true)
    Call<Void> eliminarFavorito(@Body FavoritoDTO favoritoDTO);

    @GET("favoritos/user/{id}")
    Call<List<FavoritoDTO>> obtenerFavoritosUsuario(@Path("id") int idUsuario);

    @GET("favoritos/likes/obra/{idObra}")
    Call<Integer> likesObra(@Path("idObra") int idObra);

    @GET("favoritos/likes/servicio/{idServicio}")
    Call<Integer> likesServicio(@Path("idServicio") int idServicio);

    @GET("favoritos/likes/usuario/{idArtista}")
    Call<Integer> likesUsuario(@Path("idArtista") int idArtista);
}