package com.example.artistlan.Activitys;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.artistlan.R;
import com.example.artistlan.api.RetrofitClient;
import com.example.artistlan.api.UsuarioApi;
import com.example.artistlan.api.model.UsuariosDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ActCrearCuenta extends AppCompatActivity implements View.OnClickListener {

    private Button btnCrear;
    private EditText edtEmail, edtNombre, edtTel, edtFecha, edtUsuario, edtContra, edtContraConf;
    private UsuarioApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_crear_cuenta);

        btnCrear = findViewById(R.id.CrcBtnCrc);
        edtEmail = findViewById(R.id.CrcEdtEmail);
        edtNombre = findViewById(R.id.CrcEdtNombre);
        edtTel = findViewById(R.id.CrcEdtTel);
        edtFecha = findViewById(R.id.CrcEdtFecha);
        edtUsuario = findViewById(R.id.CrcEdtUsuario);
        edtContra = findViewById(R.id.CrcEdtPass);
        edtContraConf = findViewById(R.id.CrcEdtPassConf);

        btnCrear.setOnClickListener(this);
        edtFecha.setOnClickListener(v -> mostrarDatePicker()); // muestra calendario

        api = RetrofitClient.getClient().create(UsuarioApi.class); //conexion con la api
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.CrcBtnCrc) {
            if (validarCampos()) {
                enviarCrearCuenta();
            }
        }
    }

    // Mostrar DatePicker y guardar fecha
    private void mostrarDatePicker() {
        //variables para obtener cada valor
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar sel = Calendar.getInstance();
            //logica para trasnformar item seleccionado al formato de mysql
            sel.clear();
            sel.set(Calendar.YEAR, year);
            sel.set(Calendar.MONTH, month);
            sel.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            edtFecha.setText(sdf.format(sel.getTime()));
            edtFecha.setError(null);
        }, y, m, d);

        dpd.show();
    }

    private boolean validarCampos() {
        //obtener strings a validar
        String email = edtEmail.getText().toString().trim();
        String nombre = edtNombre.getText().toString().trim();
        String tel = edtTel.getText().toString().trim();
        String fecha = edtFecha.getText().toString().trim();
        String usuario = edtUsuario.getText().toString().trim();
        String contra = edtContra.getText().toString();
        String contraConf = edtContraConf.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Correo inválido");
            edtEmail.requestFocus();
            return false;
        }
        if (nombre.isEmpty()) {
            edtNombre.setError("Ingresa nombre");
            edtNombre.requestFocus();
            return false;
        }
        if (!tel.isEmpty() && tel.length() < 7) {
            edtTel.setError("Teléfono inválido");
            edtTel.requestFocus();
            return false;
        }
        if (fecha.isEmpty()) {
            edtFecha.setError("Selecciona fecha");
            edtFecha.requestFocus();
            return false;
        }
        if (usuario.isEmpty()) {
            edtUsuario.setError("Ingresa usuario");
            edtUsuario.requestFocus();
            return false;
        }
        if (!validarContrasena(contra)) {
            edtContra.setError("Contraseña débil (mínimo 8 caracteres,1 mayúscula,1 minúsc,1 número y 1especial)");
            edtContra.requestFocus();
            return false;
        }
        if (!contra.equals(contraConf)) {
            edtContraConf.setError("Las contraseñas no coinciden");
            edtContraConf.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validarContrasena(String pass) {
        if (pass == null) return false;
        if (pass.length() < 8) return false;
        //validacion de mayuscula + minuscula +  numero + caracter especial
        Pattern p = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$");
        return p.matcher(pass).find();
    }

    private void enviarCrearCuenta() {
        btnCrear.setEnabled(false);
        Toast.makeText(this, "Creando cuenta...", Toast.LENGTH_SHORT).show();

        //Insertar los campos
        UsuariosDTO u = new UsuariosDTO();
        u.setCorreo(edtEmail.getText().toString().trim());
        u.setNombreCompleto(edtNombre.getText().toString().trim());
        u.setTelefono(edtTel.getText().toString().trim());
        u.setFechaNacimiento(edtFecha.getText().toString().trim());
        u.setUsuario(edtUsuario.getText().toString().trim());
        u.setContrasena(edtContra.getText().toString());
        u.setAdminUsuario(0);

        List<UsuariosDTO> lista = new ArrayList<>();
        lista.add(u);

        Call<List<UsuariosDTO>> call = api.crearUsuarios(lista);

        call.enqueue(new Callback<List<UsuariosDTO>>() {
            @Override
            public void onResponse(Call<List<UsuariosDTO>> call, Response<List<UsuariosDTO>> response) {
                btnCrear.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(ActCrearCuenta.this, "Cuenta creada", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String mensaje = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) mensaje += ": " + response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(ActCrearCuenta.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<UsuariosDTO>> call, Throwable t) {
                btnCrear.setEnabled(true);
                Toast.makeText(ActCrearCuenta.this, "Fallo red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}