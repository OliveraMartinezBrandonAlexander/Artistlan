package com.example.artistlan.Conector.model;

public class ValidarPasswordResponseDTO {
    private boolean valida;
    private String message;

    public boolean isValida() {
        return valida;
    }

    public void setValida(boolean valida) {
        this.valida = valida;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

