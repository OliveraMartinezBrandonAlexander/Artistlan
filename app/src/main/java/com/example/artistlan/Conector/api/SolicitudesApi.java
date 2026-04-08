package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ContadorPendientesDTO;
import com.example.artistlan.Conector.model.ResolverSolicitudRequestDTO;
import com.example.artistlan.Conector.model.SolicitudCompraCrearRequestDTO;
import com.example.artistlan.Conector.model.SolicitudDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SolicitudesApi {

    @POST("solicitudes-compra")
    Call<SolicitudDTO> crearSolicitudCompra(@Body SolicitudCompraCrearRequestDTO request);

    @GET("solicitudes-compra/recibidas/{vendedorId}")
    Call<List<SolicitudDTO>> obtenerSolicitudesRecibidas(@Path("vendedorId") int vendedorId);

    @GET("solicitudes-compra/enviadas/{compradorId}")
    Call<List<SolicitudDTO>> obtenerSolicitudesEnviadas(@Path("compradorId") int compradorId);

    @GET("solicitudes-compra/{idSolicitud}")
    Call<SolicitudDTO> obtenerSolicitudPorId(
            @Path("idSolicitud") int idSolicitud,
            @Query("actorId") int actorId
    );

    @POST("solicitudes-compra/{idSolicitud}/aceptar")
    Call<Void> aceptarSolicitud(
            @Path("idSolicitud") int idSolicitud,
            @Body ResolverSolicitudRequestDTO request
    );

    @POST("solicitudes-compra/{idSolicitud}/rechazar")
    Call<Void> rechazarSolicitud(
            @Path("idSolicitud") int idSolicitud,
            @Body ResolverSolicitudRequestDTO request
    );

    @POST("solicitudes-compra/{idSolicitud}/cancelar")
    Call<Void> cancelarSolicitud(
            @Path("idSolicitud") int idSolicitud,
            @Query("idComprador") int idComprador
    );

    @GET("solicitudes-compra/{usuarioId}/contador-pendientes")
    Call<ContadorPendientesDTO> obtenerContadorPendientes(@Path("usuarioId") int usuarioId);
}
