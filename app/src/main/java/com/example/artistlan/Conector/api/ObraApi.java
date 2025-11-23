package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ObraApi
{
    @GET("obras")
    Call<List<ObraDTO>> obtenerTodos();

    @POST("obras")
    Call<List<ObraDTO>> crearObras(@Body List<ObraDTO> obras);

    @PUT("obras")
    Call<List<ObraDTO>> actualizarObras(@Body List<ObraDTO> obras);

    @DELETE("usuarios")
    Call<Void> eliminarTodos();

}
