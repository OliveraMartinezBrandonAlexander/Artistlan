package com.example.artistlan.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class ModeracionUiMapper {

    private ModeracionUiMapper() {
    }

    public static String formatEstadoReporte(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return "No disponible";
        }

        switch (normalized) {
            case "PENDIENTE":
                return "Pendiente";
            case "EN_REVISION":
                return "En revisión";
            case "RESUELTO":
                return "Resuelto";
            case "DESCARTADO":
                return "Descartado";
            default:
                return humanizeFallback(normalized);
        }
    }

    public static String formatTipoObjetivo(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return "No disponible";
        }

        switch (normalized) {
            case "OBRA":
                return "Obra";
            case "SERVICIO":
                return "Servicio";
            case "USUARIO":
                return "Usuario";
            default:
                return humanizeFallback(normalized);
        }
    }

    public static String formatPrioridad(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return "No disponible";
        }

        switch (normalized) {
            case "BAJA":
                return "Baja";
            case "MEDIA":
                return "Media";
            case "ALTA":
                return "Alta";
            default:
                return humanizeFallback(normalized);
        }
    }

    public static String formatAccionResolucion(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return "No disponible";
        }

        switch (normalized) {
            case "DESCARTAR_REPORTE":
                return "Descartar reporte";
            case "OCULTAR_CONTENIDO":
                return "Ocultar contenido";
            case "REACTIVAR_CONTENIDO":
                return "Reactivar contenido";
            case "ELIMINAR_CONTENIDO_LOGICO":
                return "Retirar contenido";
            case "ADVERTENCIA":
                return "Advertencia";
            case "SUSPENDER_USUARIO":
                return "Suspender usuario";
            case "REACTIVAR_USUARIO":
                return "Reactivar usuario";
            case "BLOQUEAR_USUARIO_PERMANENTE":
                return "Bloquear usuario permanentemente";
            case "SIN_ACCION":
                return "Sin acción";
            default:
                return humanizeFallback(normalized);
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        return normalized.replaceAll("_+", "_");
    }

    private static String humanizeFallback(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "No disponible";
        }

        String[] parts = value.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            String lower = part.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(lower.charAt(0)));
            if (lower.length() > 1) {
                builder.append(lower.substring(1));
            }
        }

        return builder.length() > 0 ? builder.toString() : "No disponible";
    }
}
