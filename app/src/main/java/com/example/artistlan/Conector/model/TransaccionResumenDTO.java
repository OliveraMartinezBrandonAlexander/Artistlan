package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class TransaccionResumenDTO {

    private Integer idTransaccion;

    @SerializedName(value = "idObra", alternate = {"obraId"})
    private Integer idObra;

    @SerializedName(value = "fechaTransaccion", alternate = {
            "fechaCompra",
            "fechaVenta",
            "fecha",
            "fechaOperacion",
            "createdAt"
    })
    private String fechaTransaccion;

    @SerializedName(value = "precio", alternate = {"monto", "total"})
    private Double precio;

    @SerializedName(value = "estado", alternate = {"estadoTransaccion", "estatus"})
    private String estado;

    @SerializedName(value = "tituloObra", alternate = {"titulo", "nombreObra"})
    private String tituloObra;

    @SerializedName(value = "imagenObra", alternate = {"imagen1", "miniatura", "urlImagen"})
    private String imagenObra;

    @SerializedName(value = "nombreArtista", alternate = {"nombreAutor", "artista", "autor"})
    private String nombreArtista;

    @SerializedName(value = "nombreComprador", alternate = {"nombreCliente"})
    private String nombreComprador;

    @SerializedName(value = "nombreVendedor", alternate = {"nombrePropietario"})
    private String nombreVendedor;

    @SerializedName(value = "obra", alternate = {"obraDTO", "detalleObra"})
    private ObraDTO obra;

    private UsuariosDTO comprador;
    private UsuariosDTO vendedor;

    public Integer getIdTransaccion() {
        return idTransaccion;
    }

    public Integer getIdObra() {
        if (idObra != null) {
            return idObra;
        }
        return obra != null ? obra.getIdObra() : null;
    }

    public String getFechaTransaccion() {
        return fechaTransaccion;
    }

    public Double getPrecio() {
        if (precio != null) {
            return precio;
        }
        return obra != null ? obra.getPrecio() : null;
    }

    public String getEstado() {
        if (estado != null && !estado.trim().isEmpty()) {
            return estado;
        }
        return obra != null ? obra.getEstado() : null;
    }

    public String getTituloObra() {
        if (tituloObra != null && !tituloObra.trim().isEmpty()) {
            return tituloObra;
        }
        return obra != null ? obra.getTitulo() : null;
    }

    public String getImagenObra() {
        if (imagenObra != null && !imagenObra.trim().isEmpty()) {
            return imagenObra;
        }
        return obra != null ? obra.getImagen1() : null;
    }

    public String getNombreArtista() {
        if (nombreArtista != null && !nombreArtista.trim().isEmpty()) {
            return nombreArtista;
        }
        if (obra != null && obra.getNombreAutor() != null && !obra.getNombreAutor().trim().isEmpty()) {
            return obra.getNombreAutor();
        }
        return vendedor != null ? vendedor.getNombreCompleto() : null;
    }

    public String getNombreComprador() {
        if (nombreComprador != null && !nombreComprador.trim().isEmpty()) {
            return nombreComprador;
        }
        return comprador != null ? comprador.getNombreCompleto() : null;
    }

    public String getNombreVendedor() {
        if (nombreVendedor != null && !nombreVendedor.trim().isEmpty()) {
            return nombreVendedor;
        }
        if (vendedor != null && vendedor.getNombreCompleto() != null && !vendedor.getNombreCompleto().trim().isEmpty()) {
            return vendedor.getNombreCompleto();
        }
        return getNombreArtista();
    }
}
