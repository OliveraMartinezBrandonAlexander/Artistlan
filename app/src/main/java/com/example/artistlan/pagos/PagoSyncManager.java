package com.example.artistlan.pagos;

import android.content.Context;
import android.content.SharedPreferences;

public final class PagoSyncManager {

    private static final String PREFS_NAME = "paypal_pago_sync";
    private static final String KEY_LAST_CAPTURE_AT = "last_capture_at";

    private PagoSyncManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static long getLastCaptureAt(Context context) {
        return prefs(context).getLong(KEY_LAST_CAPTURE_AT, 0L);
    }

    public static void markCaptureSuccess(Context context) {
        prefs(context).edit()
                .putLong(KEY_LAST_CAPTURE_AT, System.currentTimeMillis())
                .apply();
    }
}
