package com.example.artistlan.Conector.model;

public class TwoFactorVerifyLoginRequest {
    private String temporaryToken;
    private String code;

    public TwoFactorVerifyLoginRequest() {
    }

    public TwoFactorVerifyLoginRequest(String temporaryToken, String code) {
        this.temporaryToken = temporaryToken;
        this.code = code;
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
}
