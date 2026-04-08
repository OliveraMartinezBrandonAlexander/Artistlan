package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CarritoDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CarritoApi {

    @GET("carrito/{idUsuario}")
    Call<List<CarritoDTO>> obtenerCarritoUsuario(@Path("idUsuario") int idUsuario);

    @DELETE("carrito/eliminar/{idUsuario}/{idObra}")
    Call<Void> eliminarDelCarrito(
            @Path("idUsuario") int idUsuario,
            @Path("idObra") int idObra
    );
}
