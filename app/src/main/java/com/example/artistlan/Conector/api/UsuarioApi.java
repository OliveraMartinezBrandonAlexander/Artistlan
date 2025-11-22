package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.UsuariosDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface UsuarioApi
{
    @GET("usuarios")
    Call<List<UsuariosDTO>> obtenerTodos();

    @POST("usuarios")
    Call<List<UsuariosDTO>> crearUsuarios(@Body List<UsuariosDTO> usuarios);

    @PUT("usuarios")
    Call<List<UsuariosDTO>> actualizarUsuarios(@Body List<UsuariosDTO> usuarios);

    @DELETE("usuarios")
    Call<Void> eliminarTodos();

}
