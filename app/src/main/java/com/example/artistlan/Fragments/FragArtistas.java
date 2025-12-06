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
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.ArtistaDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.Carrusel.adapter.PalabraCarruselAdapter;
import com.example.artistlan.Carrusel.layout.CenterZoomLayoutManager;
import com.example.artistlan.Carrusel.model.PalabraCarruselItem;
import com.example.artistlan.TarjetaTextoArtista.adapter.TarjetaTextoArtistaAdapter;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragArtistas extends Fragment implements PalabraCarruselAdapter.OnCategoriaClickListener {

    private RecyclerView recyclerViewArtistas;
    private RecyclerView recyclerViewCarrusel;
    private TarjetaTextoArtistaAdapter adapter;
    private PalabraCarruselAdapter carruselAdapter;
    private ImageButton btnIzq, btnDer;
    private Button btnAplicarFiltro;
    private List<PalabraCarruselItem> profesionesArtistas;
    private String profesionFiltroActual = "";
    private CenterZoomLayoutManager layoutManager;
    private List<TarjetaTextoArtistaItem> listaArtistas = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_artistas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new BotonesMenuSuperior(this, view);

        configurarCarrusel(view);
        configurarArtistas(view);
        configurarBotonFiltro(view);

        cargarArtistas();
    }

    // ---------------- Carrusel ------------------
    private void configurarCarrusel(View view) {
        recyclerViewCarrusel = view.findViewById(R.id.recyclerCarruselArtistas);
        btnIzq = view.findViewById(R.id.btnCarruselIzquierdoArtistas);
        btnDer = view.findViewById(R.id.btnCarruselDerechoArtistas);

        layoutManager = new CenterZoomLayoutManager(getContext());
        recyclerViewCarrusel.setLayoutManager(layoutManager);

        profesionesArtistas = obtenerProfesionesDeBD();
        carruselAdapter = new PalabraCarruselAdapter(profesionesArtistas, requireContext(), this);
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
            recyclerViewCarrusel.postDelayed(() -> carruselAdapter.setItemSeleccionado(1), 100);
        });
    }

    private void encontrarItemCentral() {
        int centerX = recyclerViewCarrusel.getWidth() / 2;
        float centerY = recyclerViewCarrusel.getHeight() / 2.0f;
        View centerView = recyclerViewCarrusel.findChildViewUnder(centerX, centerY);

        if (centerView != null) {
            int position = recyclerViewCarrusel.getChildAdapterPosition(centerView);
            if (position != RecyclerView.NO_POSITION && position > 0 && position < carruselAdapter.getItemCount() - 1) {
                carruselAdapter.setItemSeleccionado(position);
            }
        }
    }

    private void configurarBotonFiltro(View view) {
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltroArtistas);
        btnAplicarFiltro.setVisibility(View.GONE);

        btnAplicarFiltro.setOnClickListener(v -> {
            PalabraCarruselItem profesionSeleccionada = carruselAdapter.getCategoriaSeleccionada();
            if (profesionSeleccionada != null) {
                // por ahora dejamos el filtro pendiente
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

    private void configurarArtistas(View view) {
        recyclerViewArtistas = view.findViewById(R.id.recyclerArtistas);
        recyclerViewArtistas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TarjetaTextoArtistaAdapter(listaArtistas, requireContext());
        recyclerViewArtistas.setAdapter(adapter);
    }

    private void cargarArtistas() {
        UsuarioApi api = RetrofitClient.getClient().create(UsuarioApi.class);
        Call<List<ArtistaDTO>> call = api.getArtistas();

        call.enqueue(new Callback<List<ArtistaDTO>>() {
            @Override
            public void onResponse(Call<List<ArtistaDTO>> call, Response<List<ArtistaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ArtistaDTO> artistas = response.body();
                    for (ArtistaDTO artista : artistas) {
                        obtenerMiniObras(artista);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ArtistaDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void obtenerMiniObras(ArtistaDTO artista) {
        ObraApi obraApi = RetrofitClient.getClient().create(ObraApi.class);
        Call<List<ObraDTO>> call = obraApi.obtenerObrasDeUsuario(artista.getIdUsuario());

        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(Call<List<ObraDTO>> call, Response<List<ObraDTO>> response) {
                List<String> miniObras = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    List<ObraDTO> obras = response.body();
                    for (int i = 0; i < Math.min(3, obras.size()); i++) {
                        miniObras.add(obras.get(i).getImagen1());
                    }
                }
                while (miniObras.size() < 3) {
                    miniObras.add(null);
                }

                TarjetaTextoArtistaItem item = new TarjetaTextoArtistaItem(
                        artista.getUsuario(),
                        artista.getCategoria(),
                        artista.getDescripcion(),
                        artista.getFotoPerfil(),
                        miniObras
                );

                listaArtistas.add(item);
                adapter.actualizarLista(new ArrayList<>(listaArtistas));
            }

            @Override
            public void onFailure(Call<List<ObraDTO>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}