package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class CarritoDTO {

    private Integer idCarrito;
    private Integer idUsuario;
    @SerializedName(value = "idObra", alternate = {"obraId"})
    private Integer idObra;
    private String fechaAgregado;

    @SerializedName(value = "obra", alternate = {"obraDTO", "detalleObra"})
    private ObraDTO obra;

    @SerializedName(value = "titulo", alternate = {"nombreObra"})
    private String titulo;
    private String descripcion;
    private String estado;
    private Double precio;
    private String imagen1;
    private String imagen2;
    private String imagen3;
    private String tecnicas;
    private String medidas;
    private Integer likes;
    private String nombreAutor;
    private String nombreCategoria;
    private String fotoPerfilAutor;

    public Integer getIdCarrito() {
        return idCarrito;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public Integer getIdObra() {
        if (idObra != null) {
            return idObra;
        }
        return obra != null ? obra.getIdObra() : null;
    }

    public String getFechaAgregado() {
        return fechaAgregado;
    }

    public ObraDTO getObra() {
        return obra;
    }

    public String getTitulo() {
        if (titulo != null) {
            return titulo;
        }
        return obra != null ? obra.getTitulo() : null;
    }

    public String getDescripcion() {
        if (descripcion != null) {
            return descripcion;
        }
        return obra != null ? obra.getDescripcion() : null;
    }

    public String getEstado() {
        if (estado != null) {
            return estado;
        }
        return obra != null ? obra.getEstado() : null;
    }

    public Double getPrecio() {
        if (precio != null) {
            return precio;
        }
        return obra != null ? obra.getPrecio() : null;
    }

    public String getImagen1() {
        if (imagen1 != null) {
            return imagen1;
        }
        return obra != null ? obra.getImagen1() : null;
    }

    public String getImagen2() {
        if (imagen2 != null) {
            return imagen2;
        }
        return obra != null ? obra.getImagen2() : null;
    }

    public String getImagen3() {
        if (imagen3 != null) {
            return imagen3;
        }
        return obra != null ? obra.getImagen3() : null;
    }

    public String getTecnicas() {
        if (tecnicas != null) {
            return tecnicas;
        }
        return obra != null ? obra.getTecnicas() : null;
    }

    public String getMedidas() {
        if (medidas != null) {
            return medidas;
        }
        return obra != null ? obra.getMedidas() : null;
    }

    public Integer getLikes() {
        if (likes != null) {
            return likes;
        }
        return obra != null ? obra.getLikes() : null;
    }

    public String getNombreAutor() {
        if (nombreAutor != null) {
            return nombreAutor;
        }
        return obra != null ? obra.getNombreAutor() : null;
    }

    public String getNombreCategoria() {
        if (nombreCategoria != null) {
            return nombreCategoria;
        }
        return obra != null ? obra.getNombreCategoria() : null;
    }

    public String getFotoPerfilAutor() {
        if (fotoPerfilAutor != null) {
            return fotoPerfilAutor;
        }
        return obra != null ? obra.getFotoPerfilAutor() : null;
    }
}
