package com.example.artistlan.Fragments;

import com.example.artistlan.Conector.model.NotificacionDTO;
import com.example.artistlan.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MensajeUiUtils {

    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");

    private MensajeUiUtils() {
    }

    public static String formatearFechaCorta(String fechaRaw) {
        if (fechaRaw == null || fechaRaw.trim().isEmpty()) {
            return "Fecha no disponible";
        }

        String valor = fechaRaw.trim();
        Date fecha = intentarParseo(valor,
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        );
        if (fecha == null) {
            return valor;
        }
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", LOCALE_ES_MX).format(fecha);
    }

    public static long toEpochMillis(String fechaRaw) {
        if (fechaRaw == null || fechaRaw.trim().isEmpty()) {
            return 0L;
        }
        Date fecha = intentarParseo(fechaRaw.trim(),
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        );
        return fecha == null ? 0L : fecha.getTime();
    }

    public static int parsearContador(JsonElement body) {
        if (body == null || body.isJsonNull()) {
            return 0;
        }
        try {
            if (body.isJsonPrimitive()) {
                return Math.max(0, body.getAsInt());
            }
            if (body.isJsonArray()) {
                return body.getAsJsonArray().size();
            }
            if (body.isJsonObject()) {
                JsonObject object = body.getAsJsonObject();
                String[] keys = new String[]{
                        "total",
                        "contador",
                        "count",
                        "noLeidas",
                        "no_leidas",
                        "pendientes",
                        "pending"
                };
                for (String key : keys) {
                    JsonElement value = object.get(key);
                    if (value != null && !value.isJsonNull() && value.isJsonPrimitive()) {
                        return Math.max(0, value.getAsInt());
                    }
                }
            }
        } catch (Exception ignored) {
            return 0;
        }
        return 0;
    }

    public static String etiquetaReferencia(String referenciaTipo, Integer referenciaId) {
        if (referenciaTipo == null || referenciaTipo.trim().isEmpty()) {
            return "";
        }
        String tipo = referenciaTipo.trim();
        if (referenciaId == null) {
            return "Ref: " + tipo;
        }
        return "Ref: " + tipo + " #" + referenciaId;
    }

    public static String obtenerTextoCtaSemantico(NotificacionDTO item) {
        if (item == null) {
            return null;
        }
        String tipo = normalizar(item.getTipo());
        if (tipo.contains("obra_vendida") || tipo.contains("compra_confirmada")) {
            return "Ir a historial";
        }
        if (tipo.contains("solicitud_aceptada")
                || tipo.contains("reserva_liberada")
                || tipo.contains("reserva_expirada")
                || tipo.contains("reserva_cancelada")
                || tipo.contains("carrito")) {
            return "Ir al carrito";
        }
        if (tipo.contains("solicitud")) {
            return "Ir a solicitudes";
        }
        return null;
    }

    public static Integer obtenerDestinoSemantico(NotificacionDTO item) {
        if (item == null) {
            return null;
        }
        String tipo = normalizar(item.getTipo());
        if (tipo.contains("obra_vendida") || tipo.contains("compra_confirmada")) {
            return R.id.fragTransacciones;
        }
        if (tipo.contains("solicitud_aceptada")
                || tipo.contains("reserva_liberada")
                || tipo.contains("reserva_expirada")
                || tipo.contains("reserva_cancelada")
                || tipo.contains("carrito")) {
            return R.id.fragCarrito;
        }
        if (tipo.contains("solicitud")) {
            return R.id.fragCentroMensajes;
        }
        return null;
    }

    public static boolean destinoSemanticoRequiereSolicitudesTab(NotificacionDTO item) {
        if (item == null) {
            return false;
        }
        String tipo = normalizar(item.getTipo());
        return tipo.contains("solicitud");
    }

    public static String formatearMensajeConMotivo(String mensajeRaw) {
        if (mensajeRaw == null) {
            return "";
        }
        String mensaje = mensajeRaw.trim();
        if (mensaje.isEmpty()) {
            return mensaje;
        }

        String[] etiquetas = new String[]{
                "Motivo de rechazo:",
                "motivo de rechazo:",
                "Motivo:",
                "motivo:"
        };

        for (String etiqueta : etiquetas) {
            int idx = mensaje.indexOf(etiqueta);
            if (idx >= 0) {
                int inicioValor = idx + etiqueta.length();
                if (inicioValor < mensaje.length()) {
                    while (inicioValor < mensaje.length() && Character.isWhitespace(mensaje.charAt(inicioValor))) {
                        inicioValor++;
                    }
                    String valor = inicioValor < mensaje.length() ? mensaje.substring(inicioValor).trim() : "";
                    String prefijo = mensaje.substring(0, idx).trim();
                    StringBuilder salida = new StringBuilder();
                    if (!prefijo.isEmpty()) {
                        salida.append(prefijo).append("\n\n");
                    }
                    salida.append(etiqueta).append("\n").append(valor);
                    return salida.toString().trim();
                }
            }
        }
        return mensaje;
    }

    private static Date intentarParseo(String value, String... patrones) {
        for (String patron : patrones) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(patron, Locale.US);
                parser.setLenient(true);
                return parser.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toLowerCase(Locale.ROOT);
    }
}
