package com.example.artistlan.HistoriaArte.model;

public class HistoriaArteItem {

    private final String categoria;
    private final String titulo;
    private final String resumen;
    private final String contenido;
    private final String periodo;
    private final String datoClave;
    private final String palabrasClave;
    private final int iconResId;
    private boolean expandido;

    public HistoriaArteItem(String categoria, String titulo, String resumen, String contenido) {
        this(categoria, titulo, resumen, contenido, categoria, "", "", 0);
    }

    public HistoriaArteItem(
            String categoria,
            String titulo,
            String resumen,
            String contenido,
            String periodo,
            String datoClave,
            String palabrasClave,
            int iconResId
    ) {
        this.categoria = categoria;
        this.titulo = titulo;
        this.resumen = resumen;
        this.contenido = contenido;
        this.periodo = periodo;
        this.datoClave = datoClave;
        this.palabrasClave = palabrasClave;
        this.iconResId = iconResId;
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

    public String getPeriodo() {
        return periodo;
    }

    public String getDatoClave() {
        return datoClave;
    }

    public String getPalabrasClave() {
        return palabrasClave;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isExpandido() {
        return expandido;
    }

    public void setExpandido(boolean expandido) {
        this.expandido = expandido;
    }
}
