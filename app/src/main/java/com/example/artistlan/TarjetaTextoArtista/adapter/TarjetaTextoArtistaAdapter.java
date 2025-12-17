package com.example.artistlan.TarjetaTextoArtista.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoArtista.model.TarjetaTextoArtistaItem;

import java.util.ArrayList;
import java.util.List;

public class TarjetaTextoArtistaAdapter extends RecyclerView.Adapter<TarjetaTextoArtistaAdapter.ViewHolder> {

    private final Context context;
    private List<TarjetaTextoArtistaItem> listaArtistas;

    private int tarjetaExpandida = -1;

    public TarjetaTextoArtistaAdapter(List<TarjetaTextoArtistaItem> listaArtistas, Context context) {
        this.listaArtistas = (listaArtistas != null) ? listaArtistas : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoartista, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        TarjetaTextoArtistaItem artista = listaArtistas.get(position);

        holder.nombre.setText(artista.getNombre());
        holder.descripcion.setText(artista.getDescripcion());
        holder.categoria.setText(artista.getCategoria()); // ahora se ve en expandido

        // Foto perfil (circular)
        String fotoPerfil = artista.getFotoPerfil();
        Object perfilSource = (fotoPerfil != null && !fotoPerfil.trim().isEmpty())
                ? fotoPerfil
                : R.drawable.fotoperfilprueba;

        Glide.with(context)
                .load(perfilSource)
                .placeholder(R.drawable.fotoperfilprueba)
                .error(R.drawable.fotoperfilprueba)
                .circleCrop()
                .into(holder.imgPerfil);

        // Mini obras (más seguro si vienen null/empty)
        List<String> obras = artista.getMiniObras();

        cargarMiniObra(holder.imgMini1, (obras != null && obras.size() > 0) ? obras.get(0) : null);
        cargarMiniObra(holder.imgMini2, (obras != null && obras.size() > 1) ? obras.get(1) : null);
        cargarMiniObra(holder.imgMini3, (obras != null && obras.size() > 2) ? obras.get(2) : null);

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
            // Aquí iría la navegación a perfil del artista
        });
    }

    private void cargarMiniObra(ImageView img, String url) {
        Object source = (url != null && !url.trim().isEmpty())
                ? url
                : R.drawable.imagencargaobras;

        Glide.with(context)
                .load(source)
                .placeholder(R.drawable.imagencargaobras)
                .error(R.drawable.imagencargaobras)
                .into(img);
    }

    @Override
    public int getItemCount() {
        return listaArtistas != null ? listaArtistas.size() : 0;
    }

    private void animarVista(View v, boolean expandir) {
        if (expandir) {
            if (v.getVisibility() == View.VISIBLE && v.getScaleY() == 1f) return;

            v.setVisibility(View.VISIBLE);
            v.setAlpha(0f);
            v.setScaleY(0f);
            v.animate().alpha(1f).scaleY(1f).setDuration(140).start();
        } else {
            if (v.getVisibility() == View.GONE) return;

            v.animate().alpha(0f).scaleY(0f).setDuration(160)
                    .withEndAction(() -> v.setVisibility(View.GONE))
                    .start();
        }
    }

    public void actualizarLista(List<TarjetaTextoArtistaItem> nuevaLista) {
        this.listaArtistas = (nuevaLista != null) ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, categoria, descripcion, mensaje;
        ImageView imgPerfil, imgMini1, imgMini2, imgMini3;
        View expandedSection;
        Button btnVisitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPerfil = itemView.findViewById(R.id.imgPerfil);
            nombre = itemView.findViewById(R.id.nombre);
            categoria = itemView.findViewById(R.id.categoria);
            descripcion = itemView.findViewById(R.id.descripcion);
            mensaje = itemView.findViewById(R.id.mensaje);

            imgMini1 = itemView.findViewById(R.id.imgMini1);
            imgMini2 = itemView.findViewById(R.id.imgMini2);
            imgMini3 = itemView.findViewById(R.id.imgMini3);

            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
        }
    }
}
