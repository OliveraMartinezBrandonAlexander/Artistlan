package com.example.artistlan.utils;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class PublicationDateFormatter {
    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");

    private PublicationDateFormatter() {
    }

    @Nullable
    public static String formatear(@Nullable String fechaRaw) {
        if (fechaRaw == null || fechaRaw.trim().isEmpty()) {
            return null;
        }

        String valor = fechaRaw.trim();
        boolean soloFecha = valor.matches("\\d{4}-\\d{2}-\\d{2}");
        Date fecha = intentarParseo(valor,
                soloFecha ? "yyyy-MM-dd" : null,
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
        );

        if (fecha == null) {
            return null;
        }

        String formatoSalida = soloFecha ? "dd/MM/yyyy" : "dd/MM/yyyy HH:mm";
        return crearFormato(formatoSalida).format(fecha);
    }

    @Nullable
    private static Date intentarParseo(String valor, String... formatos) {
        for (String formato : formatos) {
            if (formato == null) {
                continue;
            }
            try {
                return crearFormato(formato).parse(valor);
            } catch (ParseException ignored) {
                // Se intenta con el siguiente formato soportado.
            }
        }
        return null;
    }

    private static SimpleDateFormat crearFormato(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, LOCALE_ES_MX);
        format.setLenient(false);
        return format;
    }
}
