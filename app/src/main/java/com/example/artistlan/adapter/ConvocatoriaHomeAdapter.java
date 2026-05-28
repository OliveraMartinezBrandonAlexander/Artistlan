package com.example.artistlan.adapter;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.model.ConvocatoriaDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConvocatoriaHomeAdapter extends RecyclerView.Adapter<ConvocatoriaHomeAdapter.ViewHolder> {

    public interface OnVerMasClick {
        void onClick(String url);
    }

    private final List<ConvocatoriaDTO> items = new ArrayList<>();
    private final Set<Integer> expandedPositions = new HashSet<>();
    private final OnVerMasClick onVerMasClick;

    public ConvocatoriaHomeAdapter(OnVerMasClick onVerMasClick) {
        this.onVerMasClick = onVerMasClick;
    }

    public void actualizar(List<ConvocatoriaDTO> nuevas) {
        items.clear();
        expandedPositions.clear();
        if (nuevas != null) items.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main_convocatoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConvocatoriaDTO item = items.get(position);

        holder.tvTitulo.setText(textoSeguro(item.getTitulo(), "Sin titulo"));
        DescripcionVisual descripcionVisual = prepararDescripcion(textoSeguro(item.getDescripcion(), "Sin descripcion"));
        holder.tvDescripcion.setText(descripcionVisual.resumen);
        holder.tvDetalle.setText(descripcionVisual.detalle);
        boolean expandido = expandedPositions.contains(position);
        boolean tieneDetalle = !TextUtils.isEmpty(descripcionVisual.detalle);
        holder.tvDetalle.setVisibility(tieneDetalle && expandido ? View.VISIBLE : View.GONE);
        holder.tvVerDescripcion.setVisibility(tieneDetalle ? View.VISIBLE : View.GONE);
        holder.tvVerDescripcion.setText(expandido ? "Ocultar detalles" : "Ver detalles");
        String fecha = textoSeguro(item.getFecha(), "");
        holder.tvFecha.setText(fecha.isEmpty() ? "Sin fecha" : "Fecha: " + fecha);

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        CardThemeHelper.applyThemedSurface(holder.layoutCard, tm, 18);
        CardThemeHelper.applyGlassChipSection(holder.layoutHeaderChip, tm, 16);
        CardThemeHelper.applyThemedSurface(holder.layoutBody, tm, 14);
        CardThemeHelper.applyThemedSurface(holder.layoutEnlaceSection, tm, 14);
        CardThemeHelper.applyChip(holder.tvFecha, tm);
        holder.tvTitulo.setBackground(null);
        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvDescripcionLabel, tm);
        ThemeApplier.applyTextSecondary(holder.tvEnlaceLabel, tm);
        ThemeApplier.applyTextPrimary(holder.tvDescripcion, tm);
        ThemeApplier.applyTextPrimary(holder.tvDetalle, tm);
        CardThemeHelper.applySubtleDivider(holder.dividerHeader, tm);
        CardThemeHelper.applySubtleDivider(holder.dividerEnlace, tm);
        CardThemeHelper.applyFilterSurface(holder.tvVerDescripcion, tm);
        holder.tvVerDescripcion.setTextColor(tm.color(ThemeKeys.FILTER_BUTTON_STROKE));
        CardThemeHelper.applyPrimaryBubbleButton(holder.btnVerMas, tm);
        holder.tvVerDescripcion.setGravity(Gravity.CENTER);

        holder.tvVerDescripcion.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            if (expandedPositions.contains(currentPosition)) {
                expandedPositions.remove(currentPosition);
            } else {
                expandedPositions.add(currentPosition);
            }
            notifyItemChanged(currentPosition);
        });

        String enlace = item.getEnlace();
        boolean enlaceValido = !TextUtils.isEmpty(enlace)
                && (enlace.startsWith("http://") || enlace.startsWith("https://"));
        holder.btnVerMas.setEnabled(enlaceValido);
        holder.btnVerMas.setAlpha(enlaceValido ? 1f : 0.5f);
        holder.btnVerMas.setOnClickListener(v -> {
            if (enlaceValido) onVerMasClick.onClick(enlace);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String textoSeguro(String valor, String fallback) {
        if (valor == null) {
            return fallback;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? fallback : limpio;
    }

    private DescripcionVisual prepararDescripcion(String descripcion) {
        String limpio = descripcion.replace("\r\n", "\n").replace('\r', '\n').trim();
        String[] partes = limpio.split("\\n+");
        List<String> lineas = new ArrayList<>();
        for (String parte : partes) {
            String linea = parte.trim();
            if (!linea.isEmpty()) {
                lineas.add(linea);
            }
        }
        if (lineas.size() > 1) {
            StringBuilder detalle = new StringBuilder();
            for (int i = 1; i < lineas.size(); i++) {
                if (detalle.length() > 0) {
                    detalle.append("\n");
                }
                detalle.append(formatearDetalle(lineas.get(i)));
            }
            return new DescripcionVisual(lineas.get(0), detalle.toString());
        }
        if (limpio.length() > 180) {
            int corte = encontrarCorteNatural(limpio, 150, 210);
            return new DescripcionVisual(limpio.substring(0, corte).trim(), limpio.substring(corte).trim());
        }
        return new DescripcionVisual(limpio, "");
    }

    private String formatearDetalle(String linea) {
        if (linea.contains(":")) {
            return "- " + linea;
        }
        return linea;
    }

    private int encontrarCorteNatural(String texto, int desde, int hasta) {
        int limite = Math.min(hasta, texto.length());
        for (int i = desde; i < limite; i++) {
            char c = texto.charAt(i);
            if (c == '.' || c == ';' || c == ':') {
                return i + 1;
            }
        }
        int espacio = texto.lastIndexOf(' ', limite);
        return espacio > desde ? espacio : limite;
    }

    private static class DescripcionVisual {
        final String resumen;
        final String detalle;

        DescripcionVisual(String resumen, String detalle) {
            this.resumen = resumen;
            this.detalle = detalle;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvDescripcionLabel;
        final TextView tvEnlaceLabel;
        final TextView tvDescripcion;
        final TextView tvDetalle;
        final TextView tvVerDescripcion;
        final TextView tvFecha;
        final Button btnVerMas;
        final LinearLayout layoutCard;
        final LinearLayout layoutBody;
        final View layoutHeaderChip;
        final View layoutEnlaceSection;
        final View dividerHeader;
        final View dividerEnlace;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCard = itemView.findViewById(R.id.layoutMainConvocatoriaCard);
            layoutBody = itemView.findViewById(R.id.layoutMainConvocatoriaBody);
            layoutHeaderChip = itemView.findViewById(R.id.layoutMainConvocatoriaHeaderChip);
            layoutEnlaceSection = itemView.findViewById(R.id.layoutMainConvocatoriaEnlaceSection);
            tvTitulo = itemView.findViewById(R.id.tvMainTituloConvocatoria);
            tvDescripcionLabel = itemView.findViewById(R.id.tvMainDescripcionLabelConvocatoria);
            tvEnlaceLabel = itemView.findViewById(R.id.tvMainEnlaceLabelConvocatoria);
            tvDescripcion = itemView.findViewById(R.id.tvMainDescripcionConvocatoria);
            tvDetalle = itemView.findViewById(R.id.tvMainDetalleConvocatoria);
            tvVerDescripcion = itemView.findViewById(R.id.tvMainVerDescripcionConvocatoria);
            tvFecha = itemView.findViewById(R.id.tvMainFechaConvocatoria);
            btnVerMas = itemView.findViewById(R.id.btnMainVerMasConvocatoria);
            dividerHeader = itemView.findViewById(R.id.dividerMainConvocatoriaHeader);
            dividerEnlace = itemView.findViewById(R.id.dividerMainConvocatoriaEnlace);
        }
    }
}
