package com.example.artistlan.Carrusel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;
import com.example.artistlan.Carrusel.model.ObraCarrusel;

import java.util.List;

public class CarruselAdapter extends RecyclerView.Adapter<CarruselAdapter.CarruselViewHolder> {
    private List<ObraCarrusel> obras;
    private Context context;

    public CarruselAdapter(List<ObraCarrusel> obras, Context context) {
        this.obras = obras;
        this.context = context;
    }

    @NonNull
    @Override
    public CarruselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrusel, parent, false);
        return new CarruselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarruselViewHolder holder, int position) {
        ObraCarrusel obra = obras.get(position);
        holder.imgObra.setImageResource(obra.getImagen());
        holder.tvTitulo.setText(obra.getTitulo());
        holder.tvDescripcion.setText(obra.getDescripcion());
        holder.tvAutor.setText(obra.getAutor());
        holder.tvLikes.setText(obra.getLikes());
    }

    @Override
    public int getItemCount() {
        return obras.size();
    }

    public static class CarruselViewHolder extends RecyclerView.ViewHolder {
        ImageView imgObra;
        TextView tvTitulo, tvDescripcion, tvAutor, tvLikes;

        public CarruselViewHolder(@NonNull View itemView) {
            super(itemView);
            imgObra = itemView.findViewById(R.id.imgObra);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvLikes = itemView.findViewById(R.id.tvLikes);
        }
    }
}
