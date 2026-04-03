package com.example.artistlan.Conector.model;

public class CambiarRolRequestDTO {
    private String rol;

    public CambiarRolRequestDTO(String rol) {
        this.rol = rol;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}