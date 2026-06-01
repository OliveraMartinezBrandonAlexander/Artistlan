package com.example.artistlan.Admin.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.Conector.model.AdminPuntoSerieDTO;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminVentasSemanalesChartView extends View {

    private static final float DEFAULT_HEIGHT_DP = 286f;
    private static final float TOP_AREA_DP = 24f;
    private static final float LEFT_AXIS_AREA_DP = 44f;
    private static final float BOTTOM_LABEL_AREA_DP = 74f;
    private static final float BAR_WIDTH_DP = 26f;
    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");

    private final List<AdminPuntoSerieDTO> items = new ArrayList<>();
    private final Paint plotFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueChipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueChipStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint amountPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint yLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF plotRect = new RectF();
    private final RectF barRect = new RectF();
    private final RectF glowRect = new RectF();
    private final RectF chipRect = new RectF();
    private final Path trendPath = new Path();

    @Nullable
    private ValueAnimator animator;
    private float animationProgress = 1f;

    public AdminVentasSemanalesChartView(Context context) {
        super(context);
        init();
    }

    public AdminVentasSemanalesChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdminVentasSemanalesChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        plotStrokePaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStyle(Paint.Style.STROKE);
        linePaint.setStyle(Paint.Style.STROKE);
        barGlowPaint.setStyle(Paint.Style.FILL);
        valueChipStrokePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        amountPaint.setTextAlign(Paint.Align.CENTER);
        yLabelPaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setItems(@Nullable List<AdminPuntoSerieDTO> nuevos) {
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

        animator = ValueAnimator.ofFloat(0.16f, 1f);
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
        int desiredWidth = Math.round(dp(340f));
        int desiredHeight = Math.round(dp(DEFAULT_HEIGHT_DP));
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        ThemeManager themeManager = new ThemeManager(getContext());
        configurarPaints(themeManager);

        float chartLeft = getPaddingLeft() + dp(LEFT_AXIS_AREA_DP);
        float chartTop = getPaddingTop() + dp(TOP_AREA_DP);
        float chartRight = getWidth() - getPaddingRight() - dp(16f);
        float chartBottom = getHeight() - getPaddingBottom() - dp(BOTTOM_LABEL_AREA_DP);
        float chartHeight = Math.max(dp(32f), chartBottom - chartTop);

        plotRect.set(chartLeft - dp(12f), chartTop - dp(8f), chartRight + dp(8f), chartBottom + dp(8f));
        canvas.drawRoundRect(plotRect, dp(22f), dp(22f), plotFillPaint);
        canvas.drawRoundRect(plotRect, dp(22f), dp(22f), plotStrokePaint);

        if (items.isEmpty()) {
            canvas.drawText("Sin datos disponibles", plotRect.centerX(), plotRect.centerY(), valuePaint);
            return;
        }

        long maxValue = obtenerMaximo();
        dibujarGuias(canvas, chartLeft, chartRight, chartTop, chartBottom, chartHeight, maxValue);

        int count = Math.max(1, items.size());
        float slotWidth = (chartRight - chartLeft) / count;
        float[] centersX = new float[count];
        float[] topsY = new float[count];
        long[] values = new long[count];

        int indiceMaximo = obtenerIndiceMaximo(maxValue);
        for (int i = 0; i < count; i++) {
            AdminPuntoSerieDTO item = items.get(i);
            long total = item != null ? Math.max(0L, item.getValor()) : 0L;
            float centerX = chartLeft + (slotWidth * i) + (slotWidth / 2f);
            float ratio = total <= 0L ? 0f : (total / (float) maxValue);
            float animatedRatio = ratio * animationProgress;
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
                    0.26f
            );
            int bottomColor = ColorUtils.blendARGB(baseColor, themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL), 0.14f);

            glowRect.set(
                    centerX - dp(BAR_WIDTH_DP / 2f + 4f),
                    barTop - dp(5f),
                    centerX + dp(BAR_WIDTH_DP / 2f + 4f),
                    barBottom + dp(3f)
            );
            barGlowPaint.setColor(ColorUtils.setAlphaComponent(baseColor, total > 0L ? 58 : 22));
            canvas.drawRoundRect(glowRect, dp(16f), dp(16f), barGlowPaint);

            barRect.set(centerX - dp(BAR_WIDTH_DP / 2f), barTop, centerX + dp(BAR_WIDTH_DP / 2f), barBottom);
            barPaint.setShader(new LinearGradient(
                    centerX,
                    barTop,
                    centerX,
                    barBottom,
                    new int[]{topColor, baseColor, bottomColor},
                    new float[]{0f, 0.56f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(barRect, dp(16f), dp(16f), barPaint);

            centersX[i] = centerX;
            topsY[i] = barTop;
            values[i] = total;
        }

        dibujarLineaTendencia(canvas, themeManager, centersX, topsY, values);

        for (int i = 0; i < count; i++) {
            AdminPuntoSerieDTO item = items.get(i);
            if (item == null) {
                continue;
            }
            dibujarChipValor(canvas, themeManager, centersX[i], topsY[i] - dp(8f), values[i], chartTop);
            dibujarEtiqueta(canvas, item.getEtiqueta(), formatearMontoCorto(item.getMontoSeguro()), centersX[i], chartBottom + dp(22f));
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
            canvas.drawText(String.valueOf(value), chartLeft - dp(10f), y + dp(4f), yLabelPaint);
        }
    }

    private void dibujarLineaTendencia(@NonNull Canvas canvas,
                                       @NonNull ThemeManager themeManager,
                                       @NonNull float[] centersX,
                                       @NonNull float[] topsY,
                                       @NonNull long[] values) {
        if (centersX.length == 0) {
            return;
        }

        trendPath.reset();
        trendPath.moveTo(centersX[0], topsY[0]);
        for (int i = 1; i < centersX.length; i++) {
            trendPath.lineTo(centersX[i], topsY[i]);
        }
        canvas.drawPath(trendPath, linePaint);

        for (int i = 0; i < centersX.length; i++) {
            int pointColor = values[i] > 0L
                    ? themeManager.color(i == obtenerIndiceMaximo(obtenerMaximo())
                    ? ThemeKeys.ACCENT_SECONDARY
                    : ThemeKeys.ACCENT_PRIMARY)
                    : ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 170);
            pointFillPaint.setColor(ColorUtils.blendARGB(pointColor, Color.WHITE, 0.18f));
            pointStrokePaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 220));
            pointStrokePaint.setStrokeWidth(dp(1.1f));
            canvas.drawCircle(centersX[i], topsY[i], dp(4.8f), pointFillPaint);
            canvas.drawCircle(centersX[i], topsY[i], dp(4.8f), pointStrokePaint);
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

    private void dibujarEtiqueta(@NonNull Canvas canvas,
                                 @Nullable String etiqueta,
                                 @NonNull String monto,
                                 float centerX,
                                 float baseY) {
        String dia = etiqueta == null || etiqueta.trim().isEmpty()
                ? "-"
                : etiqueta.trim().toLowerCase(LOCALE_ES_MX).replace(".", "");
        canvas.drawText(dia, centerX, baseY, labelPaint);
        canvas.drawText(monto, centerX, baseY + dp(15f), amountPaint);
    }

    private long obtenerMaximo() {
        long maxValue = 0L;
        for (AdminPuntoSerieDTO item : items) {
            if (item != null) {
                maxValue = Math.max(maxValue, Math.max(0L, item.getValor()));
            }
        }
        return maxValue > 0L ? maxValue : 1L;
    }

    private int obtenerIndiceMaximo(long maxValue) {
        for (int i = 0; i < items.size(); i++) {
            AdminPuntoSerieDTO item = items.get(i);
            if (item != null && Math.max(0L, item.getValor()) == maxValue) {
                return i;
            }
        }
        return -1;
    }

    private void configurarPaints(@NonNull ThemeManager themeManager) {
        plotFillPaint.setColor(ColorUtils.blendARGB(
                themeManager.color(ThemeKeys.ACCOUNT_GLASS_PANEL),
                ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.ACCENT_SECONDARY), 40),
                0.18f
        ));

        plotStrokePaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 205));
        plotStrokePaint.setStrokeWidth(dp(1.2f));

        axisPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 220));
        axisPaint.setStrokeWidth(dp(1.2f));

        gridPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.CARD_BORDER), 88));
        gridPaint.setStrokeWidth(dp(1f));

        linePaint.setColor(ColorUtils.blendARGB(
                themeManager.color(ThemeKeys.ACCENT_SECONDARY),
                themeManager.color(ThemeKeys.ACCENT_PRIMARY),
                0.34f
        ));
        linePaint.setStrokeWidth(dp(2.2f));

        valuePaint.setColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
        valuePaint.setTextSize(sp(11.5f));
        valuePaint.setFakeBoldText(true);

        labelPaint.setColor(themeManager.color(ThemeKeys.TEXT_SECONDARY));
        labelPaint.setTextSize(sp(10.5f));
        labelPaint.setFakeBoldText(true);

        amountPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.TEXT_SECONDARY), 210));
        amountPaint.setTextSize(sp(9.4f));

        yLabelPaint.setColor(ColorUtils.setAlphaComponent(themeManager.color(ThemeKeys.TEXT_SECONDARY), 210));
        yLabelPaint.setTextSize(sp(10f));
    }

    @NonNull
    private String formatearMontoCorto(double monto) {
        if (monto >= 1_000_000d) {
            return String.format(Locale.US, "$%.1fM", monto / 1_000_000d);
        }
        if (monto >= 1_000d) {
            return String.format(Locale.US, "$%.1fk", monto / 1_000d);
        }
        if (monto <= 0d) {
            return "$0";
        }
        if (monto >= 100d) {
            return String.format(Locale.US, "$%.0f", monto);
        }
        return String.format(Locale.US, "$%.1f", monto);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
