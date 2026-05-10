package com.example.artistlan.Conector.model;

public class AuthErrorResponseDTO {
    private String message;
    private String estadoCuenta;
    private String fechaFinSuspension;

    public String getMessage() {
        return message;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public String getFechaFinSuspension() {
        return fechaFinSuspension;
    }
}
