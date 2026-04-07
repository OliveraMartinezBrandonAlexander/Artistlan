package com.example.artistlan.TarjetaTextoServicio.model;

public class TarjetaTextoServicioItem {

    private Integer idServicio;
    private Integer idAutor;
    private String titulo;
    private String descripcion;
    private String contacto;
    private String tipoContacto;
    private String tecnicas;
    private String autor;
    private String fotoPerfilAutor;
    private String categoria;
    private Double precioMin;
    private Double precioMax;
    private int likes;
    private boolean favorito;
    private boolean expandido = false;

    public TarjetaTextoServicioItem(Integer idServicio, String titulo, String descripcion, String contacto,
                                    String tecnicas, String autor, String categoria, String fotoPerfilAutor,
                                    int likes, boolean favorito, boolean expandido) {
        this(idServicio, null, titulo, descripcion, contacto, null, tecnicas, autor, categoria, fotoPerfilAutor, null, null, likes, favorito, expandido);
    }

    public TarjetaTextoServicioItem(Integer idServicio, Integer idAutor, String titulo, String descripcion,
                                    String contacto, String tipoContacto, String tecnicas, String autor,
                                    String categoria, String fotoPerfilAutor, Double precioMin, Double precioMax,
                                    int likes, boolean favorito, boolean expandido) {
        this.idServicio = idServicio;
        this.idAutor = idAutor;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.contacto = contacto;
        this.tipoContacto = tipoContacto;
        this.tecnicas = tecnicas;
        this.autor = autor;
        this.categoria = categoria;
        this.fotoPerfilAutor = fotoPerfilAutor;
        this.precioMin = precioMin;
        this.precioMax = precioMax;
        this.likes = likes;
        this.favorito = favorito;
        this.expandido = expandido;
    }

    public Integer getIdServicio() { return idServicio; }
    public Integer getIdAutor() { return idAutor; }
    public Integer getIdUsuario() { return idAutor; }
    public void setIdUsuario(Integer idUsuario) { this.idAutor = idUsuario; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getContacto() { return contacto; }
    public String getTipoContacto() { return tipoContacto; }
    public String getTecnicas() { return tecnicas; }
    public String getAutor() { return autor; }
    public String getCategoria() { return categoria; }
    public Double getPrecioMin() { return precioMin; }
    public Double getPrecioMax() { return precioMax; }
    public String getFotoPerfilAutor() { return fotoPerfilAutor; }
    public void setFotoPerfilAutor(String fotoPerfilAutor) { this.fotoPerfilAutor = fotoPerfilAutor; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public boolean isFavorito() { return favorito; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
}
