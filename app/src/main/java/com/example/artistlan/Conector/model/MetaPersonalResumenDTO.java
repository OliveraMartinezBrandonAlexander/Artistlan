package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class MetaPersonalResumenDTO {

    @SerializedName("total")
    private Integer total;

    @SerializedName("activas")
    private Integer activas;

    @SerializedName("porComenzar")
    private Integer porComenzar;

    @SerializedName("enProceso")
    private Integer enProceso;

    @SerializedName("completadas")
    private Integer completadas;

    @SerializedName("expiradas")
    private Integer expiradas;

    @SerializedName("canceladas")
    private Integer canceladas;

    @SerializedName("porcentajeGlobal")
    private BigDecimal porcentajeGlobal;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getActivas() {
        return activas;
    }

    public void setActivas(Integer activas) {
        this.activas = activas;
    }

    public Integer getPorComenzar() {
        return porComenzar;
    }

    public void setPorComenzar(Integer porComenzar) {
        this.porComenzar = porComenzar;
    }

    public Integer getEnProceso() {
        return enProceso;
    }

    public void setEnProceso(Integer enProceso) {
        this.enProceso = enProceso;
    }

    public Integer getCompletadas() {
        return completadas;
    }

    public void setCompletadas(Integer completadas) {
        this.completadas = completadas;
    }

    public Integer getExpiradas() {
        return expiradas;
    }

    public void setExpiradas(Integer expiradas) {
        this.expiradas = expiradas;
    }

    public Integer getCanceladas() {
        return canceladas;
    }

    public void setCanceladas(Integer canceladas) {
        this.canceladas = canceladas;
    }

    public BigDecimal getPorcentajeGlobal() {
        return porcentajeGlobal;
    }

    public void setPorcentajeGlobal(BigDecimal porcentajeGlobal) {
        this.porcentajeGlobal = porcentajeGlobal;
    }
}
