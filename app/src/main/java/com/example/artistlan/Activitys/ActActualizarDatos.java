package com.example.artistlan.Activitys;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CategoriaApi;
import com.example.artistlan.Conector.api.CategoriaUsuariosApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.CategoriaDTO;
import com.example.artistlan.Conector.model.CategoriaUsuariosDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.Conector.repository.FirebaseImageRepository;
import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActActualizarDatos extends AppCompatActivity implements View.OnClickListener {

    private Button btnActualizarDatos;
    private ImageButton IsbtnRegresar;
    private EditText etCorreo, etNombre, etDescripcion, etRedes, etTelefono, etFecha, etUsuario, etContra;
    private ImageView btnCambiarFoto, imgFotoPerfil;
    private Spinner spinnerCategoriaUsuario;
    private String contrasenaOriginal;

    private UsuarioApi api;
    private List<CategoriaDTO> listaCategorias;

    private ActivityResultLauncher<String> seleccionarImagenperfilLauncher;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_actualizar_datos);

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

        btnActualizarDatos.setOnClickListener(this);
        IsbtnRegresar.setOnClickListener(this);

        api = RetrofitClient.getClient().create(UsuarioApi.class);

        // Campos bloqueados
        etCorreo.setEnabled(false);
        etUsuario.setEnabled(false);

        // DatePicker
        etFecha.setOnClickListener(v -> mostrarDatePicker());

        // Selector de imagen
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
        btnCambiarFoto.setOnClickListener(v -> seleccionarImagenperfilLauncher.launch("image/*"));

        // Cargar datos del usuario y categorías
        cargarDatosUsuario();
        cargarCategoriasDesdeApi();
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
                    nombresCategorias.add("Ninguna"); // posición 0

                    for (CategoriaDTO c : response.body()) {
                        int id = c.getIdCategoria();

                        if (id >= 19 && id <= 37) {
                            listaCategorias.add(c);
                            nombresCategorias.add(c.getNombreCategoria());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ActActualizarDatos.this,
                            android.R.layout.simple_spinner_item, nombresCategorias);
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
        if (v.getId() == R.id.IsbtnRegresar) finish();
        else if (v.getId() == R.id.btnActualizarDatos) actualizarUsuario();
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

                    // Actualizar SharedPreferences con los datos básicos
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

}