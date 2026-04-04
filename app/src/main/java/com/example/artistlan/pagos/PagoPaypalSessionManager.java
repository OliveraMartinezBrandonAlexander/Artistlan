package com.example.artistlan.pagos;

import android.content.Context;
import android.content.SharedPreferences;

public final class PagoPaypalSessionManager {

    private static final String PREFS_NAME = "paypal_pago_session";
    private static final String KEY_PAYPAL_ORDER_ID = "paypal_order_id";
    private static final String KEY_OBRA_ID = "obra_id";
    private static final String KEY_COMPRADOR_ID = "comprador_id";
    private static final String KEY_PENDING_CAPTURE = "pending_capture";
    private static final String KEY_APPROVAL_RECEIVED_FROM_DEEP_LINK = "approval_received_from_deep_link";

    private PagoPaypalSessionManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void savePendingOrder(Context context, String paypalOrderId, int obraId, int compradorId) {
        prefs(context).edit()
                .putString(KEY_PAYPAL_ORDER_ID, paypalOrderId)
                .putInt(KEY_OBRA_ID, obraId)
                .putInt(KEY_COMPRADOR_ID, compradorId)
                .putBoolean(KEY_PENDING_CAPTURE, false)
                .apply();
    }

    public static void markApprovalOpened(Context context) {
        prefs(context).edit()
                .putBoolean(KEY_PENDING_CAPTURE, true)
                .apply();
    }

    public static void markApprovalReceivedFromDeepLink(Context context, String paypalOrderId) {
        SharedPreferences.Editor editor = prefs(context).edit()
                .putBoolean(KEY_PENDING_CAPTURE, true)
                .putBoolean(KEY_APPROVAL_RECEIVED_FROM_DEEP_LINK, true);
        if (paypalOrderId != null && !paypalOrderId.trim().isEmpty()) {
            editor.putString(KEY_PAYPAL_ORDER_ID, paypalOrderId.trim());
        }
        editor.apply();
    }

    public static boolean hasApprovalFromDeepLink(Context context) {
        return prefs(context).getBoolean(KEY_APPROVAL_RECEIVED_FROM_DEEP_LINK, false);
    }

    public static void clearApprovalDeepLinkFlag(Context context) {
        prefs(context).edit()
                .remove(KEY_APPROVAL_RECEIVED_FROM_DEEP_LINK)
                .apply();
    }

    public static boolean shouldCaptureOnReturn(Context context) {
        return prefs(context).getBoolean(KEY_PENDING_CAPTURE, false)
                && getPendingOrderId(context) != null;
    }

    public static String getPendingOrderId(Context context) {
        return prefs(context).getString(KEY_PAYPAL_ORDER_ID, null);
    }

    public static int getPendingObraId(Context context) {
        return prefs(context).getInt(KEY_OBRA_ID, -1);
    }

    public static int getPendingCompradorId(Context context) {
        return prefs(context).getInt(KEY_COMPRADOR_ID, -1);
    }

    public static void clear(Context context) {
        prefs(context).edit()
                .remove(KEY_PAYPAL_ORDER_ID)
                .remove(KEY_OBRA_ID)
                .remove(KEY_COMPRADOR_ID)
                .remove(KEY_PENDING_CAPTURE)
                .remove(KEY_APPROVAL_RECEIVED_FROM_DEEP_LINK)
                .apply();
    }
}
