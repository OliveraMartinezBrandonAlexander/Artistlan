package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.AdminCategoriaStatsDTO;
import com.example.artistlan.Conector.model.AdminCrecimientoDTO;
import com.example.artistlan.Conector.model.AdminObservacionDTO;
import com.example.artistlan.Conector.model.AdminObservacionRequestDTO;
import com.example.artistlan.Conector.model.AdminRankingResponseDTO;
import com.example.artistlan.Conector.model.AdminSerieTemporalDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminEstadisticasApi {

    @GET("admin/estadisticas/categorias")
    Call<List<AdminCategoriaStatsDTO>> obtenerCategorias(@Query("tipo") String tipo);

    @GET("admin/estadisticas/ventas-semanales")
    Call<AdminSerieTemporalDTO> obtenerVentasSemanales(@Query("fecha") String fecha);

    @GET("admin/estadisticas/ranking")
    Call<AdminRankingResponseDTO> obtenerRanking(@Query("tipo") String tipo, @Query("limit") Integer limit);

    @GET("admin/estadisticas/crecimiento")
    Call<AdminCrecimientoDTO> obtenerCrecimiento(@Query("tipo") String tipo, @Query("fecha") String fecha);

    @GET("admin/estadisticas/observaciones")
    Call<List<AdminObservacionDTO>> obtenerObservaciones(
            @Query("tipoEstadistica") String tipoEstadistica,
            @Query("tipoDato") String tipoDato,
            @Query("fechaInicioPeriodo") String fechaInicioPeriodo,
            @Query("fechaFinPeriodo") String fechaFinPeriodo
    );

    @POST("admin/estadisticas/observaciones")
    Call<AdminObservacionDTO> crearObservacion(@Body AdminObservacionRequestDTO request);

    @PUT("admin/estadisticas/observaciones/{id}")
    Call<AdminObservacionDTO> actualizarObservacion(
            @Path("id") Integer idObservacion,
            @Body AdminObservacionRequestDTO request
    );

    @DELETE("admin/estadisticas/observaciones/{id}")
    Call<Void> eliminarObservacion(@Path("id") Integer idObservacion);
}
