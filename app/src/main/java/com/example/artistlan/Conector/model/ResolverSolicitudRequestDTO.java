package com.example.artistlan.Conector.model;

public class ResolverSolicitudRequestDTO {

    private Integer idVendedor;
    private String motivo;

    public ResolverSolicitudRequestDTO(Integer idVendedor) {
        this(idVendedor, null);
    }

    public ResolverSolicitudRequestDTO(Integer idVendedor, String motivo) {
        this.idVendedor = idVendedor;
        this.motivo = motivo;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public String getMotivo() {
        return motivo;
    }
}
