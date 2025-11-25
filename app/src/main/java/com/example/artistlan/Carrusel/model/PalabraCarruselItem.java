package com.example.artistlan.Carrusel.model;

public class PalabraCarruselItem {
    private String palabra;
    private boolean seleccionado;
    private String idCategoria;
    private int colorNormal;
    private int colorSeleccionado;

    public PalabraCarruselItem(String palabra, String idCategoria, int colorNormal, int colorSeleccionado) {
        this.palabra = palabra;
        this.idCategoria = idCategoria;
        this.seleccionado = false;
        this.colorNormal = colorNormal;
        this.colorSeleccionado = colorSeleccionado;
    }

    public String getPalabra() { return palabra; }
    public boolean isSeleccionado() { return seleccionado; }
    public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }
    public String getIdCategoria() { return idCategoria; }
    public int getColorNormal() { return colorNormal; }
    public int getColorSeleccionado() { return colorSeleccionado; }
}