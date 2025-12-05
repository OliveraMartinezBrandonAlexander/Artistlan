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

        // Configurar el carrusel de tipos de servicios
        configurarCarrusel(view);

        // Configurar el RecyclerView de servicios
        configurarServicios(view);

        // Configurar botón de aplicar filtro
        configurarBotonFiltro(view);

        // Cargar datos de la red
        cargarTodosLosServicios();
    }

    private void configurarCarrusel(View view) {
        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselServicios);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdoServicios);
        btnDer = view.findViewById(R.id.btnCarruselDerechoServicios);

        // Configurar LayoutManager personalizado
        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        // HABILITAR SCROLL HORIZONTAL
        recyclerViewCarrusel.setHasFixedSize(false);
        recyclerViewCarrusel.setNestedScrollingEnabled(true);

        tiposServicios = obtenerProfesionesDeBD();

        carruselAdapter = new PalabraCarruselAdapter(tiposServicios, requireContext(), this);
        recyclerViewCarrusel.setAdapter(carruselAdapter);

        // AGREGAR SCROLL LISTENER PARA DETECTAR ITEM CENTRAL - ESTO ES LO QUE FALTABA
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
            // Permitir que el RecyclerView maneje el scroll táctil
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
            PalabraCarruselItem tipoServicioSeleccionado = carruselAdapter.getCategoriaSeleccionada();
            if (tipoServicioSeleccionado != null) {
                aplicarFiltro(tipoServicioSeleccionado.getIdCategoria());
                animarBoton(v);
            }
        });
    }

    private List<PalabraCarruselItem> obtenerProfesionesDeBD() {
        List<PalabraCarruselItem> profesiones = new ArrayList<>();

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
    private void aplicarFiltro(String idTipoServicio) {
        tipoServicioFiltroActual = idTipoServicio;

        PalabraCarruselItem tipoServicio = carruselAdapter.getCategoriaSeleccionada();
        if (tipoServicio != null) {
            System.out.println("Aplicando filtro para tipo de servicio: " + tipoServicio.getPalabra());
            filtrarServiciosLocalmente(tipoServicio.getPalabra());
        }
    }

    private void filtrarServiciosLocalmente(String tipoServicio) {
        List<TarjetaTextoServicioItem> serviciosFiltrados = new ArrayList<>();

        if (listaServicios.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para filtrar.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (TarjetaTextoServicioItem servicio : listaServicios) {
            if (servicio.getTecnicas() != null &&
                    servicio.getTecnicas().toLowerCase().contains(tipoServicio.toLowerCase())) {
                serviciosFiltrados.add(servicio);
            }
        }

        if (serviciosFiltrados.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontraron servicios para: " + tipoServicio, Toast.LENGTH_SHORT).show();
            adapter.actualizarLista(new ArrayList<>(listaServicios));
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
        btnAplicarFiltro.setVisibility(View.VISIBLE);
        btnAplicarFiltro.setText("Aplicar Filtro: " + categoria.getPalabra());
    }

    // Métodos para futura implementación con base de datos
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