package com.example.artistlan.Admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.model.AdminCategoriaStatsDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoriaStatsAdapter extends RecyclerView.Adapter<AdminCategoriaStatsAdapter.ViewHolder> {

    private static final int PROGRESS_MAX = 1000;
    private static final int MIN_VISIBLE_PROGRESS = 70;

    private final List<AdminCategoriaStatsDTO> items = new ArrayList<>();
    private long maxTotal = 0L;

    public void actualizar(@Nullable List<AdminCategoriaStatsDTO> nuevos) {
        items.clear();
        maxTotal = 0L;
        if (nuevos != null) {
            items.addAll(nuevos);
            for (AdminCategoriaStatsDTO item : nuevos) {
                if (item != null) {
                    maxTotal = Math.max(maxTotal, Math.max(0L, item.getTotal()));
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_categoria_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminCategoriaStatsDTO item = items.get(position);
        String categoria = item.getCategoria() == null || item.getCategoria().trim().isEmpty()
                ? "Sin categoria"
                : item.getCategoria().trim();
        long total = Math.max(0L, item.getTotal());
        int progress = calcularProgress(total);

        holder.tvCategoria.setText(categoria);
        holder.tvCantidad.setText(String.valueOf(total));
        holder.tvDetalle.setText(construirDetalle(total));

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        CardThemeHelper.applyMessageCard(holder.cardRoot, tm, total > 0);
        CardThemeHelper.applySoftChip(holder.tvCantidad, tm);
        ThemeApplier.applyTextPrimary(holder.tvCategoria, tm);
        ThemeApplier.applyTextSecondary(holder.tvDetalle, tm);

        int accent = (position % 2 == 0)
                ? tm.color(ThemeKeys.ACCENT_PRIMARY)
                : tm.color(ThemeKeys.ACCENT_SECONDARY);
        int track = ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 90);
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        holder.progressBar.setTrackThickness(Math.round(10f * density));
        holder.progressBar.setTrackCornerRadius(Math.round(6f * density));
        holder.progressBar.setIndicatorColor(accent);
        holder.progressBar.setTrackColor(track);
        holder.progressBar.setMax(PROGRESS_MAX);
        holder.progressBar.setProgressCompat(progress, false);
        holder.progressBar.setIndicatorColor(accent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int calcularProgress(long total) {
        if (maxTotal <= 0L || total <= 0L) {
            return 0;
        }
        int progress = (int) Math.round((total * PROGRESS_MAX) / (double) maxTotal);
        return Math.min(PROGRESS_MAX, Math.max(MIN_VISIBLE_PROGRESS, progress));
    }

    @NonNull
    private String construirDetalle(long total) {
        if (total <= 0L) {
            return "Sin registros visibles";
        }
        if (total == 1L) {
            return "1 registro visible";
        }
        return total + " registros visibles";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardRoot;
        final TextView tvCategoria;
        final TextView tvCantidad;
        final TextView tvDetalle;
        final LinearProgressIndicator progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardCategoriaStatRoot);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaNombreStat);
            tvCantidad = itemView.findViewById(R.id.tvCategoriaCantidadStat);
            tvDetalle = itemView.findViewById(R.id.tvCategoriaDetalleStat);
            progressBar = itemView.findViewById(R.id.progressCategoriaBarStat);
        }
    }
}
