package com.example.artistlan.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.Auth2FAApi;
import com.example.artistlan.Conector.model.TwoFactorResendRequest;
import com.example.artistlan.Conector.model.TwoFactorResponse;
import com.example.artistlan.Conector.model.TwoFactorVerifyActivationRequest;
import com.example.artistlan.Conector.model.TwoFactorVerifyLoginRequest;
import com.example.artistlan.Conector.model.TwoFactorVerifyLoginResponse;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActVerificarOtpLogin extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_2fa_mode";
    public static final String EXTRA_TEMPORARY_TOKEN = "extra_temporary_token";
    public static final String EXTRA_USUARIO = "extra_usuario";
    public static final String EXTRA_CORREO = "extra_correo";

    public static final String MODE_LOGIN = "MODE_LOGIN";
    public static final String MODE_ACTIVATION = "MODE_ACTIVATION";

    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000L;
    private static final long RESEND_COOLDOWN_MS = 120 * 1000L;

    private EditText etCode;
    private Button btnVerify;
    private Button btnResend;
    private TextView tvTimer;
    private TextView tvInfo;
    private TextView tvResendCooldown;

    private Auth2FAApi auth2FAApi;
    private SessionManager sessionManager;
    private CountDownTimer otpCountDownTimer;
    private CountDownTimer resendCountDownTimer;
    private boolean resendCooldownActive = false;

    private String mode = MODE_LOGIN;
    private String temporaryToken;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_verificar_otp_login);

        etCode = findViewById(R.id.otpCodeInput);
        btnVerify = findViewById(R.id.otpVerifyButton);
        btnResend = findViewById(R.id.otpResendButton);
        tvTimer = findViewById(R.id.otpTimerText);
        tvInfo = findViewById(R.id.otpInfoText);
        tvResendCooldown = findViewById(R.id.otpResendCooldownText);

        auth2FAApi = RetrofitClient.getClient().create(Auth2FAApi.class);
        sessionManager = new SessionManager(this);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null || mode.trim().isEmpty()) {
            mode = MODE_LOGIN;
        }

        temporaryToken = getIntent().getStringExtra(EXTRA_TEMPORARY_TOKEN);
        jwtToken = sessionManager.getToken();

        if (MODE_LOGIN.equals(mode)) {
            if (temporaryToken == null || temporaryToken.trim().isEmpty()) {
                Toast.makeText(this, "Token temporal invalido. Inicia sesion nuevamente.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else if (MODE_ACTIVATION.equals(mode)) {
            if (jwtToken == null || jwtToken.trim().isEmpty()) {
                Toast.makeText(this, "Sesion no valida. Vuelve a iniciar sesion.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        tvInfo.setText("Te enviamos un codigo al correo asociado a tu cuenta.");

        btnVerify.setOnClickListener(v -> verifyCode());
        btnResend.setOnClickListener(v -> resendCode());

        startOtpTimer();
    }

    private void verifyCode() {
        String code = etCode.getText().toString().trim();
        if (!code.matches("\\d{6}")) {
            etCode.setError("El codigo debe tener 6 digitos");
            etCode.requestFocus();
            return;
        }

        if (MODE_ACTIVATION.equals(mode)) {
            verifyActivationCode(code);
            return;
        }
        verifyLoginCode(code);
    }

    private void verifyLoginCode(String code) {
        setLoadingState(true);

        TwoFactorVerifyLoginRequest request = new TwoFactorVerifyLoginRequest(temporaryToken, code);
        auth2FAApi.verifyLogin(request).enqueue(new Callback<TwoFactorVerifyLoginResponse>() {
            @Override
            public void onResponse(Call<TwoFactorVerifyLoginResponse> call, Response<TwoFactorVerifyLoginResponse> response) {
                setLoadingState(false);

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            backendMessage != null ? backendMessage : "No se pudo verificar el codigo",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                TwoFactorVerifyLoginResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            body.getMessage() != null ? body.getMessage() : "Codigo incorrecto o expirado",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                UsuariosDTO user = body.getUser();
                if (user == null || user.getIdUsuario() == null || user.getIdUsuario() <= 0) {
                    Toast.makeText(ActVerificarOtpLogin.this, "Respuesta invalida del servidor", Toast.LENGTH_LONG).show();
                    return;
                }

                sessionManager.saveUserSession(user, body.getToken());

                Intent intent = new Intent(ActVerificarOtpLogin.this, ActFragmentoPrincipal.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<TwoFactorVerifyLoginResponse> call, Throwable t) {
                setLoadingState(false);
                Toast.makeText(ActVerificarOtpLogin.this, "Error de conexion al verificar codigo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyActivationCode(String code) {
        setLoadingState(true);

        auth2FAApi.verifyActivation(
                buildAuthorizationHeader(),
                new TwoFactorVerifyActivationRequest(code)
        ).enqueue(new Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(Call<TwoFactorResponse> call, Response<TwoFactorResponse> response) {
                setLoadingState(false);

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            backendMessage != null ? backendMessage : "No se pudo verificar el codigo",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            body.getMessage() != null ? body.getMessage() : "Codigo incorrecto o expirado",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                sessionManager.updateTwoFactorEnabled(true);
                Toast.makeText(ActVerificarOtpLogin.this, "2FA activado correctamente", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<TwoFactorResponse> call, Throwable t) {
                setLoadingState(false);
                Toast.makeText(ActVerificarOtpLogin.this, "Error de conexion al verificar codigo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendCode() {
        if (MODE_ACTIVATION.equals(mode)) {
            resendActivationCode();
            return;
        }
        resendLoginCode();
    }

    private void resendLoginCode() {
        if (temporaryToken == null || temporaryToken.trim().isEmpty()) {
            Toast.makeText(this, "Token temporal invalido", Toast.LENGTH_LONG).show();
            return;
        }

        setLoadingState(true);
        TwoFactorResendRequest request = new TwoFactorResendRequest(temporaryToken);

        auth2FAApi.resend(request).enqueue(new Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(Call<TwoFactorResponse> call, Response<TwoFactorResponse> response) {
                setLoadingState(false);

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            backendMessage != null ? backendMessage : "No se pudo reenviar el codigo",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            body.getMessage() != null ? body.getMessage() : "No se pudo reenviar el codigo",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                if (body.getTemporaryToken() != null && !body.getTemporaryToken().trim().isEmpty()) {
                    temporaryToken = body.getTemporaryToken();
                }

                startOtpTimer();
                startResendCooldown();
                Toast.makeText(ActVerificarOtpLogin.this, "Codigo reenviado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<TwoFactorResponse> call, Throwable t) {
                setLoadingState(false);
                Toast.makeText(ActVerificarOtpLogin.this, "Error de conexion al reenviar codigo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendActivationCode() {
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            Toast.makeText(this, "Sesion no valida. Vuelve a iniciar sesion.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoadingState(true);
        auth2FAApi.requestActivation(buildAuthorizationHeader()).enqueue(new Callback<TwoFactorResponse>() {
            @Override
            public void onResponse(Call<TwoFactorResponse> call, Response<TwoFactorResponse> response) {
                setLoadingState(false);

                if (!response.isSuccessful() || response.body() == null) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            backendMessage != null ? backendMessage : "No se pudo reenviar el codigo",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                TwoFactorResponse body = response.body();
                if (!Boolean.TRUE.equals(body.getSuccess())) {
                    Toast.makeText(
                            ActVerificarOtpLogin.this,
                            body.getMessage() != null ? body.getMessage() : "No se pudo reenviar el codigo",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                startOtpTimer();
                startResendCooldown();
                Toast.makeText(ActVerificarOtpLogin.this, "Codigo reenviado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<TwoFactorResponse> call, Throwable t) {
                setLoadingState(false);
                Toast.makeText(ActVerificarOtpLogin.this, "Error de conexion al reenviar codigo", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String buildAuthorizationHeader() {
        jwtToken = sessionManager.getToken();
        return "Bearer " + jwtToken;
    }

    private void setLoadingState(boolean loading) {
        btnVerify.setEnabled(!loading);
        etCode.setEnabled(!loading);
        btnResend.setEnabled(!loading && !resendCooldownActive);
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
                tvTimer.setText("El codigo ha expirado. Solicita uno nuevo.");
            }
        };
        otpCountDownTimer.start();
    }

    private void startResendCooldown() {
        resendCooldownActive = true;
        btnResend.setEnabled(false);
        tvResendCooldown.setVisibility(View.VISIBLE);
        tvResendCooldown.setText("Puedes reenviar el codigo en 120s");

        if (resendCountDownTimer != null) {
            resendCountDownTimer.cancel();
        }

        resendCountDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999L) / 1000L;
                tvResendCooldown.setText("Puedes reenviar el codigo en " + seconds + "s");
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
    }
}
