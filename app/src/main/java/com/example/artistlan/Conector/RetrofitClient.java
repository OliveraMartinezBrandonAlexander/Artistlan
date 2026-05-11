package com.example.artistlan.Conector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.artistlan.Activitys.ActIniciarSesion;
import com.example.artistlan.ArtistlanApp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.100.21:8080/api/";
    // "http://10.0.2.2:8080/api/"
    private static final long UI_NOTICE_THROTTLE_MS = 2000L;
    private static final String TAG_MODERACION_DEBUG = "ModeracionErrorDebug";
    private static final boolean ENABLE_MODERACION_DEBUG_LOGS = false;
    private static final AtomicLong lastUnauthorizedHandledAt = new AtomicLong(0L);
    private static final AtomicLong lastForbiddenHandledAt = new AtomicLong(0L);
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                    message -> {
                        if (isDebugBuild()) {
                            Log.d("ArtistlanHttp", maskSensitiveData(message));
                        }
                    }
            );
            logging.setLevel(isDebugBuild()
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                String token = getCurrentToken();
                if (token != null && original.header("Authorization") == null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();
                Response response = chain.proceed(request);
                handleSecurityResponses(request, response);
                return response;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .callTimeout(90, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    private static String getCurrentToken() {
        Context context = ArtistlanApp.getAppContext();
        if (context == null) {
            return null;
        }
        return new SessionManager(context).getToken();
    }

    private static void handleSecurityResponses(Request request, Response response) {
        if (request == null || response == null) {
            return;
        }

        String path = request.url() != null ? request.url().encodedPath() : "";
        boolean isPublicAuthRequest = path.endsWith("/api/usuarios/login")
                || path.endsWith("/api/auth/2fa/verify-login")
                || path.endsWith("/api/auth/2fa/resend");

        String authHeader = request.header("Authorization");
        boolean hasBearer = authHeader != null && authHeader.startsWith("Bearer ");
        Context appContext = ArtistlanApp.getAppContext();
        boolean hasActiveSession = appContext != null && new SessionManager(appContext).isLoggedIn();

        if (response.code() == 401 && !isPublicAuthRequest && (hasBearer || hasActiveSession)) {
            handleUnauthorized();
            return;
        }
        if (response.code() == 403 && (hasBearer || hasActiveSession)) {
            if (debeOmitirToast403Global(path)) {
                if (ENABLE_MODERACION_DEBUG_LOGS && isDebugBuild()) {
                    Log.d(TAG_MODERACION_DEBUG, "403 omitido en handler global -> endpoint=" + path
                            + ", status=" + response.code()
                            + ", motivo=flujo_moderacion_con_mensaje_especifico");
                }
                return;
            }
            if (ENABLE_MODERACION_DEBUG_LOGS && isDebugBuild()) {
                Log.d(TAG_MODERACION_DEBUG, "403 global -> endpoint=" + path
                        + ", status=" + response.code()
                        + ", accion=mostrar_toast_generico");
            }
            handleForbidden();
        }
    }

    private static boolean debeOmitirToast403Global(String path) {
        return path != null && path.startsWith("/api/moderacion/");
    }

    private static void handleUnauthorized() {
        long now = System.currentTimeMillis();
        if (now - lastUnauthorizedHandledAt.get() < UI_NOTICE_THROTTLE_MS) {
            return;
        }
        lastUnauthorizedHandledAt.set(now);

        Context appContext = ArtistlanApp.getAppContext();
        if (appContext == null) {
            return;
        }

        new SessionManager(appContext).clearSession();

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            Toast.makeText(
                    appContext,
                    "Tu sesi\u00F3n expir\u00F3 o no es v\u00E1lida. Inicia sesi\u00F3n nuevamente.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(appContext, ActIniciarSesion.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            appContext.startActivity(intent);
        });
    }

    private static void handleForbidden() {
        long now = System.currentTimeMillis();
        if (now - lastForbiddenHandledAt.get() < UI_NOTICE_THROTTLE_MS) {
            return;
        }
        lastForbiddenHandledAt.set(now);

        Context appContext = ArtistlanApp.getAppContext();
        if (appContext == null) {
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> Toast.makeText(
                appContext,
                "No tienes permisos para realizar esta acci\u00F3n.",
                Toast.LENGTH_LONG
        ).show());
    }

    private static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String masked = message;
        masked = masked.replaceAll("(\\\"contrasena\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")", "$1***$2");
        masked = masked.replaceAll("(\\\"contrasenaActual\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")", "$1***$2");
        masked = masked.replaceAll("(contrasena=)[^&\\s]*", "$1***");
        return masked;
    }

    private static boolean isDebugBuild() {
        Context context = ArtistlanApp.getAppContext();
        if (context == null || context.getApplicationInfo() == null) {
            return false;
        }
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
