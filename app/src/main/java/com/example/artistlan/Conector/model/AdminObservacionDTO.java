package com.example.artistlan.Conector.model;

public class AdminObservacionDTO {

    private Integer idObservacion;
    private Integer idAdmin;
    private String nombreAdmin;
    private String tipoEstadistica;
    private String tipoDato;
    private String fechaInicioPeriodo;
    private String fechaFinPeriodo;
    private String observacion;
    private String fechaCreacion;
    private String fechaActualizacion;

    public Integer getIdObservacion() {
        return idObservacion;
    }

    public void setIdObservacion(Integer idObservacion) {
        this.idObservacion = idObservacion;
    }

    public Integer getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getNombreAdmin() {
        return nombreAdmin;
    }

    public void setNombreAdmin(String nombreAdmin) {
        this.nombreAdmin = nombreAdmin;
    }

    public String getTipoEstadistica() {
        return tipoEstadistica;
    }

    public void setTipoEstadistica(String tipoEstadistica) {
        this.tipoEstadistica = tipoEstadistica;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getFechaInicioPeriodo() {
        return fechaInicioPeriodo;
    }

    public void setFechaInicioPeriodo(String fechaInicioPeriodo) {
        this.fechaInicioPeriodo = fechaInicioPeriodo;
    }

    public String getFechaFinPeriodo() {
        return fechaFinPeriodo;
    }

    public void setFechaFinPeriodo(String fechaFinPeriodo) {
        this.fechaFinPeriodo = fechaFinPeriodo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
