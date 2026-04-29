package com.example.artistlan.Conector.model;

public class TwoFactorVerifyActivationRequest {
    private String code;

    public TwoFactorVerifyActivationRequest() {
    }

    public TwoFactorVerifyActivationRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
