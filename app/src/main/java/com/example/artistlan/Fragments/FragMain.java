package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.artistlan.Conector.api.ConvocatoriaApi;
import com.example.artistlan.Conector.model.ConvocatoriaDTO;
import com.example.artistlan.adapter.ConvocatoriaHomeAdapter;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.Carrusel.adapter.CarruselAdapter;
import com.example.artistlan.Carrusel.model.ObraCarruselItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMain extends Fragment {

    private ViewPager2 viewPager;
    private ImageButton btnIzq, btnDer;
    private RecyclerView rvConvocatoriasMain;
    private ProgressBar pbConvocatoriasMain;
    private TextView tvConvocatoriasMainEstado;

    private ConvocatoriaHomeAdapter convocatoriaAdapter;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_frag_main, container, false);
        new BotonesMenuSuperior(this);
        viewPager = root.findViewById(R.id.viewPagerCarrusel);
        btnIzq = root.findViewById(R.id.btnCarruselIzquierdo);
        btnDer = root.findViewById(R.id.btnCarruselDerecho);

        rvConvocatoriasMain = root.findViewById(R.id.rvConvocatoriasMain);
        pbConvocatoriasMain = root.findViewById(R.id.pbConvocatoriasMain);
        tvConvocatoriasMainEstado = root.findViewById(R.id.tvConvocatoriasMainEstado);


        List<ObraCarruselItem> obras = new ArrayList<>();
        obras.add(new ObraCarruselItem(R.drawable.pin1, "Obra 1", "Descripción 1", "Superman", ""));
        obras.add(new ObraCarruselItem(R.drawable.pin2, "Obra 2", "Descripción 2", "Batman", ""));
        obras.add(new ObraCarruselItem(R.drawable.pin3, "Obra 3", "Descripción 3", "Wonder Woman", ""));

        CarruselAdapter adapter = new CarruselAdapter(obras, getContext());
        viewPager.setAdapter(adapter);

        cargarObrasCarrusel(obras, adapter);
        configurarConvocatoriasMain();
        cargarConvocatoriasMain();
        manejarSolicitudScrollConvocatorias();

        btnDer.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < obras.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                animarBoton(v);
            }
        });

        btnIzq.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                animarBoton(v);
            }
        });
        return root;
    }

    private void configurarConvocatoriasMain() {
        convocatoriaAdapter = new ConvocatoriaHomeAdapter(this::openWebPage);
        rvConvocatoriasMain.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConvocatoriasMain.setNestedScrollingEnabled(false);
        rvConvocatoriasMain.setAdapter(convocatoriaAdapter);
    }

    private void cargarConvocatoriasMain() {
        mostrarEstadoConvocatorias(true, null);
        ConvocatoriaApi api = RetrofitClient.getClient().create(ConvocatoriaApi.class);
        api.getConvocatorias().enqueue(new Callback<List<ConvocatoriaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConvocatoriaDTO>> call, @NonNull Response<List<ConvocatoriaDTO>> response) {
                if (!isAdded()) return;
                mostrarEstadoConvocatorias(false, null);
                if (!response.isSuccessful() || response.body() == null) {
                    mostrarEstadoConvocatorias(false, "No se pudieron cargar las convocatorias.");
                    return;
                }

                List<ConvocatoriaDTO> convocatorias = response.body();
                convocatoriaAdapter.actualizar(convocatorias);
                if (convocatorias.isEmpty()) {
                    mostrarEstadoConvocatorias(false, "No hay convocatorias activas por ahora.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ConvocatoriaDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mostrarEstadoConvocatorias(false, "Error de conexión al cargar convocatorias.");
            }
        });
    }

    private void mostrarEstadoConvocatorias(boolean loading, @Nullable String mensaje) {
        pbConvocatoriasMain.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (mensaje == null || mensaje.isEmpty()) {
            tvConvocatoriasMainEstado.setVisibility(View.GONE);
        } else {
            tvConvocatoriasMainEstado.setVisibility(View.VISIBLE);
            tvConvocatoriasMainEstado.setText(mensaje);
        }
    }

    private void manejarSolicitudScrollConvocatorias() {
        Bundle args = getArguments();
        if (args == null || !args.getBoolean("scroll_to_convocatorias", false)) return;

        args.putBoolean("scroll_to_convocatorias", false);

        uiHandler.post(() -> {
            if (!isAdded() || getView() == null) return;

            rvConvocatoriasMain.post(() -> {
                if (rvConvocatoriasMain != null) {
                    rvConvocatoriasMain.smoothScrollToPosition(0);
                }
            });
        });
    }

    public void openWebPage(String url) {
        try {
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("FragMain", "No se pudo abrir el navegador: " + e.getMessage());
        }
    }

    private void cargarObrasCarrusel(List<ObraCarruselItem> obras, CarruselAdapter adapter) {

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Call<List<ObraDTO>> call = api.obtenerTodasLasObras();

        call.enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(Call<List<ObraDTO>> call, Response<List<ObraDTO>> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful()) {
                    return;
                }

                List<ObraDTO> dtos = response.body();
                if (dtos == null || dtos.isEmpty()) {
                    return;
                }

                List<ObraDTO> seleccionadas;
                if (dtos.size() <= 3) {
                    seleccionadas = dtos;
                } else {
                    List<ObraDTO> copia = new ArrayList<>(dtos);
                    Collections.shuffle(copia);
                    seleccionadas = copia.subList(0, 3);
                }

                int reemplazos = Math.min(3, seleccionadas.size());

                for (int i = 0; i < reemplazos; i++) {
                    ObraDTO dto = seleccionadas.get(i);
                    ObraCarruselItem original = obras.get(i);

                    String titulo = (dto.getTitulo() != null && !dto.getTitulo().isEmpty())
                            ? dto.getTitulo()
                            : original.getTitulo();

                    String descripcion = (dto.getDescripcion() != null && !dto.getDescripcion().isEmpty())
                            ? dto.getDescripcion()
                            : original.getDescripcion();

                    String autor = (dto.getNombreAutor() != null && !dto.getNombreAutor().isEmpty())
                            ? dto.getNombreAutor()
                            : original.getAutor();

                    String likes;
                    if (dto.getLikes() != null) {
                        likes = dto.getLikes() + " likes";
                    } else {
                        likes = original.getLikes();
                    }

                    String imagenUrl = null;
                    if (dto.getImagen1() != null && !dto.getImagen1().isEmpty()) {
                        imagenUrl = dto.getImagen1();
                    }

                    String autorFotoUrl = null;
                    if (dto.getFotoPerfilAutor() != null && !dto.getFotoPerfilAutor().isEmpty()) {
                        autorFotoUrl = dto.getFotoPerfilAutor();

                        autorFotoUrl = autorFotoUrl.replace("http://localhost", "http://10.0.2.2");
                        autorFotoUrl = autorFotoUrl.replace("https://localhost", "https://10.0.2.2");

                        if (!autorFotoUrl.startsWith("http")) {
                            String base = "http://10.0.2.2:8080";
                            if (!autorFotoUrl.startsWith("/")) autorFotoUrl = "/" + autorFotoUrl;
                            autorFotoUrl = base + autorFotoUrl;
                        }
                    }


                    obras.set(i, new ObraCarruselItem(
                            original.getImagen(),
                            imagenUrl,
                            titulo,
                            descripcion,
                            autor,
                            "",
                            autorFotoUrl

                    ));
                }


                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ObraDTO>> call, Throwable t) {
                t.printStackTrace();
                if (!isAdded()) return;
                Log.e("FragMain", "Error de red al cargar obras del carrusel: " + t.getMessage());
            }
        });
    }

    private void animarBoton(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 1.2f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }
}
