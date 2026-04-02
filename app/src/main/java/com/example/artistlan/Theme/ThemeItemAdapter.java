package com.example.artistlan.Theme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;

import java.util.List;
import java.util.Locale;

public class ThemeItemAdapter extends RecyclerView.Adapter<ThemeItemAdapter.ItemVH> {

    public interface OnThemeItemClick {
        void onClick(ThemeItem item, int position);
    }

    private final List<ThemeItem> items;
    private final OnThemeItemClick listener;

    public ThemeItemAdapter(List<ThemeItem> items, OnThemeItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_color_role, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVH holder, int position) {
        ThemeItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDescription());
        holder.hex.setText(String.format(Locale.US, "#%08X", item.getColor()));
        holder.preview.setBackgroundColor(item.getColor());

        holder.itemView.setOnClickListener(v -> listener.onClick(item, position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        View preview;
        TextView title, desc, hex;

        ItemVH(@NonNull View itemView) {
            super(itemView);
            preview = itemView.findViewById(R.id.roleColorPreview);
            title = itemView.findViewById(R.id.roleTitle);
            desc = itemView.findViewById(R.id.roleDescription);
            hex = itemView.findViewById(R.id.roleHex);
        }
    }
}