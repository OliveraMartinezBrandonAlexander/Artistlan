package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CategoriaServiciosDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CategoriaServiciosApi {

    @GET("categoriaServicios")
    Call<List<CategoriaServiciosDTO>> obtenerTodos();

    @POST("categoriaServicios")
    Call<CategoriaServiciosDTO> crear(@Body CategoriaServiciosDTO dto);

    @GET("categoriaServicios/buscar")
    Call<CategoriaServiciosDTO> buscar(
            @Query("idServicio") Integer idServicio,
            @Query("idCategoria") Integer idCategoria
    );

    @DELETE("categoriaServicios")
    Call<Void> eliminar(
            @Query("idServicio") Integer idServicio,
            @Query("idCategoria") Integer idCategoria
    );
}