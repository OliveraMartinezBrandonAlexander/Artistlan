package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ActualizarImagenObraRequestDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ObraApi {
    @GET("obras")
    Call<List<ObraDTO>> obtenerTodasLasObras();

    @GET("obras")
    Call<List<ObraDTO>> obtenerTodasLasObras(@Query("usuarioId") Integer usuarioId);

    @GET("obras/{id}")
    Call<ObraDTO> obtenerObraPorId(@Path("id") int idObra, @Query("usuarioId") Integer usuarioId);

    @POST("obras")
    Call<ObraDTO> crearObra(@Body ObraDTO obra);

    @POST("obrasDeUsuario/{usuarioId}")
    Call<ObraDTO> subirObra(
            @Path("usuarioId") int idUsuario,
            @Body ObraDTO obra
    );

    @PUT("obras/{id}")
    Call<ObraDTO> actualizarObra(@Path("id") int idObra, @Body ObraDTO obra);

    @PUT("obrasDeUsuario/{usuarioId}/{obraId}")
    Call<ObraDTO> actualizarObraDeUsuario(
            @Path("usuarioId") int usuarioId,
            @Path("obraId") int obraId,
            @Body ObraDTO obra
    );

    @DELETE("obrasDeUsuario/{usuarioId}/{obraId}")
    Call<Void> eliminarObraDeUsuario(
            @Path("usuarioId") int usuarioId,
            @Path("obraId") int obraId
    );

    @PUT("obras/{id}/imagen1")
    Call<ObraDTO> actualizarImagen1(
            @Path("id") int idObra,
            @Body ActualizarImagenObraRequestDTO body
    );

    @GET("obrasDeUsuario/{idUsuario}")
    Call<List<ObraDTO>> obtenerObrasDeUsuario(@Path("idUsuario") int idUsuario);

    @GET("obrasDeUsuario/{idUsuario}")
    Call<List<ObraDTO>> obtenerObrasDeUsuario(
            @Path("idUsuario") int idUsuario,
            @Query("usuarioIdConsulta") Integer usuarioIdConsulta
    );

    @DELETE("usuarios")
    Call<Void> eliminarTodos();
}
