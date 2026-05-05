package com.example.artistlan.Conector.model;

public class ResolverReporteRequestDTO {

    private Integer idModerador;
    private String accion;
    private String tipoRespuesta;
    private String mensajeRespuesta;
    private String motivoAccion;
    private String fechaFinSuspension;

    public Integer getIdModerador() {
        return idModerador;
    }

    public void setIdModerador(Integer idModerador) {
        this.idModerador = idModerador;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }

    public String getMensajeRespuesta() {
        return mensajeRespuesta;
    }

    public void setMensajeRespuesta(String mensajeRespuesta) {
        this.mensajeRespuesta = mensajeRespuesta;
    }

    public String getMotivoAccion() {
        return motivoAccion;
    }

    public void setMotivoAccion(String motivoAccion) {
        this.motivoAccion = motivoAccion;
    }

    public String getFechaFinSuspension() {
        return fechaFinSuspension;
    }

    public void setFechaFinSuspension(String fechaFinSuspension) {
        this.fechaFinSuspension = fechaFinSuspension;
    }
}
