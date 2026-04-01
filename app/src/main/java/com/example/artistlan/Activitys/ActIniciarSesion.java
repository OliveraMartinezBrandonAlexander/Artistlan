package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActIniciarSesion extends AppCompatActivity implements View.OnClickListener {

    private View btnIniciarSesion;
    private ImageButton btnRegresar;
    private EditText etCorreo, etUsuario, etContrasena;
    private UsuarioApi api;

    private View glowTop, glowCenter, glowBottom, formContainer, dividerShimmer;
    private ObjectAnimator glowTopY, glowTopX, glowTopAlpha;
    private ObjectAnimator glowCenterY, glowCenterX, glowCenterAlpha;
    private ObjectAnimator glowBottomY, glowBottomX, glowBottomAlpha;
    private ObjectAnimator dividerShimmerAnim;

    private View resultOverlay, resultDialog;
    private View resultOk;
    private TextView resultTitle, resultMessage;
    private LottieAnimationView resultLottie, sideLottie, buttonLottie;

    private boolean waitingMode = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final long MIN_WAIT_VISIBLE_MS = 1400L;
    private long waitingShownAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_iniciar_sesion);

        etCorreo = findViewById(R.id.correoinicio);
        etUsuario = findViewById(R.id.usuarioinicio);
        etContrasena = findViewById(R.id.contrainicio);

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegresar = findViewById(R.id.IsBtnRegresar);

        glowTop = findViewById(R.id.IsGlowTop);
        glowCenter = findViewById(R.id.IsGlowCenter);
        glowBottom = findViewById(R.id.IsGlowBottom);
        formContainer = findViewById(R.id.IsFormContainer);
        dividerShimmer = findViewById(R.id.IsDividerShimmer);

        resultOverlay = findViewById(R.id.IsResultOverlay);
        resultDialog = findViewById(R.id.IsResultDialog);
        resultOk = findViewById(R.id.IsResultOk);
        resultTitle = findViewById(R.id.IsResultTitle);
        resultMessage = findViewById(R.id.IsResultMessage);
        resultLottie = findViewById(R.id.IsResultLottie);

        sideLottie = findViewById(R.id.IsLottieSide);
        buttonLottie = findViewById(R.id.IsLottieButton);

        tintLottieWhite(sideLottie);
        tintLottieWhite(buttonLottie);

        btnIniciarSesion.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);
        resultOk.setOnClickListener(v -> hideResultDialog());

        setupPressAnimation(btnIniciarSesion);
        setupPressAnimation(btnRegresar);
        setupPressAnimation(resultOk);

        api = RetrofitClient.getClient().create(UsuarioApi.class);

        ScrollView scrollView = findViewById(R.id.IsScroll);
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

    private void tintLottieWhite(LottieAnimationView lottieView) {
        if (lottieView == null) return;

        lottieView.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                new LottieValueCallback<>(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP))
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
        waitingShownAt = System.currentTimeMillis();

        resultTitle.setText("Iniciando sesión");
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

    private void showErrorDialog(String title, String message) {
        transitionFromWaitingToError(title, message);
    }

    private void transitionFromWaitingToError(String title, String message) {
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
                        waitingMode = false;
                        resultTitle.setText(title);
                        resultMessage.setText(message);
                        resultOk.setVisibility(View.VISIBLE);

                        resultLottie.cancelAnimation();
                        resultLottie.setAnimation(R.raw.lottie_error);
                        resultLottie.setRepeatCount(0);
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
        if (v.getId() == R.id.btnIniciarSesion) {
            iniciarSesion();
        } else if (v.getId() == R.id.IsBtnRegresar) {
            finish();
        }
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (correo.isEmpty() && usuario.isEmpty()) {
            etUsuario.requestFocus();
            showErrorDialog("Faltan datos", "Debes ingresar tu usuario o tu correo.");
            return;
        }
        if (contrasena.isEmpty()) {
            etContrasena.requestFocus();
            showErrorDialog("Contraseña requerida", "La contraseña es obligatoria.");
            return;
        }

        showWaitingDialog();

        Call<UsuariosDTO> call = api.login(usuario, correo, contrasena);

        call.enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> response) {

                if (response.isSuccessful() && response.body() != null) {

                    UsuariosDTO user = response.body();

                    guardarUsuarioLogeado(user, contrasena);

                    api.obtenerCategoriaUsuario(user.getIdUsuario()).enqueue(new Callback<UsuariosDTO>() {
                        @Override
                        public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> respCategoria) {
                            if (respCategoria.isSuccessful() && respCategoria.body() != null) {
                                UsuariosDTO userConCategoria = respCategoria.body();
                                SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                String categoria = userConCategoria.getCategoria();
                                editor.putString("categoria", categoria != null ? categoria : "Sin categoría");
                                editor.apply();
                            }
                        }

                        @Override
                        public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                            SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("categoria", "Sin categoría");
                            editor.apply();
                        }
                    });

                    Intent ir = new Intent(ActIniciarSesion.this, ActFragmentoPrincipal.class);
                    startActivity(ir);
                    finish();

                } else {
                    showErrorDialog("Credenciales incorrectas", "Revisa tu usuario, correo o contraseña.");
                }
            }

            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                showErrorDialog("Error de conexión", "No se pudo conectar con el servidor.");
            }
        });
    }

    private void guardarUsuarioLogeado(UsuariosDTO usuario, String contrasenaIngresada) {
        SharedPreferences prefs =
                getSharedPreferences("usuario_prefs", MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("id", usuario.getIdUsuario());
        editor.putString("usuario", usuario.getUsuario());
        editor.putString("correo", usuario.getCorreo());

        editor.putString("nombreCompleto", usuario.getNombreCompleto());
        editor.putString("contrasena", contrasenaIngresada);
        editor.putString("telefono", usuario.getTelefono());
        editor.putString("descripcion", usuario.getDescripcion());
        editor.putString("redes", usuario.getRedesSociales());
        editor.putString("fechaNac", usuario.getFechaNacimiento());

        String foto = usuario.getFotoPerfil();
        editor.putString("fotoPerfil", foto != null ? foto : "");

        editor.apply();
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
        if (sideLottie != null && !sideLottie.isAnimating()) sideLottie.playAnimation();
        if (buttonLottie != null && !buttonLottie.isAnimating()) buttonLottie.playAnimation();
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