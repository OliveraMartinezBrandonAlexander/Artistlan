package com.example.artistlan.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

public final class DialogThemeHelper {

    private DialogThemeHelper() {}

    public static void styleAlertDialog(@Nullable AlertDialog dialog, @NonNull Context context) {
        if (dialog == null) {
            return;
        }
        ThemeManager tm = new ThemeManager(context);
        styleDialogWindow(dialog, context);
        Window window = dialog.getWindow();
        if (window != null) {
            tintTextTree(window.getDecorView(), tm);
        }
        ThemeApplier.applyPrimaryButton(dialog.getButton(AlertDialog.BUTTON_POSITIVE), tm);
        ThemeApplier.applySecondaryButton(dialog.getButton(AlertDialog.BUTTON_NEGATIVE), tm);
        ThemeApplier.applySecondaryButton(dialog.getButton(AlertDialog.BUTTON_NEUTRAL), tm);
    }

    public static void styleDialogWindow(@Nullable Dialog dialog, @NonNull Context context) {
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        dialog.getWindow().setBackgroundDrawable(createDialogBackground(context));
    }

    public static void styleButtonPair(@Nullable Button primary, @Nullable Button secondary, @NonNull Context context) {
        ThemeManager tm = new ThemeManager(context);
        ThemeApplier.applyPrimaryButton(primary, tm);
        ThemeApplier.applySecondaryButton(secondary, tm);
    }

    public static GradientDrawable createDialogBackground(@NonNull Context context) {
        ThemeManager tm = new ThemeManager(context);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(tm.color(ThemeKeys.DIALOG_BG));
        bg.setCornerRadius(dpToPx(context, 22));
        bg.setStroke(dpToPx(context, 1), tm.color(ThemeKeys.ACCOUNT_GLASS_STROKE));
        return bg;
    }

    private static void tintTextTree(@Nullable View view, @NonNull ThemeManager tm) {
        if (view == null) {
            return;
        }
        if (view instanceof Button) {
            return;
        }
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(tm.color(ThemeKeys.DIALOG_TEXT));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                tintTextTree(group.getChildAt(i), tm);
            }
        }
    }

    private static int dpToPx(@NonNull Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
