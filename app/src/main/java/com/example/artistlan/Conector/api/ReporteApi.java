package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CrearReporteRequestDTO;
import com.example.artistlan.Conector.model.ReporteDetalleDTO;
import com.example.artistlan.Conector.model.ReporteResumenDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReporteApi {

    @POST("reportes")
    Call<ReporteDetalleDTO> crearReporte(@Body CrearReporteRequestDTO request);

    @GET("reportes/mis-reportes/{idUsuario}")
    Call<List<ReporteResumenDTO>> listarMisReportes(@Path("idUsuario") Integer idUsuario);

    @GET("reportes/mis-reportes/{idUsuario}/{idReporte}")
    Call<ReporteDetalleDTO> obtenerMiReporte(
            @Path("idUsuario") Integer idUsuario,
            @Path("idReporte") Integer idReporte
    );
}
