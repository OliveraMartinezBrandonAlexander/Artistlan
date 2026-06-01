package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class MetaPersonalCancelRequestDTO {

    @SerializedName("motivoCancelacion")
    private String motivoCancelacion;

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
}
