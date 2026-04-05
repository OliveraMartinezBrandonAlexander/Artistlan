package com.example.artistlan.Admin.adapter;

import android.graphics.PorterDuff;
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
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

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
        holder.tvDescripcion.setText(item.getDescripcion());
        holder.tvFecha.setText(item.getFecha() == null ? "Sin fecha" : item.getFecha());

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());

        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvDescripcion, tm);
        ThemeApplier.applyTextSecondary(holder.tvFecha, tm);
        ThemeApplier.applyPrimaryButton(holder.btnEditar, tm);
        ThemeApplier.applySecondaryButton(holder.btnEliminar, tm);

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(item));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvDescripcion;
        final TextView tvFecha;
        final Button btnEditar;
        final Button btnEliminar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloConvocatoria);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionConvocatoria);
            tvFecha = itemView.findViewById(R.id.tvFechaConvocatoria);
            btnEditar = itemView.findViewById(R.id.btnEditarConvocatoria);
            btnEliminar = itemView.findViewById(R.id.btnEliminarConvocatoria);
        }
    }
}