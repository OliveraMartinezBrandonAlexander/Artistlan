package com.example.artistlan.Activitys;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.AuthErrorResponseDTO;
import com.example.artistlan.Conector.model.LoginRequestDTO;
import com.example.artistlan.Conector.model.LoginResponseDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.PasswordPressVisibilityHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActIniciarSesion extends AppCompatActivity implements View.OnClickListener {

    private static final long MAX_REUSABLE_SESSION_IDLE_MS = 7L * 24L * 60L * 60L * 1000L;

    private View btnIniciarSesion;
    private ImageButton btnRegresar;
    private EditText etCorreo, etUsuario, etContrasena;
    private UsuarioApi api;
    private SessionManager sessionManager;

    private View glowTop, glowCenter, glowBottom, formContainer, dividerShimmer, dividerBase, rootMain;
    private ObjectAnimator glowTopY, glowTopX, glowTopAlpha;
    private ObjectAnimator glowCenterY, glowCenterX, glowCenterAlpha;
    private ObjectAnimator glowBottomY, glowBottomX, glowBottomAlpha;
    private ObjectAnimator dividerShimmerAnim;

    private View resultOverlay, resultDialog;
    private View resultOk;
    private TextView resultTitle, resultMessage;
    private TextView txtBrand, txtTitulo, txtInstruccion, txtCorreoLbl, txtUsuarioLbl, txtPassLbl;
    private LottieAnimationView resultLottie, sideLottie;

    private ThemeManager themeManager;

    private boolean waitingMode = false;
    private boolean isNavigating = false;
    private boolean suppressIdentifierWatchers = false;
    private String lastEditedIdentifierType = "";

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final long MIN_WAIT_VISIBLE_MS = 1400L;
    private long waitingShownAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_iniciar_sesion);

        themeManager = new ThemeManager(this);

        rootMain = findViewById(R.id.IsLayMain);

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
        dividerBase = findViewById(R.id.IsDividerBase);

        resultOverlay = findViewById(R.id.IsResultOverlay);
        resultDialog = findViewById(R.id.IsResultDialog);
        resultOk = findViewById(R.id.IsResultOk);
        resultTitle = findViewById(R.id.IsResultTitle);
        resultMessage = findViewById(R.id.IsResultMessage);
        resultLottie = findViewById(R.id.IsResultLottie);

        txtBrand = findViewById(R.id.IsTxtBrand);
        txtTitulo = findViewById(R.id.IsTxtTitulo);
        txtInstruccion = findViewById(R.id.IsTxtInstruccion);
        txtCorreoLbl = findViewById(R.id.IsTxtCorreoLbl);
        txtUsuarioLbl = findViewById(R.id.IsTxtUsuarioLbl);
        txtPassLbl = findViewById(R.id.IsTxtPassLbl);

        sideLottie = findViewById(R.id.IsLottieSide);

        applyThemeOnlyColors();

        btnIniciarSesion.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);
        resultOk.setOnClickListener(v -> hideResultDialog());

        setupPressAnimation(btnIniciarSesion);
        setupPressAnimation(btnRegresar);
        setupPressAnimation(resultOk);

        api = RetrofitClient.getClient().create(UsuarioApi.class);
        sessionManager = new SessionManager(this);
        setupIdentifierTracking();
        preloadSavedAccountFields(true);

        ScrollView scrollView = findViewById(R.id.IsScroll);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeHeight);
            return insets;
        });

        prepareIntro();
        startGlowAnimations();
        startIntroAnimation();
        startDividerShimmer();
    }

    private void applyThemeOnlyColors() {
        ThemeApplier.applySystemBars(this, themeManager);

        if (rootMain != null) {
            rootMain.setBackgroundColor(themeManager.color(ThemeKeys.BG_BOTTOM));
        }

        ThemeApplier.applyTextPrimary(txtBrand, themeManager);
        ThemeApplier.applyTextPrimary(txtTitulo, themeManager);
        ThemeApplier.applyTextSecondary(txtInstruccion, themeManager);
        ThemeApplier.applyTextPrimary(txtCorreoLbl, themeManager);
        ThemeApplier.applyTextPrimary(txtUsuarioLbl, themeManager);
        ThemeApplier.applyTextPrimary(txtPassLbl, themeManager);
        ThemeApplier.applyTextPrimary(resultTitle, themeManager);
        ThemeApplier.applyTextSecondary(resultMessage, themeManager);

        ThemeApplier.applyInput(etCorreo, themeManager);
        ThemeApplier.applyInput(etUsuario, themeManager);
        ThemeApplier.applyInput(etContrasena, themeManager);
        PasswordPressVisibilityHelper.attach(
                etContrasena,
                R.drawable.ic_eye,
                themeManager.color(ThemeKeys.ICON_ACTIVE)
        );

        ThemeApplier.applyPrimaryButton(btnIniciarSesion, themeManager);
        ThemeApplier.applySecondaryButton(resultOk, themeManager);

        ThemeEffectsApplier.applyGlowIntensity(glowTop, themeManager, ThemeKeys.GLOW_PRIMARY);
        ThemeEffectsApplier.applyGlowIntensity(glowCenter, themeManager, ThemeKeys.GLOW_TERTIARY);
        ThemeEffectsApplier.applyGlowIntensity(glowBottom, themeManager, ThemeKeys.GLOW_SECONDARY);

        if (dividerBase != null && dividerBase.getBackground() != null) {
            dividerBase.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_DIVIDER),
                    PorterDuff.Mode.SRC_ATOP
            );
        }

        if (dividerShimmer != null && dividerShimmer.getBackground() != null) {
            dividerShimmer.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_SHIMMER),
                    PorterDuff.Mode.SRC_ATOP
            );
        }

        tintDecorativeLottie(sideLottie, themeManager.color(ThemeKeys.ICON_ACTIVE));
    }

    private void tintDecorativeLottie(LottieAnimationView lottieView, int color) {
        if (lottieView == null) return;
        lottieView.addValueCallback(
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
        if (view == null) return;
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

        if (glowTopX != null) glowTopX.start();
        if (glowTopY != null) glowTopY.start();
        if (glowTopAlpha != null) glowTopAlpha.start();

        if (glowCenterX != null) glowCenterX.start();
        if (glowCenterY != null) glowCenterY.start();
        if (glowCenterAlpha != null) glowCenterAlpha.start();

        if (glowBottomX != null) glowBottomX.start();
        if (glowBottomY != null) glowBottomY.start();
        if (glowBottomAlpha != null) glowBottomAlpha.start();
    }

    private ObjectAnimator createAnimator(View target, String property, float from, float to, long duration) {
        if (target == null) return null;
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
            float distance = 400f;
            dividerShimmerAnim = ObjectAnimator.ofFloat(dividerShimmer, "translationX", -distance, distance);
            dividerShimmerAnim.setDuration(2000);
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
        resultMessage.setText("Esto puede tardar un momento");
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
            if (resultDialog == null || resultLottie == null) return;

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

    private void showSuccessAndNavigate() {
        if (isNavigating) return;
        isNavigating = true;
        waitingMode = false;

        long elapsed = System.currentTimeMillis() - waitingShownAt;
        long remaining = Math.max(0L, MIN_WAIT_VISIBLE_MS - elapsed);

        uiHandler.postDelayed(() -> {
            if (resultOverlay == null || resultDialog == null || resultLottie == null) {
                launchMainScreen();
                return;
            }

            resultTitle.setText("¡Bienvenido!");
            resultMessage.setText("Tu sesión se inició correctamente");
            resultOk.setVisibility(View.GONE);

            resultLottie.cancelAnimation();
            resultLottie.setAnimation(R.raw.lottie_success);
            resultLottie.setRepeatCount(0);
            resultLottie.playAnimation();

            resultOverlay.setVisibility(View.VISIBLE);
            resultOverlay.setAlpha(1f);

            resultDialog.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            if (btnIniciarSesion != null) {
                btnIniciarSesion.animate()
                        .scaleX(1.03f)
                        .scaleY(1.03f)
                        .setDuration(120)
                        .withEndAction(() -> btnIniciarSesion.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start())
                        .start();
            }

            uiHandler.postDelayed(() -> {
                if (formContainer != null) {
                    formContainer.animate()
                            .alpha(0f)
                            .translationY(-28f)
                            .scaleX(0.96f)
                            .scaleY(0.96f)
                            .setDuration(320)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                }

                if (glowTop != null) glowTop.animate().alpha(0.12f).setDuration(300).start();
                if (glowCenter != null) glowCenter.animate().alpha(0.08f).setDuration(300).start();
                if (glowBottom != null) glowBottom.animate().alpha(0.10f).setDuration(300).start();

                resultDialog.animate()
                        .alpha(0f)
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .setDuration(260)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(this::launchMainScreen)
                        .start();

                resultOverlay.animate().alpha(0f).setDuration(280).start();
            }, 900);
        }, remaining);
    }

    private void launchMainScreen() {
        Intent ir = new Intent(ActIniciarSesion.this, ActFragmentoPrincipal.class);
        ir.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(ir);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void hideResultDialog() {
        if (resultLottie != null) resultLottie.cancelAnimation();

        if (resultDialog == null || resultOverlay == null) return;

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
        if (sessionManager != null && sessionManager.hasReusableSession(MAX_REUSABLE_SESSION_IDLE_MS)) {
            sessionManager.touchSession();
            showWaitingDialog();
            showSuccessAndNavigate();
            return;
        }

        String correo = etCorreo.getText().toString().trim();
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String tipoIdentificador = resolveIdentifierType(usuario, correo);
        String usuarioOCorreo = SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(tipoIdentificador) ? correo : usuario;
        if (correo.isEmpty() && usuario.isEmpty()) {
            etUsuario.requestFocus();
            showErrorDialog("Faltan datos", "Debes ingresar tu usuario o tu correo.");
            return;
        }
        if (usuarioOCorreo.isEmpty()) {
            tipoIdentificador = usuario.isEmpty() ? SessionManager.LOGIN_IDENTIFIER_EMAIL : SessionManager.LOGIN_IDENTIFIER_USERNAME;
            usuarioOCorreo = usuario.isEmpty() ? correo : usuario;
        }
        if (contrasena.isEmpty()) {
            etContrasena.requestFocus();
            showErrorDialog("Contraseña requerida", "La contraseña es obligatoria.");
            return;
        }
        clearUnselectedIdentifierField(tipoIdentificador);
        final String identificadorUsado = usuarioOCorreo;
        final String tipoIdentificadorUsado = tipoIdentificador;
        final String usuarioSeleccionado = SessionManager.LOGIN_IDENTIFIER_USERNAME.equals(tipoIdentificadorUsado) ? identificadorUsado : "";
        final String correoSeleccionado = SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(tipoIdentificadorUsado) ? identificadorUsado : "";
        showWaitingDialog();
        LoginRequestDTO request = new LoginRequestDTO(identificadorUsado, contrasena);
        Call<LoginResponseDTO> call = api.login(request);
        call.enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO loginResponse = response.body();
                    boolean requires2FA = Boolean.TRUE.equals(loginResponse.getRequires2FA());
                    if (requires2FA) {
                        String temporaryToken = loginResponse.getTemporaryToken();
                        if (temporaryToken == null || temporaryToken.trim().isEmpty()) {
                            showErrorDialog("Error de autenticación", "No se recibió temporaryToken para continuar con 2FA.");
                            return;
                        }
                        openOtpVerificationScreen(temporaryToken, usuarioSeleccionado, correoSeleccionado, tipoIdentificadorUsado, identificadorUsado);
                        return;
                    }
                    UsuariosDTO user = loginResponse.getEffectiveUser();
                    if (user == null) {
                        showErrorDialog("Error de autenticación", "No se recibió información válida del usuario.");
                        return;
                    }
                    Integer idUsuario = user.getIdUsuario();
                    if (idUsuario == null || idUsuario <= 0) {
                        showErrorDialog("Error de autenticación", "No se recibió id de usuario válido.");
                        return;
                    }
                    String jwt = loginResponse.getToken();
                    if (jwt == null || jwt.trim().isEmpty() || "null".equalsIgnoreCase(jwt.trim())) {
                        showErrorDialog("Error de autenticación", "No se recibió token de sesión válido.");
                        return;
                    }
                    guardarUsuarioLogeado(user, jwt);
                    sessionManager.saveLastLoginIdentifier(identificadorUsado, tipoIdentificadorUsado);
                    cargarCategoriaUsuario(idUsuario);
                    showSuccessAndNavigate();
                } else {
                    handleLoginErrorResponse(response);
                }
            }
            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                showErrorDialog("Error de conexión", "No se pudo conectar con el servidor.");
            }
        });
    }

    private void handleLoginErrorResponse(Response<LoginResponseDTO> response) {
        int code = response != null ? response.code() : -1;

        if (code == 423) {
            AuthErrorResponseDTO authError = ApiErrorParser.extractAuthError(response);
            showErrorDialog("Cuenta suspendida", buildReadableAuthErrorMessage(
                    authError,
                    "Tu cuenta está suspendida temporalmente. Intenta nuevamente cuando termine la suspensión."
            ));
            return;
        }

        if (code == 403) {
            AuthErrorResponseDTO authError = ApiErrorParser.extractAuthError(response);
            showErrorDialog("Acceso bloqueado", buildReadableAuthErrorMessage(
                    authError,
                    "Tu cuenta no puede iniciar sesión. Puede estar desactivada o bloqueada."
            ));
            return;
        }

        String backendMessage = ApiErrorParser.extractMessage(response);

        if (code == 401) {
            showErrorDialog("Credenciales incorrectas", "Revisa tu usuario, correo o contraseña.");
            return;
        }

        showErrorDialog(
                "No se pudo iniciar sesión",
                backendMessage != null ? backendMessage : "Ocurrió un error al iniciar sesión. Intenta de nuevo."
        );
    }

    private String buildReadableAuthErrorMessage(AuthErrorResponseDTO authError, String fallbackMessage) {
        if (authError == null) {
            return fallbackMessage;
        }
        String message = authError.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = fallbackMessage;
        }
        String fechaFormateada = formatFechaFinSuspension(authError.getFechaFinSuspension());
        if (fechaFormateada != null && !fechaFormateada.isEmpty()) {
            return message + "\nHasta: " + fechaFormateada;
        }
        return message;
    }

    private String formatFechaFinSuspension(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.length() < 16 || value.charAt(4) != '-' || value.charAt(7) != '-' || value.charAt(10) != 'T') {
            return value;
        }
        try {
            String yyyy = value.substring(0, 4);
            String mm = value.substring(5, 7);
            String dd = value.substring(8, 10);
            String hh = value.substring(11, 13);
            String min = value.substring(14, 16);
            return dd + "/" + mm + "/" + yyyy + " " + hh + ":" + min;
        } catch (Exception ex) {
            return value;
        }
    }

    private void cargarCategoriaUsuario(Integer idUsuario) {
        api.obtenerCategoriaUsuario(idUsuario).enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> respCategoria) {
                if (respCategoria.isSuccessful() && respCategoria.body() != null) {
                    UsuariosDTO userConCategoria = respCategoria.body();
                    SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    String categoria = userConCategoria.getCategoria();
                    editor.putString("categoria", categoria != null ? categoria : "Sin categoria");
                    editor.putString("ocupacion", categoria != null ? categoria : "Sin categoria");
                    editor.apply();
                }
            }
            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("categoria", "Sin categoria");
                editor.putString("ocupacion", "Sin categoria");
                editor.apply();
            }
        });
    }

    private void preloadSavedAccountFields(boolean clearPassword) {
        if (sessionManager == null) {
            return;
        }

        suppressIdentifierWatchers = true;
        try {
            if (etCorreo != null) {
                etCorreo.setText("");
            }
            if (etUsuario != null) {
                etUsuario.setText("");
            }

            String type = sessionManager.getLastLoginIdentifierType();
            String identifier = sessionManager.getLastLoginIdentifier();

            if (identifier.isEmpty()) {
                if (SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(type)) {
                    identifier = sessionManager.getLastCorreo();
                } else if (SessionManager.LOGIN_IDENTIFIER_USERNAME.equals(type)) {
                    identifier = sessionManager.getLastUsuario();
                } else if (!sessionManager.getLastUsuario().isEmpty()) {
                    type = SessionManager.LOGIN_IDENTIFIER_USERNAME;
                    identifier = sessionManager.getLastUsuario();
                } else if (!sessionManager.getLastCorreo().isEmpty()) {
                    type = SessionManager.LOGIN_IDENTIFIER_EMAIL;
                    identifier = sessionManager.getLastCorreo();
                }
            }

            if (SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(type) && etCorreo != null) {
                etCorreo.setText(identifier);
            } else if (SessionManager.LOGIN_IDENTIFIER_USERNAME.equals(type) && etUsuario != null) {
                etUsuario.setText(identifier);
            }

            if (clearPassword && etContrasena != null) {
                etContrasena.setText("");
            }
        } finally {
            suppressIdentifierWatchers = false;
        }
    }

    private void setupIdentifierTracking() {
        if (etCorreo != null) {
            etCorreo.addTextChangedListener(new IdentifierWatcher(SessionManager.LOGIN_IDENTIFIER_EMAIL));
            etCorreo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    lastEditedIdentifierType = SessionManager.LOGIN_IDENTIFIER_EMAIL;
                }
            });
        }
        if (etUsuario != null) {
            etUsuario.addTextChangedListener(new IdentifierWatcher(SessionManager.LOGIN_IDENTIFIER_USERNAME));
            etUsuario.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    lastEditedIdentifierType = SessionManager.LOGIN_IDENTIFIER_USERNAME;
                }
            });
        }
    }

    private String resolveIdentifierType(String usuario, String correo) {
        boolean hasUsuario = usuario != null && !usuario.trim().isEmpty();
        boolean hasCorreo = correo != null && !correo.trim().isEmpty();
        if (hasUsuario && !hasCorreo) {
            return SessionManager.LOGIN_IDENTIFIER_USERNAME;
        }
        if (hasCorreo && !hasUsuario) {
            return SessionManager.LOGIN_IDENTIFIER_EMAIL;
        }
        if (hasUsuario && hasCorreo) {
            if (SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(lastEditedIdentifierType)
                    || SessionManager.LOGIN_IDENTIFIER_USERNAME.equals(lastEditedIdentifierType)) {
                return lastEditedIdentifierType;
            }
            if (sessionManager != null) {
                String savedType = sessionManager.getLastLoginIdentifierType();
                if (SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(savedType)
                        || SessionManager.LOGIN_IDENTIFIER_USERNAME.equals(savedType)) {
                    return savedType;
                }
            }
        }
        return SessionManager.LOGIN_IDENTIFIER_USERNAME;
    }

    private void clearUnselectedIdentifierField(String selectedType) {
        suppressIdentifierWatchers = true;
        try {
            if (SessionManager.LOGIN_IDENTIFIER_EMAIL.equals(selectedType)) {
                if (etUsuario != null) {
                    etUsuario.setText("");
                }
            } else if (etCorreo != null) {
                etCorreo.setText("");
            }
        } finally {
            suppressIdentifierWatchers = false;
        }
    }

    private class IdentifierWatcher implements TextWatcher {
        private final String type;

        IdentifierWatcher(String type) {
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!suppressIdentifierWatchers) {
                lastEditedIdentifierType = type;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private void openOtpVerificationScreen(String temporaryToken, String usuario, String correo, String identifierType, String identifier) {
        waitingMode = false;
        if (resultLottie != null) {
            resultLottie.cancelAnimation();
        }
        if (resultOverlay != null) {
            resultOverlay.setAlpha(0f);
            resultOverlay.setVisibility(View.GONE);
        }

        Intent intent = new Intent(ActIniciarSesion.this, ActVerificarOtpLogin.class);
        intent.putExtra(ActVerificarOtpLogin.EXTRA_MODE, ActVerificarOtpLogin.MODE_LOGIN);
        intent.putExtra(ActVerificarOtpLogin.EXTRA_TEMPORARY_TOKEN, temporaryToken);
        intent.putExtra(ActVerificarOtpLogin.EXTRA_USUARIO, usuario != null ? usuario : "");
        intent.putExtra(ActVerificarOtpLogin.EXTRA_CORREO, correo != null ? correo : "");
        intent.putExtra(ActVerificarOtpLogin.EXTRA_IDENTIFIER_TYPE, identifierType != null ? identifierType : "");
        intent.putExtra(ActVerificarOtpLogin.EXTRA_IDENTIFIER, identifier != null ? identifier : "");
        startActivity(intent);
        Toast.makeText(this, "Te enviamos un código a tu correo.", Toast.LENGTH_SHORT).show();
    }
    private void guardarUsuarioLogeado(UsuariosDTO usuario, String jwtToken) {
        SharedPreferences prefs = getSharedPreferences("usuario_prefs", MODE_PRIVATE);
        String categoria = usuario.getCategoria();
        sessionManager.saveUserSession(usuario, jwtToken);
        if (categoria != null && !categoria.trim().isEmpty()) {
            prefs.edit()
                    .putString("categoria", categoria)
                    .putString("ocupacion", categoria)
                    .apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelGlowAnimations();
        cancelAnimator(dividerShimmerAnim);
        if (resultLottie != null) resultLottie.cancelAnimation();
        if (sideLottie != null) sideLottie.cancelAnimation();
        uiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeManager = new ThemeManager(this);
        applyThemeOnlyColors();
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
        if (resultLottie != null) resultLottie.cancelAnimation();
        if (sideLottie != null) sideLottie.cancelAnimation();
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
