package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class ContadorPendientesDTO {

    @SerializedName("pendientes")
    private Integer pendientes;

    public int getPendientes() {
        return pendientes == null ? 0 : Math.max(0, pendientes);
    }
}
