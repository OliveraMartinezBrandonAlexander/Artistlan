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
    private List<TarjetaTextoServicioItem> listaServicios;
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

        cargarTodosLosServicios();
    }

    private void configurarCarrusel(View view) {
        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselServicios);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdoServicios);
        btnDer = view.findViewById(R.id.btnCarruselDerechoServicios);

        // Configurar LayoutManager personalizado
        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        // Crear lista de tipos de servicios para el carrusel
        tiposServicios = obtenerTiposServiciosDeBD();

        carruselAdapter = new PalabraCarruselAdapter(tiposServicios, requireContext(), this);
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

    private List<PalabraCarruselItem> obtenerTiposServiciosDeBD() {
        // TODO: CONECTAR CON BASE DE DATOS - Reemplazar con tu lógica real
        List<PalabraCarruselItem> tipos = new ArrayList<>();

        // Ejemplo de tipos de servicios - reemplaza con datos reales de BD
        String[] tiposArray = {
                "Retratos", "Murales", "Ilustración", "Diseño", "Fotografía",
                "Escultura", "Pintura", "Digital", "Acuarela", "Óleo",
                "Acrílico", "Grabado", "Cerámica", "Textil", "Collage",
                "Restauración", "Clases", "Personalizado"
        };

        int colorNormal = 0xFF4B2056;
        int colorSeleccionado = 0xFF6A2D7A;

        for (String tipo : tiposArray) {
            tipos.add(new PalabraCarruselItem(tipo, "serv_" + tipo.toLowerCase(), colorNormal, colorSeleccionado));
        }

        return tipos;
    }

    private void aplicarFiltro(String idTipoServicio) {
        tipoServicioFiltroActual = idTipoServicio;

        PalabraCarruselItem tipoServicio = carruselAdapter.getCategoriaSeleccionada();
        if (tipoServicio != null) {
            System.out.println("Aplicando filtro para tipo de servicio: " + tipoServicio.getPalabra());

            // TODO: CONECTAR CON BASE DE DATOS - Implementar filtrado real
            // Por ahora, filtramos localmente los datos de prueba
            filtrarServiciosLocalmente(tipoServicio.getPalabra());
        }
    }

    private void filtrarServiciosLocalmente(String tipoServicio) {
        List<TarjetaTextoServicioItem> serviciosFiltrados = new ArrayList<>();

        for (TarjetaTextoServicioItem servicio : listaServicios) {
            // Verificar si las técnicas del servicio contienen el tipo seleccionado
            if (servicio.getTecnicas() != null &&
                    servicio.getTecnicas().toLowerCase().contains(tipoServicio.toLowerCase())) {
                serviciosFiltrados.add(servicio);
            }
        }

        // Si no hay coincidencias, mostrar todos
        if (serviciosFiltrados.isEmpty()) {
            serviciosFiltrados = new ArrayList<>(listaServicios);
            System.out.println("No se encontraron servicios para: " + tipoServicio + ". Mostrando todos.");
        }

        // Actualizar el adapter con la lista filtrada
        adapter.actualizarLista(serviciosFiltrados);
    }

    private void configurarServicios(View view) {
        recyclerServicios = view.findViewById(R.id.recyclerServicios);
        recyclerServicios.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Datos de prueba - TODO: CONECTAR CON BASE DE DATOS
        listaServicios = new ArrayList<>();
        listaServicios.add(new TarjetaTextoServicioItem(
                "Retratos digitales",
                "Hago ilustraciones personalizadas en estilo digital",
                "55-555-555",
                "Digital",
                "Art By Lua"
        ));
        listaServicios.add(new TarjetaTextoServicioItem(
                "Murales personalizados",
                "Murales en pared y negocios con acrílico y spray",
                "44-444-444",
                "Acrílico / Spray",
                "MurArt Studio"
        ));
        listaServicios.add(new TarjetaTextoServicioItem(
                "Pintura al óleo",
                "Retratos y paisajes en técnica de óleo tradicional",
                "33-333-333",
                "Óleo",
                "Estudio Clásico"
        ));
        listaServicios.add(new TarjetaTextoServicioItem(
                "Fotografía artística",
                "Sesiones de fotografía conceptual y artística",
                "66-666-666",
                "Fotografía",
                "Lens Art"
        ));
        listaServicios.add(new TarjetaTextoServicioItem(
                "Clases de acuarela",
                "Enseño técnicas de acuarela para principiantes",
                "77-777-777",
                "Clases",
                "Acuarela Studio"
        ));
        listaServicios.add(new TarjetaTextoServicioItem(
                "Retratos al óleo",
                "Retratos realistas en óleo sobre lienzo",
                "88-888-888",
                "Óleo",
                "Retratos Clásicos"
        ));

        adapter = new TarjetaTextoServicioAdapter(listaServicios, requireContext());
        recyclerServicios.setAdapter(adapter);
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

        // Recargar todos los servicios sin filtro
        if (adapter != null && listaServicios != null) {
            adapter.actualizarLista(listaServicios);
        }

        System.out.println("Filtro de servicios limpiado");
    }
    private List<TarjetaTextoServicioItem> convertir(List<ServicioDTO> dtoList) {
        List<TarjetaTextoServicioItem> lista = new ArrayList<>();

        for (ServicioDTO dto : dtoList) {
            lista.add(new TarjetaTextoServicioItem(
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getContacto(),
                    dto.getTecnicas(),
                    dto.getNombreUsuario()
            ));
        }
        return lista;
    }

    private void cargarTodosLosServicios() {
        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Call<List<ServicioDTO>> call = api.obtenerTodos();

        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(Call<List<ServicioDTO>> call, Response<List<ServicioDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TarjetaTextoServicioItem> items = convertir(response.body());
                    adapter = new TarjetaTextoServicioAdapter(items, requireContext());
                    recyclerServicios.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ServicioDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}