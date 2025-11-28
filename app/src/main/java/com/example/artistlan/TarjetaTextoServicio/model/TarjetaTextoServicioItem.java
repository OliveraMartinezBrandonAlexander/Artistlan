package com.example.artistlan.TarjetaTextoServicio.model;

public class TarjetaTextoServicioItem {

    private String titulo;
    private String descripcion;
    private String contacto;
    private String tecnicas;
    private String autor;
    private String categoria;
    private boolean expandido = false;

    public TarjetaTextoServicioItem(String titulo, String descripcion, String contacto, String tecnicas, String autor, String categoria) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contacto = contacto;
        this.tecnicas = tecnicas;
        this.autor = autor;
        this.categoria = categoria;
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
    public String getCategoria() { return categoria; }

    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
}