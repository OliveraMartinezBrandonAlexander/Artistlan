package com.example.artistlan.Conector.model;

public class PasswordResetConfirmRequestDTO {
    private String temporaryToken;
    private String code;
    private String nuevaContrasena;
    private String confirmarContrasena;

    public PasswordResetConfirmRequestDTO() {
    }

    public PasswordResetConfirmRequestDTO(
            String temporaryToken,
            String code,
            String nuevaContrasena,
            String confirmarContrasena
    ) {
        this.temporaryToken = temporaryToken;
        this.code = code;
        this.nuevaContrasena = nuevaContrasena;
        this.confirmarContrasena = confirmarContrasena;
    }

    public String getTemporaryToken() {
        return temporaryToken;
    }

    public void setTemporaryToken(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }
}
