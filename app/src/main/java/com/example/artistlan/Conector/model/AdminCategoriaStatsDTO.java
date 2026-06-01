package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class AdminCategoriaStatsDTO {

    private Integer idCategoria;
    private String categoria;

    @SerializedName(value = "total", alternate = {"cantidad"})
    private long total;

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCantidad() {
        return total;
    }
}
