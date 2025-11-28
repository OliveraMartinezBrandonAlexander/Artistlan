package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.R;
import com.example.artistlan.Carrusel.adapter.PalabraCarruselAdapter;
import com.example.artistlan.Carrusel.layout.CenterZoomLayoutManager;
import com.example.artistlan.Carrusel.model.PalabraCarruselItem;

import java.util.ArrayList;
import java.util.List;

public class FragArtistas extends Fragment implements PalabraCarruselAdapter.OnCategoriaClickListener {

    private RecyclerView recyclerViewArtistas;
    private RecyclerView recyclerViewCarrusel;
    private PalabraCarruselAdapter carruselAdapter;
    private ImageButton btnIzq, btnDer;
    private Button btnAplicarFiltro;
    private List<PalabraCarruselItem> profesionesArtistas;
    private String profesionFiltroActual = "";
    private CenterZoomLayoutManager layoutManager;

    // TODO: Crear un adapter específico para artistas cuando lo necesites
    // private ArtistaAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_artistas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        // Configurar el carrusel de profesiones
        configurarCarrusel(view);

        // Configurar el RecyclerView de artistas
        configurarArtistas(view);

        // Configurar botón de aplicar filtro
        configurarBotonFiltro(view);
    }

    private void configurarCarrusel(View view) {
        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselArtistas);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdoArtistas);
        btnDer = view.findViewById(R.id.btnCarruselDerechoArtistas);

        // Configurar LayoutManager personalizado
        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        // Crear lista de profesiones para el carrusel
        profesionesArtistas = obtenerProfesionesDeBD();

        carruselAdapter = new PalabraCarruselAdapter(profesionesArtistas, requireContext(), this);
        recyclerViewCarrusel.setAdapter(carruselAdapter);

        // Scroll listener para detectar el item central
        recyclerViewCarrusel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    encontrarItemCentral();
                }
            }
        });

        // Configurar botones de navegación
        btnDer.setOnClickListener(v -> {
            int currentPosition = carruselAdapter.getItemSeleccionado();
            if (currentPosition < carruselAdapter.getItemCount() - 2) {
                recyclerViewCarrusel.smoothScrollToPosition(currentPosition + 1);
                animarBoton(v);
            }
        });

        btnIzq.setOnClickListener(v -> {
            int currentPosition = carruselAdapter.getItemSeleccionado();
            if (currentPosition > 1) {
                recyclerViewCarrusel.smoothScrollToPosition(currentPosition - 1);
                animarBoton(v);
            }
        });

        // Establecer posición inicial (primer item real)
        recyclerViewCarrusel.post(() -> {
            recyclerViewCarrusel.smoothScrollToPosition(1);
            recyclerViewCarrusel.postDelayed(() -> {
                carruselAdapter.setItemSeleccionado(1);
            }, 100);
        });
    }

    private void encontrarItemCentral() {
        int centerX = recyclerViewCarrusel.getWidth() / 2;
        float centerY = recyclerViewCarrusel.getHeight() / 2.0f;

        View centerView = recyclerViewCarrusel.findChildViewUnder(centerX, centerY);

        if (centerView != null) {
            int position = recyclerViewCarrusel.getChildAdapterPosition(centerView);
            if (position != RecyclerView.NO_POSITION) {
                // Solo permitir selección de items reales (no vacíos)
                if (position > 0 && position < carruselAdapter.getItemCount() - 1) {
                    carruselAdapter.setItemSeleccionado(position);
                }
            }
        }
    }

    private void configurarBotonFiltro(View view) {
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltroArtistas);
        btnAplicarFiltro.setVisibility(View.GONE);

        btnAplicarFiltro.setOnClickListener(v -> {
            PalabraCarruselItem profesionSeleccionada = carruselAdapter.getCategoriaSeleccionada();
            if (profesionSeleccionada != null) {
                aplicarFiltro(profesionSeleccionada.getIdCategoria());
                animarBoton(v);
            }
        });
    }

    private List<PalabraCarruselItem> obtenerProfesionesDeBD() {
        // TODO: CONECTAR CON BASE DE DATOS - Reemplazar con tu lógica real
        List<PalabraCarruselItem> profesiones = new ArrayList<>();

        // Ejemplo de profesiones - reemplaza con datos reales de BD
        String[] profesionesArray = {
                "Pintor", "Escultor", "Fotógrafo", "Ilustrador", "Dibujante",
                "Grabador", "Ceramista", "Muralista", "Digital", "Acuarelista",
                "Retratista", "Paisajista", "Abstracto", "Conceptual",
                "Collagista", "Textil", "Mixta", "Profesional"
        };

        int colorNormal = 0xFF4B2056;
        int colorSeleccionado = 0xFF6A2D7A;

        for (String profesion : profesionesArray) {
            profesiones.add(new PalabraCarruselItem(profesion, "prof_" + profesion.toLowerCase(), colorNormal, colorSeleccionado));
        }

        return profesiones;
    }

    private void aplicarFiltro(String idProfesion) {
        profesionFiltroActual = idProfesion;

        PalabraCarruselItem profesion = carruselAdapter.getCategoriaSeleccionada();
        if (profesion != null) {
            System.out.println("Aplicando filtro para profesión: " + profesion.getPalabra());

            // TODO: CONECTAR CON BASE DE DATOS - Implementar filtrado real
            // Ejemplo:
            // List<Artista> artistasFiltrados = tuRepositorio.obtenerArtistasPorProfesion(idProfesion);
            // adapter.actualizarLista(artistasFiltrados);
        }
    }

    private void configurarArtistas(View view) {
        recyclerViewArtistas = view.findViewById(R.id.recyclerArtistas);
        recyclerViewArtistas.setLayoutManager(new LinearLayoutManager(getContext()));

        // TODO: CONECTAR CON BASE DE DATOS - Reemplazar con datos reales
        // Por ahora solo mostramos un mensaje
        // List<Artista> listaArtistas = obtenerArtistasDeBD();
        // adapter = new ArtistaAdapter(listaArtistas, requireContext());
        // recyclerViewArtistas.setAdapter(adapter);

        // Mensaje temporal
        recyclerViewArtistas.setVisibility(View.GONE); // Ocultar hasta que implementes el adapter
    }

    private void animarBoton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }

    @Override
    public void onCategoriaClick(int position, PalabraCarruselItem categoria) {
        if (position > 0 && position < carruselAdapter.getItemCount() - 1) {
            recyclerViewCarrusel.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onCategoriaCentrada(int position, PalabraCarruselItem categoria) {
        btnAplicarFiltro.setVisibility(View.VISIBLE);
        btnAplicarFiltro.setText("Aplicar Filtro: " + categoria.getPalabra());
    }

    // Métodos para futura implementación con base de datos
    public String getProfesionFiltroActual() {
        return profesionFiltroActual;
    }

    public void limpiarFiltro() {
        profesionFiltroActual = "";
        btnAplicarFiltro.setVisibility(View.GONE);

        // TODO: Recargar todos los artistas sin filtro
        // configurarArtistas(getView());

        System.out.println("Filtro de artistas limpiado");
    }
}