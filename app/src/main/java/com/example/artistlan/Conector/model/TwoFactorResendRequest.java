package com.example.artistlan.Conector.model;

public class TwoFactorResendRequest {
    private String temporaryToken;

    public TwoFactorResendRequest() {
    }

    public TwoFactorResendRequest(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }

    public String getTemporaryToken() {
        return temporaryToken;
    }

    public void setTemporaryToken(String temporaryToken) {
        this.temporaryToken = temporaryToken;
    }
}
