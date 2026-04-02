package com.example.artistlan.Theme;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;

import java.util.List;

public class ThemeColorRoleAdapter extends RecyclerView.Adapter<ThemeColorRoleAdapter.RoleViewHolder> {

    public interface OnRoleClickListener {
        void onRoleClick(ThemeColorRole role, int position);
    }

    private final List<ThemeColorRole> roles;
    private final OnRoleClickListener listener;

    public ThemeColorRoleAdapter(List<ThemeColorRole> roles, OnRoleClickListener listener) {
        this.roles = roles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_color_role, parent, false);
        return new RoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoleViewHolder holder, int position) {
        ThemeColorRole role = roles.get(position);

        holder.tvTitle.setText(role.getTitle());
        holder.tvDescription.setText(role.getDescription());
        holder.colorPreview.setBackgroundColor(role.getColor());
        holder.tvHex.setText(String.format("#%08X", role.getColor()));

        holder.itemView.setOnClickListener(v -> listener.onRoleClick(role, position));
    }

    @Override
    public int getItemCount() {
        return roles.size();
    }

    public void updateItem(int position, int color) {
        roles.get(position).setColor(color);
        notifyItemChanged(position);
    }

    static class RoleViewHolder extends RecyclerView.ViewHolder {
        View colorPreview;
        TextView tvTitle, tvDescription, tvHex;

        public RoleViewHolder(@NonNull View itemView) {
            super(itemView);
            colorPreview = itemView.findViewById(R.id.roleColorPreview);
            tvTitle = itemView.findViewById(R.id.roleTitle);
            tvDescription = itemView.findViewById(R.id.roleDescription);
            tvHex = itemView.findViewById(R.id.roleHex);
        }
    }
}