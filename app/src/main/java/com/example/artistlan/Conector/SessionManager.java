package com.example.artistlan.Conector;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.artistlan.Conector.model.UsuariosDTO;

public class SessionManager {

    public static final String PREF_NAME = "usuario_prefs";
    private static final String KEY_ID = "id";
    private static final String KEY_ID_USUARIO = "idUsuario";
    private static final String KEY_USUARIO = "usuario";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_NOMBRE_COMPLETO = "nombreCompleto";
    private static final String KEY_ROL = "rol";
    private static final String KEY_DESCRIPCION = "descripcion";
    private static final String KEY_FOTO_PERFIL = "fotoPerfil";
    private static final String KEY_TELEFONO = "telefono";
    private static final String KEY_REDES_SOCIALES = "redesSociales";
    private static final String KEY_REDES_LEGACY = "redes";
    private static final String KEY_FECHA_NACIMIENTO = "fechaNacimiento";
    private static final String KEY_FECHA_NAC_LEGACY = "fechaNac";
    private static final String KEY_UBICACION = "ubicacion";
    private static final String KEY_TWO_FACTOR_ENABLED = "twoFactorEnabled";
    private static final String KEY_JWT_TOKEN = "jwtToken";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(UsuariosDTO user, String token) {
        if (user == null) {
            return;
        }

        Integer idUsuario = user.getIdUsuario();
        if (idUsuario == null) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ID, idUsuario);
        editor.putInt(KEY_ID_USUARIO, idUsuario);
        editor.putString(KEY_USUARIO, valueOrEmpty(user.getUsuario()));
        editor.putString(KEY_CORREO, valueOrEmpty(user.getCorreo()));
        editor.putString(KEY_NOMBRE_COMPLETO, valueOrEmpty(user.getNombreCompleto()));
        editor.putString(KEY_ROL, valueOrDefault(user.getRol(), "USER"));
        editor.putString(KEY_DESCRIPCION, valueOrEmpty(user.getDescripcion()));
        editor.putString(KEY_FOTO_PERFIL, valueOrEmpty(user.getFotoPerfil()));
        editor.putString(KEY_TELEFONO, valueOrEmpty(user.getTelefono()));
        editor.putString(KEY_REDES_SOCIALES, valueOrEmpty(user.getRedesSociales()));
        editor.putString(KEY_REDES_LEGACY, valueOrEmpty(user.getRedesSociales()));
        editor.putString(KEY_FECHA_NACIMIENTO, valueOrEmpty(user.getFechaNacimiento()));
        editor.putString(KEY_FECHA_NAC_LEGACY, valueOrEmpty(user.getFechaNacimiento()));
        editor.putString(KEY_UBICACION, valueOrEmpty(user.getUbicacion()));
        editor.putBoolean(KEY_TWO_FACTOR_ENABLED, Boolean.TRUE.equals(user.getTwoFactorEnabled()));

        String tokenNormalizado = sanitizeToken(token);
        if (tokenNormalizado != null) {
            editor.putString(KEY_JWT_TOKEN, tokenNormalizado);
        } else {
            editor.remove(KEY_JWT_TOKEN);
        }

        // Limpieza de datos sensibles de versiones anteriores.
        editor.remove("contrasena");
        editor.apply();
    }

    public String getToken() {
        return sanitizeToken(prefs.getString(KEY_JWT_TOKEN, null));
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_ID_USUARIO) && prefs.getInt(KEY_ID_USUARIO, -1) > 0;
    }

    public boolean isTwoFactorEnabled() {
        return prefs.getBoolean(KEY_TWO_FACTOR_ENABLED, false);
    }

    public void updateTwoFactorEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TWO_FACTOR_ENABLED, enabled).apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean hasValidToken() {
        return getToken() != null;
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    private String sanitizeToken(String token) {
        if (token == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }
}
