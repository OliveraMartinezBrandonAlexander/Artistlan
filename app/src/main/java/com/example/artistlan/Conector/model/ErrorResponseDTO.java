package com.example.artistlan.Conector.model;

public class ErrorResponseDTO {
    private String message;
    private String error;

    public String getMessage() {
        if (message != null && !message.trim().isEmpty()) {
            return message.trim();
        }
        if (error != null && !error.trim().isEmpty()) {
            return error.trim();
        }
        return null;
    }

    public String getError() {
        return error;
    }
}

