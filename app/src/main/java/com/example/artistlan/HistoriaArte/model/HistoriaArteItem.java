package com.example.artistlan.HistoriaArte.model;

public class HistoriaArteItem {

    private final String categoria;
    private final String titulo;
    private final String resumen;
    private final String contenido;
    private boolean expandido;

    public HistoriaArteItem(String categoria, String titulo, String resumen, String contenido) {
        this.categoria = categoria;
        this.titulo = titulo;
        this.resumen = resumen;
        this.contenido = contenido;
        this.expandido = false;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public String getContenido() {
        return contenido;
    }

    public boolean isExpandido() {
        return expandido;
    }

    public void setExpandido(boolean expandido) {
        this.expandido = expandido;
    }
}