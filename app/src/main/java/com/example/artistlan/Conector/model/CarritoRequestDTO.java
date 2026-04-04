package com.example.artistlan.Conector.model;

public class CarritoRequestDTO {

    private Integer idUsuario;
    private Integer idObra;

    public CarritoRequestDTO() {
    }

    public CarritoRequestDTO(Integer idUsuario, Integer idObra) {
        this.idUsuario = idUsuario;
        this.idObra = idObra;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }
}
