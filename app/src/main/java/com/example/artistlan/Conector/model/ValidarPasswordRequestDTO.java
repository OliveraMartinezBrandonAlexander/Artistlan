package com.example.artistlan.Conector.model;

public class ValidarPasswordRequestDTO {
    private String contrasena;

    public ValidarPasswordRequestDTO() {
    }

    public ValidarPasswordRequestDTO(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}

