package com.example.artistlan.Activitys;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActActualizarDatos extends AppCompatActivity implements View.OnClickListener {

    private Button btnActualizarDatos, btnEliminarCuenta;
    private ImageButton IsbtnRegresar;
    private EditText etCorreo, etNombre, etDescripcion, etRedes, etTelefono, etFecha, etUsuario;
    private ImageView btnCambiarFoto, imgFotoPerfil;
    private Spinner spinnerCategoriaUsuario;
    private String contrasenaOriginal;

    private UsuarioApi api;
    private List<CategoriaDTO> listaCategorias;

    private ActivityResultLauncher<String> seleccionarImagenperfilLauncher;
    private ActivityResultLauncher<Void> tomarFotoPerfilLauncher;
    private ActivityResultLauncher<String> permisoCamaraPerfilLauncher;
    private Uri imageUri = null;

    // Theme
    private ThemeManager themeManager;
    private View rootMain, topDivider, cardDivider, cardContainer;
    private TextView txtTitulo, txtDesc, txtIndicacion, tvCorreo, tvUsuario, tvFotoPerfil,
            tvNombre, tvDescripcion, tvCategoria, tvRedes, tvTelefono, tvFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_actualizar_datos);

        themeManager = new ThemeManager(this);

        // Theme refs
        rootMain = findViewById(R.id.main);
        topDivider = findViewById(R.id.IsTopDivider);
        cardDivider = findViewById(R.id.IsCardDivider);
        cardContainer = findViewById(R.id.IsLayCard);

        txtTitulo = findViewById(R.id.IsTxtTitulo);
        txtDesc = findViewById(R.id.IsTxtDesc);
        txtIndicacion = findViewById(R.id.IsTxtindicacion);
        tvCorreo = findViewById(R.id.tvCorreo);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvFotoPerfil = findViewById(R.id.tvFotoPerfil);
        tvNombre = findViewById(R.id.tvNombre);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvCategoria = findViewById(R.id.lsTxtCategoria);
        tvRedes = findViewById(R.id.tvRedes);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvFecha = findViewById(R.id.tvFecha);

        // Enlazar XML
        etCorreo = findViewById(R.id.correo);
        etNombre = findViewById(R.id.nombre);
        etDescripcion = findViewById(R.id.descripcion);
        etRedes = findViewById(R.id.redes);
        etTelefono = findViewById(R.id.telefono);
        etFecha = findViewById(R.id.CrcEdtFecha);
        etUsuario = findViewById(R.id.usuario);
        spinnerCategoriaUsuario = findViewById(R.id.spinnerCategoriaUsuario);

        btnActualizarDatos = findViewById(R.id.btnActualizarDatos);
        IsbtnRegresar = findViewById(R.id.IsbtnRegresar);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        applyThemeOnlyColors();

        btnEliminarCuenta.setOnClickListener(this);
        btnActualizarDatos.setOnClickListener(this);
        IsbtnRegresar.setOnClickListener(this);

        api = RetrofitClient.getClient().create(UsuarioApi.class);

        etCorreo.setEnabled(false);
        etUsuario.setEnabled(false);

        etFecha.setOnClickListener(v -> mostrarDatePicker());

        seleccionarImagenperfilLauncher =
                registerForActivityResult(
                        new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                imageUri = uri;
                                imgFotoPerfil.setImageURI(uri);
                            }
                        }
                );

        tomarFotoPerfilLauncher =
                registerForActivityResult(
                        new androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview(),
                        bitmap -> {
                            if (bitmap != null) {
                                Uri cameraUri = guardarBitmapPerfilEnCache(bitmap);
                                if (cameraUri == null) {
                                    Toast.makeText(this, "No se pudo procesar la foto tomada", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                imageUri = cameraUri;
                                Glide.with(this).load(imageUri).centerCrop().into(imgFotoPerfil);
                            }
                        }
                );

        permisoCamaraPerfilLauncher =
                registerForActivityResult(
                        new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                tomarFotoPerfilLauncher.launch(null);
                            } else {
                                Toast.makeText(this, "Debes conceder permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show();
                            }
                        }
                );

        btnCambiarFoto.setOnClickListener(v -> mostrarOpcionesFotoPerfil());

        cargarDatosUsuario();
        cargarCategoriasDesdeApi();
    }

    private void applyThemeOnlyColors() {
        ThemeApplier.applySystemBars(this, themeManager);

        if (rootMain != null) {
            rootMain.setBackgroundColor(themeManager.color(ThemeKeys.BG_BOTTOM));
        }

        if (cardContainer != null && cardContainer.getBackground() != null) {
            cardContainer.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                    PorterDuff.Mode.SRC_ATOP
            );
            cardContainer.setAlpha(0.96f);
        }

        ThemeApplier.applyTextPrimary(txtTitulo, themeManager);
        ThemeApplier.applyTextSecondary(txtDesc, themeManager);
        ThemeApplier.applyTextSecondary(txtIndicacion, themeManager);

        ThemeApplier.applyTextPrimary(tvCorreo, themeManager);
        ThemeApplier.applyTextPrimary(tvUsuario, themeManager);
        ThemeApplier.applyTextPrimary(tvFotoPerfil, themeManager);
        ThemeApplier.applyTextPrimary(tvNombre, themeManager);
        ThemeApplier.applyTextPrimary(tvDescripcion, themeManager);
        ThemeApplier.applyTextPrimary(tvCategoria, themeManager);
        ThemeApplier.applyTextPrimary(tvRedes, themeManager);
        ThemeApplier.applyTextPrimary(tvTelefono, themeManager);
        ThemeApplier.applyTextPrimary(tvFecha, themeManager);

        ThemeApplier.applyInput(etCorreo, themeManager);
        ThemeApplier.applyInput(etUsuario, themeManager);
        ThemeApplier.applyInput(etNombre, themeManager);
        ThemeApplier.applyInput(etDescripcion, themeManager);
        ThemeApplier.applyInput(etRedes, themeManager);
        ThemeApplier.applyInput(etTelefono, themeManager);
        ThemeApplier.applyInput(etFecha, themeManager);

        if (spinnerCategoriaUsuario != null && spinnerCategoriaUsuario.getBackground() != null) {
            spinnerCategoriaUsuario.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.INPUT_BG),
                    PorterDuff.Mode.SRC_ATOP
            );
        }

        ThemeApplier.applyPrimaryButton(btnActualizarDatos, themeManager);
        ThemeApplier.applySecondaryButton(btnEliminarCuenta, themeManager);

        if (IsbtnRegresar != null) {
            IsbtnRegresar.setColorFilter(themeManager.color(ThemeKeys.ICON_ACTIVE), PorterDuff.Mode.SRC_ATOP);
        }

        if (btnCambiarFoto != null) {
            btnCambiarFoto.setColorFilter(themeManager.color(ThemeKeys.ICON_ACTIVE), PorterDuff.Mode.SRC_ATOP);
        }

        if (topDivider != null && topDivider.getBackground() != null) {
            topDivider.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCOUNT_DIVIDER), PorterDuff.Mode.SRC_ATOP);
        }

        if (cardDivider != null && cardDivider.getBackground() != null) {
            cardDivider.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCOUNT_DIVIDER), PorterDuff.Mode.SRC_ATOP);
        }

        ThemeEffectsApplier.applyPanelGlass(cardContainer, themeManager);
    }

    private void mostrarOpcionesFotoPerfil() {
        String[] opciones = {"Elegir de galería", "Tomar foto con cámara"};

        new AlertDialog.Builder(this)
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        seleccionarImagenperfilLauncher.launch("image/*");
                    } else if (which == 1) {
                        abrirCamaraPerfilConPermiso();
                    }
                })
                .show();
    }

    private void abrirCamaraPerfilConPermiso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            tomarFotoPerfilLauncher.launch(null);
            return;
        }
        permisoCamaraPerfilLauncher.launch(Manifest.permission.CAMERA);
    }

    private Uri guardarBitmapPerfilEnCache(Bitmap bitmap) {
        File picturesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "perfil");
        if (!picturesDir.exists() && !picturesDir.mkdirs()) {
            return null;
        }

        File imageFile = new File(picturesDir, "perfil_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                anio, mes, dia
        );
        datePickerDialog.show();
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);

        etNombre.setText(prefs.getString("nombreCompleto", ""));
        etUsuario.setText(prefs.getString("usuario", ""));
        etCorreo.setText(prefs.getString("correo", ""));
        etDescripcion.setText(prefs.getString("descripcion", ""));
        etRedes.setText(prefs.getString("redes", ""));
        etTelefono.setText(prefs.getString("telefono", ""));
        etFecha.setText(prefs.getString("fechaNac", ""));
        contrasenaOriginal = prefs.getString("contrasena", "");

        String foto = prefs.getString("fotoPerfil", "");
        if (!foto.isEmpty()) {
            Glide.with(this).load(foto).centerCrop().into(imgFotoPerfil);
        } else {
            imgFotoPerfil.setImageResource(R.drawable.fotoperfilprueba);
        }
    }

    private void cargarCategoriasDesdeApi() {
        listaCategorias = new ArrayList<>();
        CategoriaApi apiCategoria = RetrofitClient.getClient().create(CategoriaApi.class);
        apiCategoria.obtenerCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCategorias.clear();
                    List<String> nombresCategorias = new ArrayList<>();
                    nombresCategorias.add("Ninguna");

                    for (CategoriaDTO c : response.body()) {
                        int id = c.getIdCategoria();
                        if (id >= 19 && id <= 37) {
                            listaCategorias.add(c);
                            nombresCategorias.add(c.getNombreCategoria());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ActActualizarDatos.this,
                            android.R.layout.simple_spinner_item,
                            nombresCategorias
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoriaUsuario.setAdapter(adapter);

                    SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                    String categoriaActual = prefs.getString("categoria", "Ninguna");
                    int posicion = nombresCategorias.indexOf(categoriaActual);
                    spinnerCategoriaUsuario.setSelection(posicion >= 0 ? posicion : 0);
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this, "Error al cargar categorías: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.IsbtnRegresar) {
            finish();
        } else if (v.getId() == R.id.btnActualizarDatos) {
            actualizarUsuario();
        } else if (v.getId() == R.id.btnEliminarCuenta) {
            mostrarDialogoEliminarCuenta();
        }
    }

    private void mostrarDialogoEliminarCuenta() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción es permanente y eliminará tu cuenta y toda tu información. ¿Deseas continuar?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    dialog.dismiss();

                    View view = LayoutInflater.from(this).inflate(R.layout.dialog_ingresar_password, null);
                    EditText etPassword = view.findViewById(R.id.etPasswordDialog);

                    AlertDialog dialogPassword = new AlertDialog.Builder(this)
                            .setTitle("Confirma tu contraseña")
                            .setView(view)
                            .setCancelable(false)
                            .setPositiveButton("Eliminar", null)
                            .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                            .create();

                    dialogPassword.setOnShowListener(d -> {
                        Button btnEliminar = dialogPassword.getButton(AlertDialog.BUTTON_POSITIVE);
                        btnEliminar.setOnClickListener(v2 -> {
                            String passwordIngresada = etPassword.getText().toString().trim();
                            if (passwordIngresada.isEmpty()) {
                                etPassword.setError("Ingresa tu contraseña");
                                etPassword.requestFocus();
                                return;
                            }

                            SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                            String passwordActual = prefs.getString("contrasena", "");

                            if (passwordIngresada.equals(passwordActual)) {
                                dialogPassword.dismiss();
                                eliminarCuentaConApi();
                            } else {
                                etPassword.setError("Contraseña incorrecta");
                                etPassword.requestFocus();
                            }
                        });
                    });

                    dialogPassword.show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuentaConApi() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        api.eliminarUsuario(idUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    prefs.edit().clear().apply();

                    Toast.makeText(ActActualizarDatos.this,
                            "Cuenta eliminada correctamente",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ActActualizarDatos.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(ActActualizarDatos.this,
                            "Error al eliminar la cuenta (Código: " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarUsuario() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("id", -1);
        if (idUsuario == -1) {
            Toast.makeText(this, "Error: No se encontró sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String fechaNac = etFecha.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.requestFocus();
            Toast.makeText(this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fechaNac.isEmpty()) {
            Toast.makeText(this, "Por favor elige tu fecha de nacimiento.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            subirFotoPerfil(idUsuario, prefs, () -> enviarActualizacionUsuario(idUsuario, prefs));
        } else {
            enviarActualizacionUsuario(idUsuario, prefs);
        }
    }

    private void subirFotoPerfil(int idUsuario, SharedPreferences prefs, Runnable onSuccess) {
        FirebaseImageRepository repo = new FirebaseImageRepository();
        repo.subirFotoPerfilYGuardarEnBD(idUsuario, imageUri, new FirebaseImageRepository.ImagenListener() {
            @Override
            public void onSuccess(String urlNueva) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fotoPerfil", urlNueva);
                editor.apply();
                onSuccess.run();
            }

            @Override
            public void onError(String mensaje) {
                Toast.makeText(ActActualizarDatos.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enviarActualizacionUsuario(int idUsuario, SharedPreferences prefs) {
        UsuariosDTO usuarioActualizado = new UsuariosDTO();

        int posicionSeleccionada = spinnerCategoriaUsuario.getSelectedItemPosition();
        Integer idCategoria = null;

        if (posicionSeleccionada > 0) {
            idCategoria = listaCategorias.get(posicionSeleccionada - 1).getIdCategoria();
        }
        usuarioActualizado.setIdCategoria(idCategoria);

        usuarioActualizado.setNombreCompleto(etNombre.getText().toString().trim());
        usuarioActualizado.setCorreo(etCorreo.getText().toString().trim());
        usuarioActualizado.setUsuario(etUsuario.getText().toString().trim());
        usuarioActualizado.setDescripcion(etDescripcion.getText().toString().trim());
        usuarioActualizado.setRedesSociales(etRedes.getText().toString().trim());
        usuarioActualizado.setTelefono(etTelefono.getText().toString().trim());
        usuarioActualizado.setFechaNacimiento(etFecha.getText().toString().trim());
        usuarioActualizado.setFotoPerfil(prefs.getString("fotoPerfil", ""));
        usuarioActualizado.setContrasena(contrasenaOriginal);

        api.actualizarUsuario(idUsuario, usuarioActualizado).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nombreCompleto", usuarioActualizado.getNombreCompleto());
                    editor.putString("descripcion", usuarioActualizado.getDescripcion());
                    editor.putString("redes", usuarioActualizado.getRedesSociales());
                    editor.putString("telefono", usuarioActualizado.getTelefono());
                    editor.putString("fechaNac", usuarioActualizado.getFechaNacimiento());
                    editor.putString("categoria", spinnerCategoriaUsuario.getSelectedItem().toString());
                    editor.apply();

                    Toast.makeText(ActActualizarDatos.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ActActualizarDatos.this,
                            "Error al actualizar (Código: " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeManager = new ThemeManager(this);
        applyThemeOnlyColors();
    }
}