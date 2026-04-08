package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class ConvocatoriaDTO {
    @SerializedName(value = "idConvocatoria", alternate = {"id", "id_convocatoria"})
    private Integer idConvocatoria;
    @SerializedName(value = "titulo", alternate = {"tituloConvocatoria", "nombre"})
    private String titulo;
    @SerializedName(value = "descripcion", alternate = {"detalle", "descripcionConvocatoria"})
    private String descripcion;
    @SerializedName(value = "fecha", alternate = {"fechaCierre", "fechaConvocatoria"})
    private String fecha;
    @SerializedName(value = "enlace", alternate = {"url", "link"})
    private String enlace;

    public Integer getIdConvocatoria() {
        return idConvocatoria;
    }

    public void setIdConvocatoria(Integer idConvocatoria) {
        this.idConvocatoria = idConvocatoria;
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

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }
}
