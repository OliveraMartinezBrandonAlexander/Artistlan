package com.example.artistlan.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DialogConfig {

    public enum Type {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        CONFIRM,
        DANGER,
        PASSWORD,
        REPORT
    }

    @Nullable private final String title;
    @Nullable private final String message;
    @Nullable private final String positiveText;
    @Nullable private final String negativeText;
    @Nullable private final String neutralText;
    @NonNull private final Type type;
    private final boolean cancelable;
    @DrawableRes private final int iconRes;
    @Nullable private final Runnable positiveCallback;
    @Nullable private final Runnable negativeCallback;
    @Nullable private final Runnable neutralCallback;

    private DialogConfig(@NonNull Builder builder) {
        title = builder.title;
        message = builder.message;
        positiveText = builder.positiveText;
        negativeText = builder.negativeText;
        neutralText = builder.neutralText;
        type = builder.type;
        cancelable = builder.cancelable;
        iconRes = builder.iconRes;
        positiveCallback = builder.positiveCallback;
        negativeCallback = builder.negativeCallback;
        neutralCallback = builder.neutralCallback;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @Nullable public String getTitle() { return title; }
    @Nullable public String getMessage() { return message; }
    @Nullable public String getPositiveText() { return positiveText; }
    @Nullable public String getNegativeText() { return negativeText; }
    @Nullable public String getNeutralText() { return neutralText; }
    @NonNull public Type getType() { return type; }
    public boolean isCancelable() { return cancelable; }
    @DrawableRes public int getIconRes() { return iconRes; }
    @Nullable public Runnable getPositiveCallback() { return positiveCallback; }
    @Nullable public Runnable getNegativeCallback() { return negativeCallback; }
    @Nullable public Runnable getNeutralCallback() { return neutralCallback; }

    public static final class Builder {
        @Nullable private String title;
        @Nullable private String message;
        @Nullable private String positiveText;
        @Nullable private String negativeText;
        @Nullable private String neutralText;
        @NonNull private Type type = Type.INFO;
        private boolean cancelable = true;
        @DrawableRes private int iconRes = 0;
        @Nullable private Runnable positiveCallback;
        @Nullable private Runnable negativeCallback;
        @Nullable private Runnable neutralCallback;

        private Builder() {}

        @NonNull public Builder setTitle(@Nullable String title) {
            this.title = title;
            return this;
        }

        @NonNull public Builder setMessage(@Nullable String message) {
            this.message = message;
            return this;
        }

        @NonNull public Builder setPositiveText(@Nullable String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        @NonNull public Builder setNegativeText(@Nullable String negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        @NonNull public Builder setNeutralText(@Nullable String neutralText) {
            this.neutralText = neutralText;
            return this;
        }

        @NonNull public Builder setType(@NonNull Type type) {
            this.type = type;
            return this;
        }

        @NonNull public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        @NonNull public Builder setIconRes(@DrawableRes int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        @NonNull public Builder setOnPositive(@Nullable Runnable positiveCallback) {
            this.positiveCallback = positiveCallback;
            return this;
        }

        @NonNull public Builder setOnNegative(@Nullable Runnable negativeCallback) {
            this.negativeCallback = negativeCallback;
            return this;
        }

        @NonNull public Builder setOnNeutral(@Nullable Runnable neutralCallback) {
            this.neutralCallback = neutralCallback;
            return this;
        }

        @NonNull public DialogConfig build() {
            return new DialogConfig(this);
        }
    }
}
