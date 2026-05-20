package com.example.artistlan.Conector;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.example.artistlan.Conector.model.UsuariosDTO;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

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
    private static final String KEY_LAST_ACTIVE_AT = "lastActiveAt";
    private static final String KEY_LAST_CORREO = "lastCorreo";
    private static final String KEY_LAST_USUARIO = "lastUsuario";
    private static final String KEY_LAST_LOGIN_IDENTIFIER = "lastLoginIdentifier";
    private static final String KEY_LAST_LOGIN_IDENTIFIER_TYPE = "lastLoginIdentifierType";

    public static final String LOGIN_IDENTIFIER_EMAIL = "correo";
    public static final String LOGIN_IDENTIFIER_USERNAME = "usuario";

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
        editor.putString(KEY_LAST_USUARIO, valueOrEmpty(user.getUsuario()));
        editor.putString(KEY_LAST_CORREO, valueOrEmpty(user.getCorreo()));
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
        editor.putLong(KEY_LAST_ACTIVE_AT, System.currentTimeMillis());

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

    public String getLastCorreo() {
        String correo = prefs.getString(KEY_LAST_CORREO, null);
        if (correo == null || correo.trim().isEmpty()) {
            correo = prefs.getString(KEY_CORREO, "");
        }
        return correo != null ? correo.trim() : "";
    }

    public String getLastUsuario() {
        String usuario = prefs.getString(KEY_LAST_USUARIO, null);
        if (usuario == null || usuario.trim().isEmpty()) {
            usuario = prefs.getString(KEY_USUARIO, "");
        }
        return usuario != null ? usuario.trim() : "";
    }

    public void saveLastLoginIdentifier(String identifier, String type) {
        String cleanIdentifier = identifier != null ? identifier.trim() : "";
        String cleanType = normalizeIdentifierType(type);
        if (cleanIdentifier.isEmpty() || cleanType == null) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_LAST_LOGIN_IDENTIFIER, cleanIdentifier)
                .putString(KEY_LAST_LOGIN_IDENTIFIER_TYPE, cleanType);

        if (LOGIN_IDENTIFIER_EMAIL.equals(cleanType)) {
            editor.putString(KEY_LAST_CORREO, cleanIdentifier);
        } else {
            editor.putString(KEY_LAST_USUARIO, cleanIdentifier);
        }

        editor.apply();
    }

    public String getLastLoginIdentifier() {
        String identifier = prefs.getString(KEY_LAST_LOGIN_IDENTIFIER, "");
        return identifier != null ? identifier.trim() : "";
    }

    public String getLastLoginIdentifierType() {
        String type = normalizeIdentifierType(prefs.getString(KEY_LAST_LOGIN_IDENTIFIER_TYPE, ""));
        if (type != null) {
            return type;
        }

        String identifier = getLastLoginIdentifier();
        if (!identifier.isEmpty()) {
            return identifier.contains("@") ? LOGIN_IDENTIFIER_EMAIL : LOGIN_IDENTIFIER_USERNAME;
        }

        if (!getLastUsuario().isEmpty()) {
            return LOGIN_IDENTIFIER_USERNAME;
        }
        if (!getLastCorreo().isEmpty()) {
            return LOGIN_IDENTIFIER_EMAIL;
        }
        return "";
    }

    public boolean hasReusableSession(long maxIdleMs) {
        String token = getToken();
        if (!isLoggedIn() || token == null) {
            clearSession();
            return false;
        }

        long now = System.currentTimeMillis();
        if (isTokenExpired(token, now)) {
            clearSession();
            return false;
        }

        long lastActiveAt = prefs.getLong(KEY_LAST_ACTIVE_AT, 0L);
        if (lastActiveAt <= 0L) {
            touchSession();
            return true;
        }

        if (maxIdleMs > 0L && now - lastActiveAt > maxIdleMs) {
            clearSession();
            return false;
        }

        return true;
    }

    public void touchSession() {
        if (!isLoggedIn() || !hasValidToken()) {
            return;
        }
        prefs.edit().putLong(KEY_LAST_ACTIVE_AT, System.currentTimeMillis()).apply();
    }

    public boolean isTwoFactorEnabled() {
        return prefs.getBoolean(KEY_TWO_FACTOR_ENABLED, false);
    }

    public void updateTwoFactorEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_TWO_FACTOR_ENABLED, enabled).apply();
    }

    public void clearSession() {
        String lastCorreo = getLastCorreo();
        String lastUsuario = getLastUsuario();
        String lastIdentifier = getLastLoginIdentifier();
        String lastIdentifierType = getLastLoginIdentifierType();

        SharedPreferences.Editor editor = prefs.edit().clear();
        if (!lastCorreo.isEmpty()) {
            editor.putString(KEY_LAST_CORREO, lastCorreo);
        }
        if (!lastUsuario.isEmpty()) {
            editor.putString(KEY_LAST_USUARIO, lastUsuario);
        }
        if (!lastIdentifier.isEmpty() && !lastIdentifierType.isEmpty()) {
            editor.putString(KEY_LAST_LOGIN_IDENTIFIER, lastIdentifier);
            editor.putString(KEY_LAST_LOGIN_IDENTIFIER_TYPE, lastIdentifierType);
        }
        editor.apply();
    }

    public boolean hasValidToken() {
        return getToken() != null;
    }

    private boolean isTokenExpired(String token, long nowMs) {
        Long expirationMs = extractJwtExpirationMillis(token);
        return expirationMs == null || nowMs >= expirationMs;
    }

    private Long extractJwtExpirationMillis(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            byte[] decodedPayload = Base64.decode(
                    parts[1],
                    Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING
            );
            String payloadJson = new String(decodedPayload, StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);
            long expSeconds = payload.optLong("exp", -1L);
            if (expSeconds <= 0L) {
                return null;
            }
            return expSeconds * 1000L;
        } catch (Exception ex) {
            return null;
        }
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

    private String normalizeIdentifierType(String type) {
        if (type == null) {
            return null;
        }
        String clean = type.trim().toLowerCase();
        if (LOGIN_IDENTIFIER_EMAIL.equals(clean)) {
            return LOGIN_IDENTIFIER_EMAIL;
        }
        if (LOGIN_IDENTIFIER_USERNAME.equals(clean)) {
            return LOGIN_IDENTIFIER_USERNAME;
        }
        return null;
    }
}
