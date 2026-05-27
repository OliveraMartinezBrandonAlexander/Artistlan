package com.example.artistlan.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;

public final class ArtistlanDialogFactory {

    public interface PasswordSubmitCallback {
        void onSubmit(@NonNull String password, @NonNull PasswordDialogHandle handle);
    }

    public static final class PasswordDialogHandle {
        private final AlertDialog dialog;
        private final EditText passwordInput;
        private final TextView messageView;
        private final Button positiveButton;
        private final Button negativeButton;
        private final ProgressBar progressBar;
        private final String defaultMessage;

        private PasswordDialogHandle(
                @NonNull AlertDialog dialog,
                @NonNull EditText passwordInput,
                @NonNull TextView messageView,
                @NonNull Button positiveButton,
                @NonNull Button negativeButton,
                @NonNull ProgressBar progressBar,
                @NonNull String defaultMessage
        ) {
            this.dialog = dialog;
            this.passwordInput = passwordInput;
            this.messageView = messageView;
            this.positiveButton = positiveButton;
            this.negativeButton = negativeButton;
            this.progressBar = progressBar;
            this.defaultMessage = defaultMessage;
        }

        public void setLoading(boolean loading) {
            passwordInput.setEnabled(!loading);
            positiveButton.setEnabled(!loading);
            negativeButton.setEnabled(!loading);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        public void showError(@NonNull String message) {
            passwordInput.setText("");
            passwordInput.requestFocus();
            passwordInput.setError(message);
            messageView.setText(message);
            setLoading(false);
        }

        private void clearInlineError() {
            passwordInput.setError(null);
            messageView.setText(defaultMessage);
        }

        public void dismiss() {
            dialog.dismiss();
        }
    }

    private ArtistlanDialogFactory() {}

    @Nullable
    public static AlertDialog show(@NonNull Fragment fragment, @NonNull DialogConfig config) {
        if (!fragment.isAdded() || fragment.getContext() == null) {
            return null;
        }
        return show(fragment.requireContext(), config);
    }

    @Nullable
    public static AlertDialog show(@NonNull Context context, @NonNull DialogConfig config) {
        if (!canShow(context)) {
            return null;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_artistlan_base, null, false);
        bindBaseView(context, view, config);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(config.isCancelable())
                .create();
        dialog.show();
        styleCustomDialog(dialog, context);

        Button positive = view.findViewById(R.id.artistlanDialogPositive);
        Button negative = view.findViewById(R.id.artistlanDialogNegative);
        Button neutral = view.findViewById(R.id.artistlanDialogNeutral);
        positive.setOnClickListener(v -> {
            positive.setEnabled(false);
            dialog.dismiss();
            run(config.getPositiveCallback());
        });
        negative.setOnClickListener(v -> {
            dialog.dismiss();
            run(config.getNegativeCallback());
        });
        neutral.setOnClickListener(v -> {
            dialog.dismiss();
            run(config.getNeutralCallback());
        });
        return dialog;
    }

    @Nullable
    public static AlertDialog showPassword(
            @NonNull Fragment fragment,
            @NonNull String title,
            @NonNull String message,
            @NonNull String hint,
            @NonNull String positiveText,
            @NonNull String negativeText,
            @NonNull PasswordSubmitCallback callback
    ) {
        if (!fragment.isAdded() || fragment.getContext() == null) {
            return null;
        }
        return showPassword(fragment.requireContext(), title, message, hint, positiveText, negativeText, callback);
    }

    @Nullable
    public static AlertDialog showPassword(
            @NonNull Context context,
            @NonNull String title,
            @NonNull String message,
            @NonNull String hint,
            @NonNull String positiveText,
            @NonNull String negativeText,
            @NonNull PasswordSubmitCallback callback
    ) {
        if (!canShow(context)) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_artistlan_password, null, false);
        View root = view.findViewById(R.id.artistlanPasswordRoot);
        TextView badgeView = view.findViewById(R.id.artistlanPasswordBadge);
        TextView titleView = view.findViewById(R.id.artistlanPasswordTitle);
        TextView messageView = view.findViewById(R.id.artistlanPasswordMessage);
        EditText passwordInput = view.findViewById(R.id.artistlanPasswordInput);
        ProgressBar progressBar = view.findViewById(R.id.artistlanPasswordProgress);
        Button positiveButton = view.findViewById(R.id.artistlanPasswordPositive);
        Button negativeButton = view.findViewById(R.id.artistlanPasswordNegative);
        ThemeManager tm = new ThemeManager(context);
        root.setBackground(DialogThemeHelper.createFieldDialogBackground(context));
        badgeView.setText("!");
        titleView.setText(title);
        messageView.setText(message);
        passwordInput.setHint(hint);
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        CardThemeHelper.applySecondaryBubbleSurface(badgeView, badgeView, tm);
        ThemeApplier.applyTextPrimary(titleView, tm);
        ThemeApplier.applyTextSecondary(messageView, tm);
        ThemeApplier.applyInput(passwordInput, tm);
        CardThemeHelper.applyPrimaryBubbleButton(positiveButton, tm);
        CardThemeHelper.applySecondaryBubbleButton(negativeButton, tm);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();
        dialog.setOnDismissListener(d -> passwordInput.setText(""));
        dialog.show();
        styleFieldCustomDialog(dialog, context);

