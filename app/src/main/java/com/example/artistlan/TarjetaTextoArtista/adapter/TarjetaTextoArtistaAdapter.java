package com.example.artistlan.TarjetaTextoArtista.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class TarjetaTextoArtistaAdapter extends RecyclerView.Adapter<TarjetaTextoArtistaAdapter.ViewHolder> {

    private List<TarjetaTextoArtistaItem> listaArtistas;
    private Context context;
    private int tarjetaExpandida = -1;

    private static final String[] DEFAULT_DESCRIPCIONES = new String[] {
            "Hola, estoy usando Artistlan",
            "En busca del arte",
            "Creando algo nuevo cada día",
            "Compartiendo mi pasión por el arte",
            "Arte en proceso, gracias por visitar"
    };


    public TarjetaTextoArtistaAdapter(List<TarjetaTextoArtistaItem> listaArtistas, Context context) {
        this.listaArtistas = listaArtistas;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoartista, parent, false);
        return new ViewHolder(view);
    }

    private String safeText(String value, String fallback) {
        if (value == null) return fallback;
        String v = value.trim();
        return v.isEmpty() ? fallback : v;
    }

    private String keyFor(TarjetaTextoArtistaItem a, int position) {
        String seed =
                safeText(a.getNombre(), "") + "|" +
                        safeText(a.getCategoria(), "") + "|" +
                        safeText(a.getFotoPerfil(), "") + "|" +
                        position;
        return String.valueOf(seed.hashCode());
    }

    private String descripcionDefaultPara(TarjetaTextoArtistaItem artista, int position) {
        int seed = keyFor(artista, position).hashCode();
        Random r = new Random(seed);
        return DEFAULT_DESCRIPCIONES[r.nextInt(DEFAULT_DESCRIPCIONES.length)];
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TarjetaTextoArtistaItem artista = listaArtistas.get(position);

        // Textos principales
        holder.nombre.setText(artista.getNombre());
        holder.categoria.setText("Categoría: " + safeText(artista.getCategoria(), "Sin categoria"));

        String desc = artista.getDescripcion();
        if (desc == null || desc.trim().isEmpty()) {
            holder.descripcion.setText(descripcionDefaultPara(artista, position));
        } else {
            holder.descripcion.setText(desc);
        }

        // Foto perfil
        String fotoPerfil = artista.getFotoPerfil();
        Object targetSource = (fotoPerfil != null && !fotoPerfil.isEmpty())
                ? fotoPerfil
                : R.drawable.fotoperfilprueba;

        Glide.with(context)
                .load(targetSource)
                .placeholder(R.drawable.fotoperfilprueba)
                .error(R.drawable.fotoperfilprueba)
                .circleCrop()
                .into(holder.imgPerfil);

        // Mini obras
        List<String> obras = artista.getMiniObras();
        if (obras.size() > 0)
            Glide.with(context).load(obras.get(0)).placeholder(R.drawable.imagencargaobras).into(holder.imgMini1);
        else
            holder.imgMini1.setImageResource(R.drawable.imagencargaobras);

        if (obras.size() > 1)
            Glide.with(context).load(obras.get(1)).placeholder(R.drawable.imagencargaobras).into(holder.imgMini2);
        else
            holder.imgMini2.setImageResource(R.drawable.imagencargaobras);

        if (obras.size() > 2)
            Glide.with(context).load(obras.get(2)).placeholder(R.drawable.imagencargaobras).into(holder.imgMini3);
        else
            holder.imgMini3.setImageResource(R.drawable.imagencargaobras);

//        //  LIKE local
//        String key = keyFor(artista, position);
//
//        boolean liked = likedMap.containsKey(key) && Boolean.TRUE.equals(likedMap.get(key));
//        int likesCount = likesCountMap.containsKey(key) ? likesCountMap.get(key) : 0;
//
//        holder.likes.setText(String.valueOf(likesCount));
//        holder.btnLike.setImageResource(liked ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple);
//
//        holder.btnLike.setOnClickListener(v -> {
//            int currentPos = holder.getAdapterPosition();
//            if (currentPos == RecyclerView.NO_POSITION) return;
//
//            String k = keyFor(listaArtistas.get(currentPos), currentPos);
//
//            boolean currentLiked = likedMap.containsKey(k) && Boolean.TRUE.equals(likedMap.get(k));
//            int currentCount = likesCountMap.containsKey(k) ? likesCountMap.get(k) : 0;
//
//            if (currentLiked) {
//                // quitar like
//                likedMap.put(k, false);
//                currentCount = Math.max(0, currentCount - 1);
//            } else {
//                // dar like
//                likedMap.put(k, true);
//                currentCount = currentCount + 1;
//            }
//
//            likesCountMap.put(k, currentCount);
//
//            holder.likes.setText(String.valueOf(currentCount));
//            holder.btnLike.setImageResource(Boolean.TRUE.equals(likedMap.get(k))
//                    ? R.drawable.ic_heart_red
//                    : R.drawable.ic_heart_purple);
//
//            animarLike(holder.btnLike);
//        });

        // Expandible
        boolean expandido = (tarjetaExpandida == position);
        animarVista(holder.expandedSection, expandido);

        holder.itemView.setOnClickListener(v -> {
            int previous = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (previous == currentPosition) {
                tarjetaExpandida = -1;
            } else {
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
        return listaArtistas.size();
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

//    private void animarLike(ImageButton btn) {
//        btn.animate()
//                .scaleX(0.75f)
//                .scaleY(0.75f)
//                .setDuration(80)
//                .withEndAction(() ->
//                        btn.animate()
//                                .scaleX(1f)
//                                .scaleY(1f)
//                                .setDuration(80)
//                                .start()
//                ).start();
//    }

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

//            likes = itemView.findViewById(R.id.likes);
//            btnLike = itemView.findViewById(R.id.btnLike);

            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
        }
    }

    public void actualizarLista(List<TarjetaTextoArtistaItem> nuevaLista) {
        this.listaArtistas.clear();
        this.listaArtistas.addAll(nuevaLista);
        notifyDataSetChanged();
    }
}
