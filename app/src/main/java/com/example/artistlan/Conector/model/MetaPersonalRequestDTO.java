package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class MetaPersonalRequestDTO {

    @SerializedName("tipoMeta")
    private String tipoMeta;

    @SerializedName("objetivo")
    private BigDecimal objetivo;

    @SerializedName("fechaInicio")
    private String fechaInicio;

    @SerializedName("fechaFin")
    private String fechaFin;

    public String getTipoMeta() {
        return tipoMeta;
    }

    public void setTipoMeta(String tipoMeta) {
        this.tipoMeta = tipoMeta;
    }

    public BigDecimal getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(BigDecimal objetivo) {
        this.objetivo = objetivo;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }
}
