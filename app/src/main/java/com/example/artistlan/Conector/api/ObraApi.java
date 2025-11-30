package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ActualizarImagenObraRequestDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ObraApi
{
    @GET("obras")
    Call<List<ObraDTO>> obtenerTodos();

    @POST("obras")
    Call<ObraDTO> crearObra(@Body ObraDTO obra);

    @PUT("obras/{id}")
    Call<ObraDTO> actualizarObra(@Path("id") int idObra, @Body ObraDTO obra);

    @DELETE("usuarios")
    Call<Void> eliminarTodos();

    @PUT("obras/{id}/imagen1")
    Call<ObraDTO> actualizarImagen1(
            @Path("id") int idObra,
            @Body ActualizarImagenObraRequestDTO body
    );
}
