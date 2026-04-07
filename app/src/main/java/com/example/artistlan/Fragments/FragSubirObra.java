package com.example.artistlan.Fragments;

import android.app.AlertDialog;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSubirObra extends Fragment implements View.OnClickListener {

    public static final String ARG_MODO_EDICION = "modo_edicion";
    public static final String ARG_OBRA_ID = "obra_id";

    private Button btnSubirImg;
    private Button btnSubirObra;
    private ImageButton btnRegresar;
    private ImageView imgPreviewObra;

    private Uri uriImagenObra;
    private FirebaseImageRepository firebaseRepo;
    private ActivityResultLauncher<String> seleccionarImagenObraLauncher;
    private ActivityResultLauncher<Void> tomarFotoLauncher;
    private ActivityResultLauncher<String> permisoCamaraLauncher;

    private android.widget.EditText etTituloObra, etDescripcion, etPrecio, etMedidaAncho, etMedidaAlto, etTecnicas;
    private CheckBox cbAutoriaObra;
    private android.widget.RadioGroup rgOpciones;
    private Spinner spinnerCategoria;
    private List<CategoriaDTO> listaCategorias = new ArrayList<>();
    private boolean modoEdicion = false;
    private int idObraEditar = -1;
    private ObraDTO obraActual;
    private String imagenActualUrl;
    private Integer categoriaPendienteId;
    private String categoriaPendienteNombre;
    private TextView txtTituloPantalla;
    private TextView txtDescripcionPantalla;
    private TextView txtPrecio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            modoEdicion = args.getBoolean(ARG_MODO_EDICION, false);
            idObraEditar = args.getInt(ARG_OBRA_ID, -1);
        }

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
        tomarFotoLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.TakePicturePreview(),
                        bitmap -> {
                            if (bitmap != null) {
                                Uri imagenCameraUri = guardarBitmapEnCache(bitmap);
                                if (imagenCameraUri == null) {
                                    Toast.makeText(getContext(), "No se pudo procesar la foto tomada.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                uriImagenObra = imagenCameraUri;
                                if (imgPreviewObra != null) {
                                    Glide.with(this).load(uriImagenObra).into(imgPreviewObra);
                                }
                            }
                        }
                );

        permisoCamaraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                tomarFotoLauncher.launch(null);
                            } else if (isAdded()) {
                                Toast.makeText(getContext(), "Debes conceder permiso de cámara para tomar fotos.", Toast.LENGTH_LONG).show();
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
        etMedidaAncho = view.findViewById(R.id.medidaAncho);
        etMedidaAlto = view.findViewById(R.id.medidaAlto);
        etTecnicas    = view.findViewById(R.id.edit_text_tecnica);
        cbAutoriaObra = view.findViewById(R.id.checkAutoriaObra);
        txtTituloPantalla = view.findViewById(R.id.IsTxtTitulo);
        txtDescripcionPantalla = view.findViewById(R.id.IsTxtDesc);

        spinnerCategoria = view.findViewById(R.id.categoria);
        cargarCategorias();

        imgPreviewObra = view.findViewById(R.id.imgPreviewObra);
        btnSubirImg = view.findViewById(R.id.btnSubirImg);
        btnSubirObra = view.findViewById(R.id.btnSubirObra);

        btnSubirImg.setOnClickListener(v -> mostrarOpcionesImagen());

        btnSubirObra.setOnClickListener(v ->
                validarYMostrarDialogoObra()
        );


        txtPrecio = view.findViewById(R.id.IsTxtPrecio);

        txtPrecio.setVisibility(View.GONE);
        etPrecio.setVisibility(View.GONE);
        configurarModoPantalla();
        if (modoEdicion) {
            cargarObraParaEditar();
        }

        rgOpciones.setOnCheckedChangeListener((group, checkedId) ->
                actualizarBloquePrecioSegunEstado(checkedId)
        );

        return view;
    }

    private void mostrarOpcionesImagen() {
        if (!isAdded()) return;

        String[] opciones = {"Elegir de galería", "Tomar foto con cámara"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        seleccionarImagenObraLauncher.launch("image/*");
                    } else if (which == 1) {
                        abrirCamaraConPermiso();
                    }
                })
                .show();
    }

    private void abrirCamaraConPermiso() {
        if (!isAdded()) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            tomarFotoLauncher.launch(null);
            return;
        }

        permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
    }

    @Nullable
    private Uri guardarBitmapEnCache(@NonNull Bitmap bitmap) {
        if (getContext() == null) return null;

        File picturesDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "obras");
        if (!picturesDir.exists() && !picturesDir.mkdirs()) {
            return null;
        }

        File imageFile = new File(picturesDir, "obra_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            return FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    imageFile
            );
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
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

                List<CategoriaDTO> filtradas = new ArrayList<>();

                for (CategoriaDTO c : listaCategorias) {
                    int id = c.getIdCategoria();
                    if (id >= 1 && id <= 18) {
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
                seleccionarCategoriaPendiente();
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void configurarModoPantalla() {
        if (!modoEdicion) {
            return;
        }
        txtTituloPantalla.setText("Editar Obra");
        txtDescripcionPantalla.setText("Actualiza la información de tu obra. El precio solo se puede asignar una vez al pasar de exhibición a venta.");
        btnSubirImg.setText("CAMBIAR IMAGEN");
        btnSubirObra.setText("GUARDAR CAMBIOS");
    }

    private void cargarObraParaEditar() {
        int idUsuario = obtenerIdUsuarioLogueado();
        if (idUsuario <= 0 || idObraEditar <= 0) {
            Toast.makeText(getContext(), "No se pudo cargar la obra.", Toast.LENGTH_LONG).show();
            return;
        }

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        api.obtenerObraPorId(idObraEditar, idUsuario).enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudo cargar la obra.", Toast.LENGTH_LONG).show();
                    return;
                }

                obraActual = response.body();
                precargarObra(obraActual);
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red al cargar la obra.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void precargarObra(ObraDTO obra) {
        etTituloObra.setText(obra.getTitulo());
        etDescripcion.setText(obra.getDescripcion());
        cargarCamposDeMedidas(obra.getMedidas());
        etTecnicas.setText(obra.getTecnicas());

        String estado = obra.getEstado();
        String estadoNormalizado = estado != null ? estado.trim().toLowerCase().replace("_", " ") : "";
        if (obra.getPrecio() != null) {
            etPrecio.setText(String.valueOf(obra.getPrecio()));
        } else {
            etPrecio.setText("");
        }

        if (estadoNormalizado.contains("venta")) {
            rgOpciones.check(R.id.rbdventa);
            actualizarBloquePrecioSegunEstado(R.id.rbdventa);
        } else {
            rgOpciones.check(R.id.rbexhibicion);
            actualizarBloquePrecioSegunEstado(R.id.rbexhibicion);
        }

        imagenActualUrl = obra.getImagen1();
        if (imagenActualUrl != null && !imagenActualUrl.isEmpty()) {
            Glide.with(this)
                    .load(imagenActualUrl)
                    .placeholder(R.drawable.imagensubirobra)
                    .into(imgPreviewObra);
        }

        categoriaPendienteId = obra.getIdCategoria();
        categoriaPendienteNombre = obra.getNombreCategoria();
        seleccionarCategoriaPendiente();
    }

    private void seleccionarCategoriaPendiente() {
        if (listaCategorias.isEmpty()) {
            return;
        }

        for (int i = 0; i < listaCategorias.size(); i++) {
            CategoriaDTO categoria = listaCategorias.get(i);
            boolean coincideId = categoriaPendienteId != null && categoriaPendienteId.equals(categoria.getIdCategoria());
            boolean coincideNombre = categoriaPendienteNombre != null
                    && categoria.getNombreCategoria() != null
                    && categoria.getNombreCategoria().equalsIgnoreCase(categoriaPendienteNombre);
            if (coincideId || coincideNombre) {
                spinnerCategoria.setSelection(i + 1);
                categoriaPendienteId = null;
                categoriaPendienteNombre = null;
                return;
            }
        }
    }

    private void cargarCamposDeMedidas(String medidas) {
        if (medidas == null) {
            etMedidaAncho.setText("");
            etMedidaAlto.setText("");
            return;
        }

        String limpio = medidas.toLowerCase().replace("cm", "").trim();
        String[] partes = limpio.split("[xX]");
        if (partes.length >= 2) {
            etMedidaAncho.setText(partes[0].trim());
            etMedidaAlto.setText(partes[1].trim());
            return;
        }

        etMedidaAncho.setText(limpio);
        etMedidaAlto.setText("");
    }

    private String construirMedidas() {
        String ancho = normalizarCampoMedida(etMedidaAncho.getText().toString());
        String alto = normalizarCampoMedida(etMedidaAlto.getText().toString());
        if (ancho.isEmpty() || alto.isEmpty()) {
            return "";
        }
        return ancho + " x " + alto + " cm";
    }

    private String normalizarCampoMedida(String valor) {
        if (valor == null) return "";
        return valor.replace("cm", "").trim();
    }

    private CategoriaDTO obtenerCategoriaSeleccionada() {
        int pos = spinnerCategoria.getSelectedItemPosition();
        if (pos > 0 && pos <= listaCategorias.size()) {
            return listaCategorias.get(pos - 1);
        }

        if (modoEdicion && obraActual != null && obraActual.getIdCategoria() != null) {
            CategoriaDTO categoria = new CategoriaDTO();
            categoria.setIdCategoria(obraActual.getIdCategoria());
            categoria.setNombreCategoria(obraActual.getNombreCategoria());
            return categoria;
        }

        return null;
    }

    private int obtenerIdUsuarioLogueado() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("idUsuario", prefs.getInt("id", -1));
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

        View menuInferior = requireActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        btnRegresar = view.findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(this);

        new BotonesMenuSuperior(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() == null) return;
        View menuInferior = getActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRegresar) {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }


    private void validarYMostrarDialogoObra() {

        if (uriImagenObra == null && (!modoEdicion || imagenActualUrl == null || imagenActualUrl.trim().isEmpty())) {
            Toast.makeText(getContext(), "Selecciona una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!modoEdicion && (cbAutoriaObra == null || !cbAutoriaObra.isChecked())) {
            Toast.makeText(getContext(), "Debes confirmar la autoria de la obra.", Toast.LENGTH_LONG).show();
            return;
        }

        int idUsuario = obtenerIdUsuarioLogueado();
        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = construirMedidas();
        String tecnica = etTecnicas.getText().toString().trim();

        int radioId = rgOpciones.getCheckedRadioButtonId();
        if (titulo.isEmpty() || descripcion.isEmpty() || tecnica.isEmpty() || medidas.isEmpty() || radioId == -1) {
            Toast.makeText(getContext(), "Completa los campos obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        CategoriaDTO categoria = obtenerCategoriaSeleccionada();
        if (categoria == null) {
            Toast.makeText(getContext(), "Selecciona una categoria.", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();
        String estadoNormalizado = estado.trim().toLowerCase().replace("_", " ");
        boolean esVenta = estadoNormalizado.contains("venta");

        Double precio = modoEdicion && obraActual != null ? obraActual.getPrecio() : null;
        boolean puedeAsignarPrecioEnEdicion = puedeAsignarPrecioPrimeraVezEnEdicion();
        if (esVenta && (!modoEdicion || puedeAsignarPrecioEnEdicion)) {
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
                Toast.makeText(getContext(), "Precio invalido.", Toast.LENGTH_LONG).show();
                return;
            }
        }

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
                "Título:\n" + titulo + "\n\n" +
                        "Descripción:\n" + descripcion + "\n\n" +
                        "Estado:\n" + estado + "\n\n" +
                        "Técnica:\n" + tecnica + "\n\n" +
                        (precio != null
                                ? "Precio:\n$" + precio + "\n\n"
                                : ""
                        ) +
                        (!medidas.isEmpty()
                                ? "Medidas:\n" + medidas + "\n\n"
                                : ""
                        ) +
                        "Categoría:\n" + categoria.getNombreCategoria();

        txtResumen.setText(resumen);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnEditar.setOnClickListener(v -> dialog.dismiss());

        btnPublicar.setOnClickListener(v -> {
            btnPublicar.setEnabled(false);
            btnPublicar.setText(modoEdicion ? "Guardando..." : "Publicando...");

            dialog.dismiss();

            guardarObra();
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
        String medidas = construirMedidas();
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
                Toast.makeText(getContext(), "Formato de precio invÃ¡lido.", Toast.LENGTH_LONG).show();
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

    private void guardarObra() {
        int idUsuario = obtenerIdUsuarioLogueado();
        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error: No se encontró ID de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        if (uriImagenObra != null) {
            Toast.makeText(getContext(), "Cargando....", Toast.LENGTH_SHORT).show();
            firebaseRepo.subirImagenSolo(idUsuario, uriImagenObra, new FirebaseImageRepository.ImagenListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    persistirObra(idUsuario, imageUrl);
                }

                @Override
                public void onError(String mensajeError) {
                    Toast.makeText(getContext(), "Error al subir imagen: " + mensajeError, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        persistirObra(idUsuario, imagenActualUrl);
    }

    private void persistirObra(int idUsuario, String imageUrl) {
        String titulo = etTituloObra.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String medidas = construirMedidas();
        String tecnica = etTecnicas.getText().toString().trim();
        int radioId = rgOpciones.getCheckedRadioButtonId();

        CategoriaDTO categoria = obtenerCategoriaSeleccionada();
        if (categoria == null || categoria.getIdCategoria() <= 0) {
            Toast.makeText(getContext(), "Selecciona una categoria valida.", Toast.LENGTH_LONG).show();
            return;
        }
        if (titulo.isEmpty() || descripcion.isEmpty() || medidas.isEmpty() || radioId == -1 || tecnica.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        RadioButton rb = rgOpciones.findViewById(radioId);
        String estado = rb.getText().toString();
        String estadoNormalizado = estado.trim().toLowerCase().replace("_", " ");
        boolean esVenta = estadoNormalizado.contains("venta");

        Double precioDouble = null;
        boolean puedeAsignarPrecioEnEdicion = puedeAsignarPrecioPrimeraVezEnEdicion();
        if (!modoEdicion || puedeAsignarPrecioEnEdicion) {
            if (esVenta && precioStr.isEmpty()) {
                Toast.makeText(getContext(), "Debes ingresar un precio para obras en venta.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!precioStr.isEmpty()) {
                try {
                    precioDouble = Double.parseDouble(precioStr);
                    if (precioDouble < 0) {
                        Toast.makeText(getContext(), "El precio no puede ser negativo.", Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Formato de precio invalido.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        Integer categoriaIdParaEnviar = null;
        if (!modoEdicion) {
            categoriaIdParaEnviar = categoria.getIdCategoria();
        } else if (obraActual == null || obraActual.getIdCategoria() == null
                || !obraActual.getIdCategoria().equals(categoria.getIdCategoria())) {
            categoriaIdParaEnviar = categoria.getIdCategoria();
        }

        ObraDTO obra = new ObraDTO();
        obra.setTitulo(titulo);
        obra.setDescripcion(descripcion);
        obra.setEstado(estado);
        obra.setTecnicas(tecnica);
        if (!modoEdicion || puedeAsignarPrecioEnEdicion) {
            obra.setPrecio(precioDouble);
        }
        obra.setMedidas(medidas);
        if (categoriaIdParaEnviar != null) {
            obra.setIdCategoria(categoriaIdParaEnviar);
        }
        obra.setImagen1(imageUrl);
        obra.setIdUsuario(idUsuario);

        if (obraActual != null) {
            obra.setImagen2(obraActual.getImagen2());
            obra.setImagen3(obraActual.getImagen3());
            obra.setLikes(obraActual.getLikes() != null ? obraActual.getLikes() : 0);
        } else {
            obra.setLikes(0);
        }

        if (modoEdicion) {
            actualizarObraEnBD(idUsuario, obra);
        } else {
            insertarObraEnBD(idUsuario, obra);
        }
    }

    private void actualizarBloquePrecioSegunEstado(int checkedId) {
        boolean esVenta = checkedId == R.id.rbdventa;
        if (esVenta) {
            txtPrecio.setVisibility(View.VISIBLE);
            etPrecio.setVisibility(View.VISIBLE);

            boolean editable = !modoEdicion || puedeAsignarPrecioPrimeraVezEnEdicion();
            etPrecio.setEnabled(editable);
            etPrecio.setFocusable(editable);
            etPrecio.setFocusableInTouchMode(editable);
            aplicarIndicadorBloqueoPrecio(!editable);

            etPrecio.animate().alpha(1f).setDuration(200);
            txtPrecio.animate().alpha(1f).setDuration(200);
            return;
        }

        txtPrecio.setVisibility(View.GONE);
        etPrecio.setVisibility(View.GONE);
        aplicarIndicadorBloqueoPrecio(false);
        if (!modoEdicion) {
            etPrecio.setText("");
        }
        etPrecio.animate().alpha(0f).setDuration(200);
        txtPrecio.animate().alpha(0f).setDuration(200);
    }

    private boolean puedeAsignarPrecioPrimeraVezEnEdicion() {
        if (!modoEdicion || obraActual == null) {
            return false;
        }
        Double precioActual = obraActual.getPrecio();
        return precioActual == null || precioActual <= 0d;
    }

    private void aplicarIndicadorBloqueoPrecio(boolean bloqueado) {
        if (txtPrecio == null || etPrecio == null) {
            return;
        }
        txtPrecio.setText(bloqueado
                ? "Precio en pesos mxn (bloqueado)"
                : "Precio en pesos mxn");
        etPrecio.setAlpha(bloqueado ? 0.65f : 1f);
    }

    private void insertarObraEnBD(int idUsuario, ObraDTO obra) {
        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        Call<ObraDTO> call = api.subirObra(idUsuario, obra);

        call.enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Obra subida con exito", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(FragSubirObra.this).popBackStack();
                } else {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(getContext(),
                            backendMessage != null ? backendMessage : "Error al insertar obra. Codigo " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }

    private void actualizarObraEnBD(int idUsuario, ObraDTO obra) {
        if (idObraEditar <= 0) {
            Toast.makeText(getContext(), "No se pudo actualizar la obra.", Toast.LENGTH_LONG).show();
            return;
        }

        ObraApi api = RetrofitClient.getClient().create(ObraApi.class);
        api.actualizarObraDeUsuario(idUsuario, idObraEditar, obra).enqueue(new Callback<ObraDTO>() {
            @Override
            public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Obra actualizada con exito", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(FragSubirObra.this).popBackStack();
                } else {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(getContext(),
                            backendMessage != null ? backendMessage : "Error al actualizar obra. Codigo " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}




