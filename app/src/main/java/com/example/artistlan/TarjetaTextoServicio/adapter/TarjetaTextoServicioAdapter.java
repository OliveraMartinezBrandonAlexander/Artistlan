package com.example.artistlan.TarjetaTextoServicio.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.ArrayList;
import java.util.List;

public class TarjetaTextoServicioAdapter extends RecyclerView.Adapter<TarjetaTextoServicioAdapter.ViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClick(TarjetaTextoServicioItem servicioItem, int position);
    }

    private static final long LIKE_BUTTON_COOLDOWN_MS = 500L;
    private OnLikeClickListener onLikeClickListener;
    private final List<TarjetaTextoServicioItem> listaServicios;
    private final List<TarjetaTextoServicioItem> listaOriginal;
    private final Context context;
    private int tarjetaExpandida = -1;

    public TarjetaTextoServicioAdapter(List<TarjetaTextoServicioItem> listaServicios, Context context) {
        this.listaServicios = listaServicios;
        this.listaOriginal = new ArrayList<>(listaServicios);
        this.context = context;
    }

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) {
        this.onLikeClickListener = onLikeClickListener;
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
        TarjetaTextoServicioItem servicio = listaServicios.get(position);

        holder.autor.setText(servicio.getAutor());
        holder.titulo.setText(servicio.getTitulo());
        holder.descripcion.setText("Descripción: " + servicio.getDescripcion());
        holder.contacto.setText("Contacto: " + servicio.getContacto());
        holder.tecnicas.setText("Técnicas: " + servicio.getTecnicas());
        holder.categoria.setText("Categoría: " + servicio.getCategoria());
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

        holder.itemView.setOnClickListener(v -> {
            int previous = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (previous == currentPosition) tarjetaExpandida = -1;
            else {
                tarjetaExpandida = currentPosition;
                if (previous != -1) notifyItemChanged(previous);
            }
            notifyItemChanged(currentPosition);
        });

        holder.btnVisitar.setOnClickListener(v -> {
            Toast.makeText(context, "Proximamente...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaServicios != null ? listaServicios.size() : 0;
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
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!nuevaLista.isEmpty()) notifyItemRangeInserted(0, nuevaLista.size());
    }

    public void removeItemAt(int position) {
        if (position < 0 || position >= listaServicios.size()) return;
        TarjetaTextoServicioItem item = listaServicios.remove(position);
        listaOriginal.remove(item);
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

        TextView titulo, descripcion, contacto, tecnicas, autor, categoria, likes;
        ImageView imgAutor;
        ImageButton btnLike;
        View expandedSection;
        Button btnVisitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            autor = itemView.findViewById(R.id.autor);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            contacto = itemView.findViewById(R.id.contacto);
            tecnicas = itemView.findViewById(R.id.tecnicas);
            categoria = itemView.findViewById(R.id.categoria);
            likes = itemView.findViewById(R.id.likes);
            btnLike = itemView.findViewById(R.id.btnLike);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
            imgAutor = itemView.findViewById(R.id.imgAutor);
        }
    }
}
