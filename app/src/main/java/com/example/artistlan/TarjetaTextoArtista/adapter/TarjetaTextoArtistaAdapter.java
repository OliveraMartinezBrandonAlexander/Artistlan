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

import java.util.List;

public class TarjetaTextoArtistaAdapter extends RecyclerView.Adapter<TarjetaTextoArtistaAdapter.ViewHolder> {

    private List<TarjetaTextoArtistaItem> listaArtistas;
    private Context context;

    private int tarjetaExpandida = -1;

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        TarjetaTextoArtistaItem artista = listaArtistas.get(position);

        holder.nombre.setText(artista.getNombre());
        holder.categoria.setText(artista.getCategoria());
        holder.descripcion.setText(artista.getDescripcion());

        // Mini obras
        List<String> obras = artista.getMiniObras();
        if(obras.size() > 0)
            Glide.with(context).load(obras.get(0)).placeholder(R.drawable.imagencargaobras).into(holder.imgMini1);
        else
            holder.imgMini1.setImageResource(R.drawable.imagencargaobras);

        if(obras.size() > 1)
            Glide.with(context).load(obras.get(1)).placeholder(R.drawable.imagencargaobras).into(holder.imgMini2);
        else
            holder.imgMini2.setImageResource(R.drawable.imagencargaobras);

        if(obras.size() > 2)
            Glide.with(context).load(obras.get(2)).placeholder(R.drawable.imagencargaobras).into(holder.imgMini3);
        else
            holder.imgMini3.setImageResource(R.drawable.imagencargaobras);

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


        boolean expandido = (tarjetaExpandida == position);


        if (expandido) {
            animarVista(holder.expandedSection, true);
        } else {
            animarVista(holder.expandedSection, false);
        }

        holder.itemView.setOnClickListener(v -> {

            int previous = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (previous == currentPosition) {
                tarjetaExpandida = -1;
            } else {
                tarjetaExpandida = currentPosition;

                if (previous != -1) {
                    notifyItemChanged(previous);
                }
            }

            notifyItemChanged(currentPosition);

        });

        holder.btnVisitar.setOnClickListener(v -> {
            // Aquí iría la vista de perfil de artista
        });
    }

    @Override
    public int getItemCount() {
        return listaArtistas.size();
    }

    private void animarVista(View v, boolean expandir) {
        if (expandir) {
            if (v.getVisibility() == View.VISIBLE && v.getScaleY() == 1f) return; // Ya expandida

            v.setVisibility(View.VISIBLE);
            v.setAlpha(0);
            v.setScaleY(0);
            v.animate().alpha(1f).scaleY(1f).setDuration(100).start();
        } else {
            if (v.getVisibility() == View.GONE) return;

            v.animate().alpha(0f).scaleY(0f).setDuration(150)
                    .withEndAction(() -> v.setVisibility(View.GONE))
                    .start();
        }
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
    public void actualizarLista(List<TarjetaTextoArtistaItem> nuevaLista) {
        this.listaArtistas.clear();
        this.listaArtistas.addAll(nuevaLista);
        notifyDataSetChanged();
    }

}
