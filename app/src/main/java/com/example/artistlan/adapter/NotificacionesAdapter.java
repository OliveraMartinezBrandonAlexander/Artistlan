package com.example.artistlan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Conector.model.NotificacionDTO;
import com.example.artistlan.Fragments.MensajeUiUtils;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder> {

    public interface Listener {
        void onDetalle(@NonNull NotificacionDTO item);

        void onNavegar(@NonNull NotificacionDTO item);

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
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        NotificacionDTO item = items.get(position);
        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvMensaje, tm);
        ThemeApplier.applyTextSecondary(holder.tvOrigenNombre, tm);
        ThemeApplier.applyTextSecondary(holder.tvFecha, tm);
        CardThemeHelper.applyMessageCard(holder.cardRoot, tm, !item.isLeida());
        CardThemeHelper.applyPrimaryBubbleSurface(holder.btnVerDetalle, holder.btnVerDetalle, tm);
        CardThemeHelper.applySecondaryBubbleSurface(holder.btnMarcarLeida, holder.btnMarcarLeida, tm);
        CardThemeHelper.applyFilterButton(holder.btnEliminar, tm);
        CardThemeHelper.applyAccentDot(holder.unreadDot, tm);
        CardThemeHelper.applySoftChip(holder.tvChipOrigen, tm);
        CardThemeHelper.applySoftChip(holder.tvReferencia, tm);

        holder.tvTitulo.setText(item.getTituloSeguro());
        holder.tvMensaje.setText(MensajeUiUtils.formatearMensajeConMotivo(item.getMensajeSeguro()));
        holder.tvFecha.setText(MensajeUiUtils.formatearFechaCorta(item.getFecha()));

        String origenNombre = item.getUsuarioOrigenSeguro();
        String origenVisual = origenNombre == null || origenNombre.trim().isEmpty()
                ? (item.esDeSistema() ? "Sistema" : "Usuario")
                : origenNombre.trim();
        holder.tvOrigenNombre.setText("De: " + origenVisual);

        holder.tvChipOrigen.setText(item.esDeSistema() ? "SISTEMA" : "USUARIO");

        holder.tvReferencia.setVisibility(View.GONE);

        holder.unreadDot.setVisibility(item.isLeida() ? View.GONE : View.VISIBLE);
        holder.btnMarcarLeida.setVisibility(item.isLeida() ? View.GONE : View.VISIBLE);

        Glide.with(holder.itemView)
                .load(item.getFotoOrigen())
                .placeholder(R.drawable.cuenta)
                .error(R.drawable.cuenta)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(holder.ivOrigen);

        String ctaTexto = MensajeUiUtils.obtenerTextoCtaSemantico(item);
        if (ctaTexto == null || ctaTexto.trim().isEmpty()) {
            if (MensajeUiUtils.tieneReferenciaNavegable(item)) {
                holder.btnVerDetalle.setVisibility(View.VISIBLE);
                holder.btnVerDetalle.setText("Ver detalle");
                holder.btnVerDetalle.setOnClickListener(v -> listener.onDetalle(item));
            } else {
                holder.btnVerDetalle.setVisibility(View.GONE);
                holder.btnVerDetalle.setOnClickListener(null);
            }
        } else {
            holder.btnVerDetalle.setVisibility(View.VISIBLE);
            holder.btnVerDetalle.setText(ctaTexto);
            holder.btnVerDetalle.setOnClickListener(v -> listener.onNavegar(item));
        }

        holder.cardRoot.setOnClickListener(v -> listener.onDetalle(item));
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

    public void agregarItems(List<NotificacionDTO> nuevos) {
        if (nuevos == null || nuevos.isEmpty()) {
            return;
        }
        int inicio = items.size();
        items.addAll(nuevos);
        notifyItemRangeInserted(inicio, nuevos.size());
    }

    public void marcarComoLeida(@NonNull NotificacionDTO target) {
        Integer idTarget = target.getIdNotificacion();
        for (int i = 0; i < items.size(); i++) {
            NotificacionDTO item = items.get(i);
            if (item == target) {
                item.setLeida(true);
                notifyItemChanged(i);
                return;
            }
            if (idTarget != null && idTarget.equals(item.getIdNotificacion())) {
                item.setLeida(true);
                notifyItemChanged(i);
                return;
            }
        }
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
