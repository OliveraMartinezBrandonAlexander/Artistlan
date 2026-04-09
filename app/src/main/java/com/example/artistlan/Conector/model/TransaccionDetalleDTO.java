package com.example.artistlan.Conector.model;

import com.google.gson.annotations.SerializedName;

public class TransaccionDetalleDTO {

    private Integer idTransaccion;

    @SerializedName(value = "idObra", alternate = {"obraId"})
    private Integer idObra;

    @SerializedName(value = "tipoOrigen", alternate = {"tipo", "tipoTransaccion", "origen"})
    private String tipoOrigen;

    @SerializedName(value = "estado", alternate = {"estadoTransaccion", "estatus"})
    private String estado;

    @SerializedName(value = "monto", alternate = {"precio", "total", "importe"})
    private Double monto;

    @SerializedName(value = "fechaCreacion", alternate = {
            "createdAt",
            "created_at",
            "fechaTransaccion",
            "fechaCompra",
            "fechaVenta"
    })
    private String fechaCreacion;

    @SerializedName(value = "fechaCaptura", alternate = {
            "capturedAt",
            "captured_at",
            "fecha_captura",
            "fechaPago",
            "fechaCapturaPago",
            "capturaFecha"
    })
    private String fechaCaptura;

    @SerializedName(value = "paypalOrderId", alternate = {"idOrdenPaypal", "ordenPaypalId", "orderIdPaypal"})
    private String paypalOrderId;

    @SerializedName(value = "paypalCaptureId", alternate = {"idCapturaPaypal", "capturaPaypalId", "captureIdPaypal"})
    private String paypalCaptureId;

    @SerializedName(value = "tituloObra", alternate = {"titulo", "nombreObra"})
    private String tituloObra;

    @SerializedName(value = "imagenObra", alternate = {"imagen1", "urlImagen"})
    private String imagenObra;

    @SerializedName(value = "nombreComprador", alternate = {"compradorNombre"})
    private String nombreComprador;

    @SerializedName(value = "usuarioComprador", alternate = {"compradorUsuario", "usernameComprador"})
    private String usuarioComprador;

    @SerializedName(value = "correoComprador", alternate = {"emailComprador"})
    private String correoComprador;

    @SerializedName(value = "telefonoComprador", alternate = {"celularComprador", "whatsappComprador"})
    private String telefonoComprador;

    @SerializedName(value = "tipoContactoComprador", alternate = {"contactoTipoComprador", "tipoCanalComprador"})
    private String tipoContactoComprador;

    @SerializedName(value = "contactoComprador", alternate = {"datoContactoComprador", "valorContactoComprador"})
    private String contactoComprador;

    @SerializedName(value = "fotoComprador", alternate = {"avatarComprador", "compradorFoto", "fotoPerfilComprador"})
    private String fotoComprador;

    @SerializedName(value = "nombreVendedor", alternate = {"vendedorNombre"})
    private String nombreVendedor;

    @SerializedName(value = "usuarioVendedor", alternate = {"vendedorUsuario", "usernameVendedor"})
    private String usuarioVendedor;

    @SerializedName(value = "correoVendedor", alternate = {"emailVendedor"})
    private String correoVendedor;

    @SerializedName(value = "telefonoVendedor", alternate = {"celularVendedor", "whatsappVendedor"})
    private String telefonoVendedor;

    @SerializedName(value = "tipoContactoVendedor", alternate = {"contactoTipoVendedor", "tipoCanalVendedor"})
    private String tipoContactoVendedor;

    @SerializedName(value = "contactoVendedor", alternate = {"datoContactoVendedor", "valorContactoVendedor"})
    private String contactoVendedor;

    @SerializedName(value = "fotoVendedor", alternate = {"avatarVendedor", "vendedorFoto", "fotoPerfilVendedor"})
    private String fotoVendedor;

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

    public String getTipoOrigen() {
        return tipoOrigen;
    }

    public String getEstado() {
        if (estado != null && !estado.trim().isEmpty()) {
            return estado.trim();
        }
        return obra != null ? obra.getEstado() : null;
    }

