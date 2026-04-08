package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class SolicitudDTO {

    @SerializedName(value = "idSolicitud", alternate = {"id", "solicitudId", "idNotificacion", "idSolicitudCompra"})
    private Integer idSolicitud;

    @SerializedName(value = "titulo", alternate = {
            "asunto",
            "title"
    })
    private String titulo;

    @SerializedName(value = "mensaje", alternate = {
            "descripcion",
            "detalle",
            "body",
            "mensajeVendedor",
            "comentario"
    })
    private String mensaje;

    @SerializedName(value = "fecha", alternate = {
            "fechaCreacion",
            "createdAt",
            "fechaRegistro",
            "fechaSolicitud",
            "fechaActualizacion"
    })
    private String fecha;

    @SerializedName(value = "estado", alternate = {
            "estatus",
            "status",
            "estadoSolicitud",
            "estadoCompra"
    })
    private String estado;

    @SerializedName(value = "leida", alternate = {"leido", "read"})
    private Boolean leida;

    @SerializedName(value = "nombreOrigen", alternate = {
            "origenNombre",
            "usuarioOrigen",
            "sourceName"
    })
    private String nombreOrigen;

    @SerializedName(value = "fotoOrigen", alternate = {
            "origenFoto",
            "avatarOrigen",
            "sourcePhoto"
    })
    private String fotoOrigen;

    @SerializedName(value = "referenciaTipo", alternate = {"tipoReferencia", "referenceType", "tipoObjeto"})
    private String referenciaTipo;

    @SerializedName(value = "referenciaId", alternate = {"idReferencia", "referenceId", "objetoId", "idObra"})
    private Integer referenciaId;

    @SerializedName(value = "idComprador", alternate = {"compradorId", "idUsuarioComprador"})
    private Integer idComprador;

    @SerializedName(value = "idVendedor", alternate = {"vendedorId", "idUsuarioVendedor"})
    private Integer idVendedor;

    @SerializedName(value = "tituloObra", alternate = {"nombreObra", "obraTitulo"})
    private String tituloObra;

    @SerializedName(value = "mensajeComprador", alternate = {"comentarioComprador"})
    private String mensajeComprador;

    @SerializedName(value = "motivoRechazo", alternate = {"motivo", "razonRechazo", "mensajeRechazo"})
    private String motivoRechazo;

    @SerializedName(value = "fechaRespuesta", alternate = {"respondidaEn", "fechaResolucion", "updatedAt"})
    private String fechaRespuesta;

    @SerializedName(value = "fechaExpiracionReserva", alternate = {"expiraEn", "fechaExpiracion", "reservaExpiraEn"})
    private String fechaExpiracionReserva;

    @SerializedName(value = "nombreComprador", alternate = {"compradorNombre", "nombreUsuarioComprador"})
    private String nombreComprador;

    @SerializedName(value = "fotoComprador", alternate = {"compradorFoto", "fotoUsuarioComprador"})
    private String fotoComprador;

    @SerializedName(value = "nombreVendedor", alternate = {"vendedorNombre", "nombreUsuarioVendedor"})
    private String nombreVendedor;

    @SerializedName(value = "fotoVendedor", alternate = {"vendedorFoto", "fotoUsuarioVendedor"})
    private String fotoVendedor;

    public Integer getIdSolicitud() {
        return idSolicitud;
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

    public String getEstado() {
        return estado;
    }

    public boolean isLeida() {
        return Boolean.TRUE.equals(leida);
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public String getNombreOrigen() {
        return nombreOrigen;
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

    public Integer getIdComprador() {
        return idComprador;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public String getTituloSeguro() {
        if (tituloObra != null && !tituloObra.trim().isEmpty()) {
            return tituloObra.trim();
        }
        if (titulo == null || titulo.trim().isEmpty()) {
            return "Solicitud";
        }
        return titulo.trim();
    }

    public String getMensajeSeguro() {
        if (mensajeComprador != null && !mensajeComprador.trim().isEmpty()) {
            return mensajeComprador.trim();
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Sin detalle adicional";
        }
        return mensaje.trim();
    }

    public String getTituloObra() {
        return tituloObra;
    }

    public String getMensajeComprador() {
        return mensajeComprador;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public String getFechaRespuesta() {
        return fechaRespuesta;
    }

    public String getFechaExpiracionReserva() {
        return fechaExpiracionReserva;
    }

    public String getNombreComprador() {
        return nombreComprador;
    }

    public String getFotoComprador() {
        return fotoComprador;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public String getFotoVendedor() {
        return fotoVendedor;
    }

    public boolean isPendiente() {
        String valor = normalizar(estado);
        return !valor.isEmpty()
                && (valor.contains("pendient")
                || valor.contains("nueva")
                || valor.contains("nuevo")
                || valor.contains("en espera")
                || valor.contains("por atender")
                || valor.contains("solicitad"));
    }

    public boolean isAceptada() {
        String valor = normalizar(estado);
        return valor.contains("acept")
                || valor.contains("aprobad")
                || valor.contains("confirmad");
    }

    public boolean isRechazada() {
        String valor = normalizar(estado);
        return valor.contains("rechaz")
                || valor.contains("denegad");
    }

    public boolean isCancelada() {
        String valor = normalizar(estado);
        return valor.contains("cancel")
                || valor.contains("anulad");
    }

    public boolean isExpirada() {
        String valor = normalizar(estado);
        return valor.contains("expir")
                || valor.contains("vencid")
                || valor.contains("caduc");
    }

    public boolean isPagada() {
        String valor = normalizar(estado);
        return valor.contains("pagad")
                || valor.contains("liquidad")
                || valor.contains("completad");
    }

    public boolean puedeSerCanceladaPorComprador() {
        return isPendiente();
    }

    public boolean puedeSerResueltaPorVendedor() {
        return isPendiente();
    }

    public boolean correspondeAlComprador(int actorId) {
        return idComprador == null || actorId <= 0 || idComprador == actorId;
    }

    public boolean correspondeAlVendedor(int actorId) {
        return idVendedor == null || actorId <= 0 || idVendedor == actorId;
    }

    public String getEstadoVisual() {
        if (isPendiente()) {
            return "Pendiente";
        }
        if (isAceptada()) {
            return "Aceptada";
        }
        if (isRechazada()) {
            return "Rechazada";
        }
        if (isCancelada()) {
            return "Cancelada";
        }
        if (isExpirada()) {
            return "Expirada";
        }
        if (isPagada()) {
            return "Pagada";
        }
        String valor = normalizar(estado);
        if (valor.isEmpty()) {
            return "Sin estado";
        }
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1);
    }

    public String getNombreActorContextual(boolean esRecibida) {
        if (esRecibida) {
            if (nombreComprador != null && !nombreComprador.trim().isEmpty()) {
                return nombreComprador.trim();
            }
        } else {
            if (nombreVendedor != null && !nombreVendedor.trim().isEmpty()) {
                return nombreVendedor.trim();
            }
        }
        if (nombreOrigen != null && !nombreOrigen.trim().isEmpty()) {
            return nombreOrigen.trim();
        }
        return esRecibida ? "Comprador" : "Vendedor";
    }

    public String getFotoActorContextual(boolean esRecibida) {
        if (esRecibida) {
            if (fotoComprador != null && !fotoComprador.trim().isEmpty()) {
                return fotoComprador.trim();
            }
        } else {
            if (fotoVendedor != null && !fotoVendedor.trim().isEmpty()) {
                return fotoVendedor.trim();
            }
        }
        if (fotoOrigen != null && !fotoOrigen.trim().isEmpty()) {
            return fotoOrigen.trim();
        }
        return null;
    }

    public void marcarComoAtendida(boolean aceptada) {
        this.estado = aceptada ? "ACEPTADA" : "RECHAZADA";
        this.leida = true;
    }

    public void marcarComoCancelada() {
        this.estado = "CANCELADA";
        this.leida = true;
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toLowerCase(Locale.ROOT).replace("_", " ");
    }
}
