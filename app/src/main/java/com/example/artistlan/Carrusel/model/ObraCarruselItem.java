package com.example.artistlan.Carrusel.model;

public class ObraCarruselItem {

    private int imagen;
    private String imagenUrl;
    private String titulo;
    private String descripcion;
    private String autor;
    private String likes;

    public ObraCarruselItem(int imagen, String titulo, String descripcion, String autor, String likes) {
        this.imagen = imagen;
        this.imagenUrl = null;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
    }

    public ObraCarruselItem(int imagen, String imagenUrl,
                            String titulo, String descripcion, String autor, String likes) {
        this.imagen = imagen;
        this.imagenUrl = imagenUrl;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
    }

    public int getImagen() {
        return imagen;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAutor() {
        return autor;
    }

    public String getLikes() {
        return likes;
    }
}
