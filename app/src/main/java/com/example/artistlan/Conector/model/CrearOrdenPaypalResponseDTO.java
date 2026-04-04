package com.example.artistlan.Conector.model;

public class CrearOrdenPaypalResponseDTO {

    private String paypalOrderId;
    private String approveLink;

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public String getApproveLink() {
        return approveLink;
    }

    public void setApproveLink(String approveLink) {
        this.approveLink = approveLink;
    }
}
