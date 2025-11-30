package com.example.artistlan.Fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;

public class FragSubirObra extends Fragment implements View.OnClickListener {

    private Button btnSubirImg;
    private Button btnSubirObra;
    private Button btnRegresar;
    private ImageView imgPreviewObra;

    private Uri uriImagenObra;
    private FirebaseImageRepository firebaseRepo;
    private ActivityResultLauncher<String> seleccionarImagenObraLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar repo (Firebase + Retrofit)
        firebaseRepo = new FirebaseImageRepository();

        // Launcher para abrir galería y seleccionar imagen
        seleccionarImagenObraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                uriImagenObra = uri;
                                if (imgPreviewObra != null) {
                                    Glide.with(this).load(uri).into(imgPreviewObra);
                                }
                            }
                        }
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_subir_obra, container, false);

        Spinner spinnerCategoria = view.findViewById(R.id.categoria);

        String[] categorias = {
                "Seleccione una opción", "Pintura", "Dibujo", "Escultura",
                "Fotografía", "Digital", "Acuarela", "Óleo", "Acrílico", "Grabado",
                "Cerámica", "Arte textil", "Collage", "Ilustración", "Mural",
                "Arte abstracto", "Retrato", "Paisaje", "Arte conceptual", "Otros"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categorias
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        // === NUEVO: referenciar vistas ===
        imgPreviewObra = view.findViewById(R.id.imgPreviewObra);
        btnSubirImg = view.findViewById(R.id.btnSubirImg);
        btnSubirObra = view.findViewById(R.id.btnSubirObra);

        // Abrir galería al tocar SUBIR IMAGEN
        btnSubirImg.setOnClickListener(v ->
                seleccionarImagenObraLauncher.launch("image/*")
        );

        // Subir imagen1 (de momento probando con una obra fija)
        btnSubirObra.setOnClickListener(v ->
                guardarImagen1Obra()
        );

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.GONE);

        btnRegresar = view.findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(this);

        new BotonesMenuSuperior(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.MenuInferior).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRegresar) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // =======================
    //  imagen1 de Obra
    // =======================
    private void guardarImagen1Obra() {
        if (uriImagenObra == null) {
            Toast.makeText(getContext(), "Primero selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Por ahora probar con la Obra 1 (Atardecer en la montaña).
        // Luego esto se reemplaza por el id real de la obra que se cree/edite.
        int idObra = 1;

        firebaseRepo.subirImagenObraYActualizarEnBD(
                idObra,
                uriImagenObra,
                new FirebaseImageRepository.ImagenListener() {
                    @Override
                    public void onSuccess(String urlFinal) {
                        Toast.makeText(getContext(),
                                "imagen1 actualizada correctamente",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String mensajeError) {
                        Toast.makeText(getContext(),
                                "Error: " + mensajeError,
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}
