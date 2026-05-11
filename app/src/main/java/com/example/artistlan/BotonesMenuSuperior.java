package com.example.artistlan;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;

public class BotonesMenuSuperior {
    private static final String TAG = "BotonesMenuSuperiorDebug";
    private static final boolean ENABLE_MENU_DEBUG_LOGS = false;
    private static final long NAV_DEBOUNCE_MS = 500L;

    private final Fragment fragmento;
    private ImageButton btnNotificaciones;
    private long ultimoClickNotificacionesMs = 0L;

    public BotonesMenuSuperior(Fragment fragmento) {
        this.fragmento = fragmento;
        inicializarMenuSuperior();
    }

    private void inicializarMenuSuperior() {
        ActFragmentoPrincipal activity = obtenerActividadHostSegura("inicializarMenuSuperior", true);
        if (activity == null) {
            logDebug("inicializarMenuSuperior ignorado: fragment no attached");
            return;
        }

        btnNotificaciones = activity.findViewById(R.id.btnNotificaciones);

        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(this::manejarClickNotificacionesSeguro);
        }

        if (activity != null) {
            activity.refrescarBadgeMensajes();
        }
    }

    private void manejarClickNotificacionesSeguro(View v) {
        long ahora = SystemClock.elapsedRealtime();
        if (ahora - ultimoClickNotificacionesMs < NAV_DEBOUNCE_MS) {
            logDebug("click notificaciones ignorado por debounce");
            return;
        }

        ActFragmentoPrincipal activity = obtenerActividadDesdeView(v);
        if (activity == null) {
            logDebug("clickNotificaciones -> activity desde view no disponible, usando fallback fragment");
            activity = obtenerActividadHostSegura("clickNotificaciones", true);
        } else {
            logDebug("clickNotificaciones -> activity obtenida desde view");
        }
        if (activity == null) {
            logDebug("click notificaciones ignorado: actividad no valida");
            return;
        }

        ultimoClickNotificacionesMs = ahora;
        logDebug("click notificaciones permitido: navegando a centro de mensajes");
        activity.abrirCentroMensajes(0);
    }

    @Nullable
    private ActFragmentoPrincipal obtenerActividadDesdeView(@Nullable View v) {
        if (v == null) {
            return null;
        }
        Context context = v.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof ActFragmentoPrincipal) {
                ActFragmentoPrincipal activity = (ActFragmentoPrincipal) context;
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    return activity;
                }
                return null;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private ActFragmentoPrincipal obtenerActividadHostSegura(String origen, boolean log) {
        boolean isAdded = fragmento != null && fragmento.isAdded();
        if (!isAdded) {
            if (log) {
                logDebug(origen + " -> fragment.isAdded=false");
            }
            return null;
        }

        if (fragmento.getActivity() == null) {
            if (log) {
                logDebug(origen + " -> activity=null");
            }
            return null;
        }

        if (!(fragmento.getActivity() instanceof ActFragmentoPrincipal)) {
            if (log) {
                logDebug(origen + " -> activity no es ActFragmentoPrincipal");
            }
            return null;
        }

        ActFragmentoPrincipal activity = (ActFragmentoPrincipal) fragmento.getActivity();
        if (activity.isFinishing() || activity.isDestroyed()) {
            if (log) {
                logDebug(origen + " -> activity finishing/destroyed");
            }
            return null;
        }

        if (log) {
            logDebug(origen + " -> fragment.isAdded=true, activity OK");
        }
        return activity;
    }

    private void logDebug(String mensaje) {
        if (ENABLE_MENU_DEBUG_LOGS && isDebugBuild()) {
            Log.d(TAG, mensaje);
        }
    }

    private boolean isDebugBuild() {
        Context context = fragmento != null ? fragmento.getContext() : null;
        if (context == null || context.getApplicationInfo() == null) {
            return false;
        }
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
