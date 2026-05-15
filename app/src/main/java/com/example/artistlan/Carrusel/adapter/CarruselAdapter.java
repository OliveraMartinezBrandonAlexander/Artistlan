package com.example.artistlan.Carrusel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Carrusel.model.ObraCarruselItem;
import com.example.artistlan.R;

import java.util.List;

public class CarruselAdapter extends RecyclerView.Adapter<CarruselAdapter.CarruselViewHolder> {
    private static final String PAYLOAD_LIKE_STATE = "payload_like_state";

    public interface OnCarruselActionListener {
        void onOpen(ObraCarruselItem item, int position);
        void onLike(ObraCarruselItem item, int position);
        void onAuthor(ObraCarruselItem item, int position);
    }

    private final List<ObraCarruselItem> lista;
    private final Context context;
    private OnCarruselActionListener onCarruselActionListener;

    public CarruselAdapter(List<ObraCarruselItem> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }
    public void setOnCarruselActionListener(OnCarruselActionListener listener) {
        this.onCarruselActionListener = listener;
    }

    @NonNull
    @Override
    public CarruselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrusel, parent, false);
        return new CarruselViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CarruselViewHolder holder, int position) {
        ObraCarruselItem item = lista.get(position);

        // Imagen de la obra
        if (item.getImagenUrl() != null && !item.getImagenUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImagenUrl())
                    .placeholder(item.getImagen())
                    .error(item.getImagen())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(item.getImagen());
        }

        // Foto de perfil del autor
        if (item.getAutorFotoUrl() != null && !item.getAutorFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getAutorFotoUrl())
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgAutor);
        } else {
            holder.imgAutor.setImageResource(R.drawable.fotoperfilprueba);
        }

        holder.tvTitulo.setText(item.getTitulo());
        holder.tvDescripcion.setText(item.getDescripcion());
        holder.tvAutor.setText(item.getAutor());
        bindLikeUi(holder, item, true);
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (onCarruselActionListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onCarruselActionListener.onOpen(lista.get(adapterPosition), adapterPosition);
            }
        });
        holder.imgAutor.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (onCarruselActionListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onCarruselActionListener.onAuthor(lista.get(adapterPosition), adapterPosition);
            }
        });
        holder.btnLikeCarrusel.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (onCarruselActionListener == null || adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }
            ObraCarruselItem currentItem = lista.get(adapterPosition);
            boolean likedAntesDelClick = currentItem.isUserLiked();
            int likesAntesDelClick = currentItem.getLikesCount();
            onCarruselActionListener.onLike(currentItem, adapterPosition);
            if (likedAntesDelClick != currentItem.isUserLiked()
                    || likesAntesDelClick != currentItem.getLikesCount()) {
                animateLikeButton(holder.btnLikeCarrusel, likedAntesDelClick);
            }
        });
        holder.tvAbrirCarrusel.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (onCarruselActionListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onCarruselActionListener.onOpen(lista.get(adapterPosition), adapterPosition);
            } else {
                Toast.makeText(context, item.getTitulo(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull CarruselViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_LIKE_STATE)) {
            if (position < 0 || position >= lista.size()) {
                return;
            }
            bindLikeUi(holder, lista.get(position), false);
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < lista.size()) {
            notifyItemChanged(position);
        }
    }

    public void notifyLikeChangedPartial(int position) {
        if (position >= 0 && position < lista.size()) {
            notifyItemChanged(position, PAYLOAD_LIKE_STATE);
        }
    }

    private void bindLikeUi(@NonNull CarruselViewHolder holder, @NonNull ObraCarruselItem item, boolean forceVisualState) {
        if (forceVisualState) {
            holder.btnLikeCarrusel.animate().cancel();
            holder.btnLikeCarrusel.cancelAnimation();
            holder.btnLikeCarrusel.setScaleX(1f);
            holder.btnLikeCarrusel.setScaleY(1f);
            holder.btnLikeCarrusel.setAlpha(1f);
            holder.btnLikeCarrusel.setSpeed(1f);
            holder.btnLikeCarrusel.setProgress(item.isUserLiked() ? 1f : 0f);
            return;
        }

        if (!holder.btnLikeCarrusel.isAnimating()) {
            holder.btnLikeCarrusel.setSpeed(1f);
            holder.btnLikeCarrusel.setProgress(item.isUserLiked() ? 1f : 0f);
        }
    }

    private void animateLikeButton(@NonNull com.airbnb.lottie.LottieAnimationView btnLike, boolean wasLiked) {
        btnLike.animate().cancel();
        btnLike.cancelAnimation();

        btnLike.setScaleX(0.86f);
        btnLike.setScaleY(0.86f);
        btnLike.setAlpha(0.85f);
        btnLike.setMinAndMaxProgress(0f, 1f);
        btnLike.setSpeed(wasLiked ? -1f : 1f);
        btnLike.setProgress(wasLiked ? 1f : 0f);
        btnLike.playAnimation();

        btnLike.animate()
                .scaleX(1.18f)
                .scaleY(1.18f)
                .alpha(1f)
                .setDuration(120)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    btnLike.setSpeed(1f);
                    btnLike.setProgress(wasLiked ? 0f : 1f);
                    btnLike.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(140)
                            .start();
                })
                .start();
    }

    public static class CarruselViewHolder extends RecyclerView.ViewHolder {

        ImageView imgObra, imgAutor;
        TextView tvTitulo, tvDescripcion, tvAutor, tvAbrirCarrusel;
        com.airbnb.lottie.LottieAnimationView btnLikeCarrusel;

        public CarruselViewHolder(@NonNull View itemView) {
            super(itemView);
            imgObra       = itemView.findViewById(R.id.imgObra);
            imgAutor      = itemView.findViewById(R.id.imgAutor);
            tvTitulo      = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvAutor       = itemView.findViewById(R.id.tvAutor);
            tvAbrirCarrusel = itemView.findViewById(R.id.tvAbrirCarrusel);
            btnLikeCarrusel = itemView.findViewById(R.id.btnLikeCarrusel);
        }
    }
}
