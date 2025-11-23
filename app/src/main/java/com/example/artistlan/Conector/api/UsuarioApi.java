package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.UsuariosDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioApi
{
    @GET("usuarios")
    Call<List<UsuariosDTO>> obtenerTodos();

    @POST("usuarios")
    Call<List<UsuariosDTO>> crearUsuarios(@Body List<UsuariosDTO> usuarios);


    @GET("usuarios/login")
    Call<UsuariosDTO> login(
            @Query("usuario") String usuario,
            @Query("correo") String correo,
            @Query("contrasena") String contrasena
    );

    @GET("api/usuarios/{id}")
    Call<UsuariosDTO> obtenerUsuarioPorId(@Path("id") int id);
}
