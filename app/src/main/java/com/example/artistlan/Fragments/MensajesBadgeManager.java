package com.example.artistlan.Fragments;

import androidx.annotation.NonNull;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.NotificacionesApi;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.model.ContadorPendientesDTO;
import com.example.artistlan.Conector.model.SolicitudDTO;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class MensajesBadgeManager {

    public interface BadgeCallback {
        void onBadgeResult(int totalPendientes);
    }

    public interface BadgeDetalleCallback {
        void onBadgeResult(@NonNull BadgeDetalle detalle);
    }

    public static final class BadgeDetalle {
        private final int notificacionesNoLeidas;
        private final int solicitudesPendientes;

        public BadgeDetalle(int notificacionesNoLeidas, int solicitudesPendientes) {
            this.notificacionesNoLeidas = Math.max(0, notificacionesNoLeidas);
            this.solicitudesPendientes = Math.max(0, solicitudesPendientes);
        }

        public int getNotificacionesNoLeidas() {
            return notificacionesNoLeidas;
        }

        public int getSolicitudesPendientes() {
            return solicitudesPendientes;
        }

        public int getTotalPendientes() {
            return notificacionesNoLeidas + solicitudesPendientes;
        }
    }

    private MensajesBadgeManager() {
    }

    public static void refrescarBadge(int idUsuario, @NonNull BadgeCallback callback) {
        refrescarBadgeDetalle(idUsuario, detalle -> callback.onBadgeResult(detalle.getTotalPendientes()));
    }

    public static void refrescarBadgeDetalle(int idUsuario, @NonNull BadgeDetalleCallback callback) {
        if (idUsuario <= 0) {
            callback.onBadgeResult(new BadgeDetalle(0, 0));
            return;
        }

        NotificacionesApi notificacionesApi = RetrofitClient.getClient().create(NotificacionesApi.class);
        SolicitudesApi solicitudesApi = RetrofitClient.getClient().create(SolicitudesApi.class);

        AtomicInteger pendientes = new AtomicInteger(2);
        int[] notificaciones = new int[]{0};
        int[] solicitudes = new int[]{0};

        Callback<JsonElement> contadorNotificacionesCb = new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    notificaciones[0] = MensajeUiUtils.parsearContador(response.body());
                }
                intentarTerminar();
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                intentarTerminar();
            }

            private void intentarTerminar() {
                if (pendientes.decrementAndGet() == 0) {
                    callback.onBadgeResult(new BadgeDetalle(notificaciones[0], solicitudes[0]));
                }
            }
        };

        Callback<ContadorPendientesDTO> contadorSolicitudesCb = new Callback<ContadorPendientesDTO>() {
            @Override
            public void onResponse(@NonNull Call<ContadorPendientesDTO> call, @NonNull Response<ContadorPendientesDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    solicitudes[0] = response.body().getPendientes();
                }
                intentarTerminar();
            }

            @Override
            public void onFailure(@NonNull Call<ContadorPendientesDTO> call, @NonNull Throwable t) {
                intentarTerminar();
            }

            private void intentarTerminar() {
                if (pendientes.decrementAndGet() == 0) {
                    callback.onBadgeResult(new BadgeDetalle(notificaciones[0], solicitudes[0]));
                }
            }
        };

        notificacionesApi.obtenerContadorNoLeidas(idUsuario).enqueue(contadorNotificacionesCb);
        solicitudesApi.obtenerContadorPendientes(idUsuario).enqueue(contadorSolicitudesCb);
    }

    public static int contarPendientesSolicitudes(List<SolicitudDTO> solicitudes) {
        if (solicitudes == null) {
            return 0;
        }
        int pendientes = 0;
        for (SolicitudDTO solicitud : solicitudes) {
            if (solicitud != null && solicitud.isPendiente()) {
                pendientes++;
            }
        }
        return pendientes;
    }
}
