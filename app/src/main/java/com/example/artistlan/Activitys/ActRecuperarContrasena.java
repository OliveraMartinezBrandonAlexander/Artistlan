package com.example.artistlan.Activitys;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.example.artistlan.utils.LottieFeedbackDialog;
import com.example.artistlan.utils.PasswordPressVisibilityHelper;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActRecuperarContrasena extends AppCompatActivity implements View.OnClickListener {

    private static final int OTP_LENGTH = 6;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 72;
    private static final String DEFAULT_REQUEST_MESSAGE =
            "Si la cuenta existe y es recuperable, enviaremos un código de recuperación.";
    private static final String REQUEST_TOKEN_CREATED_MESSAGE =
            "Código enviado. Revisa tu correo.";
    private static final String DEFAULT_RESEND_MESSAGE =
            "Si la solicitud sigue siendo válida, enviaremos un nuevo código.";
    private static final String DEFAULT_SUCCESS_MESSAGE =
            "Contraseña actualizada correctamente.";

    private ImageButton btnRegresar;
    private Button btnEnviarCodigo;
    private Button btnCambiarContrasena;
    private Button btnReenviarCodigo;
    private TextView txtVolverLogin;
    private TextView txtBrand;
    private TextView txtTitulo;
    private TextView txtInstruccion;
    private TextView txtCorreoUsuarioLbl;
    private TextView txtCodigoLbl;
    private TextView txtNuevaContrasenaLbl;
    private TextView txtConfirmarContrasenaLbl;
    private TextView txtEstadoSolicitud;
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
    private LottieFeedbackDialog feedbackDialog;
    private PasswordResetApi passwordResetApi;

    private String temporaryToken;
    private boolean requestInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_act_recuperar_contrasena);

        themeManager = new ThemeManager(this);
        feedbackDialog = new LottieFeedbackDialog(this);
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
        txtVolverLogin = findViewById(R.id.RcTxtVolverLogin);

        txtBrand = findViewById(R.id.RcTxtBrand);
        txtTitulo = findViewById(R.id.RcTxtTitulo);
        txtInstruccion = findViewById(R.id.RcTxtInstruccion);
        txtCorreoUsuarioLbl = findViewById(R.id.RcTxtCorreoUsuarioLbl);
        txtCodigoLbl = findViewById(R.id.RcTxtCodigoLbl);
        txtNuevaContrasenaLbl = findViewById(R.id.RcTxtNuevaContrasenaLbl);
        txtConfirmarContrasenaLbl = findViewById(R.id.RcTxtConfirmarContrasenaLbl);
        txtEstadoSolicitud = findViewById(R.id.RcTxtEstadoSolicitud);

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

        if (txtVolverLogin != null) {
            txtVolverLogin.setTextColor(themeManager.color(ThemeKeys.ICON_ACTIVE));
        }
        if (btnRegresar != null) {
            btnRegresar.setColorFilter(themeManager.color(ThemeKeys.ICON_ACTIVE), PorterDuff.Mode.SRC_IN);
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

        ThemeApplier.applyPrimaryButton(btnEnviarCodigo, themeManager);
        ThemeApplier.applyPrimaryButton(btnCambiarContrasena, themeManager);
        ThemeApplier.applySecondaryButton(btnReenviarCodigo, themeManager);

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
        txtVolverLogin.setOnClickListener(this);

        ThemeApplier.animatePress(btnRegresar);
        ThemeApplier.animatePress(btnEnviarCodigo);
        ThemeApplier.animatePress(btnCambiarContrasena);
        ThemeApplier.animatePress(btnReenviarCodigo);
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
            txtInstruccion.setText("Revisa tu correo y completa el cambio de contraseña.");
            txtEstadoSolicitud.setText("Captura el código de 6 dígitos. Expira en 5 minutos.");
            etCodigo.requestFocus();
            return;
        }

        txtInstruccion.setText("Ingresa tu correo o usuario para recibir un código de recuperación.");
        txtEstadoSolicitud.setText("Te enviaremos un código si la cuenta puede recuperarse.");
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
        } else if (viewId == R.id.RcBtnRegresar || viewId == R.id.RcTxtVolverLogin) {
            finish();
        }
    }

    private void solicitarCodigo() {
        clearAllErrors();

        String usuarioOCorreo = getTrimmedText(etCorreoUsuario);
        if (usuarioOCorreo.isEmpty()) {
            showFieldError(etCorreoUsuario, "Ingresa tu correo o usuario.");
            return;
        }

        setLoadingState("Enviando código...");
        passwordResetApi.requestReset(new PasswordResetRequestDTO(usuarioOCorreo))
                .enqueue(new Callback<PasswordResetResponseDTO>() {
                    @Override
                    public void onResponse(Call<PasswordResetResponseDTO> call, Response<PasswordResetResponseDTO> response) {
                        closeLoadingState();
                        if (response.isSuccessful() && response.body() != null) {
                            PasswordResetResponseDTO body = response.body();
                            temporaryToken = trimToNull(body.getTemporaryToken());
                            if (temporaryToken != null) {
                                updateRecoveryStep(true);
                                feedbackDialog.showSuccess(REQUEST_TOKEN_CREATED_MESSAGE, null);
                                return;
                            }

                            updateRecoveryStep(false);
                            txtEstadoSolicitud.setText(safeMessage(body.getMessage(), DEFAULT_REQUEST_MESSAGE));
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
            feedbackDialog.showError("Solicita un código de recuperación.");
            return;
        }

        String codigo = getTrimmedText(etCodigo);
        String nuevaContrasena = getRawText(etNuevaContrasena);
        String confirmarContrasena = getRawText(etConfirmarContrasena);

        if (codigo.isEmpty()) {
            showFieldError(etCodigo, "Ingresa el código de recuperación.");
            return;
        }
        if (!codigo.matches("\\d{" + OTP_LENGTH + "}")) {
            showFieldError(etCodigo, "Ingresa un código válido de 6 dígitos.");
            return;
        }
        if (isBlankForPassword(nuevaContrasena)) {
            showFieldError(etNuevaContrasena, "Ingresa una nueva contraseña.");
            return;
        }
        if (isBlankForPassword(confirmarContrasena)) {
            showFieldError(etConfirmarContrasena, "Confirma la nueva contraseña.");
            return;
        }
        if (nuevaContrasena.length() < PASSWORD_MIN_LENGTH || nuevaContrasena.length() > PASSWORD_MAX_LENGTH) {
            showFieldError(etNuevaContrasena, "Usa una contraseña de 8 a 72 caracteres.");
            return;
        }
        if (!TextUtils.equals(nuevaContrasena, confirmarContrasena)) {
            showFieldError(etConfirmarContrasena, "Las contraseñas no coinciden.");
            return;
        }

        setLoadingState("Actualizando contraseña...");
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
            feedbackDialog.showError("Solicita un código de recuperación.");
            return;
        }

        setLoadingState("Reenviando código...");
        passwordResetApi.resend(new PasswordResetResendRequestDTO(temporaryToken))
                .enqueue(new Callback<PasswordResetResponseDTO>() {
                    @Override
                    public void onResponse(Call<PasswordResetResponseDTO> call, Response<PasswordResetResponseDTO> response) {
                        closeLoadingState();
                        if (response.isSuccessful() && response.body() != null) {
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

        if (code == 429) {
            feedbackDialog.showError(safeMessage(backendMessage, "Espera antes de solicitar otro código."));
            return;
        }

        feedbackDialog.showError(
                safeMessage(backendMessage, "No se pudo enviar el código. Intenta de nuevo.")
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
                showFieldError(etCodigo, "Código incorrecto.");
                return;
            }
            if (containsText(normalized, "codigo expirado") || containsText(normalized, "token invalido")) {
                temporaryToken = null;
                updateRecoveryStep(false);
                feedbackDialog.showError("El código ya no es válido. Solicita otro código.");
                etCorreoUsuario.requestFocus();
                return;
            }
            if (containsText(normalized, "contrasenas no coinciden")) {
                showFieldError(etConfirmarContrasena, "Las contraseñas no coinciden.");
                return;
            }
            if (containsText(normalized, "debe tener entre")) {
                showFieldError(etNuevaContrasena, "Usa una contraseña de 8 a 72 caracteres.");
                return;
            }
            if (containsText(normalized, "diferente a la actual")) {
                showFieldError(etNuevaContrasena, "La nueva contraseña debe ser diferente a la actual.");
                return;
            }
        }

        feedbackDialog.showError(
                safeMessage(backendMessage, "No se pudo cambiar la contraseña. Intenta de nuevo.")
        );
    }

    private void manejarErrorReenvio(Response<PasswordResetResponseDTO> response) {
        String backendMessage = ApiErrorParser.extractMessage(response);
        String normalized = normalize(backendMessage);
        int code = response != null ? response.code() : -1;

        if (code == 429) {
            feedbackDialog.showError(safeMessage(backendMessage, "Espera antes de solicitar un nuevo código."));
            return;
        }

        if (code == 400 || code == 410 || containsText(normalized, "token invalido") || containsText(normalized, "codigo expirado")) {
            temporaryToken = null;
            updateRecoveryStep(false);
            feedbackDialog.showError("La solicitud ya no es válida. Pide un código nuevo.");
            etCorreoUsuario.requestFocus();
            return;
        }

        feedbackDialog.showError(
                safeMessage(backendMessage, "No se pudo reenviar el código.")
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
        btnReenviarCodigo.setEnabled(enabled);
        btnRegresar.setEnabled(enabled);
        txtVolverLogin.setEnabled(enabled);

        btnEnviarCodigo.setAlpha(enabled ? 1f : 0.68f);
        btnCambiarContrasena.setAlpha(enabled ? 1f : 0.68f);
        btnReenviarCodigo.setAlpha(enabled ? 1f : 0.68f);
        btnRegresar.setAlpha(enabled ? 1f : 0.68f);
        txtVolverLogin.setAlpha(enabled ? 1f : 0.68f);
    }

    private void showFieldError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
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
        if (sideLottie != null && !sideLottie.isAnimating()) {
            sideLottie.playAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sideLottie != null) {
            sideLottie.cancelAnimation();
        }
        if (feedbackDialog != null) {
            feedbackDialog.release();
        }
    }
}
