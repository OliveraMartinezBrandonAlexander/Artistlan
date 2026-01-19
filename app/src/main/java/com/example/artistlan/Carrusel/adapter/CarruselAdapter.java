package com.example.artistlan.Carrusel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Carrusel.model.ObraCarruselItem;
import com.example.artistlan.R;

import java.util.List;

public class CarruselAdapter extends RecyclerView.Adapter<CarruselAdapter.CarruselViewHolder> {

    private final List<ObraCarruselItem> lista;
    private final Context context;

    public CarruselAdapter(List<ObraCarruselItem> lista, Context context) {
        this.lista = lista;
        this.context = context;
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
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class CarruselViewHolder extends RecyclerView.ViewHolder {

        ImageView imgObra, imgAutor;
        TextView tvTitulo, tvDescripcion, tvAutor;

        public CarruselViewHolder(@NonNull View itemView) {
            super(itemView);
            imgObra       = itemView.findViewById(R.id.imgObra);
            imgAutor      = itemView.findViewById(R.id.imgAutor);
            tvTitulo      = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvAutor       = itemView.findViewById(R.id.tvAutor);
        }
    }
}
