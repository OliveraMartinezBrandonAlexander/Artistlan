package com.example.artistlan.Conector.model;

public class TwoFactorVerifyLoginResponse {
    private Boolean success;
    private String token;
    private UsuariosDTO user;
    private String message;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuariosDTO getUser() {
        return user;
    }

    public void setUser(UsuariosDTO user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
