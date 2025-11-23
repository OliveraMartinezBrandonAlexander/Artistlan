package com.example.artistlan.Activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActIniciarSesion extends AppCompatActivity implements View.OnClickListener {

    Button btnIniciarSesion;
    EditText etCorreo, etUsuario, etContrasena;
    UsuarioApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_iniciar_sesion);

        etCorreo = findViewById(R.id.correoinicio);
        etUsuario = findViewById(R.id.usuarioinicio);
        etContrasena = findViewById(R.id.contrainicio);

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIniciarSesion.setOnClickListener(this);

        api = RetrofitClient.getClient().create(UsuarioApi.class);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnIniciarSesion) {
            iniciarSesion();
        }
    }

    private void iniciarSesion() {

        String correo = etCorreo.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (correo.isEmpty() || usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<UsuariosDTO> call = api.login(usuario, correo, contrasena);
        call.enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> response) {

                if (response.isSuccessful() && response.body() != null) {

                    UsuariosDTO user = response.body();

                    // GUARDAR TODA LA INFO DEL USUARIO
                    guardarUsuarioLogeado(user);

                    Toast.makeText(ActIniciarSesion.this,
                            "Bienvenido " + user.getUsuario(),
                            Toast.LENGTH_SHORT).show();

                    Intent ir = new Intent(ActIniciarSesion.this, ActFragmentoPrincipal.class);
                    startActivity(ir);
                    finish();
                }
                else {
                    Toast.makeText(ActIniciarSesion.this,
                            "Credenciales incorrectas",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                Toast.makeText(ActIniciarSesion.this,
                        "Error al conectar con el servidor",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarUsuarioLogeado(UsuariosDTO usuario) {

        SharedPreferences prefs =
                getSharedPreferences("usuario_prefs", MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("id", usuario.getIdUsuario());
        editor.putString("usuario", usuario.getUsuario());
        editor.putString("correo", usuario.getCorreo());
        editor.putString("nombre", usuario.getNombreCompleto());
        editor.putString("telefono", usuario.getTelefono());
        editor.putString("descripcion", usuario.getDescripcion());
        editor.putString("redes", usuario.getRedesSociales());
        editor.putString("fechaNac", usuario.getFechaNacimiento());
        editor.putString("fotoPerfil", usuario.getFotoPerfil());

        editor.apply();
    }
}