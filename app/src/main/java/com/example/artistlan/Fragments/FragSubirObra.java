package com.example.artistlan.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
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
        cargarCategorias();

        imgPreviewObra = view.findViewById(R.id.imgPreviewObra);
        btnSubirImg = view.findViewById(R.id.btnSubirImg);
        btnSubirObra = view.findViewById(R.id.btnSubirObra);

        btnSubirImg.setOnClickListener(v ->
                seleccionarImagenObraLauncher.launch("image/*")
        );

        btnSubirObra.setOnClickListener(v ->
                validarYMostrarDialogoObra()
        );


        TextView txtPrecio = view.findViewById(R.id.IsTxtPrecio);

        txtPrecio.setVisibility(View.GONE);
        etPrecio.setVisibility(View.GONE);

        rgOpciones.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbdventa) {
                // En venta → mostrar precio
                txtPrecio.setVisibility(View.VISIBLE);
                etPrecio.setVisibility(View.VISIBLE);
                etPrecio.setEnabled(true);

                etPrecio.animate().alpha(1f).setDuration(200);
                txtPrecio.animate().alpha(1f).setDuration(200);

            } else if (checkedId == R.id.rbexhibicion) {
                // En exhibición → ocultar precio
                txtPrecio.setVisibility(View.GONE);
                etPrecio.setVisibility(View.GONE);
                etPrecio.setText("");

                etPrecio.animate().alpha(0f).setDuration(200);
                txtPrecio.animate().alpha(0f).setDuration(200);
            }
        });

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

        ScrollView scrollView = view.findViewById(R.id.fragScrollSubirObra);

        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeHeight
            );
            return insets;
        });

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


    private void validarYMostrarDialogoObra() {

        if (uriImagenObra == null) {
            Toast.makeText(getContext(), "Selecciona una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = etMedidas.getText().toString().trim();
        String tecnica = etTecnicas.getText().toString().trim();

        int radioId = rgOpciones.getCheckedRadioButtonId();
        if (titulo.isEmpty() || descripcion.isEmpty() || tecnica.isEmpty()
                || medidas.isEmpty() || radioId == -1) {
            Toast.makeText(getContext(), "Completa los campos obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(getContext(), "Selecciona una categoría.", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();

        Double precio = null;

        if (estado.equalsIgnoreCase("En venta")) {
            if (precioStr.isEmpty()) {
                Toast.makeText(getContext(), "Debes ingresar un precio para obras en venta.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                precio = Double.parseDouble(precioStr);
                if (precio < 0) {
                    Toast.makeText(getContext(), "El precio no puede ser negativo.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Precio inválido.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            precio = null;
        }



        CategoriaDTO categoria = listaCategorias.get(pos - 1);

        mostrarDialogConfirmacionObra(
                idUsuario, titulo, descripcion, estado,
                tecnica, medidas, precio, categoria
        );
    }

    private void mostrarDialogConfirmacionObra(
            int idUsuario,
            String titulo,
            String descripcion,
            String estado,
            String tecnica,
            String medidas,
            Double precio,
            CategoriaDTO categoria
    ) {

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_confirmar_obra, null);

        TextView txtResumen = view.findViewById(R.id.txtResumenObra);
        Button btnEditar = view.findViewById(R.id.btnEditar);
        Button btnPublicar = view.findViewById(R.id.btnConfirmarPublicar);

        String resumen =
                "🖼 Título:\n" + titulo + "\n\n" +
                        "📝 Descripción:\n" + descripcion + "\n\n" +
                        "📌 Estado:\n" + estado + "\n\n" +
                        "🎨 Técnica:\n" + tecnica + "\n\n" +
                        (precio != null
                                ? "💰 Precio:\n$" + precio + "\n\n"
                                : ""
                        ) +
                        (!medidas.isEmpty()
                                ? "📐 Medidas:\n" + medidas + " cm " + "\n\n"
                                : ""
                        ) +
                        "🏷 Categoría:\n" + categoria.getNombreCategoria();

        txtResumen.setText(resumen);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnEditar.setOnClickListener(v -> dialog.dismiss());

        btnPublicar.setOnClickListener(v -> {
            btnPublicar.setEnabled(false);
            btnPublicar.setText("Publicando...");

            dialog.dismiss();

            subirObraCompleta();
        });

        dialog.show();
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

        if (titulo.isEmpty() || descripcion.isEmpty()  || radioId == -1 || tecnica.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        Double precioDouble = null;

        if (!precioStr.isEmpty()) {
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
        }


        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();

        Toast.makeText(getContext(), "Cargando....", Toast.LENGTH_SHORT).show();

        final Double precioFinal = precioDouble;
        firebaseRepo.subirImagenSolo(idUsuario, uriImagenObra, new FirebaseImageRepository.ImagenListener() {

            @Override
            public void onSuccess(String imageUrl) {

                ObraDTO nuevaObra = new ObraDTO();
                nuevaObra.setTitulo(titulo);
                nuevaObra.setDescripcion(descripcion);
                nuevaObra.setEstado(estado);
                nuevaObra.setTecnicas(tecnica);

                if (precioFinal != null) {
                    nuevaObra.setPrecio(precioFinal);
                }

                nuevaObra.setMedidas(medidas);
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
                    Toast.makeText(getContext(), "¡Obra subida con éxito! " , Toast.LENGTH_LONG).show();
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