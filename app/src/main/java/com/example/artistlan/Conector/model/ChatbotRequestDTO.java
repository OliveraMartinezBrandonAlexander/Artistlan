package com.example.artistlan.Conector.model;

public class ChatbotRequestDTO {

    private String message;
    private String sessionId;
    private Integer idUsuario;

    public ChatbotRequestDTO() {
    }

    public ChatbotRequestDTO(String message, String sessionId, Integer idUsuario) {
        this.message = message;
        this.sessionId = sessionId;
        this.idUsuario = idUsuario;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}
