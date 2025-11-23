package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.ServicioDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ServicioApi
{
    @GET("servicios")
    Call<List<ServicioDTO>> obtenerTodos();

    @POST("servicios")
    Call<List<ServicioDTO>> crearServicios(@Body List<ServicioDTO> servicios);

    @PUT("servicios")
    Call<List<ServicioDTO>> actualizarServicios(@Body List<ServicioDTO> servicios);

    @DELETE("servicios")
    Call<Void> eliminarTodos();

}
