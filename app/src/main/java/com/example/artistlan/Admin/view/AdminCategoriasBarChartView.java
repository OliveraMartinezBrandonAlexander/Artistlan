package com.example.artistlan.Admin.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.Conector.model.AdminCategoriaStatsDTO;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCategoriasBarChartView extends View {

    private static final float DEFAULT_HEIGHT_DP = 272f;
    private static final float SLOT_WIDTH_DP = 102f;
    private static final float BAR_WIDTH_DP = 40f;
    private static final float LEFT_AXIS_AREA_DP = 42f;
    private static final float TOP_AREA_DP = 22f;
    private static final float BOTTOM_LABEL_AREA_DP = 62f;

    private final List<AdminCategoriaStatsDTO> items = new ArrayList<>();
    private final Paint plotFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueChipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueChipStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint yLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF plotRect = new RectF();
    private final RectF barRect = new RectF();
    private final RectF glowRect = new RectF();
    private final RectF chipRect = new RectF();

    @Nullable
    private ValueAnimator animator;
    private float animationProgress = 1f;

    public AdminCategoriasBarChartView(Context context) {
        super(context);
        init();
    }

    public AdminCategoriasBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdminCategoriasBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        plotStrokePaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStyle(Paint.Style.STROKE);
        barGlowPaint.setStyle(Paint.Style.FILL);
        valueChipStrokePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        yLabelPaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setItems(@Nullable List<AdminCategoriaStatsDTO> nuevos) {
        items.clear();
        if (nuevos != null) {
            items.addAll(nuevos);
        }
        iniciarAnimacion();
        requestLayout();
        invalidate();
    }

    private void iniciarAnimacion() {
        if (animator != null) {
            animator.cancel();
        }

        if (items.isEmpty()) {
            animationProgress = 1f;
            return;
        }

        animator = ValueAnimator.ofFloat(0.18f, 1f);
        animator.setDuration(520L);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            Object value = animation.getAnimatedValue();
            animationProgress = value instanceof Float ? (Float) value : 1f;
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = Math.round(getPaddingLeft()
                + getPaddingRight()
                + dp(LEFT_AXIS_AREA_DP + 22f)
                + Math.max(1, items.size()) * dp(SLOT_WIDTH_DP));
        int desiredHeight = Math.round(dp(DEFAULT_HEIGHT_DP));

        int measuredWidth = resolveSize(desiredWidth, widthMeasureSpec);
        int measuredHeight = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        ThemeManager themeManager = new ThemeManager(getContext());
        configurarPaints(themeManager);

        float chartLeft = getPaddingLeft() + dp(LEFT_AXIS_AREA_DP);
        float chartTop = getPaddingTop() + dp(TOP_AREA_DP);
        float chartBottom = getHeight() - getPaddingBottom() - dp(BOTTOM_LABEL_AREA_DP);
        float chartHeight = Math.max(dp(32f), chartBottom - chartTop);
        float slotWidth = dp(SLOT_WIDTH_DP);
        float barWidth = dp(BAR_WIDTH_DP);
        float chartRight = Math.max(
                chartLeft + (Math.max(1, items.size()) * slotWidth),
                getWidth() - getPaddingRight() - dp(14f)
        );

        plotRect.set(
                chartLeft - dp(12f),
                chartTop - dp(8f),
                chartRight + dp(8f),
                chartBottom + dp(8f)
        );
        canvas.drawRoundRect(plotRect, dp(22f), dp(22f), plotFillPaint);
        canvas.drawRoundRect(plotRect, dp(22f), dp(22f), plotStrokePaint);

        if (items.isEmpty()) {
            canvas.drawText("Sin datos disponibles", plotRect.centerX(), plotRect.centerY(), valuePaint);
            return;
        }

        long maxValue = obtenerMaximo();
        dibujarGuias(canvas, chartLeft, chartRight, chartTop, chartBottom, chartHeight, maxValue);

        int indiceMaximo = obtenerIndiceMaximo(maxValue);
        for (int i = 0; i < items.size(); i++) {
            AdminCategoriaStatsDTO item = items.get(i);
            if (item == null) {
                continue;
            }

            long total = Math.max(0L, item.getTotal());
            float centerX = chartLeft + (slotWidth * i) + (slotWidth / 2f);
            float normalized = total <= 0L ? 0f : (total / (float) maxValue);
            float animatedRatio = normalized * animationProgress;
            float barHeight = total <= 0L
                    ? dp(5f)
                    : Math.max(dp(14f), chartHeight * animatedRatio);
            float barBottom = chartBottom - dp(2f);
            float barTop = barBottom - barHeight;

            int baseColor = i == indiceMaximo
                    ? themeManager.color(ThemeKeys.ACCENT_SECONDARY)
                    : themeManager.color(ThemeKeys.ACCENT_PRIMARY);
            int topColor = ColorUtils.blendARGB(
                    i == indiceMaximo
                            ? themeManager.color(ThemeKeys.ACCENT_SECONDARY_LIGHT)
                            : themeManager.color(ThemeKeys.ACCENT_PRIMARY_LIGHT),
                    Color.WHITE,
                    0.28f
            );
            int bottomColor = ColorUtils.blendARGB(baseColor, themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL), 0.18f);

            glowRect.set(
                    centerX - (barWidth / 2f) - dp(4f),
                    barTop - dp(6f),
                    centerX + (barWidth / 2f) + dp(4f),
                    barBottom + dp(3f)
            );
            barGlowPaint.setColor(ColorUtils.setAlphaComponent(baseColor, total > 0L ? 56 : 24));
            canvas.drawRoundRect(glowRect, dp(18f), dp(18f), barGlowPaint);

            barRect.set(centerX - (barWidth / 2f), barTop, centerX + (barWidth / 2f), barBottom);
            barPaint.setShader(new LinearGradient(
                    centerX,
                    barTop,
                    centerX,
                    barBottom,
                    new int[]{topColor, baseColor, bottomColor},
                    new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(barRect, dp(18f), dp(18f), barPaint);

            dibujarChipValor(canvas, themeManager, centerX, barTop - dp(8f), total, chartTop);
            dibujarEtiqueta(canvas, item.getCategoria(), centerX, chartBottom + dp(22f));
        }
    }

    private void dibujarGuias(Canvas canvas,
                              float chartLeft,
                              float chartRight,
                              float chartTop,
                              float chartBottom,
                              float chartHeight,
                              long maxValue) {
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint);

        for (int step = 0; step <= 4; step++) {
            float ratio = step / 4f;
            float y = chartBottom - (chartHeight * ratio);
            long value = Math.round(maxValue * ratio);

            canvas.drawLine(chartLeft, y, chartRight, y, step == 0 ? axisPaint : gridPaint);
            canvas.drawText(formatearValor(value), chartLeft - dp(10f), y + dp(4f), yLabelPaint);
        }
    }

    private void dibujarChipValor(@NonNull Canvas canvas,
                                  @NonNull ThemeManager themeManager,
                                  float centerX,
                                  float anchorBottom,
                                  long total,
                                  float chartTop) {
        String texto = String.valueOf(total);
        float chipPaddingH = dp(10f);
        float chipHeight = dp(24f);
        float chipWidth = Math.max(dp(34f), valuePaint.measureText(texto) + (chipPaddingH * 2f));
        float chipBottom = Math.max(chartTop + chipHeight, anchorBottom);
        float chipTop = chipBottom - chipHeight;

        chipRect.set(centerX - (chipWidth / 2f), chipTop, centerX + (chipWidth / 2f), chipBottom);
        valueChipPaint.setColor(ColorUtils.blendARGB(
                themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                themeManager.color(ThemeKeys.CARD_CHIP_BG),
                0.72f
        ));
        valueChipStrokePaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 205));
        valueChipStrokePaint.setStrokeWidth(dp(1.2f));

        canvas.drawRoundRect(chipRect, dp(12f), dp(12f), valueChipPaint);
        canvas.drawRoundRect(chipRect, dp(12f), dp(12f), valueChipStrokePaint);
        canvas.drawText(texto, centerX, chipRect.centerY() + dp(4f), valuePaint);
    }

    private void dibujarEtiqueta(@NonNull Canvas canvas, @Nullable String categoria, float centerX, float baseY) {
        String[] lineas = dividirEtiqueta(categoria);
        canvas.drawText(lineas[0], centerX, baseY, labelPaint);
        if (lineas[1] != null) {
            canvas.drawText(lineas[1], centerX, baseY + dp(13f), labelPaint);
        }
    }

    @NonNull
    private String[] dividirEtiqueta(@Nullable String categoria) {
        String limpia = categoria == null ? "" : categoria.trim();
        if (limpia.isEmpty()) {
            return new String[]{"Sin cat.", null};
        }

        if (limpia.length() <= 12) {
            return new String[]{limpia, null};
        }

        String[] palabras = limpia.split("\\s+");
        if (palabras.length >= 2) {
            StringBuilder lineaUno = new StringBuilder();
            StringBuilder lineaDos = new StringBuilder();

            for (String palabra : palabras) {
                if (lineaUno.length() == 0 && palabra.length() <= 12) {
                    lineaUno.append(palabra);
                    continue;
                }
                if (lineaUno.length() > 0 && lineaUno.length() + 1 + palabra.length() <= 12) {
                    lineaUno.append(' ').append(palabra);
                    continue;
                }
                if (lineaDos.length() == 0 && palabra.length() <= 12) {
                    lineaDos.append(palabra);
                    continue;
                }
                if (lineaDos.length() > 0 && lineaDos.length() + 1 + palabra.length() <= 12) {
                    lineaDos.append(' ').append(palabra);
                    continue;
                }
                break;
            }

            if (lineaUno.length() > 0) {
                return new String[]{
                        lineaUno.toString(),
                        lineaDos.length() > 0 ? lineaDos.toString() : null
                };
            }
        }

        return new String[]{limpia.substring(0, 11).trim() + ".", null};
    }

    private long obtenerMaximo() {
        long maxValue = 0L;
        for (AdminCategoriaStatsDTO item : items) {
            if (item != null) {
                maxValue = Math.max(maxValue, Math.max(0L, item.getTotal()));
            }
        }
        return maxValue > 0L ? maxValue : 1L;
    }

    private int obtenerIndiceMaximo(long maxValue) {
        for (int i = 0; i < items.size(); i++) {
            AdminCategoriaStatsDTO item = items.get(i);
            if (item != null && Math.max(0L, item.getTotal()) == maxValue) {
                return i;
            }
        }
        return -1;
    }

    private void configurarPaints(@NonNull ThemeManager themeManager) {
        plotFillPaint.setColor(ColorUtils.blendARGB(
                themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.ACCENT_PRIMARY), 46),
                0.24f
        ));

        plotStrokePaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 200));
        plotStrokePaint.setStrokeWidth(dp(1.2f));

        axisPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 220));
        axisPaint.setStrokeWidth(dp(1.2f));

        gridPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 92));
        gridPaint.setStrokeWidth(dp(1f));

        valuePaint.setColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
        valuePaint.setTextSize(sp(11.5f));
        valuePaint.setFakeBoldText(true);

        labelPaint.setColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        labelPaint.setTextSize(sp(10.5f));

        yLabelPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.TEXT_SECONDARY), 210));
        yLabelPaint.setTextSize(sp(10f));
    }

    @NonNull
    private String formatearValor(long valor) {
        if (valor >= 1_000_000L) {
            return String.format(Locale.US, "%.1fM", valor / 1_000_000f);
        }
        if (valor >= 1_000L) {
            return String.format(Locale.US, "%.1fk", valor / 1_000f);
        }
        return String.valueOf(valor);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
