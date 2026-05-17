package com.example.artistlan.Carrusel.model;

public class ObraCarruselItem {

    private int imagen;
    private String imagenUrl;
    private String titulo;
    private String descripcion;
    private String autor;
    private String likes;
    private String autorFotoUrl;
    private Integer idObra;
    private Integer idAutor;
    private int likesCount;
    private boolean userLiked;
    private String estado;
    private String tecnicas;
    private String medidas;
    private Double precio;
    private String tipoArte;

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
    public Integer getIdObra() { return idObra; }
    public void setIdObra(Integer idObra) { this.idObra = idObra; }
    public Integer getIdAutor() { return idAutor; }
    public void setIdAutor(Integer idAutor) { this.idAutor = idAutor; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public boolean isUserLiked() { return userLiked; }
    public void setUserLiked(boolean userLiked) { this.userLiked = userLiked; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getTecnicas() { return tecnicas; }
    public void setTecnicas(String tecnicas) { this.tecnicas = tecnicas; }
    public String getMedidas() { return medidas; }
    public void setMedidas(String medidas) { this.medidas = medidas; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getTipoArte() { return tipoArte; }
    public void setTipoArte(String tipoArte) { this.tipoArte = tipoArte; }
}
