package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ReporteDetalleDTO;
import com.example.artistlan.Conector.model.ReporteResumenDTO;
import com.example.artistlan.Conector.model.ResolverReporteRequestDTO;
import com.example.artistlan.Conector.model.RespuestaModeracionDTO;
import com.example.artistlan.Conector.model.TomarReporteRequestDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ModeracionApi {

    @GET("moderacion/reportes")
    Call<List<ReporteResumenDTO>> listarReportes(
            @Query("idModeradorSolicitante") Integer idModeradorSolicitante,
            @Query("estado") String estado,
            @Query("prioridad") String prioridad,
            @Query("tipoObjetivo") String tipoObjetivo,
            @Query("soloMios") Boolean soloMios
    );

    @GET("moderacion/reportes/{idReporte}")
    Call<ReporteDetalleDTO> obtenerDetalleReporte(
            @Path("idReporte") Integer idReporte,
            @Query("idModeradorSolicitante") Integer idModeradorSolicitante
    );

    @POST("moderacion/reportes/{idReporte}/tomar")
    Call<RespuestaModeracionDTO> tomarReporte(
            @Path("idReporte") Integer idReporte,
            @Body TomarReporteRequestDTO request
    );

    @POST("moderacion/reportes/{idReporte}/resolver")
    Call<RespuestaModeracionDTO> resolverReporte(
            @Path("idReporte") Integer idReporte,
            @Body ResolverReporteRequestDTO request
    );
}
