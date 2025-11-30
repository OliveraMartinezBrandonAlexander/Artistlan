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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.ObrasLikesApi;
import com.example.artistlan.Conector.model.ObraDTO;

import com.example.artistlan.R;
import com.example.artistlan.Carrusel.adapter.PalabraCarruselAdapter;
import com.example.artistlan.Carrusel.layout.CenterZoomLayoutManager;
import com.example.artistlan.Carrusel.model.PalabraCarruselItem;

import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private final Map<Integer, Boolean> likesLocal = new HashMap<>();
    private ObraApi obraApi;
    private ObrasLikesApi likesApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_arte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        obraApi = RetrofitClient.getClient().create(ObraApi.class);
        likesApi = RetrofitClient.getClient().create(ObrasLikesApi.class);

        configurarCarrusel(view);
        configurarObras(view);
        configurarBotonFiltro(view);
    }

    // -------------------------------------------------------------
    // CARRUSEL
    // -------------------------------------------------------------
    private void configurarCarrusel(View view) {

        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselArte);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdo);
        btnDer = view.findViewById(R.id.btnCarruselDerecho);

        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        palabrasArte = obtenerCategoriasDeBD();

        carruselAdapter = new PalabraCarruselAdapter(palabrasArte, requireContext(), this);
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

        btnDer.setOnClickListener(v -> {
            int current = carruselAdapter.getItemSeleccionado();
            if (current < carruselAdapter.getItemCount() - 2) {
                recyclerViewCarrusel.smoothScrollToPosition(current + 1);
                animarBoton(v);
            }
        });

        btnIzq.setOnClickListener(v -> {
            int current = carruselAdapter.getItemSeleccionado();
            if (current > 1) {
                recyclerViewCarrusel.smoothScrollToPosition(current - 1);
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
            if (position != RecyclerView.NO_POSITION &&
                    position > 0 && position < carruselAdapter.getItemCount() - 1) {

                carruselAdapter.setItemSeleccionado(position);
            }
        }
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

        for (String cat : categoriasArray) {
            categorias.add(new PalabraCarruselItem(
                    cat,
                    cat,
                    colorNormal,
                    colorSeleccionado
            ));
        }

        return categorias;
    }

    // -------------------------------------------------------------
    // BOTÓN FILTRO
    // -------------------------------------------------------------
    private void configurarBotonFiltro(View view) {
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltro);
        btnAplicarFiltro.setVisibility(View.GONE);

        btnAplicarFiltro.setOnClickListener(v -> {
            PalabraCarruselItem categoria = carruselAdapter.getCategoriaSeleccionada();
            if (categoria != null) {
                aplicarFiltro(categoria.getPalabra());
                animarBoton(v);
            }
        });
    }

    private void aplicarFiltro(String categoriaNombre) {

        if (categoriaFiltroActual.equals(categoriaNombre)) {
            categoriaFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();
        } else {
            categoriaFiltroActual = categoriaNombre;
            Toast.makeText(getContext(), "Filtrando: " + categoriaNombre, Toast.LENGTH_SHORT).show();
        }

        obtenerObrasDeAPI();
    }

    // -------------------------------------------------------------
    // OBRAS Y ADAPTER
    // -------------------------------------------------------------
    private void configurarObras(View view) {
        recyclerViewObras = view.findViewById(R.id.recyclerObras);
        recyclerViewObras.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
        recyclerViewObras.setAdapter(adapter);

        obtenerObrasDeAPI();
    }

    private void obtenerObrasDeAPI() {

        Call<List<ObraDTO>> call = obraApi.obtenerTodasLasObras();

        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                List<TarjetaTextoObraItem> items = new ArrayList<>();

                for (ObraDTO dto : response.body()) {

                    // FILTRO
                    if (!categoriaFiltroActual.isEmpty() &&
                            !categoriaFiltroActual.equalsIgnoreCase(dto.getTecnicas())) {
                        continue;
                    }

                    items.add(new TarjetaTextoObraItem(
                            dto.getIdObra(),
                            dto.getTitulo(),
                            dto.getDescripcion(),
                            dto.getEstado(),
                            dto.getPrecio(),
                            dto.getImagen1(),
                            dto.getImagen2(),
                            dto.getImagen3(),
                            dto.getTecnicas(),
                            dto.getMedidas(),
                            dto.getLikes() != null ? dto.getLikes() : 0,
                            dto.getNombreAutor(),
                            dto.getNombreCategoria(),
                            false,
                            false
                    ));
                }

                adapter.actualizarLista(items);
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

//    public void inicializarLikes(int idObra, ImageView btnLike, TextView tvLikes) {
//
//        // siempre ponlo morado primero para evitar basura reciclada
//        btnLike.setImageResource(R.drawable.ic_heart_purple);
//
//        boolean yaLike = likesLocal.getOrDefault(idObra, false);
//
//        likesApi.obtenerLikes(idObra).enqueue(new Callback<Integer>() {
//            @Override
//            public void onResponse(Call<Integer> call, Response<Integer> response) {
//                if (!response.isSuccessful()) return;
//
//                int likes = response.body();
//                tvLikes.setText(String.valueOf(likes));
//
//                // AQUI se pone el color correcto con certeza
//                btnLike.setImageResource(
//                        yaLike ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple
//                );
//            }
//
//            @Override
//            public void onFailure(Call<Integer> call, Throwable t) {}
//        });
//
//        btnLike.setOnClickListener(v -> {
//            boolean like = likesLocal.getOrDefault(idObra, false);
//
//            if (like) {
//                quitarLike(idObra, btnLike, tvLikes);
//            } else {
//                darLike(idObra, btnLike, tvLikes);
//            }
//        });
//    }
//
//    private void darLike(int idObra, ImageView btnLike, TextView tvLikes) {
//
//        likesApi.darLike(idObra).enqueue(new Callback<ObraDTO>() {
//            @Override
//            public void onResponse(Call<ObraDTO> call, Response<ObraDTO> response) {
//                if (!response.isSuccessful()) return;
//
//                ObraDTO obra = response.body();
//                tvLikes.setText(String.valueOf(obra.getLikes()));
//
//                likesLocal.put(idObra, true);
//
//                btnLike.setImageResource(R.drawable.ic_heart_red);
//            }
//
//            @Override
//            public void onFailure(Call<ObraDTO> call, Throwable t) {}
//        });
//    }
//
//    private void quitarLike(int idObra, ImageView btnLike, TextView tvLikes) {
//
//        likesApi.quitarLike(idObra).enqueue(new Callback<ObraDTO>() {
//            @Override
//            public void onResponse(Call<ObraDTO> call, Response<ObraDTO> response) {
//                if (!response.isSuccessful()) return;
//
//                ObraDTO obra = response.body();
//                tvLikes.setText(String.valueOf(obra.getLikes()));
//
//                likesLocal.put(idObra, false);
//
//                btnLike.setImageResource(R.drawable.ic_heart_purple);
//            }
//
//            @Override
//            public void onFailure(Call<ObraDTO> call, Throwable t) {}
//        });
//    }

    // -------------------------------------------------------------
    // ANIMACIÓN BOTÓN
    // -------------------------------------------------------------
    private void animarBoton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }

    // -------------------------------------------------------------
    // EVENTOS CARRUSEL
    // -------------------------------------------------------------
    @Override
    public void onCategoriaClick(int position, PalabraCarruselItem categoria) {
        if (position > 0 && position < carruselAdapter.getItemCount() - 1) {
            recyclerViewCarrusel.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onCategoriaCentrada(int position, PalabraCarruselItem categoria) {
        if (!categoria.getPalabra().isEmpty()) {
            btnAplicarFiltro.setVisibility(View.VISIBLE);
            btnAplicarFiltro.setText("Aplicar Filtro: " + categoria.getPalabra());
        } else {
            btnAplicarFiltro.setVisibility(View.GONE);
        }
    }
}