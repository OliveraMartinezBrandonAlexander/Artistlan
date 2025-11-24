package com.example.artistlan.Carrusel.model;

public class ObraCarruselItem {
    private int imagen;
    private String titulo;
    private String descripcion;
    private String autor;
    private String likes;

    public ObraCarruselItem(int imagen, String titulo, String descripcion, String autor, String likes) {
        this.imagen = imagen;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.autor = autor;
        this.likes = likes;
    }

    public int getImagen() { return imagen; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getAutor() { return autor; }
    public String getLikes() { return likes; }
}
