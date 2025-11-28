package com.example.artistlan.TarjetaTextoServicio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.List;

public class TarjetaTextoServicioAdapter extends RecyclerView.Adapter<TarjetaTextoServicioAdapter.TarjetaTextoServicioViewHolder> {

    private List<TarjetaTextoServicioItem> listaServicios;
    private Context context;

    public TarjetaTextoServicioAdapter(List<TarjetaTextoServicioItem> listaServicios, Context context) {
        this.listaServicios = listaServicios;
        this.context = context;
    }

    @NonNull
    @Override
    public TarjetaTextoServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoservicio, parent, false);
        return new TarjetaTextoServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaTextoServicioViewHolder holder, int position) {
        TarjetaTextoServicioItem servicio = listaServicios.get(position);

        holder.titulo.setText(servicio.getTitulo());
        holder.descripcion.setText(servicio.getDescripcion());
        holder.contacto.setText(servicio.getContacto());
        holder.tecnicas.setText(servicio.getTecnicas());
        holder.autor.setText(servicio.getAutor());
    }

    @Override
    public int getItemCount() {
        return listaServicios.size();
    }

    public void actualizarLista(List<TarjetaTextoServicioItem> nuevaLista) {
        this.listaServicios = nuevaLista;
        notifyDataSetChanged();
    }

    public static class TarjetaTextoServicioViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, descripcion, contacto, tecnicas, autor;

        public TarjetaTextoServicioViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            contacto = itemView.findViewById(R.id.contacto);
            tecnicas = itemView.findViewById(R.id.tecnicas);
            autor = itemView.findViewById(R.id.autor);
        }
    }
}