package com.example.artistlan.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirObra extends Fragment implements View.OnClickListener {

    private Button btnSubirImg;
    private Button btnSubirObra;
    private ImageButton btnRegresar;
    private ImageView imgPreviewObra;

    private Uri uriImagenObra;
    private FirebaseImageRepository firebaseRepo;
    private ActivityResultLauncher<String> seleccionarImagenObraLauncher;

    private android.widget.EditText etTituloObra, etDescripcion, etPrecio, etMedidas, etTecnicas;
    private android.widget.RadioGroup rgOpciones;
    private Spinner spinnerCategoria;

    // Lista real que viene de la API
    private List<CategoriaDTO> listaCategorias = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseRepo = new FirebaseImageRepository();

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

        etTituloObra = view.findViewById(R.id.tituloObra);
        rgOpciones   = view.findViewById(R.id.radioGroupOpciones);
        etDescripcion = view.findViewById(R.id.descripcion);
        etPrecio      = view.findViewById(R.id.precio);
        etMedidas     = view.findViewById(R.id.medidas);
        etTecnicas    = view.findViewById(R.id.edit_text_tecnica);

        spinnerCategoria = view.findViewById(R.id.categoria);

        // Se cargan dinámicamente desde la API
        cargarCategorias();

        imgPreviewObra = view.findViewById(R.id.imgPreviewObra);
        btnSubirImg = view.findViewById(R.id.btnSubirImg);
        btnSubirObra = view.findViewById(R.id.btnSubirObra);

        btnSubirImg.setOnClickListener(v ->
                seleccionarImagenObraLauncher.launch("image/*")
        );

        btnSubirObra.setOnClickListener(v ->
                subirObraCompleta()
        );

        return view;
    }

    private void cargarCategorias() {

        CategoriaApi api = RetrofitClient.getClient().create(CategoriaApi.class);

        api.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudieron obtener las categorías", Toast.LENGTH_LONG).show();
                    return;
                }

                listaCategorias = response.body();

                List<String> profesiones = Arrays.asList(
                        "pintor", "escultor", "fotógrafo", "ilustrador",
                        "diseñador gráfico", "diseñador industrial", "diseñador de moda",
                        "caricaturista", "animador", "artesano", "ceramista", "grabador",
                        "artista digital", "artista plástico", "maquetador", "decorador",
                        "restaurador de arte", "graffitero", "modelador 3d"
                );

                // FILTRAR PARA QUE EL SPINNER NO MUESTRE PROFESIONES
                List<CategoriaDTO> filtradas = new ArrayList<>();

                for (CategoriaDTO c : listaCategorias) {
                    String nombre = c.getNombreCategoria().trim().toLowerCase();
                    if (!profesiones.contains(nombre)) {
                        filtradas.add(c);
                    }
                }

                listaCategorias = filtradas;

                List<String> nombres = new ArrayList<>();
                nombres.add("Seleccione una categoría");

                for (CategoriaDTO c : listaCategorias) {
                    nombres.add(c.getNombreCategoria());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_spinner_item,
                        nombres
                );

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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


    private void subirObraCompleta() {
        if (uriImagenObra == null) {
            Toast.makeText(getContext(), "Primero selecciona una imagen para la obra.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: No se encontró ID de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = etMedidas.getText().toString().trim();
        String tecnica = etTecnicas.getText().toString().trim();

        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(getContext(), "Selecciona una categoría válida.", Toast.LENGTH_LONG).show();
            return;
        }

        int categoriaId = listaCategorias.get(pos - 1).getIdCategoria();

        int radioId = rgOpciones.getCheckedRadioButtonId();

        if (titulo.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || medidas.isEmpty() || radioId == -1 || tecnica.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos.", Toast.LENGTH_LONG).show();
            return;
        }

        Double precioDouble;
        try {
            precioDouble = Double.parseDouble(precioStr);
            if (precioDouble < 0) {
                Toast.makeText(getContext(), "El precio no puede ser negativo.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Formato de precio inválido.", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();

        Toast.makeText(getContext(), "Cargando....", Toast.LENGTH_SHORT).show();

        firebaseRepo.subirImagenSolo(idUsuario, uriImagenObra, new FirebaseImageRepository.ImagenListener() {
            @Override
            public void onSuccess(String imageUrl) {

                ObraDTO nuevaObra = new ObraDTO();
                nuevaObra.setTitulo(titulo);
                nuevaObra.setDescripcion(descripcion);
                nuevaObra.setEstado(estado);
                nuevaObra.setPrecio(precioDouble);
                nuevaObra.setMedidas(medidas);
                nuevaObra.setTecnicas(tecnica);

                nuevaObra.setIdCategoria(categoriaId);
                nuevaObra.setImagen1(imageUrl);
                nuevaObra.setLikes(0);

                insertarObraEnBD(idUsuario, nuevaObra);
            }

            @Override
            public void onError(String mensajeError) {
                Toast.makeText(getContext(), "Error al subir imagen: " + mensajeError, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertarObraEnBD(int idUsuario, ObraDTO obra) {
        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Call<ObraDTO> call = api.subirObra(idUsuario, obra);

        call.enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "¡Obra subida con éxito! ID: " + response.body().getIdObra(), Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error al insertar obra. Código " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}