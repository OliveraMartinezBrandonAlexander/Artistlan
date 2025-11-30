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
    Call<List<ObraDTO>> obtenerTodasLasObras();

    @POST("obras")
    Call<ObraDTO> crearObra(@Body ObraDTO obra);

    @POST("obrasDeUsuario/{usuarioId}")
    Call<ObraDTO> subirObra(
            @Path("usuarioId") int idUsuario,
            @Body ObraDTO obra
    );

    @PUT("obras/{id}")
    Call<ObraDTO> actualizarObra(@Path("id") int idObra, @Body ObraDTO obra);

    @PUT("obras/{id}/imagen1")
    Call<ObraDTO> actualizarImagen1(
            @Path("id") int idObra,
            @Body ActualizarImagenObraRequestDTO body
    );

    @GET("obrasDeUsuario/{idUsuario}")
    Call<List<ObraDTO>> obtenerObrasDeUsuario(@Path("idUsuario") int idUsuario);

    @DELETE("usuarios")
    Call<Void> eliminarTodos();
}
