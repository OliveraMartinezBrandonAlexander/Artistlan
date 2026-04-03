package com.example.artistlan.TarjetaTextoObra.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.List;

public class TarjetaTextoObraAdapter extends RecyclerView.Adapter<TarjetaTextoObraAdapter.ViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClick(TarjetaTextoObraItem obraItem, int position);
    }

    private static final long LIKE_BUTTON_COOLDOWN_MS = 500L;
    private OnLikeClickListener onLikeClickListener;
    private final List<TarjetaTextoObraItem> listaObras;
    private final List<TarjetaTextoObraItem> listaOriginal;
    private final Context context;
    private int tarjetaExpandida = -1;

    public TarjetaTextoObraAdapter(List<TarjetaTextoObraItem> listaObras, Context context) {
        this.listaObras = listaObras;
        this.listaOriginal = new ArrayList<>(listaObras);
        this.context = context;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.onLikeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoobra, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TarjetaTextoObraItem obra = listaObras.get(position);

        holder.titulo.setText(obra.getTitulo());
        holder.autor.setText(obra.getNombreAutor());
        holder.descripcion.setText(obra.getDescripcion());
        holder.estado.setText("Estado: " + obra.getEstado());
        holder.tecnica.setText("Técnica: " + obra.getTecnicas());
        holder.medidas.setText((obra.getMedidas() != null && !obra.getMedidas().isEmpty()) ? "Medidas: " + obra.getMedidas() + " cm" : "Medidas: N/A");
        holder.precio.setText(obra.getPrecio() != null ? "Precio: $ " + String.format("%,.2f", obra.getPrecio()) : "Precio: N/A");
        holder.categoria.setText("Categoría: " + obra.getNombreCategoria());
        holder.likes.setText(String.valueOf(obra.getLikes()));

        holder.btnLike.setImageResource(obra.isUserLiked() ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple);
        holder.btnLike.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
            animateLikeButton(holder.btnLike, obra.isUserLiked());

            int adapterPosition = holder.getAdapterPosition();
            if (onLikeClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onLikeClickListener.onLikeClick(listaObras.get(adapterPosition), adapterPosition);
            }
        });

        String fotoPerfil = obra.getFotoPerfilAutor();
        Glide.with(context).load((fotoPerfil != null && !fotoPerfil.isEmpty()) ? fotoPerfil : R.drawable.fotoperfilprueba).placeholder(R.drawable.fotoperfilprueba).circleCrop().into(holder.imgAutor);

        String imagenObra = obra.getImagen1();
        if (imagenObra != null && !imagenObra.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(imagenObra).placeholder(R.drawable.imagencargaobras).error(R.drawable.imagencargaobras).into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(R.drawable.imagencargaobras);
        }

        boolean expandido = (position == tarjetaExpandida);
        animarVista(holder.expandedSection, expandido);
        obra.setExpandido(expandido);

        holder.itemView.setOnClickListener(v -> {
            int previousExpanded = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (previousExpanded == currentPosition) tarjetaExpandida = -1;
            else {
                tarjetaExpandida = currentPosition;
                if (previousExpanded != -1) notifyItemChanged(previousExpanded);
            }
            notifyItemChanged(currentPosition);
        });

        holder.btnVisitar.setOnClickListener(v -> Toast.makeText(context, "Proximamente...", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
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

    private void animarVista(View view, boolean expandir) {
        if (expandir) {
            view.setVisibility(View.VISIBLE);
            view.setScaleY(0f);
            view.setAlpha(0f);

            view.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(160)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, descripcion, estado, tecnica, medidas, precio, categoria, likes, autor;
        ImageView imgAutor, imgObra;
        ImageButton btnLike;
        View expandedSection;
        Button btnVisitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAutor = itemView.findViewById(R.id.imgAutor);
            imgObra = itemView.findViewById(R.id.imgObra);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            estado = itemView.findViewById(R.id.estado);
            tecnica = itemView.findViewById(R.id.tecnica);
            medidas = itemView.findViewById(R.id.medidas);
            precio = itemView.findViewById(R.id.precio);
            autor = itemView.findViewById(R.id.autor);
            categoria = itemView.findViewById(R.id.categoria);
            likes = itemView.findViewById(R.id.likes);
            btnLike = itemView.findViewById(R.id.btnLike);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
        }
    }

    public void filtrar(String texto) {
        List<TarjetaTextoObraItem> listaFiltrada = new ArrayList<>();

        if (texto == null || texto.isEmpty()) listaFiltrada.addAll(listaOriginal);
        else {
            texto = texto.toLowerCase();
            for (TarjetaTextoObraItem obra : listaOriginal) {
                if (obra.getTitulo() != null && obra.getTitulo().toLowerCase().contains(texto)) listaFiltrada.add(obra);
            }
        }
        int oldSize = listaObras.size();
        listaObras.clear();
        listaObras.addAll(listaFiltrada);
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!listaFiltrada.isEmpty()) notifyItemRangeInserted(0, listaFiltrada.size());
    }

    public void actualizarLista(List<TarjetaTextoObraItem> nuevaLista) {
        int oldSize = listaObras.size();
        listaOriginal.clear();
        listaOriginal.addAll(nuevaLista);
        listaObras.clear();
        listaObras.addAll(nuevaLista);
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!nuevaLista.isEmpty()) notifyItemRangeInserted(0, nuevaLista.size());
    }
    public void removeItemAt(int position) {
        if (position < 0 || position >= listaObras.size()) return;
        TarjetaTextoObraItem item = listaObras.remove(position);
        listaOriginal.remove(item);
        notifyItemRemoved(position);
    }
    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < listaObras.size()) notifyItemChanged(position);
    }
}