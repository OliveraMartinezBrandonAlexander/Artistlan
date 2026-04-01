package com.example.artistlan.Fragments;

import java.util.List;

public interface FilterableExplorarFragment {

    List<String> getFilterOptions();

    String getActiveFilter();

    void applyFilter(String filter);

    void clearFilter();
}
