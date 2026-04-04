package com.example.artistlan.Conector.model;

public class CapturarOrdenPaypalResponseDTO {

    private Boolean success;
    private Boolean completado;
    private String status;
    private String message;
    private String mensaje;
    private String paypalOrderId;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getCompletado() {
        return completado;
    }

    public void setCompletado(Boolean completado) {
        this.completado = completado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public String resolveUserMessage() {
        if (message != null && !message.trim().isEmpty()) return message.trim();
        if (mensaje != null && !mensaje.trim().isEmpty()) return mensaje.trim();
        if (status != null && !status.trim().isEmpty()) return status.trim();
        return null;
    }
}
