package com.example.artistlan.Fragments;

public final class ObrasUiRefreshCoordinator {

    private static boolean refreshExplorarObrasPending = false;
    private static boolean refreshHomePending = false;

    private ObrasUiRefreshCoordinator() {
        // Utility class
    }

    public static synchronized void notificarReservaLiberadaDesdeCarrito() {
        refreshExplorarObrasPending = true;
        refreshHomePending = true;
    }

    public static synchronized boolean isRefreshExplorarObrasPending() {
        return refreshExplorarObrasPending;
    }

    public static synchronized void clearRefreshExplorarObrasPending() {
        refreshExplorarObrasPending = false;
    }

    public static synchronized boolean isRefreshHomePending() {
        return refreshHomePending;
    }

    public static synchronized void clearRefreshHomePending() {
        refreshHomePending = false;
    }
}
