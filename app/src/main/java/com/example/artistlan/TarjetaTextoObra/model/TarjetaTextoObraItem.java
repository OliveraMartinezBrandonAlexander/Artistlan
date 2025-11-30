package com.example.artistlan.TarjetaTextoObra.model;

public class TarjetaTextoObraItem {

    private int idObra;
    private String titulo;
    private String descripcion;
    private String estado;
    private Double precio;
    private String imagen1;
    private String imagen2;
    private String imagen3;
    private String tecnicas;
    private String medidas;
    private int likes;
    private String nombreAutor;
    private String nombreCategoria;
    private boolean liked = false;
    private boolean expandido = false;


    public TarjetaTextoObraItem(int idObra, String titulo, String descripcion, String estado, Double precio, String imagen1, String imagen2, String imagen3, String tecnicas, String medidas, int likes, String nombreAutor, String nombreCategoria, boolean liked, boolean expandido) {
        this.idObra = idObra;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.precio = precio;
        this.imagen1 = imagen1;
        this.imagen2 = imagen2;
        this.imagen3 = imagen3;
        this.tecnicas = tecnicas;
        this.medidas = medidas;
        this.likes = likes;
        this.nombreAutor = nombreAutor;
        this.nombreCategoria = nombreCategoria;
        this.liked = liked;
        this.expandido = expandido;

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

    public int getLikes() {
        return likes;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public String getNombreAutor() {
        return nombreAutor;
    }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }

    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getIdObra() {
        return idObra;
    }

    public void setIdObra(int idObra) {
        this.idObra = idObra;
    }
}
