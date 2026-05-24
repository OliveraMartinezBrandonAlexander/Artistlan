package com.example.artistlan.utils;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
        ensureDialog();
        bindState("Cargando", message, R.raw.lottie_time, true, false, null);
        showIfNeeded();
    }

    public void showSuccess(@NonNull String message, @Nullable Runnable onDismiss) {
        ensureDialog();
        bindState("\u00C9xito", message, R.raw.lottie_success, false, false, null);
        showIfNeeded();
        scheduleSuccessDismiss(onDismiss);
    }

    public void showError(@NonNull String message) {
        ensureDialog();
        bindState("Error", message, R.raw.lottie_error, false, true, this::dismiss);
        showIfNeeded();
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
        ThemeApplier.applyTextPrimary(titleView, tm);
        ThemeApplier.applyTextSecondary(messageView, tm);
        ThemeApplier.applySecondaryButton(okButton, tm);

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
            @Nullable Runnable okAction
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
            dialog.setCancelable(showOkButton);
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
        if (!dialog.isShowing()) {
            dialog.show();
        }
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.getDecorView().setPadding(0, 0, 0, 0);
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
}

