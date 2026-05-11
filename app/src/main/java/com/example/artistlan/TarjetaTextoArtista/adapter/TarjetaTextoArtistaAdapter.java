package com.example.artistlan.TarjetaTextoArtista.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.OvershootInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TarjetaTextoArtistaAdapter extends RecyclerView.Adapter<TarjetaTextoArtistaAdapter.ViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClick(TarjetaTextoArtistaItem artistaItem, int position);
    }

    public interface OnVisitarClickListener {
        void onVisitarClick(TarjetaTextoArtistaItem artistaItem, int position);
    }

    private static final long LIKE_BUTTON_COOLDOWN_MS = 500L;
    private OnLikeClickListener onLikeClickListener;
    private OnVisitarClickListener onVisitarClickListener;
    private List<TarjetaTextoArtistaItem> listaArtistas;
    private List<TarjetaTextoArtistaItem> listaOriginal;
    private Context context;
    private int tarjetaExpandida = -1;
    private Integer currentUserId;

    private static final String[] DEFAULT_DESCRIPCIONES = new String[] {
            "Hola, estoy usando Artistlan",
            "En busca del arte",
            "Creando algo nuevo cada día",
            "Compartiendo mi pasión por el arte",
            "Arte en proceso, gracias por visitar"
    };


    public TarjetaTextoArtistaAdapter(List<TarjetaTextoArtistaItem> listaArtistas, Context context) {
        this.listaArtistas = listaArtistas != null ? listaArtistas : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.listaArtistas);
        this.context = context;
    }

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) {
        this.onLikeClickListener = onLikeClickListener;
    }
    public void setOnVisitarClickListener(OnVisitarClickListener onVisitarClickListener) {
        this.onVisitarClickListener = onVisitarClickListener;
    }
    public void setCurrentUserId(Integer currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarjetatextoartista, parent, false);
        return new ViewHolder(view);
    }

    private String safeText(String value, String fallback) {
        if (value == null) return fallback;
        String v = value.trim();
        return v.isEmpty() ? fallback : v;
    }



    private String descripcionDefaultPara(TarjetaTextoArtistaItem artista, int position) {
        Random r = new Random((artista.getNombre() + "|" + position).hashCode());
        return DEFAULT_DESCRIPCIONES[r.nextInt(DEFAULT_DESCRIPCIONES.length)];
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        TarjetaTextoArtistaItem artista = listaArtistas.get(position);

        ThemeApplier.applyTextPrimary(holder.nombre, tm);
        ThemeApplier.applyTextSecondary(holder.categoria, tm);
        ThemeApplier.applyTextSecondary(holder.descripcion, tm);
        ThemeApplier.applyTextSecondary(holder.likes, tm);
        ThemeApplier.applyPrimaryButton(holder.btnVisitar, tm);
        ThemeApplier.applyCardContainer(holder.itemView, tm);

        holder.nombre.setText(artista.getNombre());
        holder.categoria.setText("Categoría: " + safeText(artista.getCategoria(), "Sin categoria"));
        holder.descripcion.setText((artista.getDescripcion() == null || artista.getDescripcion().trim().isEmpty()) ? descripcionDefaultPara(artista, position) : artista.getDescripcion());
        holder.likes.setText(String.valueOf(artista.getLikes()));
        holder.btnLike.setImageResource(artista.isFavorito() ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple);
        holder.btnLike.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
            animateLikeButton(holder.btnLike, artista.isFavorito());

            int adapterPosition = holder.getAdapterPosition();
            if (onLikeClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onLikeClickListener.onLikeClick(listaArtistas.get(adapterPosition), adapterPosition);
            }
        });

        Glide.with(context)
                .load((artista.getFotoPerfil() != null && !artista.getFotoPerfil().isEmpty())
                        ? artista.getFotoPerfil()
                        : R.drawable.fotoperfilprueba)
                .placeholder(R.drawable.fotoperfilprueba)
                .error(R.drawable.fotoperfilprueba)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(holder.imgPerfil);

        List<String> obras = artista.getMiniObras();
        int miniSizePx = (int) (76 * holder.itemView.getResources().getDisplayMetrics().density);
        if (obras.size() > 0) cargarMiniObra(holder.imgMini1, obras.get(0), miniSizePx); else holder.imgMini1.setImageResource(R.drawable.imagencargaobras);
        if (obras.size() > 1) cargarMiniObra(holder.imgMini2, obras.get(1), miniSizePx); else holder.imgMini2.setImageResource(R.drawable.imagencargaobras);
        if (obras.size() > 2) cargarMiniObra(holder.imgMini3, obras.get(2), miniSizePx); else holder.imgMini3.setImageResource(R.drawable.imagencargaobras);

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

        boolean esPerfilPropio = artista.getIdArtista() != null
                && currentUserId != null
                && artista.getIdArtista().equals(currentUserId);
        holder.btnVisitar.setVisibility(esPerfilPropio ? View.GONE : View.VISIBLE);
        holder.btnVisitar.setEnabled(!esPerfilPropio);

        holder.btnVisitar.setOnClickListener(v -> {
            if (esPerfilPropio) {
                return;
            }
            int adapterPosition = holder.getAdapterPosition();
            if (onVisitarClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onVisitarClickListener.onVisitarClick(listaArtistas.get(adapterPosition), adapterPosition);
            } else {
                Toast.makeText(context, "Perfil no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaArtistas.size();
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

    private void animarVista(View v, boolean expandir) {
        if (expandir) {
            if (v.getVisibility() == View.VISIBLE && v.getScaleY() == 1f) return;
            v.setVisibility(View.VISIBLE);
            v.setAlpha(0);
            v.setScaleY(0);
            v.animate().alpha(1f).scaleY(1f).setDuration(120).start();
        } else {
            if (v.getVisibility() == View.GONE) return;
            v.animate().alpha(0f).scaleY(0f).setDuration(160)
                    .withEndAction(() -> v.setVisibility(View.GONE))
                    .start();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, categoria, descripcion, mensaje, likes;
        ImageView imgPerfil, imgMini1, imgMini2, imgMini3;
        ImageButton btnLike;
        View expandedSection;
        Button btnVisitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPerfil = itemView.findViewById(R.id.imgPerfil);
            nombre = itemView.findViewById(R.id.nombre);
            descripcion = itemView.findViewById(R.id.descripcion);
            categoria = itemView.findViewById(R.id.categoria);
            mensaje = itemView.findViewById(R.id.mensaje);
            imgMini1 = itemView.findViewById(R.id.imgMini1);
            imgMini2 = itemView.findViewById(R.id.imgMini2);
            imgMini3 = itemView.findViewById(R.id.imgMini3);
            likes = itemView.findViewById(R.id.likes);
            btnLike = itemView.findViewById(R.id.btnLike);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
        }
    }

    public void filtrar(String texto){
        List<TarjetaTextoArtistaItem> listaFiltrada = new ArrayList<>();

        if(texto == null || texto.isEmpty()) listaFiltrada.addAll(listaOriginal);
        else {
            texto = texto.toLowerCase();
            for(TarjetaTextoArtistaItem artista : listaOriginal){
                if(artista.getNombre() != null && artista.getNombre().toLowerCase().contains(texto)) listaFiltrada.add(artista);
            }
        }

        int oldSize = listaArtistas.size();
        listaArtistas.clear();
        listaArtistas.addAll(listaFiltrada);
        tarjetaExpandida = -1;
        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (!listaFiltrada.isEmpty()) {
            notifyItemRangeInserted(0, listaFiltrada.size());
        }
    }

    public void actualizarLista(List<TarjetaTextoArtistaItem> nuevaLista) {
        int oldSize = listaArtistas.size();
        listaOriginal.clear();
        if (nuevaLista != null) {
            listaOriginal.addAll(nuevaLista);
        }
        listaArtistas.clear();
        if (nuevaLista != null) {
            listaArtistas.addAll(nuevaLista);
        }
        tarjetaExpandida = -1;
        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (nuevaLista != null && !nuevaLista.isEmpty()) {
            notifyItemRangeInserted(0, nuevaLista.size());
        }
    }

    public void agregarItems(List<TarjetaTextoArtistaItem> nuevosItems) {
        if (nuevosItems == null || nuevosItems.isEmpty()) {
            return;
        }
        int start = listaArtistas.size();
        listaArtistas.addAll(nuevosItems);
        listaOriginal.addAll(nuevosItems);
        notifyItemRangeInserted(start, nuevosItems.size());
    }

    public void removeItemAt(int position) {
        if (position < 0 || position >= listaArtistas.size()) return;
        TarjetaTextoArtistaItem item = listaArtistas.remove(position);
        listaOriginal.remove(item);
        notifyItemRemoved(position);
    }

    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < listaArtistas.size()) notifyItemChanged(position);
    }

    private void cargarMiniObra(@NonNull ImageView target, String url, int miniSizePx) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.imagencargaobras)
                .error(R.drawable.imagencargaobras)
                .thumbnail(0.25f)
                .override(miniSizePx, miniSizePx)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(target);
    }
}
