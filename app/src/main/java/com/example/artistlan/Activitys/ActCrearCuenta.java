package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActCrearCuenta extends AppCompatActivity implements View.OnClickListener {

    private Button btnCrear;
    private ImageButton btnRegresar;
    private EditText edtEmail, edtNombre, edtTel, edtFecha, edtUsuario, edtContra, edtContraConf;
    private UsuarioApi api;

    private View glowTop, glowCenter, glowBottom, formContainer, dividerShimmer;
    private ObjectAnimator glowTopY, glowTopX, glowTopAlpha;
    private ObjectAnimator glowCenterY, glowCenterX, glowCenterAlpha;
    private ObjectAnimator glowBottomY, glowBottomX, glowBottomAlpha;
    private ObjectAnimator dividerShimmerAnim;

    private View resultOverlay, resultDialog;
    private Button resultOk;
    private TextView resultTitle, resultMessage;
    private LottieAnimationView resultLottie, sideLottie;

    private boolean closeAfterDialog = false;
    private boolean waitingMode = false;

    // Nuevo: control para suavizar cambio de carga -> resultado
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final long MIN_WAIT_VISIBLE_MS = 1400L;
    private long waitingShownAt = 0L;

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

        btnRegresar = findViewById(R.id.CrcBtnRegresar);

        glowTop = findViewById(R.id.CrcGlowTop);
        glowCenter = findViewById(R.id.CrcGlowCenter);
        glowBottom = findViewById(R.id.CrcGlowBottom);
        formContainer = findViewById(R.id.CrcFormContainer);
        dividerShimmer = findViewById(R.id.CrcDividerShimmer);

        resultOverlay = findViewById(R.id.CrcResultOverlay);
        resultDialog = findViewById(R.id.CrcResultDialog);
        resultOk = findViewById(R.id.CrcResultOk);
        resultTitle = findViewById(R.id.CrcResultTitle);
        resultMessage = findViewById(R.id.CrcResultMessage);
        resultLottie = findViewById(R.id.CrcResultLottie);
        sideLottie = findViewById(R.id.CrcLottieSide);

        tintSideLottie(Color.WHITE);

        btnCrear.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);
        resultOk.setOnClickListener(v -> {
            hideResultDialog();
            if (closeAfterDialog) {
                finish();
            }
        });

        edtFecha.setOnClickListener(v -> mostrarDatePicker());

        setupPressAnimation(btnCrear);
        setupPressAnimation(btnRegresar);
        setupPressAnimation(resultOk);

        api = RetrofitClient.getClient().create(UsuarioApi.class);

        ScrollView scrollView = findViewById(R.id.CrcScroll);

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

        prepareIntro();
        startGlowAnimations();
        startIntroAnimation();
        startDividerShimmer();
    }

    private void tintSideLottie(int color) {
        if (sideLottie == null) return;

        sideLottie.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                new LottieValueCallback<>(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP))
        );
    }

    private void prepareIntro() {
        if (formContainer != null) {
            formContainer.setAlpha(0f);
            formContainer.setTranslationY(46f);
            formContainer.setScaleX(0.97f);
            formContainer.setScaleY(0.97f);
        }

        if (resultOverlay != null) {
            resultOverlay.setVisibility(View.GONE);
            resultOverlay.setAlpha(0f);
        }
    }

    private void startIntroAnimation() {
        if (formContainer != null) {
            formContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(650)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void setupPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    break;
            }
            return false;
        });
    }

    private void startGlowAnimations() {
        cancelGlowAnimations();

        glowTopX = createAnimator(glowTop, "translationX", -60f, 25f, 5200);
        glowTopY = createAnimator(glowTop, "translationY", -40f, 60f, 6100);
        glowTopAlpha = createAnimator(glowTop, "alpha", 0.26f, 0.50f, 2600);

        glowCenterX = createAnimator(glowCenter, "translationX", -20f, 22f, 4300);
        glowCenterY = createAnimator(glowCenter, "translationY", 80f, 170f, 5000);
        glowCenterAlpha = createAnimator(glowCenter, "alpha", 0.10f, 0.24f, 2200);

        glowBottomX = createAnimator(glowBottom, "translationX", 80f, -10f, 5600);
        glowBottomY = createAnimator(glowBottom, "translationY", 80f, -30f, 6400);
        glowBottomAlpha = createAnimator(glowBottom, "alpha", 0.24f, 0.52f, 3000);

        glowTopX.start();
        glowTopY.start();
        glowTopAlpha.start();

        glowCenterX.start();
        glowCenterY.start();
        glowCenterAlpha.start();

        glowBottomX.start();
        glowBottomY.start();
        glowBottomAlpha.start();
    }

    private ObjectAnimator createAnimator(View target, String property, float from, float to, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, property, from, to);
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private void startDividerShimmer() {
        if (dividerShimmer == null) return;

        dividerShimmer.post(() -> {
            cancelAnimator(dividerShimmerAnim);

            float distance = 135f;
            dividerShimmerAnim = ObjectAnimator.ofFloat(dividerShimmer, "translationX", -distance, distance);
            dividerShimmerAnim.setDuration(1500);
            dividerShimmerAnim.setRepeatCount(ValueAnimator.INFINITE);
            dividerShimmerAnim.setRepeatMode(ValueAnimator.RESTART);
            dividerShimmerAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            dividerShimmerAnim.start();
        });
    }

    private void showWaitingDialog() {
        waitingMode = true;
        closeAfterDialog = false;
        waitingShownAt = System.currentTimeMillis();

        resultTitle.setText("Creando cuenta");
        resultMessage.setText("Esto puede tardar un tiempo");
        resultOk.setVisibility(View.GONE);

        resultOverlay.setVisibility(View.VISIBLE);
        resultOverlay.setAlpha(0f);
        resultDialog.setScaleX(0.90f);
        resultDialog.setScaleY(0.90f);
        resultDialog.setAlpha(0f);

        resultLottie.setAnimation(R.raw.lottie_time);
        resultLottie.setRepeatCount(ValueAnimator.INFINITE);
        resultLottie.playAnimation();

        resultOverlay.animate().alpha(1f).setDuration(220).start();

        resultDialog.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(320)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void showSuccessDialog(String title, String message) {
        waitingMode = false;
        closeAfterDialog = true;
        transitionFromWaitingToResult(title, message, R.raw.lottie_success, false);
    }

    private void showErrorDialog(String title, String message) {
        waitingMode = false;
        closeAfterDialog = false;
        transitionFromWaitingToResult(title, message, R.raw.lottie_error, false);
    }

    private void transitionFromWaitingToResult(String title, String message, int rawRes, boolean loop) {
        long elapsed = System.currentTimeMillis() - waitingShownAt;
        long remaining = Math.max(0L, MIN_WAIT_VISIBLE_MS - elapsed);

        uiHandler.postDelayed(() -> {
            resultDialog.animate()
                    .alpha(0.65f)
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(180)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        resultLottie.cancelAnimation();
                        resultTitle.setText(title);
                        resultMessage.setText(message);
                        resultOk.setVisibility(View.VISIBLE);

                        resultLottie.setAnimation(rawRes);
                        resultLottie.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
                        resultLottie.playAnimation();

                        resultDialog.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(240)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    })
                    .start();
        }, remaining);
    }

    private void hideResultDialog() {
        resultLottie.cancelAnimation();

        resultDialog.animate()
                .alpha(0.92f)
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(120)
                .withEndAction(() -> resultOverlay.animate()
                        .alpha(0f)
                        .setDuration(180)
                        .withEndAction(() -> {
                            resultOverlay.setVisibility(View.GONE);
                            if (waitingMode) {
                                resultOk.setVisibility(View.GONE);
                            } else {
                                resultOk.setVisibility(View.VISIBLE);
                            }
                        })
                        .start())
                .start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.CrcBtnCrc) {
            if (validarCampos()) {
                verificarNombreUsuario();
            }
        } else if (v.getId() == R.id.CrcBtnRegresar) {
            finish();
        }
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar sel = Calendar.getInstance();
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
            edtContra.setError("Contraseña débil (mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 número)");
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

        Pattern p = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
        return p.matcher(pass).find();
    }

    private void verificarNombreUsuario() {
        btnCrear.setEnabled(false);
        edtUsuario.setError(null);
        edtEmail.setError(null);

        String correo = edtEmail.getText().toString().trim();
        String usuario = edtUsuario.getText().toString().trim();

        showWaitingDialog();

        Call<String> call = api.existeUsuario(usuario, correo);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                btnCrear.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String resultado = response.body();

                    switch (resultado) {
                        case "USUARIO_DUPLICADO":
                            edtUsuario.setError("Usuario ya existe");
                            edtUsuario.requestFocus();
                            showErrorDialog("Usuario ocupado", "El nombre de usuario ya está registrado.");
                            break;

                        case "CORREO_DUPLICADO":
                            edtEmail.setError("Correo ya existe");
                            edtEmail.requestFocus();
                            showErrorDialog("Correo duplicado", "El correo electrónico ya está registrado.");
                            break;

                        case "AMBOS_DUPLICADOS":
                            edtUsuario.setError("Usuario ya existe");
                            edtEmail.setError("Correo ya existe");
                            edtUsuario.requestFocus();
                            showErrorDialog("Datos duplicados", "Usuario y correo ya están registrados.");
                            break;

                        case "OK":
                            enviarCrearCuenta();
                            break;

                        default:
                            showErrorDialog("Error del servidor", "Respuesta inesperada: " + resultado);
                            break;
                    }
                } else {
                    showErrorDialog("Error al verificar", "Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                btnCrear.setEnabled(true);
                t.printStackTrace();
                showErrorDialog("Fallo de red", "No se pudo verificar: " + t.getMessage());
            }
        });
    }

    private void enviarCrearCuenta() {
        btnCrear.setEnabled(false);

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
                    showSuccessDialog("Cuenta creada", "Tu cuenta se creó correctamente.");
                } else {
                    String mensaje = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            mensaje += ": " + response.errorBody().string();
                        }
                    } catch (Exception ignored) { }
                    showErrorDialog("No se pudo crear", mensaje);
                }
            }

            @Override
            public void onFailure(Call<List<UsuariosDTO>> call, Throwable t) {
                btnCrear.setEnabled(true);
                showErrorDialog("Fallo de red", "No se pudo crear la cuenta: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelGlowAnimations();
        cancelAnimator(dividerShimmerAnim);
        resultLottie.cancelAnimation();
        uiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGlowAnimations();
        startDividerShimmer();
        if (sideLottie != null && !sideLottie.isAnimating()) {
            sideLottie.playAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelGlowAnimations();
        cancelAnimator(dividerShimmerAnim);
        resultLottie.cancelAnimation();
        uiHandler.removeCallbacksAndMessages(null);
    }

    private void cancelGlowAnimations() {
        cancelAnimator(glowTopX);
        cancelAnimator(glowTopY);
        cancelAnimator(glowTopAlpha);

        cancelAnimator(glowCenterX);
        cancelAnimator(glowCenterY);
        cancelAnimator(glowCenterAlpha);

        cancelAnimator(glowBottomX);
        cancelAnimator(glowBottomY);
        cancelAnimator(glowBottomAlpha);
    }

    private void cancelAnimator(ObjectAnimator animator) {
        if (animator != null) animator.cancel();
    }
}