package com.example.artistlan.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

import java.util.List;

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
        styleDialogButton(dialog.getButton(AlertDialog.BUTTON_POSITIVE), tm, true);
        styleDialogButton(dialog.getButton(AlertDialog.BUTTON_NEGATIVE), tm, false);
        styleDialogButton(dialog.getButton(AlertDialog.BUTTON_NEUTRAL), tm, false);
    }

    public static void styleDialogWindow(@Nullable Dialog dialog, @NonNull Context context) {
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        dialog.getWindow().setBackgroundDrawable(createDialogBackground(context));
    }

    public static void applyDialogWindowSize(@Nullable Dialog dialog, @NonNull Context context) {
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        Window window = dialog.getWindow();
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int maxWidth = dpToPx(context, 340);
        int sideMargin = dpToPx(context, 40);
        int targetWidth = Math.min(maxWidth, Math.max(dpToPx(context, 280), screenWidth - sideMargin));
        window.setLayout(targetWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        window.getDecorView().setPadding(0, 0, 0, 0);
    }

    public static void applyFieldDialogWindowSize(@Nullable Dialog dialog, @NonNull Context context) {
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        Window window = dialog.getWindow();
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int maxWidth = dpToPx(context, 380);
        int minWidth = dpToPx(context, 300);
        int sideMargin = dpToPx(context, 32);
        int targetWidth = Math.min(maxWidth, Math.max(minWidth, screenWidth - sideMargin));
        window.setLayout(targetWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        window.getDecorView().setPadding(0, 0, 0, 0);
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

    public static GradientDrawable createFieldDialogBackground(@NonNull Context context) {
        ThemeManager tm = new ThemeManager(context);
        GradientDrawable bg = createDialogBackground(context);
        bg.setColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.DIALOG_BG), 232));
        bg.setStroke(dpToPx(context, 1), ColorUtils.setAlphaComponent(tm.color(ThemeKeys.ACCOUNT_GLASS_STROKE), 210));
        return bg;
    }

    public static <T> ArrayAdapter<T> createDialogComboAdapter(@NonNull Context context, @NonNull List<T> values) {
        return new ArrayAdapter<T>(context, android.R.layout.simple_spinner_item, values) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                styleComboText(view, false);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                styleComboText(view, true);
                return view;
            }

            private void styleComboText(@Nullable View view, boolean dropdown) {
                if (!(view instanceof TextView)) {
                    return;
                }
                ThemeManager tm = new ThemeManager(context);
                TextView textView = (TextView) view;
                textView.setTextColor(dropdown ? tm.color(ThemeKeys.TEXT_PRIMARY) : tm.color(ThemeKeys.FILTER_BUTTON_STROKE));
                textView.setTextSize(dropdown ? 14f : 15f);
                textView.setSingleLine(false);
                int horizontal = dpToPx(context, dropdown ? 14 : 16);
                int vertical = dpToPx(context, dropdown ? 12 : 10);
                textView.setPadding(horizontal, vertical, horizontal, vertical);
                if (dropdown) {
                    textView.setBackground(createComboDropdownBackground(context));
                }
            }
        };
    }

    public static void applyDialogComboStyle(@Nullable Spinner spinner, @NonNull Context context) {
        if (spinner == null) {
            return;
        }
        ThemeManager tm = new ThemeManager(context);
        spinner.setBackground(createComboBackground(context, tm));
        spinner.setPopupBackgroundDrawable(createComboDropdownBackground(context));
        spinner.setPadding(dpToPx(context, 12), 0, dpToPx(context, 12), 0);
        spinner.setMinimumHeight(dpToPx(context, 48));
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

    @NonNull
    private static StateListDrawable createComboBackground(@NonNull Context context, @NonNull ThemeManager tm) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled}, createComboStateDrawable(context, tm, 0.48f, 0.70f));
        states.addState(new int[]{android.R.attr.state_pressed}, createComboStateDrawable(context, tm, 0.92f, 1f));
        states.addState(new int[]{android.R.attr.state_focused}, createComboStateDrawable(context, tm, 0.88f, 1f));
        states.addState(new int[]{android.R.attr.state_selected}, createComboStateDrawable(context, tm, 0.86f, 1f));
        states.addState(new int[]{}, createComboStateDrawable(context, tm, 0.78f, 0.92f));
        return states;
    }

    @NonNull
    private static GradientDrawable createComboStateDrawable(
            @NonNull Context context,
            @NonNull ThemeManager tm,
            float fillAlpha,
            float strokeAlpha
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(context, 18));
        drawable.setColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.FILTER_BUTTON_BG), Math.round(255 * fillAlpha)));
        drawable.setStroke(
                dpToPx(context, 1),
                ColorUtils.setAlphaComponent(tm.color(ThemeKeys.FILTER_BUTTON_STROKE), Math.round(255 * strokeAlpha))
        );
        return drawable;
    }

    @NonNull
    private static GradientDrawable createComboDropdownBackground(@NonNull Context context) {
        ThemeManager tm = new ThemeManager(context);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(context, 14));
        drawable.setColor(tm.color(ThemeKeys.DIALOG_BG));
        drawable.setStroke(dpToPx(context, 1), tm.color(ThemeKeys.ACCOUNT_GLASS_STROKE));
        return drawable;
    }

    private static void styleDialogButton(@Nullable Button button, @NonNull ThemeManager tm, boolean primary) {
        if (button == null) {
            return;
        }
        int backgroundColor;
        int preferredTextColor;
        if (primary) {
            ThemeApplier.applyPrimaryButton(button, tm);
            backgroundColor = tm.color(ThemeKeys.BUTTON_PRIMARY_BG);
            preferredTextColor = tm.color(ThemeKeys.BUTTON_TEXT_DARK);
        } else {
            ThemeApplier.applySecondaryButton(button, tm);
            backgroundColor = tm.color(ThemeKeys.BUTTON_SECONDARY_BG);
            preferredTextColor = tm.color(ThemeKeys.BUTTON_TEXT_LIGHT);
        }
        if (button.getBackground() != null) {
            button.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            backgroundColor = tm.color(ThemeKeys.DIALOG_BG);
        }
        button.setTextColor(resolveReadableTextColor(backgroundColor, preferredTextColor));
    }

    private static int resolveReadableTextColor(int backgroundColor, int preferredTextColor) {
        if (ColorUtils.calculateContrast(preferredTextColor, backgroundColor) >= 4.5d) {
            return preferredTextColor;
        }
        double contrastWhite = ColorUtils.calculateContrast(Color.WHITE, backgroundColor);
        double contrastBlack = ColorUtils.calculateContrast(Color.BLACK, backgroundColor);
        return contrastWhite >= contrastBlack ? Color.WHITE : Color.BLACK;
    }
}
