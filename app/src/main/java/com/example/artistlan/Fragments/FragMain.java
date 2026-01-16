package com.example.artistlan.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_frag_main, container, false);
        new BotonesMenuSuperior(this, root);
        viewPager = root.findViewById(R.id.viewPagerCarrusel);
        btnIzq = root.findViewById(R.id.btnCarruselIzquierdo);
        btnDer = root.findViewById(R.id.btnCarruselDerecho);

        Button btn1 = root.findViewById(R.id.btnEvento1);
        Button btn2 = root.findViewById(R.id.btnEvento2);
        Button btn3 = root.findViewById(R.id.btnEvento3);
        Button btn4 = root.findViewById(R.id.btnEvento4);


        List<ObraCarruselItem> obras = new ArrayList<>();
        obras.add(new ObraCarruselItem(R.drawable.pin1, "Obra 1", "Descripción 1", "Superman", "135K"));
        obras.add(new ObraCarruselItem(R.drawable.pin2, "Obra 2", "Descripción 2", "Batman", "80K"));
        obras.add(new ObraCarruselItem(R.drawable.pin3, "Obra 3", "Descripción 3", "Wonder Woman", "95K"));

        CarruselAdapter adapter = new CarruselAdapter(obras, getContext());
        viewPager.setAdapter(adapter);

        cargarObrasCarrusel(obras, adapter);

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



        btn1.setOnClickListener(v -> openWebPage("https://convocatorias.cultura.gob.mx/vigentes/detalle/4012/xvii-concurso-nacional-de-fotografia"));
        btn2.setOnClickListener(v -> openWebPage("https://bada.com.mx/convocatoria-2026/"));
        btn3.setOnClickListener(v -> openWebPage("https://www.becajenkinsdeltoro.com/"));
        btn4.setOnClickListener(v -> openWebPage("https://convocatorias.cultura.gob.mx/"));

        return root;
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

                // Selección aleatoria cuando hay más de 3
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

                    obras.set(i, new ObraCarruselItem(
                            original.getImagen(), // drawable local
                            imagenUrl,            // URL de la BD
                            titulo,
                            descripcion,
                            autor,
                            likes
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
