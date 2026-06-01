package com.example.artistlan.Admin.view;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.example.artistlan.Conector.model.AdminCategoriaStatsDTO;
import com.example.artistlan.Conector.model.AdminPuntoSerieDTO;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.model.GradientColor;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AdminChartThemeHelper {

    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");

    public static final class CategoriaDonutLegendItem {
        private final String categoria;
        private final long total;
        private final float porcentaje;
        private final int color;

        public CategoriaDonutLegendItem(@NonNull String categoria, long total, float porcentaje, int color) {
            this.categoria = categoria;
            this.total = total;
            this.porcentaje = porcentaje;
            this.color = color;
        }

        @NonNull
        public String getCategoria() {
            return categoria;
        }

        public long getTotal() {
            return total;
        }

        public float getPorcentaje() {
            return porcentaje;
        }

        public int getColor() {
            return color;
        }
    }

    public static final class CategoriaDonutModel {
        private final List<CategoriaDonutLegendItem> items;
        private final float totalGeneral;

        public CategoriaDonutModel(@NonNull List<CategoriaDonutLegendItem> items, float totalGeneral) {
            this.items = items;
            this.totalGeneral = totalGeneral;
        }

        @NonNull
        public List<CategoriaDonutLegendItem> getItems() {
            return items;
        }

        public float getTotalGeneral() {
            return totalGeneral;
        }

        public boolean isEmpty() {
            return items.isEmpty() || totalGeneral <= 0f;
        }
    }

    private AdminChartThemeHelper() {
    }

    public static void prepareCategoriasChart(@NonNull HorizontalBarChart chart, @NonNull ThemeManager tm) {
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setNoDataText("Sin datos disponibles");
        chart.setNoDataTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setDrawValueAboveBar(true);
        chart.setFitBars(true);
        chart.setPinchZoom(false);
        chart.setScaleYEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setDragEnabled(false);
        chart.setDragXEnabled(false);
        chart.setDragYEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setExtraOffsets(18f, 14f, 36f, 10f);
        chart.setMinOffset(12f);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 78));
        xAxis.setGranularity(1f);
        xAxis.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        xAxis.setTextSize(10f);
        xAxis.setYOffset(6f);
        xAxis.setAxisLineColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 210));
        xAxis.setAxisMinimum(0f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(-0.5f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        leftAxis.setTextSize(11f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisLineColor(Color.TRANSPARENT);
        leftAxis.setXOffset(10f);
        leftAxis.setDrawZeroLine(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisLineColor(Color.TRANSPARENT);
        rightAxis.setAxisMinimum(-0.5f);
    }

    public static void renderCategoriasChart(@NonNull HorizontalBarChart chart,
                                             @NonNull List<AdminCategoriaStatsDTO> items,
                                             @NonNull ThemeManager tm) {
        prepareCategoriasChart(chart, tm);

        if (items.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<GradientColor> gradientColors = new ArrayList<>();
        float maxValue = 0f;

        for (int i = 0; i < items.size(); i++) {
            AdminCategoriaStatsDTO item = items.get(i);
            long total = item != null ? Math.max(0L, item.getTotal()) : 0L;
            entries.add(new BarEntry(i, total));
            labels.add(formatearCategoriaHorizontal(item != null ? item.getCategoria() : null));

            int base = (i % 2 == 0)
                    ? tm.color(ThemeKeys.ACCENT_PRIMARY)
                    : tm.color(ThemeKeys.ACCENT_SECONDARY);
            int top = ColorUtils.blendARGB(base, Color.WHITE, 0.32f);
            gradientColors.add(new GradientColor(top, base));
            maxValue = Math.max(maxValue, total);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setGradientColors(gradientColors);
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new IntegerValueFormatter());
        dataSet.setHighLightAlpha(0);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.64f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMaximum(maxValue <= 0f ? 1f : Math.max(1f, maxValue * 1.22f));
        xAxis.setValueFormatter(new IntegerValueFormatter());

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(items.size() - 0.5f);
        leftAxis.setLabelCount(items.size(), false);
        leftAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setAxisMaximum(items.size() - 0.5f);

        chart.setData(data);
        ajustarAlturaCategoriasChart(chart, items.size());
        chart.notifyDataSetChanged();
        chart.animateY(520);
        chart.invalidate();
    }

    public static void prepareCategoriasDonutChart(@NonNull PieChart chart, @NonNull ThemeManager tm) {
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setNoDataText("Sin datos disponibles");
        chart.setNoDataTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        chart.setUsePercentValues(true);
        chart.setDrawEntryLabels(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(56f);
        chart.setTransparentCircleRadius(61f);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), 72));
        chart.setTransparentCircleAlpha(100);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setDrawSlicesUnderHole(false);
        chart.setDrawRoundedSlices(true);
        chart.setMinAngleForSlices(0f);
        chart.setExtraOffsets(8f, 8f, 8f, 8f);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        chart.setCenterText("");
        chart.setCenterTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        chart.setCenterTextSize(17f);
        chart.setCenterTextRadiusPercent(95f);
    }

    @NonNull
    public static CategoriaDonutModel buildCategoriasDonutModel(@NonNull List<AdminCategoriaStatsDTO> items,
                                                                @NonNull ThemeManager tm) {
        List<AdminCategoriaStatsDTO> conDatos = new ArrayList<>();
        float totalGeneral = 0f;

        for (AdminCategoriaStatsDTO item : items) {
            long total = item != null ? Math.max(0L, item.getTotal()) : 0L;
            if (total <= 0L) {
                continue;
            }
            conDatos.add(item);
            totalGeneral += total;
        }

        if (conDatos.isEmpty() || totalGeneral <= 0f) {
            return new CategoriaDonutModel(new ArrayList<>(), 0f);
        }

        List<Integer> colors = construirPaletteCategorias(tm, conDatos.size());
        List<CategoriaDonutLegendItem> legendItems = new ArrayList<>();

        for (int i = 0; i < conDatos.size(); i++) {
            AdminCategoriaStatsDTO item = conDatos.get(i);
            long total = Math.max(0L, item.getTotal());
            float porcentaje = (total * 100f) / totalGeneral;
            legendItems.add(new CategoriaDonutLegendItem(
                    formatearCategoriaDona(item.getCategoria()),
                    total,
                    porcentaje,
                    colors.get(i)
            ));
        }

        return new CategoriaDonutModel(legendItems, totalGeneral);
    }

    public static void renderCategoriasDonutChart(@NonNull PieChart chart,
                                                  @NonNull CategoriaDonutModel model,
                                                  @NonNull ThemeManager tm) {
        prepareCategoriasDonutChart(chart, tm);

        if (model.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (CategoriaDonutLegendItem item : model.getItems()) {
            entries.add(new PieEntry(item.getTotal(), item.getCategoria()));
            colors.add(item.getColor());
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(entries.size() > 1 ? 3.8f : 0f);
        dataSet.setSelectionShift(4f);
        dataSet.setColors(colors);
        dataSet.setDrawValues(entries.size() <= 4);
        dataSet.setValueTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        dataSet.setValueTextSize(11f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);

        PieData data = new PieData(dataSet);
        if (entries.size() <= 4) {
            data.setValueFormatter(new PercentFormatter(chart));
        } else {
            data.setDrawValues(false);
        }

        chart.setData(data);
        chart.highlightValues(null);
        chart.animateY(520);
        chart.invalidate();
    }

    public static void prepareVentasChart(@NonNull LineChart chart, @NonNull ThemeManager tm) {
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setNoDataText("Sin datos disponibles");
        chart.setNoDataTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        chart.setDrawGridBackground(false);
        chart.setScaleYEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setExtraOffsets(10f, 10f, 12f, 10f);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        xAxis.setTextSize(10.5f);
        xAxis.setYOffset(8f);
        xAxis.setAxisLineColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 210));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        leftAxis.setTextSize(10f);
        leftAxis.setGridColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 86));
        leftAxis.setAxisLineColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 210));
        leftAxis.setValueFormatter(new IntegerValueFormatter());

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public static void renderVentasChart(@NonNull LineChart chart,
                                         @NonNull List<AdminPuntoSerieDTO> puntos,
                                         @NonNull ThemeManager tm) {
        prepareVentasChart(chart, tm);

        if (puntos.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        float maxValue = 0f;

        for (int i = 0; i < puntos.size(); i++) {
            AdminPuntoSerieDTO punto = puntos.get(i);
            float valor = punto != null ? Math.max(0L, punto.getValor()) : 0f;
            entries.add(new Entry(i, valor));
            labels.add(formatearEtiquetaDia(punto != null ? punto.getEtiqueta() : null));
            maxValue = Math.max(maxValue, valor);
        }

        int accentPrimary = tm.color(ThemeKeys.ACCENT_PRIMARY);
        int accentSecondary = tm.color(ThemeKeys.ACCENT_SECONDARY);
        int fillColor = ColorUtils.blendARGB(accentPrimary, tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), 0.44f);

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(accentPrimary);
        dataSet.setCircleColor(accentSecondary);
        dataSet.setCircleHoleColor(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL));
        dataSet.setCircleRadius(4.2f);
        dataSet.setCircleHoleRadius(2.1f);
        dataSet.setLineWidth(2.8f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ColorUtils.setAlphaComponent(fillColor, 165));
        dataSet.setFillAlpha(165);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        dataSet.setValueTextSize(10.5f);
        dataSet.setValueFormatter(new IntegerValueFormatter());
        dataSet.setHighLightColor(ColorUtils.setAlphaComponent(accentSecondary, 180));

        LineData data = new LineData(dataSet);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size(), true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(maxValue <= 0f ? 1f : Math.max(1f, maxValue * 1.28f));

        chart.setData(data);
        chart.animateX(520);
        chart.invalidate();
    }

    public static void prepareCrecimientoChart(@NonNull LineChart chart, @NonNull ThemeManager tm) {
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setNoDataText("Sin datos disponibles");
        chart.setNoDataTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        chart.setDrawGridBackground(false);
        chart.setScaleYEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDragEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setExtraOffsets(12f, 12f, 14f, 10f);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        legend.setTextSize(10f);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setFormSize(14f);
        legend.setXEntrySpace(14f);
        legend.setWordWrapEnabled(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        xAxis.setTextSize(10.5f);
        xAxis.setYOffset(8f);
        xAxis.setAxisLineColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 210));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        leftAxis.setTextSize(10f);
        leftAxis.setGridColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 86));
        leftAxis.setAxisLineColor(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 210));
        leftAxis.setValueFormatter(new IntegerValueFormatter());

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public static void renderCrecimientoChart(@NonNull LineChart chart,
                                              @NonNull List<AdminPuntoSerieDTO> puntosActuales,
                                              @NonNull List<AdminPuntoSerieDTO> puntosAnteriores,
                                              @NonNull ThemeManager tm) {
        prepareCrecimientoChart(chart, tm);

        if (puntosActuales.isEmpty() && puntosAnteriores.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        List<Entry> entriesActuales = new ArrayList<>();
        List<Entry> entriesReferencia = new ArrayList<>();
        List<String> labels = construirEtiquetasSemana();
        long acumuladoActual = 0L;
        long acumuladoAnterior = 0L;
        int totalPuntos = Math.min(labels.size(), Math.max(puntosActuales.size(), puntosAnteriores.size()));
        float maxValue = 0f;

        for (int i = 0; i < totalPuntos; i++) {
            AdminPuntoSerieDTO puntoActual = i < puntosActuales.size() ? puntosActuales.get(i) : null;
            AdminPuntoSerieDTO puntoAnterior = i < puntosAnteriores.size() ? puntosAnteriores.get(i) : null;

            acumuladoActual += puntoActual != null ? Math.max(0L, puntoActual.getValor()) : 0L;
            acumuladoAnterior += puntoAnterior != null ? Math.max(0L, puntoAnterior.getValor()) : 0L;

            entriesActuales.add(new Entry(i, acumuladoActual));
            entriesReferencia.add(new Entry(i, acumuladoAnterior));
            maxValue = Math.max(maxValue, Math.max(acumuladoActual, acumuladoAnterior));
        }

        int accentPrimary = tm.color(ThemeKeys.ACCENT_PRIMARY);
        int accentSecondary = tm.color(ThemeKeys.ACCENT_SECONDARY);
        int comparison = ColorUtils.blendARGB(accentSecondary, Color.WHITE, 0.12f);

        LineDataSet actualDataSet = new LineDataSet(entriesActuales, "Actual acum.");
        actualDataSet.setColor(accentPrimary);
        actualDataSet.setCircleColor(accentPrimary);
        actualDataSet.setCircleHoleColor(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL));
        actualDataSet.setCircleRadius(4.4f);
        actualDataSet.setCircleHoleRadius(2.2f);
        actualDataSet.setLineWidth(3f);
        actualDataSet.setMode(LineDataSet.Mode.LINEAR);
        actualDataSet.setDrawFilled(false);
        actualDataSet.setDrawValues(true);
        actualDataSet.setValueTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        actualDataSet.setValueTextSize(10f);
        actualDataSet.setValueFormatter(new IntegerValueFormatter());
        actualDataSet.setHighLightColor(ColorUtils.setAlphaComponent(accentPrimary, 180));

        LineDataSet referenciaDataSet = new LineDataSet(entriesReferencia, "Anterior acum.");
        referenciaDataSet.setColor(comparison);
        referenciaDataSet.setCircleColor(comparison);
        referenciaDataSet.setCircleRadius(3.6f);
        referenciaDataSet.setCircleHoleRadius(1.8f);
        referenciaDataSet.setCircleHoleColor(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL));
        referenciaDataSet.setDrawCircles(true);
        referenciaDataSet.setLineWidth(2.2f);
        referenciaDataSet.enableDashedLine(14f, 8f, 0f);
        referenciaDataSet.setMode(LineDataSet.Mode.LINEAR);
        referenciaDataSet.setDrawFilled(false);
        referenciaDataSet.setDrawValues(false);
        referenciaDataSet.setHighLightColor(ColorUtils.setAlphaComponent(comparison, 160));

        LineData data = new LineData(actualDataSet, referenciaDataSet);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new SemanaAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size(), true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.size() - 1f);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(maxValue <= 0f ? 1f : Math.max(1f, maxValue * 1.18f));

        chart.setData(data);
        chart.animateX(520);
        chart.invalidate();
    }

    @NonNull
    private static String formatearCategoriaHorizontal(@Nullable String categoria) {
        String limpia = categoria == null ? "" : categoria.trim();
        if (limpia.isEmpty()) {
            return "Sin cat.";
        }
        if (limpia.length() <= 22) {
            return limpia;
        }
        return limpia.substring(0, 21).trim() + "...";
    }

    private static void ajustarAlturaCategoriasChart(@NonNull HorizontalBarChart chart, int itemCount) {
        ViewGroup.LayoutParams params = chart.getLayoutParams();
        if (params == null) {
            return;
        }

        float density = chart.getResources().getDisplayMetrics().density;
        int filas = Math.max(5, itemCount);
        int altoDp = 88 + (filas * 38);
        params.height = Math.round(altoDp * density);
        chart.setLayoutParams(params);
    }

    @NonNull
    private static String formatearCategoriaDona(@Nullable String categoria) {
        String limpia = categoria == null ? "" : categoria.trim();
        if (limpia.isEmpty()) {
            return "Sin categoría";
        }
        if (limpia.length() <= 28) {
            return limpia;
        }
        return limpia.substring(0, 27).trim() + "...";
    }

    @NonNull
    private static List<Integer> construirPaletteCategorias(@NonNull ThemeManager tm, int count) {
        List<Integer> palette = new ArrayList<>();
        int panel = tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL);
        int chip = tm.color(ThemeKeys.CARD_CHIP_BG);
        int[] bases = new int[]{
                Color.parseColor("#74D3AE"),
                Color.parseColor("#6FB7FF"),
                Color.parseColor("#A88CFF"),
                Color.parseColor("#F2C66D"),
                Color.parseColor("#F48E7D"),
                Color.parseColor("#4FD3C4"),
                Color.parseColor("#E8A0C7"),
                Color.parseColor("#B7A2FF"),
                Color.parseColor("#63C6B4"),
                Color.parseColor("#F0B56A"),
                Color.parseColor("#7FA6FF"),
                Color.parseColor("#F2A57E")
        };
        float[] ratios = new float[]{0.12f, 0.16f, 0.14f, 0.10f, 0.15f, 0.12f, 0.18f, 0.14f, 0.16f, 0.11f, 0.18f, 0.13f};

        for (int i = 0; i < count; i++) {
            int base = bases[i % bases.length];
            float ratio = ratios[i % ratios.length];
            int mixed = ColorUtils.blendARGB(base, panel, ratio);
            if (i % 3 == 1) {
                mixed = ColorUtils.blendARGB(mixed, chip, 0.12f);
            } else if (i % 3 == 2) {
                mixed = ColorUtils.blendARGB(mixed, Color.WHITE, 0.08f);
            }
            palette.add(mixed);
        }
        return palette;
    }

    @NonNull
    private static String formatearEtiquetaDia(@Nullable String etiqueta) {
        String limpia = etiqueta == null ? "" : etiqueta.trim();
        if (limpia.isEmpty()) {
            return "-";
        }
        return limpia.toLowerCase(LOCALE_ES_MX).replace(".", "");
    }

    @NonNull
    private static List<String> construirEtiquetasSemana() {
        List<String> labels = new ArrayList<>(7);
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            if (dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }
            labels.add(formatearEtiquetaDia(dayOfWeek.getDisplayName(TextStyle.SHORT, LOCALE_ES_MX)));
        }
        labels.add(formatearEtiquetaDia(DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, LOCALE_ES_MX)));
        return labels;
    }

    private static final class IntegerValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf(Math.round(value));
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return getFormattedValue(value);
        }

        @Override
        public String getBarLabel(BarEntry barEntry) {
            return String.valueOf(Math.round(barEntry.getY()));
        }

        @Override
        public String getPointLabel(Entry entry) {
            return String.valueOf(Math.round(entry.getY()));
        }
    }

    private static final class SemanaAxisValueFormatter extends ValueFormatter {
        private final List<String> labels;

        private SemanaAxisValueFormatter(@NonNull List<String> labels) {
            this.labels = labels;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = Math.round(value);
            if (index < 0 || index >= labels.size()) {
                return "";
            }
            if (Math.abs(value - index) > 0.05f) {
                return "";
            }
            return labels.get(index);
        }
    }
}

