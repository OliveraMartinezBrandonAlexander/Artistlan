package com.example.artistlan.Conector.model;

public class AdminObservacionRequestDTO {

    private String tipoEstadistica;
    private String tipoDato;
    private String fechaInicioPeriodo;
    private String fechaFinPeriodo;
    private String observacion;

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
}
