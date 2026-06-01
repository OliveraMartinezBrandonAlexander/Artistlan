package com.example.artistlan.Conector.model;

import java.util.ArrayList;
import java.util.List;

public class AdminCrecimientoDTO {

    private String tipo;
    private String fechaReferencia;
    private String fechaInicioSemanaActual;
    private String fechaFinSemanaActual;
    private String fechaInicioSemanaAnterior;
    private String fechaFinSemanaAnterior;
    private List<AdminPuntoSerieDTO> serieSemanaActual;
    private List<AdminPuntoSerieDTO> serieSemanaAnterior;
    private Integer diasComparados;
    private long totalSemanaActual;
    private long totalSemanaAnterior;
    private Double porcentajeCambio;
    private boolean periodoAnteriorSinDatos;
    private String mensaje;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFechaReferencia() {
        return fechaReferencia;
    }

    public void setFechaReferencia(String fechaReferencia) {
        this.fechaReferencia = fechaReferencia;
    }

    public String getFechaInicioSemanaActual() {
        return fechaInicioSemanaActual;
    }

    public void setFechaInicioSemanaActual(String fechaInicioSemanaActual) {
        this.fechaInicioSemanaActual = fechaInicioSemanaActual;
    }

    public String getFechaFinSemanaActual() {
        return fechaFinSemanaActual;
    }

    public void setFechaFinSemanaActual(String fechaFinSemanaActual) {
        this.fechaFinSemanaActual = fechaFinSemanaActual;
    }

    public String getFechaInicioSemanaAnterior() {
        return fechaInicioSemanaAnterior;
    }

    public void setFechaInicioSemanaAnterior(String fechaInicioSemanaAnterior) {
        this.fechaInicioSemanaAnterior = fechaInicioSemanaAnterior;
    }

    public String getFechaFinSemanaAnterior() {
        return fechaFinSemanaAnterior;
    }

    public void setFechaFinSemanaAnterior(String fechaFinSemanaAnterior) {
        this.fechaFinSemanaAnterior = fechaFinSemanaAnterior;
    }

    public List<AdminPuntoSerieDTO> getSerieSemanaActual() {
        return serieSemanaActual != null ? serieSemanaActual : new ArrayList<>();
    }

    public void setSerieSemanaActual(List<AdminPuntoSerieDTO> serieSemanaActual) {
        this.serieSemanaActual = serieSemanaActual;
    }

    public List<AdminPuntoSerieDTO> getSerieSemanaAnterior() {
        return serieSemanaAnterior != null ? serieSemanaAnterior : new ArrayList<>();
    }

    public void setSerieSemanaAnterior(List<AdminPuntoSerieDTO> serieSemanaAnterior) {
        this.serieSemanaAnterior = serieSemanaAnterior;
    }

    public Integer getDiasComparados() {
        return diasComparados;
    }

    public void setDiasComparados(Integer diasComparados) {
        this.diasComparados = diasComparados;
    }

    public long getTotalSemanaActual() {
        return totalSemanaActual;
    }

    public void setTotalSemanaActual(long totalSemanaActual) {
        this.totalSemanaActual = totalSemanaActual;
    }

    public long getTotalSemanaAnterior() {
        return totalSemanaAnterior;
    }

    public void setTotalSemanaAnterior(long totalSemanaAnterior) {
        this.totalSemanaAnterior = totalSemanaAnterior;
    }

    public Double getPorcentajeCambio() {
        return porcentajeCambio;
    }

    public void setPorcentajeCambio(Double porcentajeCambio) {
        this.porcentajeCambio = porcentajeCambio;
    }

    public boolean isPeriodoAnteriorSinDatos() {
        return periodoAnteriorSinDatos;
    }

    public void setPeriodoAnteriorSinDatos(boolean periodoAnteriorSinDatos) {
        this.periodoAnteriorSinDatos = periodoAnteriorSinDatos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
