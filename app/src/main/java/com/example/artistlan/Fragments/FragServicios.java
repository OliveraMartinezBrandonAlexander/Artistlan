package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.widget.Toast;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.R;
import com.example.artistlan.Carrusel.adapter.PalabraCarruselAdapter;
import com.example.artistlan.Carrusel.layout.CenterZoomLayoutManager;
import com.example.artistlan.Carrusel.model.PalabraCarruselItem;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragServicios extends Fragment implements PalabraCarruselAdapter.OnCategoriaClickListener {

    private RecyclerView recyclerServicios;
    private RecyclerView recyclerViewCarrusel;
    private TarjetaTextoServicioAdapter adapter;
    private PalabraCarruselAdapter carruselAdapter;
    private ImageButton btnIzq, btnDer;
    private Button btnAplicarFiltro;
    private List<PalabraCarruselItem> tiposServicios;
    private List<TarjetaTextoServicioItem> listaServicios = new ArrayList<>();
    private String tipoServicioFiltroActual = "";
    private CenterZoomLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        configurarCarrusel(view);
        configurarServicios(view);
        configurarBotonFiltro(view);

        cargarTodosLosServicios();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configurarCarrusel(View view) {
        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselServicios);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdoServicios);
        btnDer = view.findViewById(R.id.btnCarruselDerechoServicios);

        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        recyclerViewCarrusel.setHasFixedSize(false);
        recyclerViewCarrusel.setNestedScrollingEnabled(true);

        tiposServicios = obtenerProfesionesDeBD();

        carruselAdapter = new PalabraCarruselAdapter(tiposServicios, requireContext(), this);
        recyclerViewCarrusel.setAdapter(carruselAdapter);

        recyclerViewCarrusel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    encontrarItemCentral();
                }
            }
        });

        recyclerViewCarrusel.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

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

                if (position > 0 && position < carruselAdapter.getItemCount() - 1) {
                    carruselAdapter.setItemSeleccionado(position);
                }
            }
        }
    }

    private void configurarBotonFiltro(View view) {
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltroServicios);
        btnAplicarFiltro.setVisibility(View.GONE);

        btnAplicarFiltro.setOnClickListener(v -> {
            PalabraCarruselItem tipoSeleccionado = carruselAdapter.getCategoriaSeleccionada();
            if (tipoSeleccionado != null) {
                String tipoNombre = tipoSeleccionado.getPalabra();

                boolean yaActivo = tipoNombre.equalsIgnoreCase(tipoServicioFiltroActual);

                // Aplica o desactiva el filtro
                aplicarFiltro(tipoNombre);

                // Actualiza el texto del botón según el estado anterior
                if (yaActivo) {
                    btnAplicarFiltro.setText("Aplicar Filtro: " + tipoNombre);
                } else {
                    btnAplicarFiltro.setText("Desactivar Filtro: " + tipoNombre);
                }

                animarBoton(v);
            }
        });
    }

    private List<PalabraCarruselItem> obtenerProfesionesDeBD() {
        List<PalabraCarruselItem> profesiones = new ArrayList<>();

        String[] profesionesArray = {
                "Pintor", "Escultor", "Fotógrafo", "Ilustrador", "Diseñador gráfico",
                "Diseñador industrial", "Diseñador de moda", "Caricaturista", "Animador", "Artesano",
                "Ceramista", "Grabador", "Artista digital", "Artista plástico",
                "Maquetador", "Decorador", "Restaurador de arte", "Graffitero", "Modelador 3D"
        };

        int colorNormal = 0xFF4B2056;
        int colorSeleccionado = 0xFF6A2D7A;

        for (String profesion : profesionesArray) {
            profesiones.add(new PalabraCarruselItem(profesion, "prof_" + profesion.toLowerCase(), colorNormal, colorSeleccionado));
        }

        return profesiones;
    }
    private void aplicarFiltro(String tipoServicioNombre) {

        // Si ya estamos filtrando por este tipo -> quitar filtro
        if (tipoServicioFiltroActual.equalsIgnoreCase(tipoServicioNombre)) {
            tipoServicioFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();

            // Volvemos a mostrar TODOS los servicios
            if (adapter != null) {
                adapter.actualizarLista(new ArrayList<>(listaServicios));
            }
            return;
        }

        // Activamos nuevo filtro
        tipoServicioFiltroActual = tipoServicioNombre;
        Toast.makeText(getContext(), "Filtrando: " + tipoServicioNombre, Toast.LENGTH_SHORT).show();

        // Aplicamos filtro en la lista actual
        filtrarServiciosLocalmente(tipoServicioNombre);
    }

    private void filtrarServiciosLocalmente(String tipoServicio) {
        List<TarjetaTextoServicioItem> serviciosFiltrados = new ArrayList<>();

        if (listaServicios.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para filtrar.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (TarjetaTextoServicioItem servicio : listaServicios) {
            // Filtramos por tipo de categoria
            String categoria = servicio.getCategoria();
            if (categoria != null && categoria.equalsIgnoreCase(tipoServicio)) {
                serviciosFiltrados.add(servicio);
            }
        }

        if (serviciosFiltrados.isEmpty()) {

            // Si no hay resultados, mostramos la lista vacia
            adapter.actualizarLista(new ArrayList<>());
        } else {
            adapter.actualizarLista(serviciosFiltrados);
        }
    }

    private void configurarServicios(View view) {
        recyclerServicios = view.findViewById(R.id.recyclerServicios);
        recyclerServicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TarjetaTextoServicioAdapter(listaServicios, requireContext());
        recyclerServicios.setAdapter(adapter);
    }

    private void cargarTodosLosServicios() {
        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Call<List<ServicioDTO>> call = api.obtenerTodos();

        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(Call<List<ServicioDTO>> call, Response<List<ServicioDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TarjetaTextoServicioItem> items = convertir(response.body());

                    listaServicios.clear();
                    listaServicios.addAll(items);
                    adapter.actualizarLista(listaServicios);

                } else {
                    Toast.makeText(requireContext(), "Error al obtener servicios: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ServicioDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de red al cargar servicios.", Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private List<TarjetaTextoServicioItem> convertir(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> lista = new ArrayList<>();

        for (ServicioDTO dto : dtoList) {
            lista.add(new TarjetaTextoServicioItem(
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getContacto(),
                    dto.getTecnicas(),
                    dto.getNombreUsuario(),
                    dto.getCategoria(),
                    dto.getFotoPerfilAutor(),
                    false
            ));
        }
        return lista;
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
        if (categoria != null && !categoria.getPalabra().isEmpty()) {
            btnAplicarFiltro.setVisibility(View.VISIBLE);

            String tipoNombre = categoria.getPalabra();

            if (tipoNombre.equalsIgnoreCase(tipoServicioFiltroActual)) {
                btnAplicarFiltro.setText("Desactivar Filtro: " + tipoNombre);
            } else {
                btnAplicarFiltro.setText("Aplicar Filtro: " + tipoNombre);
            }

        } else {
            btnAplicarFiltro.setVisibility(View.GONE);
        }
    }

    public String getTipoServicioFiltroActual() {
        return tipoServicioFiltroActual;
    }

    public void limpiarFiltro() {
        tipoServicioFiltroActual = "";
        btnAplicarFiltro.setVisibility(View.GONE);

        if (adapter != null && listaServicios != null) {
            adapter.actualizarLista(new ArrayList<>(listaServicios));
        }

        System.out.println("Filtro de servicios limpiado");
    }
}