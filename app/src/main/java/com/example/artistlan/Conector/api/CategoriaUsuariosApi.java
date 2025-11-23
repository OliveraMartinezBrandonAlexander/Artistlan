package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CategoriaUsuariosDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CategoriaUsuariosApi {

    @GET("api/categorias-usuarios")
    Call<List<CategoriaUsuariosDTO>> obtenerTodos();

    @POST("api/categorias-usuarios")
    Call<CategoriaUsuariosDTO> crear(@Body CategoriaUsuariosDTO dto);

    @GET("api/categorias-usuarios/buscar")
    Call<CategoriaUsuariosDTO> buscar(
            @Query("idUsuario") Integer idUsuario,
            @Query("idCategoria") Integer idCategoria
    );

    @DELETE("api/categorias-usuarios")
    Call<Void> eliminar(
            @Query("idUsuario") Integer idUsuario,
            @Query("idCategoria") Integer idCategoria
    );
}
