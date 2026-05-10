package com.example.artistlan.Conector.model;

public class LoginRequestDTO {
    private String usuarioOCorreo;
    private String contrasena;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String usuarioOCorreo, String contrasena) {
        this.usuarioOCorreo = usuarioOCorreo;
        this.contrasena = contrasena;
    }

    public String getUsuarioOCorreo() {
        return usuarioOCorreo;
    }

    public void setUsuarioOCorreo(String usuarioOCorreo) {
        this.usuarioOCorreo = usuarioOCorreo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
