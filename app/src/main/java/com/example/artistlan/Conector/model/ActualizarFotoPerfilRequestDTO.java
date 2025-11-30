package com.example.artistlan.Conector.model;

public class ActualizarFotoPerfilRequestDTO
{
    private String fotoPerfil;

    public ActualizarFotoPerfilRequestDTO(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}
