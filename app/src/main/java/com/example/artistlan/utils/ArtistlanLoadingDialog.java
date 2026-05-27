package com.example.artistlan.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

public class ArtistlanLoadingDialog {

    private final WeakReference<Context> contextRef;
    @Nullable private final WeakReference<Fragment> fragmentRef;
    private LottieFeedbackDialog feedbackDialog;
    private boolean showing;

    public ArtistlanLoadingDialog(@NonNull Context context) {
        this.contextRef = new WeakReference<>(context);
        this.fragmentRef = null;
    }

    public ArtistlanLoadingDialog(@NonNull Fragment fragment) {
        this.contextRef = new WeakReference<>(fragment.requireContext());
        this.fragmentRef = new WeakReference<>(fragment);
    }

    public void showLoading(@NonNull String message) {
        showLoading(message, false);
    }

    public void showLoading(@NonNull String message, boolean cancelable) {
        if (!canUseUi()) {
            return;
        }
        ensureDialog();
        if (feedbackDialog != null) {
            feedbackDialog.showLoading(message, cancelable);
            showing = true;
        }
    }

    public void showSuccess(@NonNull String message, @Nullable Runnable onDismiss) {
        showSuccess(message, onDismiss, true);
    }

    public void showSuccess(@NonNull String message, @Nullable Runnable onDismiss, boolean autoDismiss) {
        if (!canUseUi()) {
            return;
        }
        ensureDialog();
        if (feedbackDialog != null) {
            feedbackDialog.showSuccess(message, wrapCallback(onDismiss), autoDismiss);
            showing = true;
        }
    }

    public void showError(@NonNull String message) {
        if (!canUseUi()) {
            return;
        }
        ensureDialog();
        if (feedbackDialog != null) {
            feedbackDialog.showError(message);
            showing = true;
        }
    }

    public void updateMessage(@NonNull String message) {
        if (feedbackDialog != null && showing && canUseUi()) {
            feedbackDialog.updateMessage(message);
        }
    }

    public void dismiss() {
        showing = false;
        if (feedbackDialog != null) {
            feedbackDialog.dismiss();
        }
    }

    public void release() {
        showing = false;
        if (feedbackDialog != null) {
            feedbackDialog.release();
            feedbackDialog = null;
        }
    }

    public boolean isShowing() {
        return showing;
    }

    private void ensureDialog() {
        if (feedbackDialog != null) {
            return;
        }
        Context context = contextRef.get();
        if (context != null) {
            feedbackDialog = new LottieFeedbackDialog(context);
        }
    }

    private boolean canUseUi() {
        Fragment fragment = fragmentRef != null ? fragmentRef.get() : null;
        if (fragment != null && (!fragment.isAdded() || fragment.getContext() == null)) {
            return false;
        }
        Context context = contextRef.get();
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
        }
        return true;
    }

    @Nullable
    private Runnable wrapCallback(@Nullable Runnable callback) {
        if (callback == null) {
            return null;
        }
        return () -> {
            showing = false;
            if (canUseUi()) {
                callback.run();
            }
        };
    }
}
