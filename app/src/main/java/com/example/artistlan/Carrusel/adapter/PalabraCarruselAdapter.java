package com.example.artistlan.Carrusel.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.artistlan.R;
import com.example.artistlan.Carrusel.model.PalabraCarruselItem;
import java.util.List;

public class PalabraCarruselAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PalabraCarruselItem> palabras;
    private Context context;
    private OnCategoriaClickListener listener;
    private int itemSeleccionado = 1;

    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnCategoriaClickListener {
        void onCategoriaClick(int position, PalabraCarruselItem categoria);
        void onCategoriaCentrada(int position, PalabraCarruselItem categoria);
    }

    public PalabraCarruselAdapter(List<PalabraCarruselItem> palabras, Context context, OnCategoriaClickListener listener) {
        this.palabras = palabras;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_carrusel_vacio, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_carrusel_palabra, parent, false);
            return new PalabraCarruselViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PalabraCarruselViewHolder) {
            int dataPosition = position - 1; // Convertir posición del adapter a posición real
            PalabraCarruselItem palabra = palabras.get(dataPosition);
            PalabraCarruselViewHolder viewHolder = (PalabraCarruselViewHolder) holder;

            viewHolder.tvPalabra.setText(palabra.getPalabra());

            // Cambiar apariencia según selección
            if (position == itemSeleccionado) {
                viewHolder.itemView.setBackgroundColor(palabra.getColorSeleccionado());
                viewHolder.tvPalabra.setTextColor(Color.WHITE);
            } else {
                viewHolder.itemView.setBackgroundColor(palabra.getColorNormal());
                viewHolder.tvPalabra.setTextColor(Color.WHITE);
            }

            viewHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoriaClick(position, palabra);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return palabras.size() + 2; // +2 por los items vacíos
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == getItemCount() - 1) {
            return TYPE_EMPTY;
        }
        return TYPE_ITEM;
    }

    public void setItemSeleccionado(int position) {
        // Solo permitir selección de items reales
        if (position > 0 && position < getItemCount() - 1) {
            int oldPosition = itemSeleccionado;
            itemSeleccionado = position;

            // Notificar cambios
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition);
            }
            notifyItemChanged(position);

            // Notificar al listener
            if (listener != null) {
                int dataPosition = position - 1;
                listener.onCategoriaCentrada(dataPosition, palabras.get(dataPosition));
            }
        }
    }

    public int getItemSeleccionado() {
        return itemSeleccionado;
    }

    public PalabraCarruselItem getCategoriaSeleccionada() {
        if (itemSeleccionado > 0 && itemSeleccionado < getItemCount() - 1) {
            int dataPosition = itemSeleccionado - 1;
            return palabras.get(dataPosition);
        }
        return null;
    }

    // ViewHolder para items vacíos
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // ViewHolder para items reales
    public static class PalabraCarruselViewHolder extends RecyclerView.ViewHolder {
        TextView tvPalabra;

        public PalabraCarruselViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPalabra = itemView.findViewById(R.id.tvPalabra);
        }
    }
}