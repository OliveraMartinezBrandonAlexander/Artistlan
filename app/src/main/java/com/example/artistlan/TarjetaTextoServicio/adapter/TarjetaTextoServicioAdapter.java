package com.example.artistlan.TarjetaTextoServicio.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TarjetaTextoServicioAdapter extends RecyclerView.Adapter<TarjetaTextoServicioAdapter.ViewHolder> {

    public interface OnLikeClickListener { void onLikeClick(TarjetaTextoServicioItem servicioItem, int position); }
    public interface OnEditClickListener { void onEditClick(TarjetaTextoServicioItem servicioItem, int position); }
    public interface OnDeleteClickListener { void onDeleteClick(TarjetaTextoServicioItem servicioItem, int position); }

    private static final long LIKE_BUTTON_COOLDOWN_MS = 500L;
    private OnLikeClickListener onLikeClickListener;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private final List<TarjetaTextoServicioItem> listaServicios;
    private final List<TarjetaTextoServicioItem> listaOriginal;
    private final Context context;
    private int tarjetaExpandida = -1;
    private Integer currentUserId;

    public TarjetaTextoServicioAdapter(List<TarjetaTextoServicioItem> listaServicios, Context context) {
        this.listaServicios = listaServicios;
        this.listaOriginal = new ArrayList<>(listaServicios);
        this.context = context;
    }

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) { this.onLikeClickListener = onLikeClickListener; }
    public void setOnEditClickListener(OnEditClickListener onEditClickListener) { this.onEditClickListener = onEditClickListener; notifyDataSetChanged(); }
    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) { this.onDeleteClickListener = onDeleteClickListener; notifyDataSetChanged(); }
    public void setCurrentUserId(Integer currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoservicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        TarjetaTextoServicioItem servicio = listaServicios.get(position);
        ThemeApplier.applyTextPrimary(holder.titulo, tm);
        ThemeApplier.applyTextSecondary(holder.autor, tm);
        ThemeApplier.applyTextSecondary(holder.descripcion, tm);
        ThemeApplier.applyTextSecondary(holder.contacto, tm);
        ThemeApplier.applyTextSecondary(holder.tipoContacto, tm);
        ThemeApplier.applyTextSecondary(holder.tecnicas, tm);
        ThemeApplier.applyTextPrimary(holder.precioRango, tm);
        ThemeApplier.applyTextSecondary(holder.categoria, tm);
        ThemeApplier.applyPrimaryButton(holder.btnContactar, tm);
        ThemeApplier.applyCardContainer(holder.itemView, tm);
        
        holder.autor.setText(safe(servicio.getAutor(), "Autor"));
        holder.titulo.setText(safe(servicio.getTitulo(), "Servicio"));
        holder.descripcion.setText("Descripción: " + safe(servicio.getDescripcion(), "Sin descripción"));
        holder.contacto.setText("Contacto: " + safe(servicio.getContacto(), "No disponible"));
        holder.tipoContacto.setText("Tipo de contacto: " + safe(servicio.getTipoContacto(), "N/A"));
        holder.tecnicas.setText("Técnicas: " + safe(servicio.getTecnicas(), "No especificadas"));
        holder.precioRango.setText(formatearPrecioRango(servicio.getPrecioMin(), servicio.getPrecioMax()));
        holder.categoria.setText("Categoría: " + safe(servicio.getCategoria(), "Sin categoría"));
        holder.likes.setText(String.valueOf(servicio.getLikes()));
        holder.btnLike.setImageResource(servicio.isFavorito() ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple);
        holder.btnLike.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
            animateLikeButton(holder.btnLike, servicio.isFavorito());
            int adapterPosition = holder.getAdapterPosition();
            if (onLikeClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onLikeClickListener.onLikeClick(listaServicios.get(adapterPosition), adapterPosition);
            }
        });

        Glide.with(holder.itemView.getContext())
                .load((servicio.getFotoPerfilAutor() != null && servicio.getFotoPerfilAutor().startsWith("http")) ? servicio.getFotoPerfilAutor() : R.drawable.fotoperfilprueba)
                .placeholder(R.drawable.fotoperfilprueba)
                .circleCrop()
                .into(holder.imgAutor);

        boolean expandido = (tarjetaExpandida == position);
        animarVista(holder.expandedSection, expandido);
        configurarMenuOpciones(holder);

        boolean esServicioPropio = servicio.getIdUsuario() != null
                && currentUserId != null
                && servicio.getIdUsuario().equals(currentUserId);
        holder.btnContactar.setVisibility(esServicioPropio ? View.GONE : View.VISIBLE);
        holder.btnContactar.setEnabled(!esServicioPropio && !TextUtils.isEmpty(servicio.getContacto()));
        holder.btnContactar.setOnClickListener(v -> contactar(servicio));

        holder.itemView.setOnClickListener(v -> {
            int previous = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) return;
            if (previous == currentPosition) tarjetaExpandida = -1;
            else {
                tarjetaExpandida = currentPosition;
                if (previous != -1) notifyItemChanged(previous);
            }
            notifyItemChanged(currentPosition);
        });
    }

    private String formatearPrecioRango(Double min, Double max) {
        if (min == null && max == null) return "Precio: A convenir";
        if (min != null && max != null) return String.format(Locale.getDefault(), "Precio: $%,.2f - $%,.2f", min, max);
        if (min != null) return String.format(Locale.getDefault(), "Precio desde: $%,.2f", min);
        return String.format(Locale.getDefault(), "Precio hasta: $%,.2f", max);
    }

    private void contactar(TarjetaTextoServicioItem servicio) {
        String tipo = safe(servicio.getTipoContacto(), "OTRO").toUpperCase(Locale.ROOT);
        String contacto = safe(servicio.getContacto(), "").trim();
        if (contacto.isEmpty()) {
            Toast.makeText(context, "Este servicio no tiene contacto disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent;
            switch (tipo) {
                case "EMAIL":
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + contacto));
                    break;
                case "WHATSAPP":
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + contacto.replaceAll("[^0-9]", "")));
                    break;
                case "TELEFONO":
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contacto));
                    break;
                case "INSTAGRAM":
                    String user = contacto.startsWith("@") ? contacto.substring(1) : contacto;
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + user));
                    break;
                default:
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_TEXT, "Contacto de servicio: " + contacto);
                    break;
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir la app de contacto", Toast.LENGTH_SHORT).show();
        }
    }
    @Override public int getItemCount() { return listaServicios != null ? listaServicios.size() : 0; }

    private String safe(String v, String def) { return (v == null || v.trim().isEmpty()) ? def : v; }


    private void configurarMenuOpciones(ViewHolder holder) {
        boolean mostrarMenu = onEditClickListener != null || onDeleteClickListener != null;
        holder.btnMoreOptions.setVisibility(mostrarMenu ? View.VISIBLE : View.GONE);
        if (!mostrarMenu) { holder.btnMoreOptions.setOnClickListener(null); return; }
        holder.btnMoreOptions.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            PopupMenu popupMenu = new PopupMenu(context, holder.btnMoreOptions);
            if (onEditClickListener != null) popupMenu.getMenu().add(0, 1, 0, "Modificar");
            if (onDeleteClickListener != null) popupMenu.getMenu().add(0, 2, 1, "Eliminar");
            popupMenu.setOnMenuItemClickListener(item -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return false;
                }

                TarjetaTextoServicioItem servicio = listaServicios.get(currentPosition);
                if (item.getItemId() == 1 && onEditClickListener != null) {
                    onEditClickListener.onEditClick(servicio, currentPosition);
                    return true;
                }
                if (item.getItemId() == 2 && onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(servicio, currentPosition);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void animateLikeButton(ImageButton btnLike, boolean wasLiked) {
        btnLike.animate().cancel();
        btnLike.setScaleX(0.82f);
        btnLike.setScaleY(0.82f);
        btnLike.setAlpha(0.75f);

        btnLike.animate()
                .scaleX(1.24f)
                .scaleY(1.24f)
                .alpha(1f)
                .setDuration(140)
                .withEndAction(() -> {
                    btnLike.setImageResource(wasLiked ? R.drawable.ic_heart_purple : R.drawable.ic_heart_red);
                    btnLike.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(220)
                            .setInterpolator(new OvershootInterpolator(2.8f))
                            .start();
                })
                .start();
    }

    public void filtrar(String texto) {
        List<TarjetaTextoServicioItem> listaFiltrada = new ArrayList<>();
        if (texto == null || texto.isEmpty()) listaFiltrada.addAll(listaOriginal);
        else {
            texto = texto.toLowerCase();
            for (TarjetaTextoServicioItem servicio : listaOriginal){
                if (servicio.getTitulo() != null && servicio.getTitulo().toLowerCase().contains(texto)) listaFiltrada.add(servicio);
            }
        }
        int oldSize = listaServicios.size();
        listaServicios.clear();
        listaServicios.addAll(listaFiltrada);
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!listaFiltrada.isEmpty()) notifyItemRangeInserted(0, listaFiltrada.size());
    }

    public void actualizarLista(List<TarjetaTextoServicioItem> nuevaLista) {
        int oldSize = listaServicios.size();
        listaOriginal.clear();
        listaOriginal.addAll(nuevaLista);
        listaServicios.clear();
        listaServicios.addAll(nuevaLista);
        tarjetaExpandida = -1;
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!nuevaLista.isEmpty()) notifyItemRangeInserted(0, nuevaLista.size());
    }

    public void removeItemAt(int position) {
        if (position < 0 || position >= listaServicios.size()) return;
        TarjetaTextoServicioItem item = listaServicios.remove(position);
        listaOriginal.remove(item);
        if (tarjetaExpandida == position) {
            tarjetaExpandida = -1;
        } else if (tarjetaExpandida > position) {
            tarjetaExpandida--;
        }
        notifyItemRemoved(position);
    }

    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < listaServicios.size()) notifyItemChanged(position);
    }

    private void animarVista(View view, boolean expandir) {
        if (expandir) {
            if (view.getVisibility() == View.VISIBLE) return;
            view.setVisibility(View.VISIBLE);
            view.setScaleY(0f);
            view.setAlpha(0f);
            view.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(140)
                    .start();
        } else {
            if (view.getVisibility() == View.GONE) return;
            view.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(160)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, descripcion, contacto, tipoContacto, tecnicas, autor, categoria, precioRango, likes;
        ImageView imgAutor;
        ImageButton btnLike, btnMoreOptions;
        View expandedSection;
        Button btnContactar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            autor = itemView.findViewById(R.id.autor);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            contacto = itemView.findViewById(R.id.contacto);
            tipoContacto = itemView.findViewById(R.id.tipoContacto);
            tecnicas = itemView.findViewById(R.id.tecnicas);
            categoria = itemView.findViewById(R.id.categoria);
            precioRango = itemView.findViewById(R.id.precioRango);
            imgAutor = itemView.findViewById(R.id.imgAutor);
            likes = itemView.findViewById(R.id.likes);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnContactar = itemView.findViewById(R.id.btnContactar);
        }
    }
}
