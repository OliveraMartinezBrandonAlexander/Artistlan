package com.example.artistlan.Carrusel.model;

public class ObraCarruselItem {

    private int imagen;
    private String imagenUrl;
    private String titulo;
    private String descripcion;
    private String autor;
    private String likes;
    private String autorFotoUrl;

    // 1) Dummy (sin URLs)
    public ObraCarruselItem(int imagen, String titulo, String descripcion, String autor, String likes) {
        this.imagen = imagen;
        this.imagenUrl = null;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
        this.autorFotoUrl = null;
    }

    // 2) Con URL de la obra
    public ObraCarruselItem(int imagen, String imagenUrl,
                            String titulo, String descripcion, String autor, String likes) {
        this.imagen = imagen;
        this.imagenUrl = imagenUrl;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
        this.autorFotoUrl = null;
    }

    // 3) Con URL de la obra + foto perfil autor
    public ObraCarruselItem(int imagen, String imagenUrl,
                            String titulo, String descripcion, String autor, String likes,
                            String autorFotoUrl) {
        this.imagen = imagen;
        this.imagenUrl = imagenUrl;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
        this.autorFotoUrl = autorFotoUrl;
    }

    public int getImagen() { return imagen; }
    public String getImagenUrl() { return imagenUrl; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getAutor() { return autor; }
    public String getLikes() { return likes; }
    public String getAutorFotoUrl() { return autorFotoUrl; }
}
