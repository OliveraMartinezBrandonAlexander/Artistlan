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

    @GET("categorias-usuarios")
    Call<List<CategoriaUsuariosDTO>> obtenerTodos();

    @POST("categorias-usuarios")
    Call<CategoriaUsuariosDTO> crear(@Body CategoriaUsuariosDTO dto);

    @GET("categorias-usuarios/buscar")
    Call<CategoriaUsuariosDTO> buscar(
            @Query("idUsuario") Integer idUsuario,
            @Query("idCategoria") Integer idCategoria
    );

    @DELETE("categorias-usuarios")
    Call<Void> eliminar(
            @Query("idUsuario") Integer idUsuario,
            @Query("idCategoria") Integer idCategoria
    );
}
