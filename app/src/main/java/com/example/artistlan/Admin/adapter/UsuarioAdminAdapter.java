package com.example.artistlan.Admin.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAdminAdapter extends RecyclerView.Adapter<UsuarioAdminAdapter.ViewHolder> {

    public interface OnCambiarRolListener {
        void onCambiarRol(UsuariosDTO usuario);
    }

    private final List<UsuariosDTO> items = new ArrayList<>();
    private final List<UsuariosDTO> itemsOriginal = new ArrayList<>();
    private final OnCambiarRolListener listener;

    public UsuarioAdminAdapter(OnCambiarRolListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<UsuariosDTO> nuevos) {
        itemsOriginal.clear();
        if (nuevos != null) itemsOriginal.addAll(nuevos);
        filtrarPorUsuario("");
    }

    public void filtrarPorUsuario(String query) {
        items.clear();
        if (query == null || query.trim().isEmpty()) {
            items.addAll(itemsOriginal);
            notifyDataSetChanged();
            return;
        }

        String filtro = query.trim().toLowerCase();
        for (UsuariosDTO usuario : itemsOriginal) {
            String username = usuario.getUsuario() != null ? usuario.getUsuario().toLowerCase() : "";
            if (username.contains(filtro)) {
                items.add(usuario);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_usuario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuariosDTO item = items.get(position);

        holder.tvNombre.setText(item.getUsuario() == null ? "Sin usuario" : item.getUsuario());
        holder.tvUsuario.setText(item.getNombreCompleto() == null ? "Sin nombre" : item.getNombreCompleto());
        holder.tvRol.setText(item.getRol() == null ? "USER" : item.getRol());

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        ThemeApplier.applyTextPrimary(holder.tvNombre, tm);
        ThemeApplier.applyTextSecondary(holder.tvUsuario, tm);
        ThemeApplier.applyTextSecondary(holder.tvRol, tm);
        ThemeApplier.applyPrimaryButton(holder.btnCambiarRol, tm);

        holder.btnCambiarRol.setOnClickListener(v -> listener.onCambiarRol(item));

        String fotoPerfil = item.getFotoPerfil();
        if (TextUtils.isEmpty(fotoPerfil)) {
            Glide.with(holder.itemView.getContext()).clear(holder.imgUsuario);
            holder.imgUsuario.setImageResource(R.drawable.fotoperfilprueba);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(fotoPerfil)
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .circleCrop()
                    .into(holder.imgUsuario);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.imgUsuario);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgUsuario;
        final TextView tvNombre;
        final TextView tvUsuario;
        final TextView tvRol;
        final Button btnCambiarRol;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUsuario = itemView.findViewById(R.id.imgUsuarioAdmin);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuarioAdmin);
            tvUsuario = itemView.findViewById(R.id.tvUsuarioAdmin);
            tvRol = itemView.findViewById(R.id.tvRolUsuarioAdmin);
            btnCambiarRol = itemView.findViewById(R.id.btnCambiarRol);
        }
    }
}