    public Double getMonto() {
        if (monto != null) {
            return monto;
        }
        return obra != null ? obra.getPrecio() : null;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public String getFechaCaptura() {
        return fechaCaptura;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public String getPaypalCaptureId() {
        return paypalCaptureId;
    }

    public String getTituloObra() {
        if (tituloObra != null && !tituloObra.trim().isEmpty()) {
            return tituloObra.trim();
        }
        return obra != null ? obra.getTitulo() : null;
    }

    public String getImagenObra() {
        if (imagenObra != null && !imagenObra.trim().isEmpty()) {
            return imagenObra.trim();
        }
        return obra != null ? obra.getImagen1() : null;
    }

    public String getNombreComprador() {
        if (nombreComprador != null && !nombreComprador.trim().isEmpty()) {
            return nombreComprador.trim();
        }
        return comprador != null ? comprador.getNombreCompleto() : null;
    }

    public String getUsuarioComprador() {
        if (usuarioComprador != null && !usuarioComprador.trim().isEmpty()) {
            return usuarioComprador.trim();
        }
        return comprador != null ? comprador.getUsuario() : null;
    }

    public String getCorreoComprador() {
        if (correoComprador != null && !correoComprador.trim().isEmpty()) {
            return correoComprador.trim();
        }
        return comprador != null ? comprador.getCorreo() : null;
    }

    public String getTelefonoComprador() {
        if (telefonoComprador != null && !telefonoComprador.trim().isEmpty()) {
            return telefonoComprador.trim();
        }
        return comprador != null ? comprador.getTelefono() : null;
    }

    public String getTipoContactoComprador() {
        if (tipoContactoComprador != null && !tipoContactoComprador.trim().isEmpty()) {
            return tipoContactoComprador.trim();
        }
        return null;
    }

    public String getContactoComprador() {
        if (contactoComprador != null && !contactoComprador.trim().isEmpty()) {
            return contactoComprador.trim();
        }
        return null;
    }

    public String getFotoComprador() {
        if (fotoComprador != null && !fotoComprador.trim().isEmpty()) {
            return fotoComprador.trim();
        }
        return comprador != null ? comprador.getFotoPerfil() : null;
    }

    public String getNombreVendedor() {
        if (nombreVendedor != null && !nombreVendedor.trim().isEmpty()) {
            return nombreVendedor.trim();
        }
        if (vendedor != null && vendedor.getNombreCompleto() != null && !vendedor.getNombreCompleto().trim().isEmpty()) {
            return vendedor.getNombreCompleto().trim();
        }
        return obra != null ? obra.getNombreAutor() : null;
    }

    public String getUsuarioVendedor() {
        if (usuarioVendedor != null && !usuarioVendedor.trim().isEmpty()) {
            return usuarioVendedor.trim();
        }
        return vendedor != null ? vendedor.getUsuario() : null;
    }

    public String getCorreoVendedor() {
        if (correoVendedor != null && !correoVendedor.trim().isEmpty()) {
            return correoVendedor.trim();
        }
        return vendedor != null ? vendedor.getCorreo() : null;
    }

    public String getTelefonoVendedor() {
        if (telefonoVendedor != null && !telefonoVendedor.trim().isEmpty()) {
            return telefonoVendedor.trim();
        }
        return vendedor != null ? vendedor.getTelefono() : null;
    }

    public String getTipoContactoVendedor() {
        if (tipoContactoVendedor != null && !tipoContactoVendedor.trim().isEmpty()) {
            return tipoContactoVendedor.trim();
        }
        return null;
    }

    public String getContactoVendedor() {
        if (contactoVendedor != null && !contactoVendedor.trim().isEmpty()) {
            return contactoVendedor.trim();
        }
        return null;
    }

    public String getFotoVendedor() {
        if (fotoVendedor != null && !fotoVendedor.trim().isEmpty()) {
            return fotoVendedor.trim();
        }
        return vendedor != null ? vendedor.getFotoPerfil() : null;
    }
}
