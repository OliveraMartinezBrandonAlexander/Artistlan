package com.example.artistlan.Activitys;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActActualizarDatos extends AppCompatActivity implements View.OnClickListener {

    private Button btnActualizarDatos, btnRegresar;
    private EditText etCorreo, etNombre, etDescripcion, etRedes, etTelefono, etFecha, etUsuario, etContra;
    private UsuarioApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_actualizar_datos);

        // Enlazar XML con Java
        etCorreo = findViewById(R.id.correo);
        etNombre = findViewById(R.id.nombre);
        etDescripcion = findViewById(R.id.descripcion);
        etRedes = findViewById(R.id.redes);
        etTelefono = findViewById(R.id.telefono);
        etFecha = findViewById(R.id.CrcEdtFecha);
        etUsuario = findViewById(R.id.usuario);
        etContra = findViewById(R.id.contra);

        btnActualizarDatos = findViewById(R.id.btnActualizarDatos);
        btnRegresar = findViewById(R.id.btnRegresar);

        btnActualizarDatos.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);

        api = RetrofitClient.getClient().create(UsuarioApi.class);

        // 1. FORZAR VISIBILIDAD DE CONTRASEÑA
        etContra.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        // 2. CARGAR DATOS (Esto debe ocurrir antes de la deshabilitación final)
        cargarDatosUsuario();

        // 3. DESHABILITACIÓN FINAL
        // Deshabilitar campos que deben mostrar su valor pero no ser editables
        etNombre.setEnabled(true);
        etCorreo.setEnabled(false);
        etUsuario.setEnabled(false);
        etContra.setEnabled(false);

        // Abrir DatePicker al tocar fecha
        etFecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                anio, mes, dia);
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

        // Cargar la contraseña visible
        etContra.setText(prefs.getString("contrasena", ""));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRegresar) {
            finish();
        } else if (v.getId() == R.id.btnActualizarDatos) {
            actualizarUsuario();
        }
    }

    private void actualizarUsuario() {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        int idUsuario = prefs.getInt("id", -1);

        if (idUsuario == -1) {
            Toast.makeText(this, "Error: No se encontró sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String redes = etRedes.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String fechaNac = etFecha.getText().toString().trim();

        String correo = etCorreo.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContra.getText().toString().trim();


        if (nombre.isEmpty()) {
            Toast.makeText(this, "El campo Nombre Completo no puede estar vacío.", Toast.LENGTH_SHORT).show();
            etNombre.requestFocus();
            return;
        }

        if (fechaNac.isEmpty()) {
            Toast.makeText(this, "Por favor, llena los campos obligatorios: Teléfono y Fecha de Nacimiento.", Toast.LENGTH_SHORT).show();
            return;
        }
        UsuariosDTO usuarioActualizado = new UsuariosDTO();

        usuarioActualizado.setNombreCompleto(nombre);
        usuarioActualizado.setCorreo(correo);
        usuarioActualizado.setUsuario(usuario);
        usuarioActualizado.setContrasena(contrasena);

        usuarioActualizado.setDescripcion(descripcion);
        usuarioActualizado.setRedesSociales(redes);
        usuarioActualizado.setTelefono(telefono);
        usuarioActualizado.setFechaNacimiento(fechaNac);

        Call<Void> call = api.actualizarUsuario(idUsuario, usuarioActualizado);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActActualizarDatos.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("nombreCompleto", nombre);
                    editor.putString("descripcion", descripcion);
                    editor.putString("redes", redes);
                    editor.putString("telefono", telefono);
                    editor.putString("fechaNac", fechaNac);
                    editor.apply();

                    finish();

                } else {
                    Toast.makeText(ActActualizarDatos.this, "Error al actualizar (Código: " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ActActualizarDatos.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}