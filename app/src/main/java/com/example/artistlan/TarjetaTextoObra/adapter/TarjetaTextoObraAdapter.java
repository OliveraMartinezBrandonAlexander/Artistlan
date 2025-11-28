package com.example.artistlan.TarjetaTextoObra.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private int tarjetaExpandida = -1;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TarjetaTextoObraItem obra = listaObras.get(position);

        // ---------- DATOS ----------
        holder.titulo.setText(obra.getTitulo());
        holder.descripcion.setText(obra.getDescripcion());
        holder.estado.setText(obra.getEstado());
        holder.tecnica.setText(obra.getTecnicas());
        holder.medidas.setText(obra.getMedidas());
        holder.precio.setText("$ " + obra.getPrecio());
        holder.categoria.setText(obra.getCategoria());
        holder.likes.setText("" + obra.getLikes());

        holder.imgAutor.setImageResource(R.drawable.fotoperfilprueba);
        holder.imgObra.setImageResource(R.drawable.ic_launcher_background);

        // ---------- CORAZÓN ----------
        holder.btnLike.setImageResource(
                obra.isLiked() ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple
        );

        holder.btnLike.setOnClickListener(v -> {
            animarLike(holder.btnLike);

            if (obra.isLiked()) {
                obra.setLiked(false);
                obra.setLikes(obra.getLikes() - 1);
                holder.btnLike.setImageResource(R.drawable.ic_heart_purple);
            } else {
                obra.setLiked(true);
                obra.setLikes(obra.getLikes() + 1);
                holder.btnLike.setImageResource(R.drawable.ic_heart_red);
            }

            holder.likes.setText("" + obra.getLikes());
        });

        // ---------- ACORDEÓN: determinar si esta tarjeta está abierta ----------
        boolean expandido = (position == tarjetaExpandida);
        holder.expandedSection.setVisibility(expandido ? View.VISIBLE : View.GONE);
        obra.setExpandido(expandido);

        // ---------- CLICK EN LA TARJETA ----------
        holder.itemView.setOnClickListener(v -> {

            int previousExpanded = tarjetaExpandida;

            if (tarjetaExpandida == position) {
                // Si vuelve a tocar la misma → cerrar
                tarjetaExpandida = -1;
                animarVista(holder.expandedSection, false);
            } else {
                // Abrir nueva tarjeta
                tarjetaExpandida = position;
                animarVista(holder.expandedSection, true);

                // Cerrar la anterior tarjeta
                if (previousExpanded != -1) {
                    notifyItemChanged(previousExpanded);
                }
            }

            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
    }


    // ===========================================================
    // ANIMACIÓN SUAVE (ESCALA + OPACIDAD)
    // ===========================================================
    private void animarVista(View view, boolean expandir) {
        if (expandir) {
            view.setVisibility(View.VISIBLE);
            view.setScaleY(0f);
            view.setAlpha(0f);

            view.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(250)
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    // ===========================================================
    // ANIMACIÓN DE POP EN EL BOTÓN LIKE
    // ===========================================================
    private void animarLike(ImageButton btn) {
        btn.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(80)
                .withEndAction(() ->
                        btn.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                ).start();
    }


    // ===========================================================
    // VIEW HOLDER
    // ===========================================================
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, descripcion, estado, tecnica, medidas, precio, categoria, likes;
        ImageView imgAutor, imgObra;
        ImageButton btnLike;
        View expandedSection;

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

            btnLike = itemView.findViewById(R.id.btnLike);

            expandedSection = itemView.findViewById(R.id.expanded_section);
        }
    }

    public void actualizarLista(List<TarjetaTextoObraItem> nuevaLista) {
        this.listaObras = nuevaLista;
        notifyDataSetChanged();
    }
}
