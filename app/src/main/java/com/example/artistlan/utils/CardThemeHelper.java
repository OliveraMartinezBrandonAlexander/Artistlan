package com.example.artistlan.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.card.MaterialCardView;

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

    public static void applySoftChip(@Nullable TextView textView, @NonNull ThemeManager tm) {
        applyChip(textView, tm);
    }

    public static void applyStatusChip(@Nullable TextView textView, @Nullable String estado, @NonNull ThemeManager tm) {
        if (textView == null) {
            return;
        }
        String normalized = normalizar(estado);
        boolean important = normalized.contains("pend") || normalized.contains("no leid") || normalized.contains("nuevo");
        int fill = important ? tm.color(ThemeKeys.BUTTON_SECONDARY_BG) : tm.color(ThemeKeys.CARD_CHIP_BG);
        int text = chooseTextColor(
                tm,
                fill,
                important ? tm.color(ThemeKeys.BUTTON_TEXT_LIGHT) : tm.color(ThemeKeys.CARD_CHIP_TEXT),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY),
                Color.WHITE,
                Color.BLACK
        );
        textView.setTextColor(text);
        textView.setBackground(roundedDrawable(
                fill,
                ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 190),
                1,
                dp(textView, 12)
        ));
    }

    public static void applyMessageCard(@Nullable MaterialCardView card, @NonNull ThemeManager tm, boolean highlighted) {
        if (card == null) {
            return;
        }
        card.setCardBackgroundColor(highlighted
                ? ColorUtils.blendARGB(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), tm.color(ThemeKeys.BUTTON_SECONDARY_BG), 0.16f)
                : tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL));
        card.setStrokeColor(highlighted ? tm.color(ThemeKeys.ACCENT_PRIMARY) : tm.color(ThemeKeys.CARD_BORDER));
        card.setStrokeWidth(Math.max(1, Math.round(dp(card, highlighted ? 2 : 1))));
        card.setRadius(dp(card, 18));
        card.setCardElevation(dp(card, highlighted ? 3 : 1));
    }

    public static void applyThemedSurface(@Nullable View view, @NonNull ThemeManager tm, int radiusDp) {
        if (view == null) {
            return;
        }
        view.setBackground(roundedDrawable(
                tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                tm.color(ThemeKeys.CARD_BORDER),
                1,
                dp(view, radiusDp)
        ));
    }

    public static void applyAccentDot(@Nullable View view, @NonNull ThemeManager tm) {
        if (view == null) {
            return;
        }
        view.setBackground(roundedDrawable(
                tm.color(ThemeKeys.ACCENT_PRIMARY),
                tm.color(ThemeKeys.ACCENT_PRIMARY_LIGHT),
                1,
                dp(view, 99)
        ));
    }

    public static void tintProgress(@Nullable ProgressBar progressBar, @NonNull ThemeManager tm) {
        if (progressBar == null) {
            return;
        }
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(tm.color(ThemeKeys.ACCENT_PRIMARY)));
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

    public static void applyFilterSurface(@Nullable View view, @NonNull ThemeManager tm) {
        if (view == null) {
            return;
        }
        view.setBackground(roundedDrawable(
                tm.color(ThemeKeys.FILTER_BUTTON_BG),
                tm.color(ThemeKeys.FILTER_BUTTON_STROKE),
                2,
                dp(view, 16)
        ));
    }

    public static void applyFilterActionButton(@Nullable Button button, @NonNull ThemeManager tm) {
        if (button == null) {
            return;
        }
        button.setAllCaps(false);
        button.setTextColor(tm.color(ThemeKeys.FILTER_BUTTON_STROKE));
        button.setBackground(roundedDrawable(
                tm.color(ThemeKeys.FILTER_BUTTON_BG),
                tm.color(ThemeKeys.FILTER_BUTTON_STROKE),
                2,
                dp(button, 14)
        ));
    }

    public static void applyPrimaryBubbleButton(@Nullable Button button, @NonNull ThemeManager tm) {
        applyBubbleView(button, button, tm, ThemeKeys.BUTTON_PRIMARY_BG, ThemeKeys.BUTTON_TEXT_DARK, ThemeKeys.BUTTON_TEXT_LIGHT);
    }

    public static void applySecondaryBubbleButton(@Nullable Button button, @NonNull ThemeManager tm) {
        applyBubbleView(button, button, tm, ThemeKeys.BUTTON_SECONDARY_BG, ThemeKeys.BUTTON_TEXT_LIGHT, ThemeKeys.BUTTON_TEXT_DARK);
    }

    public static void applyPrimaryBubbleSurface(@Nullable View view, @Nullable TextView label, @NonNull ThemeManager tm) {
        applyBubbleView(view, label, tm, ThemeKeys.BUTTON_PRIMARY_BG, ThemeKeys.BUTTON_TEXT_DARK, ThemeKeys.BUTTON_TEXT_LIGHT);
    }

    public static void applySecondaryBubbleSurface(@Nullable View view, @Nullable TextView label, @NonNull ThemeManager tm) {
        applyBubbleView(view, label, tm, ThemeKeys.BUTTON_SECONDARY_BG, ThemeKeys.BUTTON_TEXT_LIGHT, ThemeKeys.BUTTON_TEXT_DARK);
    }

    private static void applyBubbleView(
            @Nullable View view,
            @Nullable TextView label,
            @NonNull ThemeManager tm,
            @NonNull String backgroundKey,
            @NonNull String preferredTextKey,
            @NonNull String alternateTextKey
    ) {
        if (view == null) {
            return;
        }

        int backgroundColor = tm.color(backgroundKey);
        int textColor = chooseTextColor(
                tm,
                backgroundColor,
                tm.color(preferredTextKey),
                tm.color(alternateTextKey),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );

        view.setBackgroundTintList(null);
        if (label != null) {
            if (label instanceof Button) {
                ((Button) label).setAllCaps(false);
            }
            label.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
            label.setTextColor(new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_pressed},
                            new int[]{android.R.attr.state_focused},
                            new int[]{android.R.attr.state_selected},
                            new int[]{}
                    },
                    new int[]{
                            ColorUtils.setAlphaComponent(textColor, Math.round(Color.alpha(textColor) * 0.62f)),
                            textColor,
                            textColor,
                            textColor,
                            textColor
                    }
            ));
        }

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled}, createBubbleDrawable(view, tm, ColorUtils.setAlphaComponent(backgroundColor, Math.round(Color.alpha(backgroundColor) * 0.46f)), textColor));
        states.addState(new int[]{android.R.attr.state_pressed}, createBubbleDrawable(view, tm, blendForInteraction(tm, backgroundColor, 0.10f), textColor));
        states.addState(new int[]{android.R.attr.state_focused}, createBubbleDrawable(view, tm, blendForInteraction(tm, backgroundColor, 0.08f), textColor));
        states.addState(new int[]{android.R.attr.state_selected}, createBubbleDrawable(view, tm, blendForInteraction(tm, backgroundColor, 0.06f), textColor));
        states.addState(new int[]{}, createBubbleDrawable(view, tm, backgroundColor, textColor));
        view.setBackground(states);
    }

    @NonNull
    private static LayerDrawable createBubbleDrawable(@NonNull View view, @NonNull ThemeManager tm, int backgroundColor, int textColor) {
        float radius = dp(view, 40);

        GradientDrawable shadow = new GradientDrawable();
        shadow.setShape(GradientDrawable.RECTANGLE);
        shadow.setColor(ColorUtils.setAlphaComponent(composeOverThemeSurface(tm, tm.color(ThemeKeys.TEXT_PRIMARY)), 42));
        shadow.setCornerRadius(radius);

        GradientDrawable fill = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ColorUtils.blendARGB(backgroundColor, Color.WHITE, 0.24f),
                        backgroundColor,
                        ColorUtils.blendARGB(backgroundColor, Color.BLACK, 0.10f)
                }
        );
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(radius);
        fill.setStroke(Math.max(1, Math.round(dp(view, 1))), ColorUtils.setAlphaComponent(textColor, 90));

        GradientDrawable highlight = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{ColorUtils.setAlphaComponent(Color.WHITE, 78), Color.TRANSPARENT}
        );
        highlight.setShape(GradientDrawable.RECTANGLE);
        highlight.setCornerRadius(dp(view, 30));

        LayerDrawable drawable = new LayerDrawable(new Drawable[]{shadow, fill, highlight});
        drawable.setLayerInset(0, 0, Math.round(dp(view, 6)), 0, 0);
        drawable.setLayerInset(2, Math.round(dp(view, 16)), Math.round(dp(view, 8)), Math.round(dp(view, 16)), Math.round(dp(view, 34)));
        return drawable;
    }

    private static int chooseTextColor(@NonNull ThemeManager tm, int backgroundColor, int preferred, int... candidates) {
        int opaqueBackground = composeOverThemeSurface(tm, backgroundColor);
        if (safeContrast(preferred, opaqueBackground) >= 4.5d) {
            return preferred;
        }

        int selected = preferred;
        double bestContrast = safeContrast(preferred, opaqueBackground);
        for (int candidate : candidates) {
            double contrast = safeContrast(candidate, opaqueBackground);
            if (contrast > bestContrast) {
                bestContrast = contrast;
                selected = candidate;
            }
        }
        if (bestContrast >= 4.5d) {
            return selected;
        }

        double contrastWhite = safeContrast(Color.WHITE, opaqueBackground);
        double contrastBlack = safeContrast(Color.BLACK, opaqueBackground);
        return contrastWhite >= contrastBlack ? Color.WHITE : Color.BLACK;
    }

    private static int blendForInteraction(@NonNull ThemeManager tm, int color, float ratio) {
        return ColorUtils.blendARGB(color, composeOverThemeSurface(tm, tm.color(ThemeKeys.BG_MID)), ratio);
    }

    private static int composeOverThemeSurface(@NonNull ThemeManager tm, int color) {
        int bottom = ColorUtils.setAlphaComponent(tm.color(ThemeKeys.BG_BOTTOM), 255);
        int mid = tm.color(ThemeKeys.BG_MID);
        int surface = Color.alpha(mid) < 255 ? ColorUtils.compositeColors(mid, bottom) : ColorUtils.setAlphaComponent(mid, 255);
        return Color.alpha(color) < 255 ? ColorUtils.compositeColors(color, surface) : ColorUtils.setAlphaComponent(color, 255);
    }

    private static double safeContrast(int foreground, int background) {
        try {
            return ColorUtils.calculateContrast(foreground, ColorUtils.setAlphaComponent(background, 255));
        } catch (IllegalArgumentException ignored) {
            return 0d;
        }
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