        PasswordDialogHandle handle = new PasswordDialogHandle(
                dialog,
                passwordInput,
                messageView,
                positiveButton,
                negativeButton,
                progressBar,
                message
        );
        passwordInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                handle.clearInlineError();
            }
        });
        positiveButton.setOnClickListener(v -> {
            String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
            if (password.isEmpty()) {
                handle.showError("Ingresa tu contraseña");
                return;
            }
            handle.setLoading(true);
            callback.onSubmit(password, handle);
        });
        negativeButton.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    private static boolean canShow(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
        }
        return true;
    }

    @NonNull
    private static String defaultPositiveText(@NonNull DialogConfig.Type type) {
        if (type == DialogConfig.Type.DANGER) {
            return "Eliminar";
        }
        if (type == DialogConfig.Type.CONFIRM || type == DialogConfig.Type.PASSWORD) {
            return "Confirmar";
        }
        return "Aceptar";
    }

    private static boolean usesNegativeButton(@NonNull DialogConfig.Type type) {
        return type == DialogConfig.Type.CONFIRM
                || type == DialogConfig.Type.DANGER
                || type == DialogConfig.Type.PASSWORD
                || type == DialogConfig.Type.REPORT;
    }

    @NonNull
    private static String nonEmpty(@Nullable String value, @NonNull String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static void run(@Nullable Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    private static void bindBaseView(@NonNull Context context, @NonNull View view, @NonNull DialogConfig config) {
        ThemeManager tm = new ThemeManager(context);
        View root = view.findViewById(R.id.artistlanDialogRoot);
        TextView badge = view.findViewById(R.id.artistlanDialogBadge);
        TextView title = view.findViewById(R.id.artistlanDialogTitle);
        TextView message = view.findViewById(R.id.artistlanDialogMessage);
        Button positive = view.findViewById(R.id.artistlanDialogPositive);
        Button negative = view.findViewById(R.id.artistlanDialogNegative);
        Button neutral = view.findViewById(R.id.artistlanDialogNeutral);

        root.setBackground(DialogThemeHelper.createDialogBackground(context));
        badge.setText(badgeText(config.getType()));
        title.setText(nonEmpty(config.getTitle(), defaultTitle(config.getType())));
        message.setText(nonEmpty(config.getMessage(), ""));
        positive.setText(nonEmpty(config.getPositiveText(), defaultPositiveText(config.getType())));

        boolean showNegative = config.getNegativeText() != null || usesNegativeButton(config.getType());
        negative.setVisibility(showNegative ? View.VISIBLE : View.GONE);
        negative.setText(nonEmpty(config.getNegativeText(), "Cancelar"));

        boolean showNeutral = config.getNeutralText() != null;
        neutral.setVisibility(showNeutral ? View.VISIBLE : View.GONE);
        neutral.setText(nonEmpty(config.getNeutralText(), ""));

        if (config.getType() == DialogConfig.Type.DANGER || config.getType() == DialogConfig.Type.ERROR) {
            CardThemeHelper.applySecondaryBubbleSurface(badge, badge, tm);
        } else {
            CardThemeHelper.applyPrimaryBubbleSurface(badge, badge, tm);
        }
        ThemeApplier.applyTextPrimary(title, tm);
        ThemeApplier.applyTextSecondary(message, tm);
        CardThemeHelper.applyPrimaryBubbleButton(positive, tm);
        CardThemeHelper.applySecondaryBubbleButton(negative, tm);
        CardThemeHelper.applySecondaryBubbleButton(neutral, tm);
    }

    private static void styleCustomDialog(@NonNull AlertDialog dialog, @NonNull Context context) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        DialogThemeHelper.applyDialogWindowSize(dialog, context);
    }

    private static void styleFieldCustomDialog(@NonNull AlertDialog dialog, @NonNull Context context) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        DialogThemeHelper.applyFieldDialogWindowSize(dialog, context);
    }

    @NonNull
    private static String defaultTitle(@NonNull DialogConfig.Type type) {
        switch (type) {
            case SUCCESS:
                return "Listo";
            case WARNING:
                return "Atención";
            case ERROR:
                return "Error";
            case CONFIRM:
                return "Confirmar";
            case DANGER:
                return "Acción importante";
            case PASSWORD:
                return "Confirmar contraseña";
            case REPORT:
                return "Reporte";
            case INFO:
            default:
                return "Información";
        }
    }

    @NonNull
    private static String badgeText(@NonNull DialogConfig.Type type) {
        switch (type) {
            case SUCCESS:
                return "OK";
            case WARNING:
            case ERROR:
            case DANGER:
                return "!";
            case PASSWORD:
                return "*";
            case REPORT:
                return "R";
            case CONFIRM:
                return "?";
            case INFO:
            default:
                return "i";
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
