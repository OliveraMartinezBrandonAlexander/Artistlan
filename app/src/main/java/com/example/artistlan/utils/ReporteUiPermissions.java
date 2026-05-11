package com.example.artistlan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.artistlan.Conector.SessionManager;

import java.util.Locale;

public final class ReporteUiPermissions {
    private ReporteUiPermissions() {
    }

    public static boolean esRolUsuarioReportanteValido(@Nullable String rolActual) {
        String rolNormalizado = normalizarRol(rolActual);
        boolean permitido = "USER".equals(rolNormalizado) || "USUARIO".equals(rolNormalizado);
        return permitido;
    }

    public static boolean esRolAdminOModerador(@Nullable String rolActual) {
        String rolNormalizado = normalizarRol(rolActual);
        return "ADMIN".equals(rolNormalizado) || "MODERADOR".equals(rolNormalizado);
    }

    public static boolean puedeMostrarReportar(@Nullable Integer idUsuarioActual, @Nullable String rolActual) {
        boolean resultado = idUsuarioActual != null
                && idUsuarioActual > 0
                && esRolUsuarioReportanteValido(rolActual);
        return resultado;
    }

    public static boolean puedeReportarContenidoAjeno(@Nullable Integer idUsuarioActual, @Nullable Integer idDueno, @Nullable String rolActual) {
        boolean resultado = puedeMostrarReportar(idUsuarioActual, rolActual)
                && idDueno != null
                && !idDueno.equals(idUsuarioActual);
        return resultado;
    }

    @Nullable
    public static Integer resolveCurrentUserId(@Nullable Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) {
            return null;
        }
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        return idUsuario > 0 ? idUsuario : null;
    }

    @NonNull
    public static String resolveCurrentUserRole(@Nullable Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) {
            return "";
        }
        String rol = prefs.getString("rol", null);
        String modo = prefs.getString("modo", "");
        if (rol == null || rol.trim().isEmpty()) {
            // Compatibilidad con sesiones antiguas donde el rol se guardaba en "modo".
            rol = modo;
        }
        return rol != null ? rol : "";
    }

    @Nullable
    private static SharedPreferences getPrefs(@Nullable Context context) {
        if (context == null) {
            return null;
        }
        return context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    private static String normalizarRol(@Nullable String rolActual) {
        if (rolActual == null) {
            return "";
        }
        return rolActual.trim().toUpperCase(Locale.ROOT);
    }
}
