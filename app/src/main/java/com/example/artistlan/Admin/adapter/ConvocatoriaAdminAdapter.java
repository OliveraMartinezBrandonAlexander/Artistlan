package com.example.artistlan.Admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.model.ConvocatoriaDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ConvocatoriaAdminAdapter extends RecyclerView.Adapter<ConvocatoriaAdminAdapter.ViewHolder> {

    public interface AccionesListener {
        void onEditar(ConvocatoriaDTO item);
        void onEliminar(ConvocatoriaDTO item);
    }

    private final List<ConvocatoriaDTO> items = new ArrayList<>();
    private final AccionesListener listener;

    public ConvocatoriaAdminAdapter(AccionesListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<ConvocatoriaDTO> nuevas) {
        items.clear();
        if (nuevas != null) items.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_convocatoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConvocatoriaDTO item = items.get(position);
        holder.tvTitulo.setText(item.getTitulo());
        holder.tvDescripcion.setText(formatearDescripcion(item.getDescripcion()));
        holder.tvFecha.setText(item.getFecha() == null ? "Sin fecha" : "Fecha: " + item.getFecha());

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());

        CardThemeHelper.applyFlatCard(holder.card, tm);
        CardThemeHelper.applyChip(holder.tvFecha, tm);
        CardThemeHelper.applyChip(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvDescripcionLabel, tm);
        ThemeApplier.applyTextSecondary(holder.tvDescripcion, tm);
        CardThemeHelper.applyPrimaryBubbleButton(holder.btnEditar, tm);
        CardThemeHelper.applySecondaryBubbleButton(holder.btnEliminar, tm);

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(item));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatearDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return "Sin descripcion";
        }
        String limpio = descripcion.replace("\r\n", "\n").replace('\r', '\n').trim();
        String[] partes = limpio.split("\\n+");
        StringBuilder resultado = new StringBuilder();
        for (String parte : partes) {
            String linea = parte.trim();
            if (linea.isEmpty()) {
                continue;
            }
            if (resultado.length() > 0) {
                resultado.append("\n");
            }
            resultado.append(linea.contains(":") && !linea.startsWith("Resumen:") ? "- " : "").append(linea);
        }
        return resultado.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvDescripcion;
        final TextView tvFecha;
        final Button btnEditar;
        final Button btnEliminar;
        final MaterialCardView card;
        final TextView tvDescripcionLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAdminConvocatoria);
            tvTitulo = itemView.findViewById(R.id.tvTituloConvocatoria);
            tvDescripcionLabel = itemView.findViewById(R.id.tvDescripcionAdminLabelConvocatoria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionConvocatoria);
            tvFecha = itemView.findViewById(R.id.tvFechaConvocatoria);
            btnEditar = itemView.findViewById(R.id.btnEditarConvocatoria);
            btnEliminar = itemView.findViewById(R.id.btnEliminarConvocatoria);
        }
    }
}
