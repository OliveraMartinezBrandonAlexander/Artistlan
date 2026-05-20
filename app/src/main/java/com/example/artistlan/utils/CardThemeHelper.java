package com.example.artistlan.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.Normalizer;
import java.util.Locale;

public final class CardThemeHelper {

    private CardThemeHelper() {}

    public static void applyFlatCard(@Nullable View view, @NonNull ThemeManager tm) {
        if (view == null) {
            return;
        }
        if (view instanceof CardView) {
            CardView card = (CardView) view;
            card.setCardBackgroundColor(Color.TRANSPARENT);
            card.setRadius(dp(view, 18));
            card.setCardElevation(dp(view, 2));
        }
        view.setBackground(roundedDrawable(Color.TRANSPARENT, tm.color(ThemeKeys.CARD_BORDER), 2, dp(view, 18)));
    }

    public static void applyChip(@Nullable TextView textView, @NonNull ThemeManager tm) {
        if (textView == null) {
            return;
        }
        textView.setTextColor(tm.color(ThemeKeys.CARD_CHIP_TEXT));
        textView.setBackground(roundedDrawable(
                tm.color(ThemeKeys.CARD_CHIP_BG),
                ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 190),
                1,
                dp(textView, 12)
        ));
    }

    public static void applyStatusChip(@Nullable TextView textView, @Nullable String estado) {
        if (textView == null) {
            return;
        }
        int color = colorForEstado(estado);
        textView.setTextColor(textOn(color));
        textView.setBackground(roundedDrawable(ColorUtils.setAlphaComponent(color, 230), color, 1, dp(textView, 12)));
    }

    public static void applyFilterButton(@Nullable ImageButton button, @NonNull ThemeManager tm) {
        if (button == null) {
            return;
        }
        button.setBackground(roundedDrawable(
                tm.color(ThemeKeys.FILTER_BUTTON_BG),
                tm.color(ThemeKeys.FILTER_BUTTON_STROKE),
                2,
                dp(button, 16)
        ));
        button.setColorFilter(tm.color(ThemeKeys.FILTER_BUTTON_STROKE), PorterDuff.Mode.SRC_IN);
    }

    public static void applyAvatarStroke(@Nullable ShapeableImageView imageView, @NonNull ThemeManager tm) {
        if (imageView == null) {
            return;
        }
        imageView.setStrokeColor(ColorStateList.valueOf(tm.color(ThemeKeys.CARD_BORDER)));
        imageView.setStrokeWidth(1f);
    }

    @NonNull
    private static GradientDrawable roundedDrawable(int fill, int stroke, int strokeDp, float radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radiusPx);
        drawable.setColor(fill);
        drawable.setStroke(strokeDp, stroke);
        return drawable;
    }

    private static float dp(@NonNull View view, int value) {
        return value * view.getResources().getDisplayMetrics().density;
    }

    private static int colorForEstado(@Nullable String estadoRaw) {
        String estado = normalizar(estadoRaw);
        if (estado.contains("vendid")) {
            return Color.parseColor("#2563EB");
        }
        if (estado.contains("venta")) {
            return Color.parseColor("#16A34A");
        }
        if (estado.contains("reserv")) {
            return Color.parseColor("#EAB308");
        }
        if (estado.contains("exhib")) {
            return Color.parseColor("#8B5CF6");
        }
        return Color.parseColor("#64748B");
    }

    private static int textOn(int bg) {
        double luminance = (0.299 * Color.red(bg) + 0.587 * Color.green(bg) + 0.114 * Color.blue(bg)) / 255;
        return luminance > 0.58 ? Color.parseColor("#201A12") : Color.WHITE;
    }

    @NonNull
    private static String normalizar(@Nullable String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
