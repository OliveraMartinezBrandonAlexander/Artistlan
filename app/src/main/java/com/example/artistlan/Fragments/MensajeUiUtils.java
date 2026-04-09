package com.example.artistlan.Fragments;

import com.example.artistlan.Conector.model.NotificacionDTO;
import com.example.artistlan.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.Normalizer;
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
        int destino = resolverDestinoSemantico(item);
        if (destino == R.id.fragTransacciones) {
            return "Ir a historial";
        }
        if (destino == R.id.fragCarrito) {
            return "Ir al carrito";
        }
        if (destino == R.id.fragCentroMensajes) {
            return "Ir a solicitudes";
        }
        return null;
    }

    public static Integer obtenerDestinoSemantico(NotificacionDTO item) {
        int destino = resolverDestinoSemantico(item);
        return destino == 0 ? null : destino;
    }

    public static boolean destinoSemanticoRequiereSolicitudesTab(NotificacionDTO item) {
        return resolverDestinoSemantico(item) == R.id.fragCentroMensajes;
    }

    public static Integer obtenerTabTransaccionesSemantico(NotificacionDTO item) {
        String contexto = construirContexto(item);
        if (esObraVendida(contexto)) {
            return 1; // Mis ventas
        }
        if (esCompraConfirmada(contexto)) {
            return 0; // Mis compras
        }
        return null;
    }

    public static Integer obtenerModoSolicitudesSemantico(NotificacionDTO item) {
        String contexto = construirContexto(item);
        if (esSolicitudCreada(contexto)) {
            return FragSolicitudesMensajes.MODO_RECIBIDAS;
        }
        if (esSolicitudRechazadaOCancelada(contexto)) {
            return FragSolicitudesMensajes.MODO_ENVIADAS;
        }
        return null;
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
        String sinAcentos = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sinAcentos
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
    }

    private static int resolverDestinoSemantico(NotificacionDTO item) {
        if (item == null) {
            return 0;
        }

        String contexto = construirContexto(item);
        if (contexto.isEmpty()) {
            return 0;
        }

        if (esObraVendida(contexto) || esCompraConfirmada(contexto)) {
            return R.id.fragTransacciones;
        }

        if (esSolicitudCreada(contexto) || esSolicitudRechazadaOCancelada(contexto)) {
            return R.id.fragCentroMensajes;
        }

        if (esSolicitudAceptadaParaComprador(contexto)) {
            return R.id.fragCarrito;
        }
        if (contieneAlguno(contexto, "solicitud aceptada", "solicitud_aceptada")) {
            return 0;
        }

        if (esEventoReserva(contexto)) {
            return esEventoReservaParaComprador(contexto) ? R.id.fragCarrito : 0;
        }

        if (contieneAlguno(contexto, "carrito")) {
            return R.id.fragCarrito;
        }

        if (contexto.contains("solicitud")) {
            return R.id.fragCentroMensajes;
        }
        return 0;
    }

    private static String construirContexto(NotificacionDTO item) {
        StringBuilder contexto = new StringBuilder();
        appendContexto(contexto, item.getTipo());
        appendContexto(contexto, item.getTitulo());
        appendContexto(contexto, item.getMensaje());
        appendContexto(contexto, item.getReferenciaTipo());
        return normalizar(contexto.toString());
    }

    private static void appendContexto(StringBuilder sb, String value) {
        if (sb == null || value == null || value.trim().isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(value.trim());
    }

    private static boolean contieneAlguno(String texto, String... patrones) {
        if (texto == null || texto.isEmpty() || patrones == null) {
            return false;
        }
        for (String patron : patrones) {
            if (patron == null || patron.trim().isEmpty()) {
                continue;
            }
            if (texto.contains(normalizar(patron))) {
                return true;
            }
        }
        return false;
    }

    private static boolean esObraVendida(String contexto) {
        return contieneAlguno(contexto, "obra vendida", "obra_vendida", "venta concretada");
    }

    private static boolean esCompraConfirmada(String contexto) {
        return contieneAlguno(contexto,
                "compra confirmada",
                "compra_confirmada",
                "pago confirmado",
                "pago completado",
                "compra completada");
    }

    private static boolean esSolicitudCreada(String contexto) {
        return contieneAlguno(contexto, "solicitud creada", "solicitud_creada", "nueva solicitud");
    }

    private static boolean esSolicitudAceptadaParaComprador(String contexto) {
        if (!contieneAlguno(contexto, "solicitud aceptada", "solicitud_aceptada")) {
            return false;
        }
        if (contieneAlguno(contexto, "aceptaste", "has aceptado", "como vendedor")) {
            return false;
        }
        return contieneAlguno(contexto,
                "tu solicitud",
                "agregada al carrito",
                "expira en 7 dias",
                "expirara en",
                "comprador");
    }

    private static boolean esSolicitudRechazadaOCancelada(String contexto) {
        return contieneAlguno(contexto,
                "solicitud rechazada",
                "solicitud_rechazada",
                "solicitud cancelada",
                "solicitud_cancelada");
    }

    private static boolean esEventoReserva(String contexto) {
        return contieneAlguno(contexto,
                "reserva liberada",
                "reserva_liberada",
                "reserva expirada",
                "reserva_expirada");
    }

    private static boolean esEventoReservaParaComprador(String contexto) {
        if (esEventoReservaParaVendedor(contexto)) {
            return false;
        }
        if (contieneAlguno(contexto,
                "tu carrito",
                "quitaste del carrito",
                "eliminaste del carrito",
                "cancelaste tu reserva",
                "tu reserva fue cancelada",
                "puedes volver a comprar",
                "intenta nuevamente la compra",
                "tu solicitud")) {
            return true;
        }
        return contieneAlguno(contexto, "comprador", "compra");
    }

    private static boolean esEventoReservaParaVendedor(String contexto) {
        return contieneAlguno(contexto,
                "tu obra",
                "como vendedor",
                "obra de tu portafolio",
                "solicitudes de tu obra",
                "alguien cancelo su reserva",
                "volvio a estar en venta",
                "volvio a en venta");
    }
}
