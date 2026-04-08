package com.example.artistlan.adapter;

import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;

public class ConvocatoriaHomeAdapter extends RecyclerView.Adapter<ConvocatoriaHomeAdapter.ViewHolder> {

    public interface OnVerMasClick {
        void onClick(String url);
    }

    private final List<ConvocatoriaDTO> items = new ArrayList<>();
    private final OnVerMasClick onVerMasClick;

    public ConvocatoriaHomeAdapter(OnVerMasClick onVerMasClick) {
        this.onVerMasClick = onVerMasClick;
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
                .inflate(R.layout.item_main_convocatoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConvocatoriaDTO item = items.get(position);

        holder.tvTitulo.setText(textoSeguro(item.getTitulo(), "Sin titulo"));
        holder.tvDescripcion.setText(textoSeguro(item.getDescripcion(), "Sin descripcion"));
        String fecha = textoSeguro(item.getFecha(), "");
        holder.tvFecha.setText(fecha.isEmpty() ? "Sin fecha" : "Fecha: " + fecha);

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvDescripcion, tm);
        ThemeApplier.applyTextSecondary(holder.tvFecha, tm);
        ThemeApplier.applyPrimaryButton(holder.btnVerMas, tm);

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvDescripcion;
        final TextView tvFecha;
        final Button btnVerMas;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvMainTituloConvocatoria);
            tvDescripcion = itemView.findViewById(R.id.tvMainDescripcionConvocatoria);
            tvFecha = itemView.findViewById(R.id.tvMainFechaConvocatoria);
            btnVerMas = itemView.findViewById(R.id.btnMainVerMasConvocatoria);
        }
    }
}