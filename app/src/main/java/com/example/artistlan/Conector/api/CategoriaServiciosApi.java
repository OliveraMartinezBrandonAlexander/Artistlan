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

    @GET("categorias-servicios")
    Call<List<CategoriaServiciosDTO>> obtenerTodos();

    @POST("categorias-servicios")
    Call<CategoriaServiciosDTO> crear(@Body CategoriaServiciosDTO dto);

    @GET("categorias-servicios/buscar")
    Call<CategoriaServiciosDTO> buscar(
            @Query("idServicio") Integer idServicio,
            @Query("idCategoria") Integer idCategoria
    );

    @DELETE("categorias-servicios")
    Call<Void> eliminar(
            @Query("idServicio") Integer idServicio,
            @Query("idCategoria") Integer idCategoria
    );
}
