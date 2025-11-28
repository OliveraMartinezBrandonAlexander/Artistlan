package com.example.artistlan.TarjetaTextoObra.model;

public class TarjetaTextoObraItem {

    private String titulo;
    private String descripcion;
    private String estado;
    private Double precio;
    private String imagen1;
    private String imagen2;
    private String imagen3;
    private String tecnicas;
    private String medidas;
    private String categoria;
    private int likes;
    private int autor;
    private boolean liked = false;
    private boolean expandido = false;



    public TarjetaTextoObraItem(String titulo, String descripcion, String estado, Double precio, String imagen1, String imagen2, String imagen3, String tecnicas, String medidas, String categoria, int likes, int autor) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.precio = precio;
        this.imagen1 = imagen1;
        this.imagen2 = imagen2;
        this.imagen3 = imagen3;
        this.tecnicas = tecnicas;
        this.medidas = medidas;
        this.categoria= categoria;
        this.likes = likes;
        this.autor = autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public String getEstado() {
        return estado;
    }

    public String getImagen1() {
        return imagen1;
    }

    public String getImagen2() {
        return imagen2;
    }

    public String getImagen3() {
        return imagen3;
    }

    public String getTecnicas() {
        return tecnicas;
    }

    public String getMedidas() {
        return medidas;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getLikes() {
        return likes;
    }

    public int getAutor() {
        return autor;
    }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }

    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
