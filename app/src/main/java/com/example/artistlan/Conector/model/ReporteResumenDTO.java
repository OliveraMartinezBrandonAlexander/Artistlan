package com.example.artistlan.Conector.model;

public class ReporteResumenDTO {

    private Integer idReporte;
    private String tipoObjetivo;
    private Integer idObra;
    private Integer idServicio;
    private Integer idUsuarioReportado;
    private String tituloObjetivo;
    private String nombreUsuarioReportante;
    private String nombreUsuarioReportado;
    private String motivo;
    private String estado;
    private String prioridad;
    private Integer idModeradorAsignado;
    private String nombreModeradorAsignado;
    private String fechaReporte;

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

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

    public String getTituloObjetivo() {
        return tituloObjetivo;
    }

    public void setTituloObjetivo(String tituloObjetivo) {
        this.tituloObjetivo = tituloObjetivo;
    }

    public String getNombreUsuarioReportante() {
        return nombreUsuarioReportante;
    }

    public void setNombreUsuarioReportante(String nombreUsuarioReportante) {
        this.nombreUsuarioReportante = nombreUsuarioReportante;
    }

    public String getNombreUsuarioReportado() {
        return nombreUsuarioReportado;
    }

    public void setNombreUsuarioReportado(String nombreUsuarioReportado) {
        this.nombreUsuarioReportado = nombreUsuarioReportado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public Integer getIdModeradorAsignado() {
        return idModeradorAsignado;
    }

    public void setIdModeradorAsignado(Integer idModeradorAsignado) {
        this.idModeradorAsignado = idModeradorAsignado;
    }

    public String getNombreModeradorAsignado() {
        return nombreModeradorAsignado;
    }

    public void setNombreModeradorAsignado(String nombreModeradorAsignado) {
        this.nombreModeradorAsignado = nombreModeradorAsignado;
    }

    public String getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(String fechaReporte) {
        this.fechaReporte = fechaReporte;
    }
}
