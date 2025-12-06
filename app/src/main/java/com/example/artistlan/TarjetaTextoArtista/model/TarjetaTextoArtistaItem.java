package com.example.artistlan.TarjetaTextoArtista.model;

import java.util.List;

public class TarjetaTextoArtistaItem {

    private String nombre;
    private String categoria;
    private String descripcion;

    private String fotoPerfil;
    private List<String> miniObras;
    private boolean expandido = false;

    public TarjetaTextoArtistaItem(String nombre, String categoria, String descripcion,
                                   String fotoPerfil, List<String> miniObras) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.fotoPerfil = fotoPerfil;
        this.miniObras = miniObras;
    }

    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getDescripcion() { return descripcion; }
    public String getFotoPerfil() { return fotoPerfil; }
    public List<String> getMiniObras() { return miniObras; }
    public boolean isExpandido() { return expandido; }

    public void setExpandido(boolean expandido) { this.expandido = expandido; }
}