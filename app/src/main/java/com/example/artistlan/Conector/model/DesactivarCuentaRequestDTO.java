package com.example.artistlan.Conector.model;

public class DesactivarCuentaRequestDTO {

    private Integer idUsuarioSolicitante;
    private String contrasenaActual;
    private String motivo;
    private Boolean confirmacion;

    public Integer getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    public void setIdUsuarioSolicitante(Integer idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Boolean getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(Boolean confirmacion) {
        this.confirmacion = confirmacion;
    }
}
