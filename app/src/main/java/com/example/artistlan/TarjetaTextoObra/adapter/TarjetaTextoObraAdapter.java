package com.example.artistlan.TarjetaTextoObra.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.List;

public class TarjetaTextoObraAdapter extends RecyclerView.Adapter<TarjetaTextoObraAdapter.ViewHolder> {

    private List<TarjetaTextoObraItem> listaObras;
    private final Context context;
    private int tarjetaExpandida = -1;

    public TarjetaTextoObraAdapter(List<TarjetaTextoObraItem> listaObras, Context context) {
        this.listaObras = listaObras;
        this.context = context;
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
        holder.medidas.setText("Medidas: " + obra.getMedidas() + " cm");
        holder.precio.setText("Precio: $ " + String.format("%,.2f", obra.getPrecio()));
        holder.likes.setText(String.valueOf(obra.getLikes()));

        // categoría ahora está en expandido (mismo id, no rompe nada)
        holder.categoria.setText("Categoría: " + obra.getNombreCategoria());

        // Foto perfil autor
        String fotoPerfil = obra.getFotoPerfilAutor();
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(context)
                    .load(fotoPerfil)
                    .placeholder(R.drawable.fotoperfilprueba)
                    .circleCrop()
                    .into(holder.imgAutor);
        } else {
            Glide.with(context)
                    .load(R.drawable.fotoperfilprueba)
                    .circleCrop()
                    .into(holder.imgAutor);
        }

        // Imagen principal de la obra
        String imagenObra = obra.getImagen1();
        if (imagenObra != null && !imagenObra.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagenObra)
                    .placeholder(R.drawable.imagencargaobras)
                    .error(R.drawable.imagencargaobras)
                    .into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(R.drawable.imagencargaobras);
        }

        // Estado del like (icono)
        if (obra.isUserLiked()) {
            holder.btnLike.setImageResource(R.drawable.ic_heart_red);
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart_purple);
        }

        holder.btnLike.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            TarjetaTextoObraItem currentObra = listaObras.get(currentPosition);

            boolean currentlyLiked = currentObra.isUserLiked();
            int newLikesCount = currentObra.getLikes();

            if (currentlyLiked) {
                currentObra.setUserLiked(false);
                newLikesCount--;
                holder.btnLike.setImageResource(R.drawable.ic_heart_purple);
            } else {
                currentObra.setUserLiked(true);
                newLikesCount++;
                holder.btnLike.setImageResource(R.drawable.ic_heart_red);
            }

            currentObra.setLikes(newLikesCount);
            holder.likes.setText(String.valueOf(newLikesCount));

            animarLike(holder.btnLike);

            if (listener != null) {
                listener.onLikeClick(currentObra.getIdObra(), currentObra.isUserLiked());
            }

            notifyItemChanged(currentPosition);
        });

        // Expand / collapse
        boolean expandido = (position == tarjetaExpandida);
        animarVista(holder.expandedSection, expandido);
        obra.setExpandido(expandido);

        holder.itemView.setOnClickListener(v -> {
            int previousExpanded = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (previousExpanded == currentPosition) {
                tarjetaExpandida = -1;
            } else {
                tarjetaExpandida = currentPosition;
                if (previousExpanded != -1) notifyItemChanged(previousExpanded);
            }
            notifyItemChanged(currentPosition);
        });

        // Botón visitar (por ahora stub)
        holder.btnVisitar.setOnClickListener(v -> {
            if (visitarListener != null) {
                visitarListener.onVisitarClick(obra.getIdObra());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
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

    private void animarLike(ImageButton btn) {
        btn.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(80)
                .withEndAction(() ->
                        btn.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                ).start();
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

    public void actualizarLista(List<TarjetaTextoObraItem> nuevaLista) {
        this.listaObras = nuevaLista;
        notifyDataSetChanged();
    }

    // Like listener existente
    public interface OnLikeClickListener {
        void onLikeClick(int idObra, boolean userLiked);
    }

    private OnLikeClickListener listener;

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.listener = listener;
    }

    // Listener opcional para Visitar
    public interface OnVisitarClickListener {
        void onVisitarClick(int idObra);
    }

    private OnVisitarClickListener visitarListener;

    public void setOnVisitarClickListener(OnVisitarClickListener visitarListener) {
        this.visitarListener = visitarListener;
    }
}
