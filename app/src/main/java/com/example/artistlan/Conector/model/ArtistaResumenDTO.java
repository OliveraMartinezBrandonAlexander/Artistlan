package com.example.artistlan.Conector.model;

import java.util.ArrayList;
import java.util.List;

public class ArtistaResumenDTO {
    private Integer idUsuario;
    private String nombreCompleto;
    private String usuario;
    private String descripcion;
    private String fotoPerfil;
    private Integer idCategoria;
    private String categoria;
    private Integer likes;
    private Boolean esFavorito;
    private String rol;
    private List<String> miniObras;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public String getCategoria() {
        return categoria;
    }

    public Integer getLikes() {
        return likes;
    }

    public Boolean getEsFavorito() {
        return esFavorito;
    }

    public String getRol() {
        return rol;
    }

    public List<String> getMiniObras() {
        return miniObras != null ? miniObras : new ArrayList<>();
    }
}
