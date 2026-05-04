package com.example.artistlan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.model.ReporteResumenDTO;
import com.example.artistlan.R;

import java.util.ArrayList;
import java.util.List;

public class ReportesModeracionAdapter extends RecyclerView.Adapter<ReportesModeracionAdapter.ViewHolder> {

    public interface OnReporteClickListener {
        void onReporteClick(ReporteResumenDTO reporte);
    }

    private final List<ReporteResumenDTO> reportes;
    private final OnReporteClickListener onReporteClickListener;

    public ReportesModeracionAdapter(List<ReporteResumenDTO> reportes, OnReporteClickListener onReporteClickListener) {
        this.reportes = reportes != null ? reportes : new ArrayList<>();
        this.onReporteClickListener = onReporteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reporte_moderacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReporteResumenDTO reporte = reportes.get(position);
        holder.tvIdReporte.setText("Reporte #" + safeNumero(reporte.getIdReporte()));
        holder.tvTipoObjetivo.setText("Tipo: " + safeText(reporte.getTipoObjetivo(), "N/A"));
        holder.tvTituloObjetivo.setText("Objetivo: " + resolverTitulo(reporte));
        holder.tvMotivo.setText("Motivo: " + safeText(reporte.getMotivo(), "Sin motivo"));
        holder.tvEstado.setText("Estado: " + safeText(reporte.getEstado(), "N/A"));
        holder.tvPrioridad.setText("Prioridad: " + safeText(reporte.getPrioridad(), "N/A"));
        holder.tvReportante.setText("Reportante: " + safeText(reporte.getNombreUsuarioReportante(), "No disponible"));
        holder.tvModeradorAsignado.setText("Moderador: " + safeText(reporte.getNombreModeradorAsignado(), "Sin asignar"));
        holder.tvFechaReporte.setText("Fecha: " + safeText(reporte.getFechaReporte(), "No disponible"));

        holder.itemView.setOnClickListener(v -> {
            if (onReporteClickListener != null) {
                onReporteClickListener.onReporteClick(reporte);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportes.size();
    }

    public void actualizarLista(@NonNull List<ReporteResumenDTO> nuevaLista) {
        reportes.clear();
        reportes.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    private String resolverTitulo(ReporteResumenDTO reporte) {
        String titulo = safeText(reporte.getTituloObjetivo(), null);
        if (titulo != null) {
            return titulo;
        }
        String usuarioReportado = safeText(reporte.getNombreUsuarioReportado(), null);
        if (usuarioReportado != null) {
            return usuarioReportado;
        }
        return "Sin t\u00edtulo";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String safeNumero(Integer value) {
        return value != null ? String.valueOf(value) : "N/A";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdReporte;
        TextView tvTipoObjetivo;
        TextView tvTituloObjetivo;
        TextView tvMotivo;
        TextView tvEstado;
        TextView tvPrioridad;
        TextView tvReportante;
        TextView tvModeradorAsignado;
        TextView tvFechaReporte;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdReporte = itemView.findViewById(R.id.tvIdReporteModeracion);
            tvTipoObjetivo = itemView.findViewById(R.id.tvTipoObjetivoModeracion);
            tvTituloObjetivo = itemView.findViewById(R.id.tvTituloObjetivoModeracion);
            tvMotivo = itemView.findViewById(R.id.tvMotivoModeracion);
            tvEstado = itemView.findViewById(R.id.tvEstadoModeracionItem);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridadModeracionItem);
            tvReportante = itemView.findViewById(R.id.tvReportanteModeracion);
            tvModeradorAsignado = itemView.findViewById(R.id.tvModeradorAsignadoModeracion);
            tvFechaReporte = itemView.findViewById(R.id.tvFechaReporteModeracion);
        }
    }
}
