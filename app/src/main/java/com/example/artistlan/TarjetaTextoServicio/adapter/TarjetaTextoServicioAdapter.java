package com.example.artistlan.TarjetaTextoServicio.adapter;

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

import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.List;

public class TarjetaTextoServicioAdapter extends RecyclerView.Adapter<TarjetaTextoServicioAdapter.ViewHolder> {

    private List<TarjetaTextoServicioItem> listaServicios;
    private Context context;

    // Campo para manejar el estado de la tarjeta expandida
    private int tarjetaExpandida = -1;

    public TarjetaTextoServicioAdapter(List<TarjetaTextoServicioItem> listaServicios, Context context) {
        this.listaServicios = listaServicios;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoservicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TarjetaTextoServicioItem servicio = listaServicios.get(position);

        // 1. Asignar datos a los TextViews
        holder.autor.setText(servicio.getAutor());
        holder.titulo.setText(servicio.getTitulo());
        holder.descripcion.setText(servicio.getDescripcion());
        holder.contacto.setText(servicio.getContacto());
        holder.tecnicas.setText(servicio.getTecnicas());
        holder.categoria.setText(servicio.getCategoria());


        // 2. Lógica de expansión/colapso
        boolean expandido = (tarjetaExpandida == position);

        if (expandido) {
            holder.expandedSection.setVisibility(View.VISIBLE);

        } else {
            animarVista(holder.expandedSection, false);
        }

        // 3. Listener para el clic en el elemento
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

        // 4. Listener para el botón Visitar
        holder.btnVisitar.setOnClickListener(v -> {
            // Aquí iría el código para abrir la vista del usuario/contacto, por ejemplo:
            // Intent intent = new Intent(context, PerfilActivity.class);
            // intent.putExtra("autor_id", servicio.getAutorId());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaServicios.size();
    }

    public void actualizarLista(List<TarjetaTextoServicioItem> nuevaLista) {
        this.listaServicios = nuevaLista;
        notifyDataSetChanged();
    }

    private void animarVista(View view, boolean expandir) {
        if (expandir) {
            if (view.getVisibility() == View.VISIBLE) return;

            view.setVisibility(View.VISIBLE);
            view.setScaleY(0f);
            view.setAlpha(0f);

            view.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
        } else {
            if (view.getVisibility() == View.GONE) return;

            view.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, descripcion, contacto, tecnicas, autor, categoria;
        ImageView imgAutor;
        View expandedSection;
        Button btnVisitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Se añaden todos los elementos del layout XML (item_tarjetatextoservicio)
            imgAutor = itemView.findViewById(R.id.imgAutor);
            autor = itemView.findViewById(R.id.autor);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            contacto = itemView.findViewById(R.id.contacto);
            tecnicas = itemView.findViewById(R.id.tecnicas);
            categoria = itemView.findViewById(R.id.categoria);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
        }
    }
}