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
import com.example.artistlan.Conector.model.SolicitudDTO;
import com.example.artistlan.Fragments.MensajeUiUtils;
import com.example.artistlan.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder> {

    public enum ModoLista {
        RECIBIDAS,
        ENVIADAS
    }

    public interface Listener {
        void onDetalle(@NonNull SolicitudDTO item);

        void onAceptar(@NonNull SolicitudDTO item);

        void onRechazar(@NonNull SolicitudDTO item);

        void onCancelar(@NonNull SolicitudDTO item);
    }

    private final List<SolicitudDTO> items = new ArrayList<>();
    private final Listener listener;
    private ModoLista modoLista = ModoLista.RECIBIDAS;

    public SolicitudesAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_centro, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudDTO item = items.get(position);
        boolean esRecibida = modoLista == ModoLista.RECIBIDAS;

        holder.tvTitulo.setText(item.getTituloSeguro());
        holder.tvMensaje.setText(item.getMensajeSeguro());
        holder.tvFecha.setText(MensajeUiUtils.formatearFechaCorta(item.getFecha()));

        String actor = item.getNombreActorContextual(esRecibida);
        holder.tvOrigenNombre.setText((esRecibida ? "Comprador: " : "Vendedor: ") + actor);

        holder.tvEstado.setText(item.getEstadoVisual());
        holder.tvEstado.setBackgroundResource(obtenerFondoEstado(item));

        holder.unreadDot.setVisibility(View.GONE);
        holder.btnMarcarLeida.setVisibility(View.GONE);
        holder.btnEliminar.setVisibility(View.GONE);

        String referencia = MensajeUiUtils.etiquetaReferencia(item.getReferenciaTipo(), item.getReferenciaId());
        if (referencia.isEmpty()) {
            holder.tvReferencia.setVisibility(View.GONE);
        } else {
            holder.tvReferencia.setVisibility(View.VISIBLE);
            holder.tvReferencia.setText(referencia);
        }

        int surface = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_surface);
        int surfacePending = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_surface_soft);
        int strokeSoft = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_menu_stroke_soft);
        int strokePending = ContextCompat.getColor(holder.itemView.getContext(), R.color.artistlan_secondary);

        holder.cardRoot.setCardBackgroundColor(item.isPendiente() ? surfacePending : surface);
        holder.cardRoot.setStrokeColor(item.isPendiente() ? strokePending : strokeSoft);
        holder.cardRoot.setStrokeWidth(item.isPendiente() ? 2 : 1);

        boolean mostrarAceptar = modoLista == ModoLista.RECIBIDAS && item.puedeSerResueltaPorVendedor();
        boolean mostrarRechazar = modoLista == ModoLista.RECIBIDAS && item.puedeSerResueltaPorVendedor();
        boolean mostrarCancelar = modoLista == ModoLista.ENVIADAS && item.puedeSerCanceladaPorComprador();

        holder.btnAceptar.setVisibility(mostrarAceptar ? View.VISIBLE : View.GONE);
        holder.btnRechazar.setVisibility((mostrarRechazar || mostrarCancelar) ? View.VISIBLE : View.GONE);
        holder.btnRechazar.setText(mostrarCancelar ? "Cancelar" : "Rechazar");

        Glide.with(holder.itemView)
                .load(item.getFotoActorContextual(esRecibida))
                .placeholder(R.drawable.cuenta)
                .error(R.drawable.cuenta)
                .circleCrop()
                .into(holder.ivOrigen);

        holder.cardRoot.setOnClickListener(v -> listener.onDetalle(item));
        holder.btnDetalle.setOnClickListener(v -> listener.onDetalle(item));
        holder.btnAceptar.setOnClickListener(v -> listener.onAceptar(item));
        holder.btnRechazar.setOnClickListener(v -> {
            if (modoLista == ModoLista.ENVIADAS) {
                listener.onCancelar(item);
            } else {
                listener.onRechazar(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<SolicitudDTO> nuevos) {
        items.clear();
        if (nuevos != null) {
            items.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    public void setModoLista(ModoLista modoLista) {
        this.modoLista = modoLista == null ? ModoLista.RECIBIDAS : modoLista;
        notifyDataSetChanged();
    }

    public void removeItem(@NonNull SolicitudDTO target) {
        int index = items.indexOf(target);
        if (index >= 0) {
            items.remove(index);
            notifyItemRemoved(index);
            return;
        }
        notifyDataSetChanged();
    }

    public void marcarTodasLeidas() {
        notifyDataSetChanged();
    }

    public List<SolicitudDTO> getItems() {
        return items;
    }

    private int obtenerFondoEstado(SolicitudDTO item) {
        if (item.isPendiente()) return R.drawable.bg_chip_solicitud_pendiente;
        if (item.isAceptada()) return R.drawable.bg_chip_solicitud_aceptada;
        if (item.isRechazada()) return R.drawable.bg_chip_solicitud_rechazada;
        if (item.isCancelada()) return R.drawable.bg_chip_solicitud_cancelada;
        if (item.isExpirada()) return R.drawable.bg_chip_solicitud_expirada;
        if (item.isPagada()) return R.drawable.bg_chip_solicitud_pagada;
        return R.drawable.bg_chip_solicitud_atendida;
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardRoot;
        private final ImageView ivOrigen;
        private final View unreadDot;
        private final TextView tvEstado;
        private final TextView tvTitulo;
        private final TextView tvMensaje;
        private final TextView tvOrigenNombre;
        private final TextView tvFecha;
        private final TextView tvReferencia;
        private final TextView btnDetalle;
        private final TextView btnMarcarLeida;
        private final TextView btnAceptar;
        private final TextView btnRechazar;
        private final ImageButton btnEliminar;

        SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardSolicitudRoot);
            ivOrigen = itemView.findViewById(R.id.ivSolicitudOrigen);
            unreadDot = itemView.findViewById(R.id.viewSolicitudNoLeida);
            tvEstado = itemView.findViewById(R.id.tvSolicitudEstado);
            tvTitulo = itemView.findViewById(R.id.tvSolicitudTitulo);
            tvMensaje = itemView.findViewById(R.id.tvSolicitudBody);
            tvOrigenNombre = itemView.findViewById(R.id.tvSolicitudOrigenNombre);
            tvFecha = itemView.findViewById(R.id.tvSolicitudFecha);
            tvReferencia = itemView.findViewById(R.id.tvSolicitudReferencia);
            btnDetalle = itemView.findViewById(R.id.btnSolicitudDetalle);
            btnMarcarLeida = itemView.findViewById(R.id.btnSolicitudMarcarLeida);
            btnAceptar = itemView.findViewById(R.id.btnSolicitudAceptar);
            btnRechazar = itemView.findViewById(R.id.btnSolicitudRechazar);
            btnEliminar = itemView.findViewById(R.id.btnSolicitudEliminar);
        }
    }
}
