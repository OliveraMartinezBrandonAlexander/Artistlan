package com.example.artistlan.Conector.model;

public class ServicioDTO {

    private Integer idServicio;
    private String titulo;
    private String descripcion;
    private String contacto;
    private String tecnicas;
    private Integer idUsuario;
    private String nombreUsuario;
    private String categoria;
    private Integer idCategoria;
    private String fotoPerfilAutor;
    private Integer likes;
    private Boolean esFavorito;


    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTecnicas() {
        return tecnicas;
    }

    public void setTecnicas(String tecnicas) {
        this.tecnicas = tecnicas;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public String getFotoPerfilAutor() {
        return fotoPerfilAutor;
    }

    public void setFotoPerfilAutor(String fotoPerfilAutor) {
        this.fotoPerfilAutor = fotoPerfilAutor;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;

    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Boolean getEsFavorito() {
        return esFavorito;
    }

    public void setEsFavorito(Boolean esFavorito) {
        this.esFavorito = esFavorito;
    }
}
