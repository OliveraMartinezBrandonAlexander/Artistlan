package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ConvocatoriaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ConvocatoriaApi {
    @GET("convocatorias")
    Call<List<ConvocatoriaDTO>> getConvocatorias();

    @POST("convocatorias")
    Call<ConvocatoriaDTO> crearConvocatoria(@Body ConvocatoriaDTO convocatoria);

    @PUT("convocatorias/{id}")
    Call<ConvocatoriaDTO> actualizarConvocatoria(@Path("id") int id, @Body ConvocatoriaDTO convocatoria);

    @DELETE("convocatorias/{id}")
    Call<Void> eliminarConvocatoria(@Path("id") int id);
}