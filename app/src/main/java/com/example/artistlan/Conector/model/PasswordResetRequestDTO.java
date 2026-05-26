package com.example.artistlan.Conector.model;

public class PasswordResetRequestDTO {
    private String usuarioOCorreo;

    public PasswordResetRequestDTO() {
    }

    public PasswordResetRequestDTO(String usuarioOCorreo) {
        this.usuarioOCorreo = usuarioOCorreo;
    }

    public String getUsuarioOCorreo() {
        return usuarioOCorreo;
    }

    public void setUsuarioOCorreo(String usuarioOCorreo) {
        this.usuarioOCorreo = usuarioOCorreo;
    }
}
