package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CarritoDTO;
import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CarritoApi {

    @GET("carrito/{idUsuario}")
    Call<List<CarritoDTO>> obtenerCarritoUsuario(@Path("idUsuario") int idUsuario);

    @GET("carrito/{idUsuario}/total")
    Call<JsonElement> obtenerTotalCarrito(@Path("idUsuario") int idUsuario);

    @DELETE("carrito/eliminar/{idUsuario}/{idObra}")
    Call<Void> eliminarDelCarrito(
            @Path("idUsuario") int idUsuario,
            @Path("idObra") int idObra
    );

    @GET("carrito/{idUsuario}/{idObra}/contacto")
    Call<JsonElement> obtenerContactoVendedor(
            @Path("idUsuario") int idUsuario,
            @Path("idObra") int idObra
    );
}
