package com.example.artistlan.Conector.model;

public class CrearReporteRequestDTO {

    private String tipoObjetivo;
    private Integer idObra;
    private Integer idServicio;
    private Integer idUsuarioReportado;
    private Integer idUsuarioReportante;
    private String motivo;
    private String descripcion;

    public String getTipoObjetivo() {
        return tipoObjetivo;
    }

    public void setTipoObjetivo(String tipoObjetivo) {
        this.tipoObjetivo = tipoObjetivo;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public Integer getIdUsuarioReportado() {
        return idUsuarioReportado;
    }

    public void setIdUsuarioReportado(Integer idUsuarioReportado) {
        this.idUsuarioReportado = idUsuarioReportado;
    }

    public Integer getIdUsuarioReportante() {
        return idUsuarioReportante;
    }

    public void setIdUsuarioReportante(Integer idUsuarioReportante) {
        this.idUsuarioReportante = idUsuarioReportante;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
