package com.example.artistlan.Conector.model;

public class RespuestaModeracionDTO {

    private Boolean success;
    private String message;
    private Integer idReporte;
    private String estadoReporte;
    private String accionEjecutada;
    private String estadoModeracionContenido;
    private String estadoCuentaUsuario;
    private String fecha;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public String getEstadoReporte() {
        return estadoReporte;
    }

    public void setEstadoReporte(String estadoReporte) {
        this.estadoReporte = estadoReporte;
    }

    public String getAccionEjecutada() {
        return accionEjecutada;
    }

    public void setAccionEjecutada(String accionEjecutada) {
        this.accionEjecutada = accionEjecutada;
    }

    public String getEstadoModeracionContenido() {
        return estadoModeracionContenido;
    }

    public void setEstadoModeracionContenido(String estadoModeracionContenido) {
        this.estadoModeracionContenido = estadoModeracionContenido;
    }

    public String getEstadoCuentaUsuario() {
        return estadoCuentaUsuario;
    }

    public void setEstadoCuentaUsuario(String estadoCuentaUsuario) {
        this.estadoCuentaUsuario = estadoCuentaUsuario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
