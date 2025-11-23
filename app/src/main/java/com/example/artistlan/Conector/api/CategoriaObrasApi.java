package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CategoriaObrasDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CategoriaObrasApi {

    @GET("categorias-obras")
    Call<List<CategoriaObrasDTO>> obtenerTodas();

    @POST("categorias-obras")
    Call<CategoriaObrasDTO> crear(@Body CategoriaObrasDTO dto);

    @GET("categorias-obras/buscar")
    Call<CategoriaObrasDTO> buscar(
            @Query("idObra") Integer idObra,
            @Query("idCategoria") Integer idCategoria
    );

    @DELETE("categorias-obras")
    Call<Void> eliminar(
            @Query("idObra") Integer idObra,
            @Query("idCategoria") Integer idCategoria
    );
}
