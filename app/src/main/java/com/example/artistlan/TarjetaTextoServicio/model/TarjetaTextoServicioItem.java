package com.example.artistlan.TarjetaTextoServicio.model;

public class TarjetaTextoServicioItem {

    private String titulo;
    private String descripcion;
    private String contacto;
    private String tecnicas;
    private String autor;

    public TarjetaTextoServicioItem(String titulo, String descripcion, String contacto, String tecnicas, String autor) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contacto = contacto;
        this.tecnicas = tecnicas;
        this.autor = autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getContacto() {
        return contacto;
    }

    public String getTecnicas() {
        return tecnicas;
    }

    public String getAutor() {
        return autor;
    }
}