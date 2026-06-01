package com.example.artistlan.Activitys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.Auth2FAApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.AuthErrorResponseDTO;
import com.example.artistlan.Conector.model.TwoFactorResendRequest;
import com.example.artistlan.Conector.model.TwoFactorResponse;
import com.example.artistlan.Conector.model.TwoFactorVerifyActivationRequest;
import com.example.artistlan.Conector.model.TwoFactorVerifyLoginRequest;
import com.example.artistlan.Conector.model.TwoFactorVerifyLoginResponse;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.ArtistlanLoadingDialog;
import com.example.artistlan.utils.CardThemeHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActVerificarOtpLogin extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_2fa_mode";
    public static final String EXTRA_TEMPORARY_TOKEN = "extra_temporary_token";
    public static final String EXTRA_USUARIO = "extra_usuario";
    public static final String EXTRA_CORREO = "extra_correo";
    public static final String EXTRA_IDENTIFIER_TYPE = "extra_identifier_type";
    public static final String EXTRA_IDENTIFIER = "extra_identifier";

    public static final String MODE_LOGIN = "MODE_LOGIN";
    public static final String MODE_ACTIVATION = "MODE_ACTIVATION";

    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000L;
    private static final long RESEND_COOLDOWN_MS = 120 * 1000L;

    private EditText etCode;
    private Button btnVerify;
    private Button btnResend;
    private ImageButton btnBack;
    private TextView tvTimer;
    private TextView tvInfo;
    private TextView tvResendCooldown;
    private TextView tvTitle;
    private View root;
    private View card;
    private LottieAnimationView otpLottie;

    private Auth2FAApi auth2FAApi;
    private UsuarioApi usuarioApi;
    private SessionManager sessionManager;
    private ThemeManager themeManager;
    private ArtistlanLoadingDialog feedbackDialog;
    private CountDownTimer otpCountDownTimer;
    private CountDownTimer resendCountDownTimer;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean resendCooldownActive = false;
    private boolean requestInFlight = false;

    private String mode = MODE_LOGIN;
    private String temporaryToken;
    private String jwtToken;
    private String loginIdentifierType;
    private String loginIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_verificar_otp_login);

        etCode = findViewById(R.id.otpCodeInput);
        btnVerify = findViewById(R.id.otpVerifyButton);
        btnResend = findViewById(R.id.otpResendButton);
        btnBack = findViewById(R.id.otpBackButton);
        tvTimer = findViewById(R.id.otpTimerText);
        tvInfo = findViewById(R.id.otpInfoText);
        tvResendCooldown = findViewById(R.id.otpResendCooldownText);
        tvTitle = findViewById(R.id.otpTitleText);
        root = findViewById(R.id.otpRoot);
        card = findViewById(R.id.otpCard);
        otpLottie = findViewById(R.id.otpLottie);

        auth2FAApi = RetrofitClient.getClient().create(Auth2FAApi.class);
        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);
        sessionManager = new SessionManager(this);
        themeManager = new ThemeManager(this);
        feedbackDialog = new ArtistlanLoadingDialog(this);

        applyTheme();

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null || mode.trim().isEmpty()) {
            mode = MODE_LOGIN;
        }

        temporaryToken = getIntent().getStringExtra(EXTRA_TEMPORARY_TOKEN);
        loginIdentifierType = getIntent().getStringExtra(EXTRA_IDENTIFIER_TYPE);
        loginIdentifier = getIntent().getStringExtra(EXTRA_IDENTIFIER);
        jwtToken = sessionManager.getToken();

        if (MODE_LOGIN.equals(mode)) {
            if (temporaryToken == null || temporaryToken.trim().isEmpty()) {
                finishWithError("No se pudo continuar con la verificación. Inicia sesión nuevamente.");
                return;
            }
        } else if (MODE_ACTIVATION.equals(mode)) {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                finishWithError("Sesión no válida. Vuelve a iniciar sesión.");
                return;
            }
        }

        tvInfo.setText("Te enviamos un código al correo asociado a tu cuenta.");

        btnVerify.setOnClickListener(v -> verifyCode());
        btnResend.setOnClickListener(v -> resendCode());
        btnBack.setOnClickListener(v -> finish());

        startOtpTimer();
    }

    private void applyTheme() {
        ThemeApplier.applySystemBars(this, themeManager);
        if (root != null) {
            root.setBackgroundColor(themeManager.color(ThemeKeys.BG_BOTTOM));
        }
        if (card != null && card.getBackground() != null) {
            card.getBackground().setColorFilter(themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL), PorterDuff.Mode.SRC_ATOP);
        }
        ThemeApplier.applyTextPrimary(tvTitle, themeManager);
        ThemeApplier.applyTextSecondary(tvInfo, themeManager);
        ThemeApplier.applyTextSecondary(tvTimer, themeManager);
        ThemeApplier.applyTextSecondary(tvResendCooldown, themeManager);
        ThemeApplier.applyInput(etCode, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnVerify, themeManager);
        CardThemeHelper.applySecondaryBubbleButton(btnResend, themeManager);
        if (btnBack != null) {
            btnBack.setColorFilter(themeManager.color(ThemeKeys.ICON_ACTIVE), PorterDuff.Mode.SRC_IN);
            ThemeApplier.animatePress(btnBack);
        }
        ThemeApplier.animatePress(btnVerify);
        ThemeApplier.animatePress(btnResend);
    }

    private void verifyCode() {
        if (requestInFlight) {
            return;
        }
        String code = etCode.getText().toString().trim();
        if (!code.matches("\\d{6}")) {
            etCode.setError("El código debe tener 6 dígitos");
            etCode.requestFocus();
            feedbackDialog.showError("Ingresa el código de 6 dígitos.");
            return;
        }

        if (MODE_ACTIVATION.equals(mode)) {
            verifyActivationCode(code);
            return;
        }
        verifyLoginCode(code);
    }

    private void verifyLoginCode(String code) {
        setLoadingState(true, "Verificando código...");

        TwoFactorVerifyLoginRequest request = new TwoFactorVerifyLoginRequest(temporaryToken, code);
        auth2FAApi.verifyLogin(request).enqueue(new Callback<TwoFactorVerifyLoginResponse>() {
            @Override
            public void onResponse(Call<TwoFactorVerifyLoginResponse> call, Response<TwoFactorVerifyLoginResponse> response) {
                closeLoadingState();

                if (!response.isSuccessful()) {
                    handleVerifyLoginErrorResponse(response);
                    return;
                }

                if (response.body() == null) {
                    feedbackDialog.showError("No se pudo verificar el código");
                    return;
                }

                TwoFactorVerifyLoginResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    feedbackDialog.showError(body.getMessage() != null ? body.getMessage() : "Código incorrecto o expirado");
                    return;
                }

                UsuariosDTO user = body.getUser();
                if (user == null || user.getIdUsuario() == null || user.getIdUsuario() <= 0) {
                    feedbackDialog.showError("No pudimos validar tu cuenta. Intenta de nuevo.");
                    return;
                }

                String token = body.getToken();
                if (token == null || token.trim().isEmpty() || "null".equalsIgnoreCase(token.trim())) {
                    feedbackDialog.showError("No pudimos confirmar tu sesión. Inicia sesión nuevamente.");
                    return;
                }

                guardarUsuarioLogeado(user, token);
                sessionManager.saveLastLoginIdentifier(loginIdentifier, loginIdentifierType);
                cargarCategoriaUsuario(user.getIdUsuario());

                Intent intent = new Intent(ActVerificarOtpLogin.this, ActFragmentoPrincipal.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                feedbackDialog.showSuccess("Código verificado", () -> {
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(Call<TwoFactorVerifyLoginResponse> call, Throwable t) {
                closeLoadingState();
                feedbackDialog.showError("Error de conexión al verificar código");
            }
        });
    }

    private void handleVerifyLoginErrorResponse(Response<TwoFactorVerifyLoginResponse> response) {
        int code = response != null ? response.code() : -1;

        if (code == 423) {
            AuthErrorResponseDTO authError = ApiErrorParser.extractAuthError(response);
            handleBlockedVerifyLogin(buildReadableAuthErrorMessage(
                    authError,
                    "Tu cuenta está suspendida temporalmente. Intenta nuevamente cuando termine la suspensión."
            ));
            return;
        }

        if (code == 403) {
            AuthErrorResponseDTO authError = ApiErrorParser.extractAuthError(response);
            handleBlockedVerifyLogin(buildReadableAuthErrorMessage(
                    authError,
                    "Tu cuenta no puede iniciar sesión. Puede estar desactivada o bloqueada."
            ));
            return;
        }

        String backendMessage = ApiErrorParser.extractMessage(response);

        if (code == 400 || code == 401) {
            feedbackDialog.showError(backendMessage != null ? backendMessage : "Código incorrecto o expirado");
            return;
        }

        feedbackDialog.showError(backendMessage != null ? backendMessage : "No se pudo verificar el código");
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

    private void handleBlockedVerifyLogin(String message) {
        temporaryToken = null;

        Intent intent = new Intent(ActVerificarOtpLogin.this, ActIniciarSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        feedbackDialog.showError(message);
        uiHandler.postDelayed(() -> {
            if (!isFinishing()) {
                startActivity(intent);
                finish();
            }
        }, 900L);
    }

    private void verifyActivationCode(String code) {
        setLoadingState(true, "Verificando código...");

        auth2FAApi.verifyActivation(
                buildAuthorizationHeader(),
                new TwoFactorVerifyActivationRequest(code)
        ).enqueue(new Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(Call<TwoFactorResponse> call, Response<TwoFactorResponse> response) {
                closeLoadingState();

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    feedbackDialog.showError(backendMessage != null ? backendMessage : "No se pudo verificar el código");
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    feedbackDialog.showError(body.getMessage() != null ? body.getMessage() : "Código incorrecto o expirado");
                    return;
                }

                sessionManager.updateTwoFactorEnabled(true);
                feedbackDialog.showSuccess("La verificación en dos pasos se activó correctamente.", ActVerificarOtpLogin.this::finish);
            }

            @Override
            public void onFailure(Call<TwoFactorResponse> call, Throwable t) {
                closeLoadingState();
                feedbackDialog.showError("Error de conexión al verificar código");
            }
        });
    }

    private void resendCode() {
        if (requestInFlight) {
            return;
        }
        if (MODE_ACTIVATION.equals(mode)) {
            resendActivationCode();
            return;
        }
        resendLoginCode();
    }

    private void resendLoginCode() {
        if (temporaryToken == null || temporaryToken.trim().isEmpty()) {
            feedbackDialog.showError("No se pudo continuar con la verificación. Inicia sesión nuevamente.");
            return;
        }

        setLoadingState(true, "Reenviando código...");
        TwoFactorResendRequest request = new TwoFactorResendRequest(temporaryToken);

        auth2FAApi.resend(request).enqueue(new Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(Call<TwoFactorResponse> call, Response<TwoFactorResponse> response) {
                closeLoadingState();

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    feedbackDialog.showError(backendMessage != null ? backendMessage : "No se pudo reenviar el código");
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    feedbackDialog.showError(body.getMessage() != null ? body.getMessage() : "No se pudo reenviar el código");
                    return;
                }

                if (body.getTemporaryToken() != null && !body.getTemporaryToken().trim().isEmpty()) {
                    temporaryToken = body.getTemporaryToken();
                }

                startOtpTimer();
                startResendCooldown();
                feedbackDialog.showSuccess("Código reenviado", null);
            }

            @Override
            public void onFailure(Call<TwoFactorResponse> call, Throwable t) {
                closeLoadingState();
                feedbackDialog.showError("Error de conexión al reenviar código");
            }
        });
    }

    private void resendActivationCode() {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            feedbackDialog.showError("Sesión no válida. Vuelve a iniciar sesión.");
            return;
        }

        setLoadingState(true, "Reenviando código...");
        auth2FAApi.requestActivation(buildAuthorizationHeader()).enqueue(new Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(Call<TwoFactorResponse> call, Response<TwoFactorResponse> response) {
                closeLoadingState();

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    feedbackDialog.showError(backendMessage != null ? backendMessage : "No se pudo reenviar el código");
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    feedbackDialog.showError(body.getMessage() != null ? body.getMessage() : "No se pudo reenviar el código");
                    return;
                }

                startOtpTimer();
                startResendCooldown();
                feedbackDialog.showSuccess("Código reenviado", null);
            }

            @Override
            public void onFailure(Call<TwoFactorResponse> call, Throwable t) {
                closeLoadingState();
                feedbackDialog.showError("Error de conexión al reenviar código");
            }
        });
    }

    private String buildAuthorizationHeader() {
        jwtToken = sessionManager.getToken();
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            return null;
        }
        return "Bearer " + jwtToken.trim();
    }

    private void guardarUsuarioLogeado(UsuariosDTO usuario, String jwtToken) {
        SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
        String categoria = usuario.getCategoria();

        sessionManager.saveUserSession(usuario, jwtToken);

        if (categoria != null && !categoria.trim().isEmpty()) {
            prefs.edit()
                    .putString("categoria", categoria)
                    .putString("ocupacion", categoria)
                    .apply();
        }
    }

    private void cargarCategoriaUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            return;
        }

        usuarioApi.obtenerCategoriaUsuario(idUsuario).enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                String categoria = response.body().getCategoria();
                if (categoria == null || categoria.trim().isEmpty()) {
                    categoria = "Sin categoría";
                }

                SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
                prefs.edit()
                        .putString("categoria", categoria)
                        .putString("ocupacion", categoria)
                        .apply();
            }

            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                SharedPreferences prefs = getSharedPreferences(SessionManager.PREF_NAME, MODE_PRIVATE);
                prefs.edit()
                        .putString("categoria", "Sin categoría")
                        .putString("ocupacion", "Sin categoría")
                        .apply();
            }
        });
    }

    private void setLoadingState(boolean loading, String message) {
        requestInFlight = loading;
        btnVerify.setEnabled(!loading);
        etCode.setEnabled(!loading);
        btnResend.setEnabled(!loading && !resendCooldownActive);
        btnBack.setEnabled(!loading);
        btnVerify.setAlpha(loading ? 0.68f : 1f);
        btnResend.setAlpha((loading || resendCooldownActive) ? 0.68f : 1f);
        btnBack.setAlpha(loading ? 0.68f : 1f);
        if (loading) {
            feedbackDialog.showLoading(message);
        }
    }

    private void closeLoadingState() {
        requestInFlight = false;
        btnVerify.setEnabled(true);
        etCode.setEnabled(true);
        btnResend.setEnabled(!resendCooldownActive);
        btnBack.setEnabled(true);
        btnVerify.setAlpha(1f);
        btnResend.setAlpha(resendCooldownActive ? 0.68f : 1f);
        btnBack.setAlpha(1f);
        feedbackDialog.dismiss();
    }

    private void finishWithError(String message) {
        feedbackDialog.showError(message);
        uiHandler.postDelayed(() -> {
            if (!isFinishing()) {
                finish();
            }
        }, 900L);
    }

    private void startOtpTimer() {
        if (otpCountDownTimer != null) {
            otpCountDownTimer.cancel();
        }

        otpCountDownTimer = new CountDownTimer(OTP_EXPIRATION_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000L;
                long minutes = totalSeconds / 60L;
                long seconds = totalSeconds % 60L;
                tvTimer.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("El código ha expirado. Solicita uno nuevo.");
            }
        };
        otpCountDownTimer.start();
    }

    private void startResendCooldown() {
        resendCooldownActive = true;
        btnResend.setEnabled(false);
        tvResendCooldown.setVisibility(View.VISIBLE);
        tvResendCooldown.setText("Puedes reenviar el código en 120 s");

        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
        }

        resendCountDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999L) / 1000L;
                tvResendCooldown.setText("Puedes reenviar el código en " + seconds + " s");
            }

            @Override
            public void onFinish() {
                resendCooldownActive = false;
                btnResend.setEnabled(true);
                tvResendCooldown.setVisibility(View.GONE);
            }
        };

        resendCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpCountDownTimer != null) {
            otpCountDownTimer.cancel();
        }
        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
        }
        uiHandler.removeCallbacksAndMessages(null);
        if (otpLottie != null) {
            otpLottie.cancelAnimation();
        }
        if (feedbackDialog != null) {
            feedbackDialog.release();
        }
    }
}
