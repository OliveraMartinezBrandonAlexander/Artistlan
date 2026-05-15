package com.example.artistlan.Activitys;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.DesactivarCuentaRequestDTO;
import com.example.artistlan.Conector.model.RespuestaModeracionDTO;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActActualizarDatos extends AppCompatActivity implements View.OnClickListener {

    private Button btnActualizarDatos, btnEliminarCuenta;
    private ImageButton IsbtnRegresar;
    private EditText etCorreo, etNombre, etDescripcion, etRedes, etTelefono, etFecha, etUsuario, etUbicacion;
    private ImageView btnCambiarFoto, imgFotoPerfil;
    private Spinner spinnerCategoriaUsuario;

    private UsuarioApi api;
    private List<CategoriaDTO> listaCategorias;
    private SessionManager sessionManager;

    private ActivityResultLauncher<String> seleccionarImagenperfilLauncher;
    private ActivityResultLauncher<Void> tomarFotoPerfilLauncher;
    private ActivityResultLauncher<String> permisoCamaraPerfilLauncher;
    private Uri imageUri = null;

    // Theme
    private ThemeManager themeManager;
    private View rootMain, topDivider, cardDivider, cardContainer;
    private TextView txtTitulo, txtDesc, txtIndicacion, tvCorreo, tvUsuario, tvFotoPerfil,
            tvNombre, tvDescripcion, tvCategoria, tvRedes, tvTelefono, tvFecha, tvUbicacion;

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
        tvUbicacion = findViewById(R.id.tvUbicacion);

        // Enlazar XML
        etCorreo = findViewById(R.id.correo);
        etNombre = findViewById(R.id.nombre);
        etDescripcion = findViewById(R.id.descripcion);
        etRedes = findViewById(R.id.redes);
        etTelefono = findViewById(R.id.telefono);
        etFecha = findViewById(R.id.CrcEdtFecha);
        etUsuario = findViewById(R.id.usuario);
        etUbicacion = findViewById(R.id.ubicacion);
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
        ScrollView scrollView = findViewById(R.id.scrollActualizarDatos);
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

        api = RetrofitClient.getClient().create(UsuarioApi.class);
        sessionManager = new SessionManager(this);

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
        ThemeApplier.applyTextPrimary(tvUbicacion, themeManager);

        ThemeApplier.applyInput(etCorreo, themeManager);
        ThemeApplier.applyInput(etUsuario, themeManager);
        ThemeApplier.applyInput(etNombre, themeManager);
        ThemeApplier.applyInput(etDescripcion, themeManager);
        ThemeApplier.applyInput(etRedes, themeManager);
        ThemeApplier.applyInput(etTelefono, themeManager);
        ThemeApplier.applyInput(etFecha, themeManager);
        ThemeApplier.applyInput(etUbicacion, themeManager);

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

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(this, "Selecciona una imagen cuadrada para que tu foto se vea correctamente.", Toast.LENGTH_SHORT).show();
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
        Calendar fechaMaxima = obtenerFechaMaximaMayorEdad();
        Calendar inicio = Calendar.getInstance();
        inicio.setTimeInMillis(fechaMaxima.getTimeInMillis());

        int anio = inicio.get(Calendar.YEAR);
        int mes = inicio.get(Calendar.MONTH);
        int dia = inicio.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) ->
                        etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                anio, mes, dia
        );
        datePickerDialog.getDatePicker().setMaxDate(fechaMaxima.getTimeInMillis());
        datePickerDialog.show();
    }

    private Calendar obtenerFechaMaximaMayorEdad() {
        Calendar fechaMaxima = Calendar.getInstance();
        fechaMaxima.set(Calendar.HOUR_OF_DAY, 23);
        fechaMaxima.set(Calendar.MINUTE, 59);
        fechaMaxima.set(Calendar.SECOND, 59);
        fechaMaxima.set(Calendar.MILLISECOND, 999);
        fechaMaxima.add(Calendar.YEAR, -18);
        return fechaMaxima;
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
        etUbicacion.setText(prefs.getString("ubicacion", ""));

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
                    String categoriaActual = prefs.getString("ocupacion", prefs.getString("categoria", "Ninguna"));
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
            mostrarDialogoDesactivarCuentaConContrasena();
        }
    }

    private void mostrarDialogoDesactivarCuentaConContrasena() {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setPadding(dpToPx(24), dpToPx(8), dpToPx(24), 0);

        TextView tvMensaje = new TextView(this);
        tvMensaje.setText("Tu cuenta se desactivará. No se borrará físicamente.\n\nSe conservarán tu historial de compras, ventas y transacciones.\n\nPara confirmar, ingresa tu contraseña actual.");
        tvMensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        contenedor.addView(tvMensaje);

        EditText etContrasenaActual = new EditText(this);
        etContrasenaActual.setHint("Ingresa tu contraseña actual");
        etContrasenaActual.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etContrasenaActual.setSingleLine(true);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.topMargin = dpToPx(16);
        contenedor.addView(etContrasenaActual, inputParams);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Desactivar cuenta")
                .setView(contenedor)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Desactivar cuenta", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnConfirmar = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button btnCancelar = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

            btnConfirmar.setOnClickListener(v -> {
                String contrasenaActual = etContrasenaActual.getText() != null
                        ? etContrasenaActual.getText().toString().trim()
                        : "";

                if (contrasenaActual.isEmpty()) {
                    etContrasenaActual.setError("Ingresa tu contraseña actual.");
                    etContrasenaActual.requestFocus();
                    return;
                }

                etContrasenaActual.setError(null);
                desactivarCuentaConApiV11(contrasenaActual, dialog, etContrasenaActual, btnConfirmar, btnCancelar);
            });
        });

        dialog.show();
    }

    private void mostrarDialogoDesactivarCuenta() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Desactivar cuenta")
                .setMessage("Tu cuenta se desactivará y ya no podrá iniciar sesión ni operar normalmente. Tu historial de compras, ventas, reportes y transacciones se conservará. Esta acción no borra físicamente tu información. ¿Deseas confirmar la desactivación de tu cuenta?")
                .setPositiveButton("Sí, desactivar", (dialog, which) -> {
                    dialog.dismiss();
                    desactivarCuentaConApi();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void desactivarCuentaConApi() {
        SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        DesactivarCuentaRequestDTO request = new DesactivarCuentaRequestDTO();
        request.setIdUsuarioSolicitante(idUsuario);
        request.setMotivo("Cuenta desactivada desde Android por solicitud del usuario");
        request.setConfirmacion(true);

        api.desactivarCuenta(idUsuario, request).enqueue(new Callback<RespuestaModeracionDTO>() {
            @Override
            public void onResponse(Call<RespuestaModeracionDTO> call, Response<RespuestaModeracionDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActActualizarDatos.this,
                            "Tu cuenta fue desactivada correctamente",
                            Toast.LENGTH_SHORT).show();
                    cerrarSesionYRedirigirALogin();

                } else {
                    Toast.makeText(ActActualizarDatos.this,
                            construirMensajeErrorDesactivacion(response),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaModeracionDTO> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String construirMensajeErrorDesactivacion(Response<RespuestaModeracionDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        int code = response != null ? response.code() : -1;

        if (code == 409) {
            return backendMessage != null ? backendMessage : "Tu cuenta ya se encuentra desactivada o bloqueada.";
        }
        if (code == 403) {
            return backendMessage != null ? backendMessage : "No tienes permisos para desactivar esta cuenta.";
        }
        if (code == 400) {
            return backendMessage != null ? backendMessage : "La solicitud de desactivación no es válida.";
        }
        return backendMessage != null ? backendMessage : "No se pudo desactivar la cuenta. Inténtalo de nuevo más tarde.";
    }

    private void desactivarCuentaConApiV11(
            String contrasenaActual,
            androidx.appcompat.app.AlertDialog dialog,
            EditText etContrasenaActual,
            Button btnConfirmar,
            Button btnCancelar
    ) {
        SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        DesactivarCuentaRequestDTO request = new DesactivarCuentaRequestDTO();
        request.setIdUsuarioSolicitante(idUsuario);
        request.setContrasenaActual(contrasenaActual);
        request.setMotivo("Cuenta desactivada desde Android por solicitud del usuario");
        request.setConfirmacion(true);

        setEstadoDialogoDesactivacion(dialog, etContrasenaActual, btnConfirmar, btnCancelar, false);

        api.desactivarCuenta(idUsuario, request).enqueue(new Callback<RespuestaModeracionDTO>() {
            @Override
            public void onResponse(Call<RespuestaModeracionDTO> call, Response<RespuestaModeracionDTO> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(
                            ActActualizarDatos.this,
                            "Tu cuenta fue desactivada correctamente.",
                            Toast.LENGTH_LONG
                    ).show();
                    cerrarSesionYRedirigirALogin();
                } else {
                    setEstadoDialogoDesactivacion(dialog, etContrasenaActual, btnConfirmar, btnCancelar, true);
                    Toast.makeText(
                            ActActualizarDatos.this,
                            construirMensajeErrorDesactivacionV11(response),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaModeracionDTO> call, Throwable t) {
                setEstadoDialogoDesactivacion(dialog, etContrasenaActual, btnConfirmar, btnCancelar, true);
                Toast.makeText(
                        ActActualizarDatos.this,
                        "Error de conexión al desactivar la cuenta.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String construirMensajeErrorDesactivacionV11(Response<RespuestaModeracionDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        int code = response != null ? response.code() : -1;

        if (code == 400) {
            return backendMessage != null ? backendMessage : "Revisa los datos para desactivar la cuenta.";
        }
        if (code == 403) {
            return backendMessage != null ? backendMessage : "La contraseña actual es incorrecta.";
        }
        if (code == 409) {
            return backendMessage != null ? backendMessage : "La cuenta no puede desactivarse.";
        }
        return backendMessage != null ? backendMessage : "No se pudo desactivar la cuenta.";
    }

    private void setEstadoDialogoDesactivacion(
            androidx.appcompat.app.AlertDialog dialog,
            EditText etContrasenaActual,
            Button btnConfirmar,
            Button btnCancelar,
            boolean habilitado
    ) {
        if (dialog != null) {
            dialog.setCancelable(habilitado);
            dialog.setCanceledOnTouchOutside(habilitado);
        }
        if (etContrasenaActual != null) {
            etContrasenaActual.setEnabled(habilitado);
        }
        if (btnConfirmar != null) {
            btnConfirmar.setEnabled(habilitado);
        }
        if (btnCancelar != null) {
            btnCancelar.setEnabled(habilitado);
        }
        if (btnEliminarCuenta != null) {
            btnEliminarCuenta.setEnabled(habilitado);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void cerrarSesionYRedirigirALogin() {
        if (sessionManager != null) {
            sessionManager.clearSession();
        } else {
            getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE).edit().clear().apply();
        }

        Intent intent = new Intent(ActActualizarDatos.this, ActIniciarSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void actualizarUsuario() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (idUsuario == -1) {
            Toast.makeText(this, "Error: No se encontró sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        etCorreo.setError(null);
        etUsuario.setError(null);
        etNombre.setError(null);
        etTelefono.setError(null);

        String correo = getTrimmedText(etCorreo);
        String usuario = getTrimmedText(etUsuario);
        String nombre = getTrimmedText(etNombre);
        String fechaNac = getTrimmedText(etFecha);
        if (usuario.isEmpty()) {
            etUsuario.setError("El nombre de usuario es obligatorio.");
            etUsuario.requestFocus();
            return;
        }
        if (correo.isEmpty()) {
            etCorreo.setError("El correo es obligatorio.");
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingresa un correo vÃ¡lido.");
            etCorreo.requestFocus();
            return;
        }
        if (nombre.isEmpty()) {
            etNombre.setError("El nombre no puede estar vacÃ­o.");
            etNombre.requestFocus();
            Toast.makeText(this, "El nombre no puede estar vacío.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fechaNac.isEmpty()) {
            Toast.makeText(this, "Por favor elige tu fecha de nacimiento.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!esMayorDeEdad(fechaNac)) {
            Toast.makeText(this, "Debes ser mayor de edad para usar Artistlan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String telefono = etTelefono.getText().toString().trim();
        if (!telefono.isEmpty() && !esTelefonoValido(telefono)) {
            etTelefono.setError("Teléfono inválido");
            etTelefono.requestFocus();
            return;
        }

        if (imageUri != null) {
            subirFotoPerfil(idUsuario, prefs, () -> enviarActualizacionUsuario(idUsuario, prefs));
        } else {
            enviarActualizacionUsuario(idUsuario, prefs);
        }
    }

    private void subirFotoPerfil(int idUsuario, SharedPreferences prefs, Runnable onSuccess) {
        FirebaseImageRepository repo = new FirebaseImageRepository(this);
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
        String ocupacionActual = prefs.getString("ocupacion", prefs.getString("categoria", ""));

        if (posicionSeleccionada > 0) {
            CategoriaDTO categoriaSeleccionada = listaCategorias.get(posicionSeleccionada - 1);
            String nombreSeleccionado = categoriaSeleccionada.getNombreCategoria();
            boolean cambioCategoria = ocupacionActual == null
                    || !ocupacionActual.trim().equalsIgnoreCase(nombreSeleccionado != null ? nombreSeleccionado.trim() : "");
            if (cambioCategoria) {
                idCategoria = categoriaSeleccionada.getIdCategoria();
            }
        }
        usuarioActualizado.setIdCategoria(idCategoria);

        usuarioActualizado.setNombreCompleto(etNombre.getText().toString().trim());
        usuarioActualizado.setCorreo(etCorreo.getText().toString().trim());
        usuarioActualizado.setUsuario(etUsuario.getText().toString().trim());
        usuarioActualizado.setDescripcion(etDescripcion.getText().toString().trim());
        usuarioActualizado.setRedesSociales(etRedes.getText().toString().trim());
        usuarioActualizado.setTelefono(etTelefono.getText().toString().trim());
        usuarioActualizado.setFechaNacimiento(etFecha.getText().toString().trim());
        usuarioActualizado.setUbicacion(etUbicacion.getText().toString().trim());
        usuarioActualizado.setFotoPerfil(prefs.getString("fotoPerfil", ""));
        String rolActual = prefs.getString("rol", null);
        if (rolActual != null && !rolActual.trim().isEmpty()) {
            usuarioActualizado.setRol(rolActual);
        }

        api.actualizarUsuario(idUsuario, usuarioActualizado).enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> response) {
                if (response.isSuccessful()) {
                    persistirUsuarioActualizado(
                            prefs,
                            idUsuario,
                            usuarioActualizado,
                            response.body(),
                            posicionSeleccionada,
                            ocupacionActual
                    );
                    Toast.makeText(ActActualizarDatos.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    manejarErrorActualizacion(response);
                }
            }

            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void persistirUsuarioActualizado(
            SharedPreferences prefs,
            int idUsuario,
            UsuariosDTO usuarioEnviado,
            UsuariosDTO usuarioRespuesta,
            int posicionSeleccionada,
            String ocupacionActual
    ) {
        UsuariosDTO usuarioPersistido = usuarioRespuesta != null ? usuarioRespuesta : usuarioEnviado;
        usuarioPersistido.setIdUsuario(idUsuario);

        if (isBlank(usuarioPersistido.getRol())) {
            usuarioPersistido.setRol(prefs.getString("rol", "USER"));
        }
        if (isBlank(usuarioPersistido.getFotoPerfil())) {
            usuarioPersistido.setFotoPerfil(prefs.getString("fotoPerfil", ""));
        }

        String tokenActual = sessionManager != null ? sessionManager.getToken() : null;
        if (sessionManager != null && tokenActual != null) {
            sessionManager.saveUserSession(usuarioPersistido, tokenActual);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("id", idUsuario);
        editor.putInt("idUsuario", idUsuario);
        editor.putString("usuario", valueOrEmpty(usuarioPersistido.getUsuario()));
        editor.putString("correo", valueOrEmpty(usuarioPersistido.getCorreo()));
        editor.putString("nombreCompleto", valueOrEmpty(usuarioPersistido.getNombreCompleto()));
        editor.putString("descripcion", valueOrEmpty(usuarioPersistido.getDescripcion()));
        editor.putString("fotoPerfil", valueOrEmpty(usuarioPersistido.getFotoPerfil()));
        editor.putString("telefono", valueOrEmpty(usuarioPersistido.getTelefono()));
        editor.putString("redesSociales", valueOrEmpty(usuarioPersistido.getRedesSociales()));
        editor.putString("redes", valueOrEmpty(usuarioPersistido.getRedesSociales()));
        editor.putString("fechaNacimiento", valueOrEmpty(usuarioPersistido.getFechaNacimiento()));
        editor.putString("fechaNac", valueOrEmpty(usuarioPersistido.getFechaNacimiento()));
        editor.putString("ubicacion", valueOrEmpty(usuarioPersistido.getUbicacion()));
        editor.putString("rol", valueOrEmpty(usuarioPersistido.getRol()));
        editor.putBoolean("twoFactorEnabled", Boolean.TRUE.equals(usuarioPersistido.getTwoFactorEnabled()));

        String ocupacionPersistida = resolverOcupacionPersistida(usuarioPersistido, posicionSeleccionada, ocupacionActual);
        if (!isBlank(ocupacionPersistida)) {
            editor.putString("categoria", ocupacionPersistida);
            editor.putString("ocupacion", ocupacionPersistida);
        }

        editor.apply();
    }

    private String resolverOcupacionPersistida(
            UsuariosDTO usuarioPersistido,
            int posicionSeleccionada,
            String ocupacionActual
    ) {
        if (usuarioPersistido != null && !isBlank(usuarioPersistido.getCategoria())) {
            return usuarioPersistido.getCategoria().trim();
        }
        if (posicionSeleccionada > 0
                && listaCategorias != null
                && posicionSeleccionada <= listaCategorias.size()) {
            String nombreCategoria = listaCategorias.get(posicionSeleccionada - 1).getNombreCategoria();
            if (!isBlank(nombreCategoria)) {
                return nombreCategoria.trim();
            }
        }
        return isBlank(ocupacionActual) ? null : ocupacionActual.trim();
    }

    private void manejarErrorActualizacion(Response<?> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        String mensaje = backendMessage != null
                ? backendMessage
                : "Error al actualizar (CÃ³digo: " + response.code() + ")";

        if (response.code() == 409) {
            String mensajeNormalizado = backendMessage == null ? "" : backendMessage.toLowerCase(Locale.ROOT);
            if (mensajeNormalizado.contains("correo")) {
                mensaje = "El correo ya estÃ¡ en uso.";
                etCorreo.setError(mensaje);
                etCorreo.requestFocus();
            } else if (mensajeNormalizado.contains("usuario")) {
                mensaje = "El nombre de usuario ya estÃ¡ en uso.";
                etUsuario.setError(mensaje);
                etUsuario.requestFocus();
            }
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private String getTrimmedText(EditText editText) {
        return editText != null && editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean esTelefonoValido(String telefono) {
        String limpio = telefono == null ? "" : telefono.trim();
        return limpio.matches("^\\+?\\d{10,15}$");
    }

    private boolean esMayorDeEdad(String fechaNac) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            Date nacimiento = sdf.parse(fechaNac);
            if (nacimiento == null) return false;
            Calendar nac = Calendar.getInstance();
            nac.setTime(nacimiento);
            Calendar hoy = Calendar.getInstance();
            int edad = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
            if (hoy.get(Calendar.DAY_OF_YEAR) < nac.get(Calendar.DAY_OF_YEAR)) edad--;
            return edad >= 18;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeManager = new ThemeManager(this);
        applyThemeOnlyColors();
    }
}


