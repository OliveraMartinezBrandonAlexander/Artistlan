package com.example.artistlan.Conector.model;

public class SolicitudCompraCrearRequestDTO {

    private Integer idObra;
    private Integer obraId;

    private Integer idComprador;
    private Integer compradorId;

    private Integer idUsuario;
    private Integer usuarioId;
    private String mensaje;
    private String comentario;

    public SolicitudCompraCrearRequestDTO(Integer compradorId, Integer obraId) {
        this(compradorId, obraId, null);
    }

    public SolicitudCompraCrearRequestDTO(Integer compradorId, Integer obraId, String mensaje) {
        this.idObra = obraId;
        this.obraId = obraId;
        this.idComprador = compradorId;
        this.compradorId = compradorId;
        this.idUsuario = compradorId;
        this.usuarioId = compradorId;
        this.mensaje = mensaje;
        this.comentario = mensaje;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public Integer getIdComprador() {
        return idComprador;
    }

    public String getMensaje() {
        return mensaje;
    }
}
