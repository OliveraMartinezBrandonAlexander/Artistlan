package com.example.artistlan.TarjetaTextoArtista.model;

import java.util.List;

public class TarjetaTextoArtistaItem {

    private Integer idArtista;
    private String nombre;
    private String categoria;
    private String descripcion;
    private String fotoPerfil;
    private List<String> miniObras;
    private int likes;
    private boolean favorito;
    private boolean expandido = false;

    public TarjetaTextoArtistaItem(Integer idArtista, String nombre, String categoria, String descripcion,
                                   String fotoPerfil, List<String> miniObras, int likes, boolean favorito) {
        this.idArtista = idArtista;
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fotoPerfil = fotoPerfil;
        this.miniObras = miniObras;
        this.likes = likes;
        this.favorito = favorito;
    }

    public Integer getIdArtista() {
        return idArtista;
    }

    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getFotoPerfil() { return fotoPerfil; }
    public List<String> getMiniObras() { return miniObras; }
    public int getLikes() { return likes; }

    public boolean isFavorito() { return favorito; }
    public boolean isExpandido() { return expandido; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
}