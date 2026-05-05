package com.example.artistlan.Conector.model;

public class TomarReporteRequestDTO {

    private Integer idModerador;
    private String prioridad;

    public Integer getIdModerador() {
        return idModerador;
    }

    public void setIdModerador(Integer idModerador) {
        this.idModerador = idModerador;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}
