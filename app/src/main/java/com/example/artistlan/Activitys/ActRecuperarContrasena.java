package com.example.artistlan.Activitys;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.PasswordResetApi;
import com.example.artistlan.Conector.model.PasswordResetConfirmRequestDTO;
import com.example.artistlan.Conector.model.PasswordResetRequestDTO;
import com.example.artistlan.Conector.model.PasswordResetResendRequestDTO;
import com.example.artistlan.Conector.model.PasswordResetResponseDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeEffectsApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.ArtistlanLoadingDialog;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.PasswordPressVisibilityHelper;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActRecuperarContrasena extends AppCompatActivity implements View.OnClickListener {

    private static final int OTP_LENGTH = 6;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 72;
    private static final long RESEND_COOLDOWN_MS = 60_000L;
    private static final String DEFAULT_REQUEST_MESSAGE =
            "C\u00F3digo enviado. Revisa el correo asociado a tu cuenta.";
    private static final String REQUEST_TOKEN_CREATED_MESSAGE =
            "C\u00F3digo enviado. Revisa el correo asociado a tu cuenta.";
    private static final String DEFAULT_RESEND_MESSAGE =
            "C\u00F3digo reenviado. Revisa el correo asociado a tu cuenta.";
    private static final String DEFAULT_SUCCESS_MESSAGE =
            "Contrase\u00F1a actualizada correctamente.";

    private ImageButton btnRegresar;
    private Button btnEnviarCodigo;
    private Button btnCambiarContrasena;
    private Button btnReenviarCodigo;
    private Button btnCambiarCuenta;
    private TextView txtVolverLogin;
    private TextView txtBrand;
    private TextView txtTitulo;
    private TextView txtInstruccion;
    private TextView txtCorreoUsuarioLbl;
    private TextView txtCodigoLbl;
    private TextView txtNuevaContrasenaLbl;
    private TextView txtConfirmarContrasenaLbl;
    private TextView txtEstadoSolicitud;
    private TextView txtReenvioCooldown;
    private EditText etCorreoUsuario;
    private EditText etCodigo;
    private EditText etNuevaContrasena;
    private EditText etConfirmarContrasena;
    private LinearLayout panelPasoReset;
    private View rootMain;
    private View glowTop;
    private View glowCenter;
    private View glowBottom;
    private View cardContainer;
    private View dividerBase;
    private LottieAnimationView sideLottie;

    private ThemeManager themeManager;
    private ArtistlanLoadingDialog feedbackDialog;
    private PasswordResetApi passwordResetApi;

    private String temporaryToken;
    private boolean requestInFlight = false;
    private boolean resendCooldownActive = false;
    private CountDownTimer resendCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_recuperar_contrasena);

        themeManager = new ThemeManager(this);
        feedbackDialog = new ArtistlanLoadingDialog(this);
        passwordResetApi = RetrofitClient.getClient().create(PasswordResetApi.class);

        bindViews();
        applyThemeOnlyColors();
        setupInteractions();
        configureInsets();
        updateRecoveryStep(false);
    }

    private void bindViews() {
        rootMain = findViewById(R.id.RcLayMain);
        glowTop = findViewById(R.id.RcGlowTop);
        glowCenter = findViewById(R.id.RcGlowCenter);
        glowBottom = findViewById(R.id.RcGlowBottom);
        cardContainer = findViewById(R.id.RcCardContainer);
        dividerBase = findViewById(R.id.RcDividerBase);
        sideLottie = findViewById(R.id.RcLottieSide);

        btnRegresar = findViewById(R.id.RcBtnRegresar);
        btnEnviarCodigo = findViewById(R.id.RcBtnEnviarCodigo);
        btnCambiarContrasena = findViewById(R.id.RcBtnCambiarContrasena);
        btnReenviarCodigo = findViewById(R.id.RcBtnReenviarCodigo);
        btnCambiarCuenta = findViewById(R.id.RcBtnCambiarCuenta);
        txtVolverLogin = findViewById(R.id.RcTxtVolverLogin);

        txtBrand = findViewById(R.id.RcTxtBrand);
        txtTitulo = findViewById(R.id.RcTxtTitulo);
        txtInstruccion = findViewById(R.id.RcTxtInstruccion);
        txtCorreoUsuarioLbl = findViewById(R.id.RcTxtCorreoUsuarioLbl);
        txtCodigoLbl = findViewById(R.id.RcTxtCodigoLbl);
        txtNuevaContrasenaLbl = findViewById(R.id.RcTxtNuevaContrasenaLbl);
        txtConfirmarContrasenaLbl = findViewById(R.id.RcTxtConfirmarContrasenaLbl);
        txtEstadoSolicitud = findViewById(R.id.RcTxtEstadoSolicitud);
        txtReenvioCooldown = findViewById(R.id.RcTxtReenvioCooldown);

        etCorreoUsuario = findViewById(R.id.RcEtCorreoUsuario);
        etCodigo = findViewById(R.id.RcEtCodigo);
        etNuevaContrasena = findViewById(R.id.RcEtNuevaContrasena);
        etConfirmarContrasena = findViewById(R.id.RcEtConfirmarContrasena);
        panelPasoReset = findViewById(R.id.RcPanelPasoReset);
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
        }

        ThemeApplier.applyTextPrimary(txtBrand, themeManager);
        ThemeApplier.applyTextPrimary(txtTitulo, themeManager);
        ThemeApplier.applyTextSecondary(txtInstruccion, themeManager);
        ThemeApplier.applyTextPrimary(txtCorreoUsuarioLbl, themeManager);
        ThemeApplier.applyTextPrimary(txtCodigoLbl, themeManager);
        ThemeApplier.applyTextPrimary(txtNuevaContrasenaLbl, themeManager);
        ThemeApplier.applyTextPrimary(txtConfirmarContrasenaLbl, themeManager);
        ThemeApplier.applyTextSecondary(txtEstadoSolicitud, themeManager);
        ThemeApplier.applyTextSecondary(txtReenvioCooldown, themeManager);

        if (txtVolverLogin != null) {
            txtVolverLogin.setTextColor(themeManager.color(ThemeKeys.ICON_ACTIVE));
        }
        if (btnRegresar != null) {
            CardThemeHelper.applyFilterButton(btnRegresar, themeManager);
        }

        ThemeApplier.applyInput(etCorreoUsuario, themeManager);
        ThemeApplier.applyInput(etCodigo, themeManager);
        ThemeApplier.applyInput(etNuevaContrasena, themeManager);
        ThemeApplier.applyInput(etConfirmarContrasena, themeManager);

        PasswordPressVisibilityHelper.attach(
                etNuevaContrasena,
                R.drawable.ic_eye,
                themeManager.color(ThemeKeys.ICON_ACTIVE)
        );
        PasswordPressVisibilityHelper.attach(
                etConfirmarContrasena,
                R.drawable.ic_eye,
                themeManager.color(ThemeKeys.ICON_ACTIVE)
        );

        CardThemeHelper.applyPrimaryBubbleButton(btnEnviarCodigo, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnCambiarContrasena, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnReenviarCodigo, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnCambiarCuenta, themeManager);

        ThemeEffectsApplier.applyGlowIntensity(glowTop, themeManager, ThemeKeys.GLOW_PRIMARY);
        ThemeEffectsApplier.applyGlowIntensity(glowCenter, themeManager, ThemeKeys.GLOW_TERTIARY);
        ThemeEffectsApplier.applyGlowIntensity(glowBottom, themeManager, ThemeKeys.GLOW_SECONDARY);

        if (dividerBase != null && dividerBase.getBackground() != null) {
            dividerBase.getBackground().setColorFilter(
                    themeManager.color(ThemeKeys.ACCOUNT_DIVIDER),
                    PorterDuff.Mode.SRC_ATOP
            );
        }
    }

    private void setupInteractions() {
        btnRegresar.setOnClickListener(this);
        btnEnviarCodigo.setOnClickListener(this);
        btnCambiarContrasena.setOnClickListener(this);
        btnReenviarCodigo.setOnClickListener(this);
        btnCambiarCuenta.setOnClickListener(this);
        txtVolverLogin.setOnClickListener(this);

        ThemeApplier.animatePress(btnRegresar);
        ThemeApplier.animatePress(btnEnviarCodigo);
        ThemeApplier.animatePress(btnCambiarContrasena);
        ThemeApplier.animatePress(btnReenviarCodigo);
        ThemeApplier.animatePress(btnCambiarCuenta);
        ThemeApplier.animatePress(txtVolverLogin);
    }

    private void configureInsets() {
        ScrollView scrollView = findViewById(R.id.RcScroll);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeHeight);
            return insets;
        });
    }

    private void updateRecoveryStep(boolean hasValidToken) {
        boolean showResetStep = hasValidToken && !isBlank(temporaryToken);
        panelPasoReset.setVisibility(showResetStep ? View.VISIBLE : View.GONE);
        btnEnviarCodigo.setVisibility(showResetStep ? View.GONE : View.VISIBLE);
        setIdentifierLocked(showResetStep);

        if (showResetStep) {
            clearResetFields();
            hideStepAnimation();
            animateStepTransition(panelPasoReset);
            txtInstruccion.setText("Revisa tu correo asociado y completa el cambio de contrase\u00F1a.");
            txtEstadoSolicitud.setText("Captura el c\u00F3digo de 6 d\u00EDgitos. Expira en 5 minutos.");
            etCodigo.requestFocus();
            return;
        }

        stopResendCooldown();
        showStepAnimation();
        playStepAnimation(R.raw.login);
        animateStepTransition(cardContainer);
        txtInstruccion.setText("Ingresa tu nombre de usuario para enviarte un c\u00F3digo al correo asociado.");
        txtEstadoSolicitud.setText("Usaremos el correo asociado a tu nombre de usuario.");
        clearResetFields();
    }

    private void setIdentifierLocked(boolean locked) {
        etCorreoUsuario.setEnabled(!locked);
        etCorreoUsuario.setFocusable(!locked);
        etCorreoUsuario.setFocusableInTouchMode(!locked);
        etCorreoUsuario.setCursorVisible(!locked);
        etCorreoUsuario.setLongClickable(!locked);
        etCorreoUsuario.setAlpha(locked ? 0.74f : 1f);
        if (locked) {
            etCorreoUsuario.clearFocus();
        }
    }

    private void clearResetFields() {
        etCodigo.setText("");
        etNuevaContrasena.setText("");
        etConfirmarContrasena.setText("");
        etCodigo.setError(null);
        etNuevaContrasena.setError(null);
        etConfirmarContrasena.setError(null);
    }

    @Override
    public void onClick(View v) {
        if (requestInFlight) {
            return;
        }

        int viewId = v.getId();
        if (viewId == R.id.RcBtnEnviarCodigo) {
            solicitarCodigo();
        } else if (viewId == R.id.RcBtnCambiarContrasena) {
            confirmarCambioContrasena();
        } else if (viewId == R.id.RcBtnReenviarCodigo) {
            reenviarCodigo();
        } else if (viewId == R.id.RcBtnCambiarCuenta) {
            cambiarCuenta();
        } else if (viewId == R.id.RcBtnRegresar || viewId == R.id.RcTxtVolverLogin) {
            finish();
        }
    }

    private void solicitarCodigo() {
        clearAllErrors();

        String usuario = getTrimmedText(etCorreoUsuario);
        if (usuario.isEmpty()) {
            showFieldError(etCorreoUsuario, "Ingresa tu nombre de usuario.");
            feedbackDialog.showError("Ingresa tu nombre de usuario para enviar el c\u00F3digo.");
            return;
        }

        setLoadingState("Enviando c\u00F3digo...");
        passwordResetApi.requestReset(new PasswordResetRequestDTO(usuario))
                .enqueue(new Callback<PasswordResetResponseDTO>() {
                    @Override
                    public void onResponse(Call<PasswordResetResponseDTO> call, Response<PasswordResetResponseDTO> response) {
                        closeLoadingState();
                        if (response.isSuccessful() && response.body() != null) {
                            PasswordResetResponseDTO body = response.body();
                            temporaryToken = trimToNull(body.getTemporaryToken());
                            if (temporaryToken != null) {
                                updateRecoveryStep(true);
                                startResendCooldown();
                                feedbackDialog.showSuccess(REQUEST_TOKEN_CREATED_MESSAGE, null);
                                return;
                            }

                            updateRecoveryStep(false);
                            txtEstadoSolicitud.setText(safeMessage(body.getMessage(), DEFAULT_REQUEST_MESSAGE));
                            feedbackDialog.showError(safeMessage(body.getMessage(), DEFAULT_REQUEST_MESSAGE));
                            etCorreoUsuario.requestFocus();
                            return;
                        }

                        manejarErrorSolicitud(response);
                    }

                    @Override
                    public void onFailure(Call<PasswordResetResponseDTO> call, Throwable t) {
                        closeLoadingState();
                        feedbackDialog.showError("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void confirmarCambioContrasena() {
        clearAllErrors();

        if (isBlank(temporaryToken)) {
            temporaryToken = null;
            updateRecoveryStep(false);
            feedbackDialog.showError("Solicita un c\u00F3digo de recuperaci\u00F3n.");
            return;
        }

        String codigo = getTrimmedText(etCodigo);
        String nuevaContrasena = getRawText(etNuevaContrasena);
        String confirmarContrasena = getRawText(etConfirmarContrasena);

        if (codigo.isEmpty()) {
            showFieldError(etCodigo, "Ingresa el c\u00F3digo de recuperaci\u00F3n.");
            feedbackDialog.showError("Ingresa el c\u00F3digo de recuperaci\u00F3n.");
            return;
        }
        if (!codigo.matches("\\d{" + OTP_LENGTH + "}")) {
            showFieldError(etCodigo, "Ingresa un c\u00F3digo v\u00E1lido de 6 d\u00EDgitos.");
            feedbackDialog.showError("Ingresa un c\u00F3digo v\u00E1lido de 6 d\u00EDgitos.");
            return;
        }
        if (isBlankForPassword(nuevaContrasena)) {
            showFieldError(etNuevaContrasena, "Ingresa una nueva contrase\u00F1a.");
            feedbackDialog.showError("Ingresa una nueva contrase\u00F1a.");
            return;
        }
        if (isBlankForPassword(confirmarContrasena)) {
            showFieldError(etConfirmarContrasena, "Confirma la nueva contrase\u00F1a.");
            feedbackDialog.showError("Confirma la nueva contrase\u00F1a.");
            return;
        }
        if (nuevaContrasena.length() < PASSWORD_MIN_LENGTH || nuevaContrasena.length() > PASSWORD_MAX_LENGTH) {
            showFieldError(etNuevaContrasena, "Usa una contrase\u00F1a de 8 a 72 caracteres.");
            feedbackDialog.showError("Usa una contrase\u00F1a de 8 a 72 caracteres.");
            return;
        }
        if (!TextUtils.equals(nuevaContrasena, confirmarContrasena)) {
            showFieldError(etConfirmarContrasena, "Las contrase\u00F1as no coinciden.");
            feedbackDialog.showError("Las contrase\u00F1as no coinciden.");
            return;
        }

        setLoadingState("Actualizando contrase\u00F1a...");
        PasswordResetConfirmRequestDTO request = new PasswordResetConfirmRequestDTO(
                temporaryToken,
                codigo,
                nuevaContrasena,
                confirmarContrasena
        );

        passwordResetApi.confirmReset(request).enqueue(new Callback<PasswordResetResponseDTO>() {
            @Override
            public void onResponse(Call<PasswordResetResponseDTO> call, Response<PasswordResetResponseDTO> response) {
                closeLoadingState();
                if (response.isSuccessful() && response.body() != null) {
                    temporaryToken = null;
                    stopResendCooldown();
                    showStepAnimation();
                    playStepAnimation(R.raw.lottie_success);
                    feedbackDialog.showSuccess(
                            safeMessage(response.body().getMessage(), DEFAULT_SUCCESS_MESSAGE),
                            ActRecuperarContrasena.this::volverAlLogin
                    );
                    return;
                }

                manejarErrorConfirmacion(response);
            }

            @Override
            public void onFailure(Call<PasswordResetResponseDTO> call, Throwable t) {
                closeLoadingState();
                feedbackDialog.showError("No se pudo conectar con el servidor.");
            }
        });
    }

    private void reenviarCodigo() {
        clearAllErrors();

        if (isBlank(temporaryToken)) {
            temporaryToken = null;
            updateRecoveryStep(false);
            feedbackDialog.showError("Solicita un c\u00F3digo de recuperaci\u00F3n.");
            return;
        }

        setLoadingState("Reenviando c\u00F3digo...");
        passwordResetApi.resend(new PasswordResetResendRequestDTO(temporaryToken))
                .enqueue(new Callback<PasswordResetResponseDTO>() {
                    @Override
                    public void onResponse(Call<PasswordResetResponseDTO> call, Response<PasswordResetResponseDTO> response) {
                        closeLoadingState();
                        if (response.isSuccessful() && response.body() != null) {
                            startResendCooldown();
                            feedbackDialog.showSuccess(
                                    safeMessage(response.body().getMessage(), DEFAULT_RESEND_MESSAGE),
                                    null
                            );
                            return;
                        }

                        manejarErrorReenvio(response);
                    }

                    @Override
                    public void onFailure(Call<PasswordResetResponseDTO> call, Throwable t) {
                        closeLoadingState();
                        feedbackDialog.showError("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void manejarErrorSolicitud(Response<PasswordResetResponseDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        int code = response != null ? response.code() : -1;

        if (code == 404 || code == 400) {
            String message = safeMessage(backendMessage, "No encontramos una cuenta con ese nombre de usuario.");
            showFieldError(etCorreoUsuario, message);
            feedbackDialog.showError(message);
            return;
        }

        if (code == 429) {
            feedbackDialog.showError(safeMessage(backendMessage, "Espera antes de solicitar otro c\u00F3digo."));
            return;
        }

        feedbackDialog.showError(
                safeMessage(backendMessage, "No se pudo enviar el c\u00F3digo. Intenta de nuevo.")
        );
    }

    private void manejarErrorConfirmacion(Response<PasswordResetResponseDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        String normalized = normalize(backendMessage);
        int code = response != null ? response.code() : -1;

        if (code == 429) {
            feedbackDialog.showError(safeMessage(backendMessage, "Espera antes de intentarlo de nuevo."));
            return;
        }

        if (code == 400 || code == 410) {
            if (containsText(normalized, "codigo incorrecto")) {
                showFieldError(etCodigo, "C\u00F3digo incorrecto.");
                feedbackDialog.showError("C\u00F3digo incorrecto. Revisa el correo y vuelve a intentarlo.");
                return;
            }
            if (containsText(normalized, "codigo expirado") || containsText(normalized, "token invalido")) {
                temporaryToken = null;
                updateRecoveryStep(false);
                feedbackDialog.showError("El c\u00F3digo ya no es v\u00E1lido. Solicita otro c\u00F3digo.");
                etCorreoUsuario.requestFocus();
                return;
            }
            if (containsText(normalized, "contrasenas no coinciden")) {
                showFieldError(etConfirmarContrasena, "Las contrase\u00F1as no coinciden.");
                feedbackDialog.showError("Las contrase\u00F1as no coinciden.");
                return;
            }
            if (containsText(normalized, "debe tener entre")) {
                showFieldError(etNuevaContrasena, "Usa una contrase\u00F1a de 8 a 72 caracteres.");
                feedbackDialog.showError("Usa una contrase\u00F1a de 8 a 72 caracteres.");
                return;
            }
            if (containsText(normalized, "diferente a la actual")) {
                showFieldError(etNuevaContrasena, "La nueva contrase\u00F1a debe ser diferente a la actual.");
                feedbackDialog.showError("La nueva contrase\u00F1a debe ser diferente a la actual.");
                return;
            }
        }

        feedbackDialog.showError(
                safeMessage(backendMessage, "No se pudo cambiar la contrase\u00F1a. Intenta de nuevo.")
        );
    }

    private void manejarErrorReenvio(Response<PasswordResetResponseDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        String normalized = normalize(backendMessage);
        int code = response != null ? response.code() : -1;

        if (code == 429) {
            startResendCooldown();
            feedbackDialog.showError(safeMessage(backendMessage, "Espera antes de solicitar un nuevo c\u00F3digo."));
            return;
        }

        if (code == 400 || code == 410 || containsText(normalized, "token invalido") || containsText(normalized, "codigo expirado")) {
            temporaryToken = null;
            updateRecoveryStep(false);
            feedbackDialog.showError("La solicitud ya no es v\u00E1lida. Pide un c\u00F3digo nuevo.");
            etCorreoUsuario.requestFocus();
            return;
        }

        feedbackDialog.showError(
                safeMessage(backendMessage, "No se pudo reenviar el c\u00F3digo.")
        );
    }

    private void setLoadingState(String message) {
        requestInFlight = true;
        setActionButtonsEnabled(false);
        feedbackDialog.showLoading(message);
    }

    private void finishLoadingState() {
        requestInFlight = false;
        setActionButtonsEnabled(true);
    }

    private void closeLoadingState() {
        finishLoadingState();
        if (feedbackDialog != null) {
            feedbackDialog.dismiss();
        }
    }

    private void setActionButtonsEnabled(boolean enabled) {
        btnEnviarCodigo.setEnabled(enabled);
        btnCambiarContrasena.setEnabled(enabled);
        btnReenviarCodigo.setEnabled(enabled && !resendCooldownActive);
        btnCambiarCuenta.setEnabled(enabled);
        btnRegresar.setEnabled(enabled);
        txtVolverLogin.setEnabled(enabled);

        btnEnviarCodigo.setAlpha(enabled ? 1f : 0.68f);
        btnCambiarContrasena.setAlpha(enabled ? 1f : 0.68f);
        btnReenviarCodigo.setAlpha((enabled && !resendCooldownActive) ? 1f : 0.68f);
        btnCambiarCuenta.setAlpha(enabled ? 1f : 0.68f);
        btnRegresar.setAlpha(enabled ? 1f : 0.68f);
        txtVolverLogin.setAlpha(enabled ? 1f : 0.68f);
    }

    private void showFieldError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        field.animate()
                .translationX(8f)
                .setDuration(70)
                .withEndAction(() -> field.animate().translationX(0f).setDuration(90).start())
                .start();
    }

    private void cambiarCuenta() {
        temporaryToken = null;
        etCorreoUsuario.setText("");
        clearAllErrors();
        updateRecoveryStep(false);
        txtEstadoSolicitud.setText("Puedes ingresar otro nombre de usuario para recibir un nuevo c\u00F3digo.");
        etCorreoUsuario.requestFocus();
    }

    private void startResendCooldown() {
        resendCooldownActive = true;
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
        }

        if (btnReenviarCodigo != null) {
            btnReenviarCodigo.setEnabled(false);
            btnReenviarCodigo.setAlpha(0.68f);
        }
        if (txtReenvioCooldown != null) {
            txtReenvioCooldown.setVisibility(View.VISIBLE);
            txtReenvioCooldown.setText("Puedes reenviar el c\u00F3digo en 60 s");
        }

        resendCountDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999L) / 1000L;
                if (txtReenvioCooldown != null) {
                    txtReenvioCooldown.setText("Puedes reenviar el c\u00F3digo en " + seconds + " s");
                }
            }

            @Override
            public void onFinish() {
                resendCooldownActive = false;
                resendCountDownTimer = null;
                if (txtReenvioCooldown != null) {
                    txtReenvioCooldown.setVisibility(View.GONE);
                }
                if (btnReenviarCodigo != null) {
                    btnReenviarCodigo.setText("Reenviar c\u00F3digo");
                    if (!requestInFlight) {
                        btnReenviarCodigo.setEnabled(true);
                        btnReenviarCodigo.setAlpha(1f);
                    }
                }
            }
        };
        resendCountDownTimer.start();
    }

    private void stopResendCooldown() {
        resendCooldownActive = false;
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
            resendCountDownTimer = null;
        }
        if (txtReenvioCooldown != null) {
            txtReenvioCooldown.setVisibility(View.GONE);
        }
        if (btnReenviarCodigo != null) {
            btnReenviarCodigo.setText("Reenviar c\u00F3digo");
            if (!requestInFlight) {
                btnReenviarCodigo.setEnabled(true);
                btnReenviarCodigo.setAlpha(1f);
            }
        }
    }

    private void hideStepAnimation() {
        if (sideLottie == null) {
            return;
        }
        sideLottie.cancelAnimation();
        sideLottie.setVisibility(View.GONE);
    }

    private void showStepAnimation() {
        if (sideLottie == null) {
            return;
        }
        sideLottie.setVisibility(View.VISIBLE);
    }

    private void animateStepTransition(View view) {
        if (view == null) {
            return;
        }
        view.setAlpha(0.88f);
        view.setTranslationY(10f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void playStepAnimation(int rawRes) {
        if (sideLottie == null || sideLottie.getVisibility() != View.VISIBLE) {
            return;
        }
        sideLottie.cancelAnimation();
        sideLottie.setAnimation(rawRes);
        sideLottie.playAnimation();
    }

    private void clearAllErrors() {
        etCorreoUsuario.setError(null);
        etCodigo.setError(null);
        etNuevaContrasena.setError(null);
        etConfirmarContrasena.setError(null);
    }

    private void volverAlLogin() {
        Intent intent = new Intent(this, ActIniciarSesion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String getTrimmedText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private String getRawText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString();
    }

    private String safeMessage(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isBlankForPassword(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsText(String value, String fragment) {
        return value != null && fragment != null && value.contains(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeManager = new ThemeManager(this);
        applyThemeOnlyColors();
        if (sideLottie != null && sideLottie.getVisibility() == View.VISIBLE && !sideLottie.isAnimating()) {
            sideLottie.playAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
        }
        if (sideLottie != null) {
            sideLottie.cancelAnimation();
        }
        if (feedbackDialog != null) {
            feedbackDialog.release();
        }
    }
}
