package com.example.artistlan.Conector.model;

import java.util.ArrayList;
import java.util.List;

public class AdminRankingResponseDTO {

    private String tipo;
    private int limit;
    private List<AdminRankingItemDTO> items;
    private String mensaje;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<AdminRankingItemDTO> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(List<AdminRankingItemDTO> items) {
        this.items = items;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
