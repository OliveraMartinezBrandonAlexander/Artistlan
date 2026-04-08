package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class NotificacionDTO {

    @SerializedName(value = "idNotificacion", alternate = {"id", "idMensaje", "notificacionId"})
    private Integer idNotificacion;

    @SerializedName(value = "titulo", alternate = {"title", "asunto"})
    private String titulo;

    @SerializedName(value = "mensaje", alternate = {"descripcion", "detalle", "contenido", "body"})
    private String mensaje;

    @SerializedName(value = "fecha", alternate = {"fechaCreacion", "createdAt", "fechaRegistro"})
    private String fecha;

    @SerializedName(value = "leida", alternate = {"leido", "read"})
    private Boolean leida;

    @SerializedName(value = "origen", alternate = {"tipoOrigen", "source"})
    private String origen;

    @SerializedName(value = "nombreOrigen", alternate = {"origenNombre", "sourceName"})
    private String nombreOrigen;

    @SerializedName(value = "usuarioOrigen", alternate = {"loginOrigen", "usernameOrigen", "origenUsuario", "origenLogin"})
    private String usuarioOrigen;

    @SerializedName(value = "fotoOrigen", alternate = {"origenFoto", "avatarOrigen", "sourcePhoto"})
    private String fotoOrigen;

    @SerializedName(value = "referenciaTipo", alternate = {"tipoReferencia", "referenceType"})
    private String referenciaTipo;

    @SerializedName(value = "referenciaId", alternate = {"idReferencia", "referenceId"})
    private Integer referenciaId;

    @SerializedName(value = "tipo", alternate = {"categoria", "kind"})
    private String tipo;

    public Integer getIdNotificacion() {
        return idNotificacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public boolean isLeida() {
        return Boolean.TRUE.equals(leida);
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public String getOrigen() {
        return origen;
    }

    public String getNombreOrigen() {
        return nombreOrigen;
    }

    public String getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public String getFotoOrigen() {
        return fotoOrigen;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public Integer getReferenciaId() {
        return referenciaId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getUsuarioOrigenSeguro() {
        if (usuarioOrigen != null && !usuarioOrigen.trim().isEmpty()) {
            return usuarioOrigen.trim();
        }
        if (nombreOrigen != null && !nombreOrigen.trim().isEmpty() && !nombreOrigen.trim().contains(" ")) {
            return nombreOrigen.trim();
        }
        return null;
    }

    public String getTituloSeguro() {
        if (titulo == null || titulo.trim().isEmpty()) {
            return "Notificacion";
        }
        return titulo.trim();
    }

    public String getMensajeSeguro() {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Sin detalle adicional";
        }
        return mensaje.trim();
    }

    public boolean esDeSistema() {
        String valor = normalizar(origen);
        return valor.contains("sistema") || valor.contains("system");
    }

    public boolean esSolicitud() {
        String tipoNormalizado = normalizar(tipo);
        String referenciaNormalizada = normalizar(referenciaTipo);
        return tipoNormalizado.contains("solicitud")
                || referenciaNormalizada.contains("solicitud")
                || referenciaNormalizada.contains("compra")
                || referenciaNormalizada.contains("request");
    }

    public boolean esSolicitudCreada() {
        String tipoNormalizado = normalizar(tipo);
        if (tipoNormalizado.contains("solicitud_creada")
                || tipoNormalizado.contains("solicitud creada")
                || tipoNormalizado.contains("nueva_solicitud")
                || tipoNormalizado.contains("nueva solicitud")) {
            return true;
        }

        String tituloNormalizado = normalizar(titulo);
        String mensajeNormalizado = normalizar(mensaje);
        boolean mencionaCreacion = tituloNormalizado.contains("solicitud creada")
                || mensajeNormalizado.contains("solicitud creada")
                || mensajeNormalizado.contains("nueva solicitud de compra");
        if (!mencionaCreacion) {
            return false;
        }

        String referenciaNormalizada = normalizar(referenciaTipo);
        return tipoNormalizado.contains("solicitud")
                || referenciaNormalizada.contains("solicitud")
                || referenciaNormalizada.contains("compra");
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toLowerCase(Locale.ROOT);
    }
}
