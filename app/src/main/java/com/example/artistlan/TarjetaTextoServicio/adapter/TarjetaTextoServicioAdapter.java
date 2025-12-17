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

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;

import java.util.List;

public class TarjetaTextoServicioAdapter extends RecyclerView.Adapter<TarjetaTextoServicioAdapter.ViewHolder> {

    private List<TarjetaTextoServicioItem> listaServicios;
    private final Context context;
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

    private String fixUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("http")) return url;
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TarjetaTextoServicioItem servicio = listaServicios.get(position);

        holder.autor.setText(servicio.getAutor());
        holder.titulo.setText(servicio.getTitulo());
        holder.descripcion.setText(servicio.getDescripcion());
        holder.contacto.setText(servicio.getContacto());
        holder.tecnicas.setText(servicio.getTecnicas());
        holder.categoria.setText(servicio.getCategoria());

        String urlPerfil = fixUrl(servicio.getFotoPerfilAutor());

        if (urlPerfil != null) {
            Glide.with(holder.itemView.getContext())
                    .load(urlPerfil)
                    .placeholder(R.drawable.fotoperfilprueba)
                    .circleCrop()
                    .into(holder.imgAutor);
        } else {
            Glide.with(context)
                    .load(R.drawable.fotoperfilprueba)
                    .circleCrop()
                    .into(holder.imgAutor);
        }

        boolean expandido = (tarjetaExpandida == position);

        // ✅ Aquí la animación correcta (expand/collapse)
        animarVista(holder.expandedSection, expandido);

        holder.itemView.setOnClickListener(v -> {
            int previous = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();

            if (previous == currentPosition) {
                tarjetaExpandida = -1; // colapsa si era la misma
            } else {
                tarjetaExpandida = currentPosition; // expande nueva
                if (previous != -1) notifyItemChanged(previous);
            }
            notifyItemChanged(currentPosition);
        });

        holder.btnVisitar.setOnClickListener(v -> {
            // Aquí iría tu navegación (PerfilActivity, etc.)
            // Intent intent = new Intent(context, PerfilActivity.class);
            // intent.putExtra("autor_id", servicio.getAutorId());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaServicios != null ? listaServicios.size() : 0;
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
                    .setDuration(140)
                    .start();
        } else {
            if (view.getVisibility() == View.GONE) return;

            view.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(160)
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

            autor = itemView.findViewById(R.id.autor);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            contacto = itemView.findViewById(R.id.contacto);
            tecnicas = itemView.findViewById(R.id.tecnicas);
            categoria = itemView.findViewById(R.id.categoria);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            btnVisitar = itemView.findViewById(R.id.btnVisitar);
            imgAutor = itemView.findViewById(R.id.imgAutor);
        }
    }
}
