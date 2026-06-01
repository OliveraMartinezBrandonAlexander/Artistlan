package com.example.artistlan.Conector.model;

public class AdminRankingItemDTO {

    private Integer id;
    private String nombre;
    private long total;
    private String descripcionSecundaria;
    private String imagen;
    private String imagenAutor;
    private String autor;
    private String subtitulo;
    private String contacto;
    private String tipoContacto;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getDescripcionSecundaria() {
        return descripcionSecundaria;
    }

    public void setDescripcionSecundaria(String descripcionSecundaria) {
        this.descripcionSecundaria = descripcionSecundaria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getImagenAutor() {
        return imagenAutor;
    }

    public void setImagenAutor(String imagenAutor) {
        this.imagenAutor = imagenAutor;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTipoContacto() {
        return tipoContacto;
    }

    public void setTipoContacto(String tipoContacto) {
        this.tipoContacto = tipoContacto;
    }
}
