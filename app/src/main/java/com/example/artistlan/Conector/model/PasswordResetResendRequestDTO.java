package com.example.artistlan.Conector.model;

public class PasswordResetResendRequestDTO {
    private String temporaryToken;

    public PasswordResetResendRequestDTO() {
    }

    public PasswordResetResendRequestDTO(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }

    public String getTemporaryToken() {
        return temporaryToken;
    }

    public void setTemporaryToken(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }
}
