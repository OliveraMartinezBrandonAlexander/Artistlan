package com.example.artistlan.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;

public class LottieFeedbackDialog {

    private static final long SUCCESS_AUTO_DISMISS_MS = 950L;

    private final Context context;
    private Dialog dialog;
    private LottieAnimationView lottieView;
    private TextView titleView;
    private TextView messageView;
    private Button okButton;
    private Runnable successAutoDismiss;
    private final android.os.Handler uiHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public LottieFeedbackDialog(@NonNull Context context) {
        this.context = context;
    }

    public void showLoading(@NonNull String message) {
        showLoading(message, false);
    }

    public void showLoading(@NonNull String message, boolean cancelable) {
        ensureDialog();
        bindState("Cargando", message, R.raw.lottie_time, true, false, null, cancelable);
        showIfNeeded();
    }

    public void showSuccess(@NonNull String message, @Nullable Runnable onDismiss) {
        showSuccess(message, onDismiss, true);
    }

    public void showSuccess(@NonNull String message, @Nullable Runnable onDismiss, boolean autoDismiss) {
        ensureDialog();
        bindState("\u00C9xito", message, R.raw.lottie_success, false, !autoDismiss, this::dismiss, true);
        showIfNeeded();
        if (autoDismiss) {
            scheduleSuccessDismiss(onDismiss);
        }
    }

    public void showError(@NonNull String message) {
        ensureDialog();
        bindState("Error", message, R.raw.lottie_error, false, true, this::dismiss, true);
        showIfNeeded();
    }

    public void updateMessage(@NonNull String message) {
        if (messageView != null) {
            messageView.setText(message);
        }
    }

    public void dismiss() {
        cancelSuccessDismiss();
        if (lottieView != null) {
            lottieView.cancelAnimation();
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void release() {
        dismiss();
        dialog = null;
        lottieView = null;
        titleView = null;
        messageView = null;
        okButton = null;
    }

    private void ensureDialog() {
        if (dialog != null && lottieView != null && titleView != null && messageView != null && okButton != null) {
            return;
        }

        View root = LayoutInflater.from(context).inflate(R.layout.dialog_lottie_feedback, null, false);
        lottieView = root.findViewById(R.id.feedbackLottie);
        titleView = root.findViewById(R.id.feedbackTitle);
        messageView = root.findViewById(R.id.feedbackMessage);
        okButton = root.findViewById(R.id.feedbackOk);

        ThemeManager tm = new ThemeManager(context);
        root.setBackground(DialogThemeHelper.createDialogBackground(context));
        ThemeApplier.applyTextPrimary(titleView, tm);
        ThemeApplier.applyTextSecondary(messageView, tm);
        CardThemeHelper.applySecondaryBubbleButton(okButton, tm);

        dialog = new Dialog(context);
        dialog.setContentView(root);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void bindState(
            @NonNull String title,
            @NonNull String message,
            int lottieRawRes,
            boolean loop,
            boolean showOkButton,
            @Nullable Runnable okAction,
            boolean cancelable
    ) {
        cancelSuccessDismiss();
        if (titleView != null) {
            titleView.setText(title);
        }
        if (messageView != null) {
            messageView.setText(message);
        }
        if (okButton != null) {
            okButton.setVisibility(showOkButton ? View.VISIBLE : View.GONE);
            okButton.setOnClickListener(okAction == null ? null : v -> okAction.run());
        }
        if (dialog != null) {
            dialog.setCancelable(cancelable);
            dialog.setCanceledOnTouchOutside(false);
        }
        if (lottieView != null) {
            lottieView.cancelAnimation();
            lottieView.setAnimation(lottieRawRes);
            lottieView.setRepeatCount(loop ? ValueAnimator.INFINITE : 0);
            lottieView.playAnimation();
        }
    }

    private void showIfNeeded() {
        if (dialog == null) {
            return;
        }
        if (!canShow()) {
            return;
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
        Window window = dialog.getWindow();
        if (window != null) {
            DialogThemeHelper.applyDialogWindowSize(dialog, context);
        }
    }

    private void scheduleSuccessDismiss(@Nullable Runnable onDismiss) {
        successAutoDismiss = () -> {
            dismiss();
            if (onDismiss != null) {
                onDismiss.run();
            }
        };
        uiHandler.postDelayed(successAutoDismiss, SUCCESS_AUTO_DISMISS_MS);
    }

    private void cancelSuccessDismiss() {
        if (successAutoDismiss != null) {
            uiHandler.removeCallbacks(successAutoDismiss);
            successAutoDismiss = null;
        }
    }

    private boolean canShow() {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
        }
        return true;
    }
}

