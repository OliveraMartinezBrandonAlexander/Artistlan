package com.example.artistlan.Conector.model;

public class ServicioDTO
{
    private Integer idServicio;
    private String titulo;
    private String descripcion;
    private String contacto;
    private String tecnicas;
    private Integer idUsuario;

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
}
