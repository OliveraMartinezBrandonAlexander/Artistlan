package com.example.artistlan.Conector.model;

public class AdminPuntoSerieDTO {

    private String fecha;
    private String etiqueta;
    private long valor;
    private Double monto;

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public long getValor() {
        return valor;
    }

    public void setValor(long valor) {
        this.valor = valor;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public double getMontoSeguro() {
        return monto != null ? monto : 0d;
    }
}
