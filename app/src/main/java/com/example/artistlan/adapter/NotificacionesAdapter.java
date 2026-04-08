package com.example.artistlan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.model.NotificacionDTO;
import com.example.artistlan.Fragments.MensajeUiUtils;
import com.example.artistlan.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder> {

    public interface Listener {
        void onDetalle(@NonNull NotificacionDTO item);

        void onMarcarLeida(@NonNull NotificacionDTO item);

        void onEliminar(@NonNull NotificacionDTO item);
    }

    private final List<NotificacionDTO> items = new ArrayList<>();
    private final Listener listener;

    public NotificacionesAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion_centro, parent, false);
        return new NotificacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        NotificacionDTO item = items.get(position);
        holder.tvTitulo.setText(item.getTituloSeguro());
        holder.tvMensaje.setText(item.getMensajeSeguro());
        holder.tvFecha.setText(MensajeUiUtils.formatearFechaCorta(item.getFecha()));

        String origenNombre = item.getUsuarioOrigenSeguro();
        holder.tvOrigenNombre.setText(
                origenNombre == null || origenNombre.trim().isEmpty()
                        ? (item.esDeSistema() ? "Sistema" : "Usuario")
                        : origenNombre.trim()
        );

        holder.tvChipOrigen.setText(item.esDeSistema() ? "SISTEMA" : "USUARIO");
        holder.tvChipOrigen.setBackgroundResource(item.esDeSistema()
                ? R.drawable.bg_chip_mensaje_sistema
                : R.drawable.bg_chip_mensaje_usuario);

        boolean tieneReferenciaNavegable = item.getReferenciaTipo() != null
                && !item.getReferenciaTipo().trim().isEmpty()
                && item.getReferenciaId() != null;
        String referencia = MensajeUiUtils.etiquetaReferencia(item.getReferenciaTipo(), item.getReferenciaId());
        if (!tieneReferenciaNavegable || referencia.isEmpty()) {
            holder.tvReferencia.setVisibility(View.GONE);
        } else {
            holder.tvReferencia.setVisibility(View.VISIBLE);
            holder.tvReferencia.setText(referencia);
        }

        holder.unreadDot.setVisibility(item.isLeida() ? View.GONE : View.VISIBLE);
        holder.btnMarcarLeida.setVisibility(item.isLeida() ? View.GONE : View.VISIBLE);

        int surfaceRead = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_surface);
        int surfaceUnread = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_surface_soft);
        int strokeRead = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_stroke_soft);
        int strokeUnread = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_accent);

        holder.cardRoot.setCardBackgroundColor(item.isLeida() ? surfaceRead : surfaceUnread);
        holder.cardRoot.setStrokeColor(item.isLeida() ? strokeRead : strokeUnread);
        holder.cardRoot.setStrokeWidth(item.isLeida() ? 1 : 2);

        Glide.with(holder.itemView)
                .load(item.getFotoOrigen())
                .placeholder(R.drawable.cuenta)
                .error(R.drawable.cuenta)
                .circleCrop()
                .into(holder.ivOrigen);

        holder.cardRoot.setOnClickListener(v -> listener.onDetalle(item));
        holder.btnVerDetalle.setOnClickListener(v -> listener.onDetalle(item));
        holder.btnMarcarLeida.setOnClickListener(v -> listener.onMarcarLeida(item));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<NotificacionDTO> nuevos) {
        items.clear();
        if (nuevos != null) {
            items.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    public void removeItem(@NonNull NotificacionDTO target) {
        int index = items.indexOf(target);
        if (index >= 0) {
            items.remove(index);
            notifyItemRemoved(index);
            return;
        }
        notifyDataSetChanged();
    }

    public void marcarTodasLeidas() {
        for (NotificacionDTO item : items) {
            if (item != null) {
                item.setLeida(true);
            }
        }
        notifyDataSetChanged();
    }

    public List<NotificacionDTO> getItems() {
        return items;
    }

    static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardRoot;
        private final ImageView ivOrigen;
        private final View unreadDot;
        private final TextView tvChipOrigen;
        private final TextView tvTitulo;
        private final TextView tvMensaje;
        private final TextView tvOrigenNombre;
        private final TextView tvFecha;
        private final TextView tvReferencia;
        private final TextView btnVerDetalle;
        private final TextView btnMarcarLeida;
        private final ImageButton btnEliminar;

        NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardMensajeRoot);
            ivOrigen = itemView.findViewById(R.id.ivMensajeOrigen);
            unreadDot = itemView.findViewById(R.id.viewMensajeNoLeida);
            tvChipOrigen = itemView.findViewById(R.id.tvMensajeChipOrigen);
            tvTitulo = itemView.findViewById(R.id.tvMensajeTitulo);
            tvMensaje = itemView.findViewById(R.id.tvMensajeBody);
            tvOrigenNombre = itemView.findViewById(R.id.tvMensajeOrigenNombre);
            tvFecha = itemView.findViewById(R.id.tvMensajeFecha);
            tvReferencia = itemView.findViewById(R.id.tvMensajeReferencia);
            btnVerDetalle = itemView.findViewById(R.id.btnMensajeDetalle);
            btnMarcarLeida = itemView.findViewById(R.id.btnMensajeMarcarLeida);
            btnEliminar = itemView.findViewById(R.id.btnMensajeEliminar);
        }
    }
}
