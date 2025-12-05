package com.example.artistlan.Carrusel.model;

public class ObraCarruselItem {

    private int imagen;        // drawable local (pin1, pin2, pin3)
    private String imagenUrl;  // URL de la BD/Firebase (puede ser null)
    private String titulo;
    private String descripcion;
    private String autor;
    private String likes;

    // Constructor que ya usas para las 3 default
    public ObraCarruselItem(int imagen, String titulo, String descripcion, String autor, String likes) {
        this.imagen = imagen;
        this.imagenUrl = null;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
    }

    // Nuevo constructor para cuando venga info desde la BD
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
