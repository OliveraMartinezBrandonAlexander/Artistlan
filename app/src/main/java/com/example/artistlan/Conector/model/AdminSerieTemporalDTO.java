package com.example.artistlan.Conector.model;

import java.util.ArrayList;
import java.util.List;

public class AdminSerieTemporalDTO {

    private String fechaReferencia;
    private String fechaInicioPeriodo;
    private String fechaFinPeriodo;
    private List<AdminPuntoSerieDTO> puntos;
    private long totalVentas;
    private Double totalIngresos;
    private String mensaje;

    public String getFechaReferencia() {
        return fechaReferencia;
    }

    public void setFechaReferencia(String fechaReferencia) {
        this.fechaReferencia = fechaReferencia;
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

    public List<AdminPuntoSerieDTO> getPuntos() {
        return puntos != null ? puntos : new ArrayList<>();
    }

    public void setPuntos(List<AdminPuntoSerieDTO> puntos) {
        this.puntos = puntos;
    }

    public long getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(long totalVentas) {
        this.totalVentas = totalVentas;
    }

    public Double getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(Double totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public double getTotalIngresosSeguro() {
        return totalIngresos != null ? totalIngresos : 0d;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
