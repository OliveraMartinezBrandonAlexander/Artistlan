package com.example.artistlan.TarjetaTextoObra.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.List;

public class TarjetaTextoObraAdapter extends RecyclerView.Adapter<TarjetaTextoObraAdapter.ViewHolder> {

    private List<TarjetaTextoObraItem> listaObras;
    private Context context;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TarjetaTextoObraItem obra = listaObras.get(position);

        holder.titulo.setText(obra.getTitulo());
        holder.descripcion.setText(obra.getDescripcion());
        holder.estado.setText(obra.getEstado());
        holder.tecnica.setText(obra.getTecnicas());
        holder.medidas.setText(obra.getMedidas());
        holder.precio.setText("$ " + obra.getPrecio());
        holder.categoria.setText(obra.getCategoria());
        holder.likes.setText("❤️ " + obra.getLikes());

        // Imagen del autor desde recursos
        holder.imgAutor.setImageResource(R.drawable.fotoperfilprueba);

        // Imagen de obra (Glide con URL)
        // Glide.with(context).load(obra.getImagen1()).into(holder.imgObra);
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, descripcion, estado, tecnica, medidas, precio, categoria, likes;
        ImageView imgAutor, imgObra;

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
            categoria = itemView.findViewById(R.id.categoria);
            likes = itemView.findViewById(R.id.likes);
        }
    }

    public void actualizarLista(List<TarjetaTextoObraItem> nuevaLista) {
        this.listaObras = nuevaLista;
        notifyDataSetChanged();
    }

}