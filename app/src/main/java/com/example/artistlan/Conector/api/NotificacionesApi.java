package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.NotificacionDTO;
import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface NotificacionesApi {

    @GET("notificaciones/{idUsuario}")
    Call<List<NotificacionDTO>> obtenerNotificacionesPorUsuario(@Path("idUsuario") int idUsuario);

    @GET("notificaciones/{idUsuario}/{idNotificacion}")
    Call<NotificacionDTO> obtenerNotificacionPorId(
            @Path("idUsuario") int idUsuario,
            @Path("idNotificacion") int idNotificacion
    );

    @PATCH("notificaciones/{idUsuario}/{idNotificacion}/leida")
    Call<Void> marcarNotificacionComoLeida(
            @Path("idUsuario") int idUsuario,
            @Path("idNotificacion") int idNotificacion
    );

    @PATCH("notificaciones/{idUsuario}/leidas")
    Call<Void> marcarTodasComoLeidas(@Path("idUsuario") int idUsuario);

    @DELETE("notificaciones/{idUsuario}/{idNotificacion}")
    Call<Void> eliminarNotificacion(
            @Path("idUsuario") int idUsuario,
            @Path("idNotificacion") int idNotificacion
    );

    @GET("notificaciones/{idUsuario}/contador-no-leidas")
    Call<JsonElement> obtenerContadorNoLeidas(@Path("idUsuario") int idUsuario);
}
