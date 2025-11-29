package com.example.artistlan.TarjetaTextoArtista.model;

import java.util.List;

public class TarjetaTextoArtistaItem {

    private String nombre;
    private String categoria;
    private String descripcion;
    private String mensaje;
    private List<Integer> miniObras;
    private boolean expandido = false;

    public TarjetaTextoArtistaItem(String nombre, String categoria, String descripcion, String mensaje, List<Integer> miniObras) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.mensaje = mensaje;
        this.miniObras = miniObras;
    }

    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getMensaje() { return mensaje; }
    public List<Integer> getMiniObras() { return miniObras; }

    public boolean isExpandido() { return expandido; }
    public void setExpandido(boolean expandido) { this.expandido = expandido; }
}
