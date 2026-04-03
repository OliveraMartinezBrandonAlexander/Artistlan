package com.example.artistlan.TarjetaTextoServicio.model;

public class TarjetaTextoServicioItem {

    private Integer idServicio;
    private String titulo;
    private String descripcion;
    private String contacto;
    private String tecnicas;
    private String autor;
    private String fotoPerfilAutor;
    private String categoria;
    private int likes;
    private boolean favorito;
    private boolean expandido = false;


    public TarjetaTextoServicioItem(Integer idServicio, String titulo, String descripcion, String contacto,
                                    String tecnicas, String autor, String categoria, String fotoPerfilAutor,
                                    int likes, boolean favorito, boolean expandido) {
        this.idServicio = idServicio;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contacto = contacto;
        this.tecnicas = tecnicas;
        this.autor = autor;
        this.categoria = categoria;
        this.fotoPerfilAutor = fotoPerfilAutor;
        this.likes = likes;
        this.favorito = favorito;
        this.expandido = expandido;
    }

    public Integer getIdServicio() {
        return idServicio;
    }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getContacto() { return contacto; }
    public String getTecnicas() { return tecnicas; }
    public String getAutor() { return autor; }
    public String getCategoria() { return categoria; }

    public String getFotoPerfilAutor() {
        return fotoPerfilAutor;
    }

    public void setFotoPerfilAutor(String fotoPerfilAutor) {
        this.fotoPerfilAutor = fotoPerfilAutor;
    }
    public int getLikes() {
        return likes;
    }
    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isFavorito() {
        return favorito;
    }
    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
}