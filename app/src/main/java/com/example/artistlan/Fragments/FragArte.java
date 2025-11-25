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
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.List;

public class FragArte extends Fragment implements PalabraCarruselAdapter.OnCategoriaClickListener {

    private RecyclerView recyclerViewObras;
    private RecyclerView recyclerViewCarrusel;
    private TarjetaTextoObraAdapter adapter;
    private PalabraCarruselAdapter carruselAdapter;
    private ImageButton btnIzq, btnDer;
    private Button btnAplicarFiltro;
    private List<PalabraCarruselItem> palabrasArte;
    private String categoriaFiltroActual = "";
    private CenterZoomLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_arte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        // Configurar el carrusel de palabras
        configurarCarrusel(view);

        // Configurar el RecyclerView de obras
        configurarObras(view);

        // Configurar botón de aplicar filtro
        configurarBotonFiltro(view);
    }

    private void configurarCarrusel(View view) {
        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselArte);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdo);
        btnDer = view.findViewById(R.id.btnCarruselDerecho);

        // Configurar LayoutManager personalizado
        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        // Crear lista de palabras para el carrusel
        palabrasArte = obtenerCategoriasDeBD();

        carruselAdapter = new PalabraCarruselAdapter(palabrasArte, requireContext(), this);
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
            // Esperar un poco antes de establecer la selección
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
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltro);
        btnAplicarFiltro.setVisibility(View.GONE);

        btnAplicarFiltro.setOnClickListener(v -> {
            PalabraCarruselItem categoriaSeleccionada = carruselAdapter.getCategoriaSeleccionada();
            if (categoriaSeleccionada != null) {
                aplicarFiltro(categoriaSeleccionada.getIdCategoria());
                animarBoton(v);
            }
        });
    }

    private List<PalabraCarruselItem> obtenerCategoriasDeBD() {
        List<PalabraCarruselItem> categorias = new ArrayList<>();

        String[] categoriasArray = {
                "Pintura", "Dibujo", "Escultura", "Fotografía", "Digital",
                "Acuarela", "Óleo", "Acrílico", "Grabado", "Cerámica",
                "Arte textil", "Collage", "Ilustración", "Mural",
                "Arte abstracto", "Retrato", "Paisaje", "Arte conceptual"
        };

        int colorNormal = 0xFF4B2056;
        int colorSeleccionado = 0xFF6A2D7A;

        for (String categoria : categoriasArray) {
            categorias.add(new PalabraCarruselItem(categoria, "id_" + categoria.toLowerCase(), colorNormal, colorSeleccionado));
        }

        return categorias;
    }

    private void aplicarFiltro(String idCategoria) {
        categoriaFiltroActual = idCategoria;

        PalabraCarruselItem categoria = carruselAdapter.getCategoriaSeleccionada();
        if (categoria != null) {
            System.out.println("Aplicando filtro para categoría: " + categoria.getPalabra());
            // TODO: Implementar filtrado real con tu base de datos
        }
    }

    private void configurarObras(View view) {
        recyclerViewObras = view.findViewById(R.id.recyclerObras);
        recyclerViewObras.setLayoutManager(new LinearLayoutManager(getContext()));

        List<TarjetaTextoObraItem> listaObras = new ArrayList<>();

        listaObras.add(new TarjetaTextoObraItem(
                "Amanecer en la Playa",
                "Obra inspirada en un amanecer",
                "Disponible",
                3500.0,
                "url_img1",
                "url_img2",
                "url_img3",
                "Óleo",
                "30x50cm",
                "Naturaleza",
                120,
                1
        ));

        listaObras.add(new TarjetaTextoObraItem(
                "Retrato Azul",
                "Retrato expresivo en tonos azules",
                "Vendido",
                4200.0,
                "img1",
                "img2",
                "img3",
                "Acrílico",
                "50x70cm",
                "Retrato",
                200,
                3
        ));

        adapter = new TarjetaTextoObraAdapter(listaObras, requireContext());
        recyclerViewObras.setAdapter(adapter);
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
}