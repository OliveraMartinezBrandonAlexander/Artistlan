package com.example.artistlan.Conector.model;

import java.util.ArrayList;
import java.util.List;

public class PageResponseArtistaDTO {
    private List<ArtistaResumenDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public List<ArtistaResumenDTO> getContent() {
        return content != null ? content : new ArrayList<>();
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isLast() {
        return last;
    }
}
