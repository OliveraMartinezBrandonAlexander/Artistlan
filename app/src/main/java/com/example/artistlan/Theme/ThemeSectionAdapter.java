package com.example.artistlan.Theme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.R;

import java.util.List;

public class ThemeSectionAdapter extends RecyclerView.Adapter<ThemeSectionAdapter.SectionVH> {

    public interface OnThemeColorRequested {
        void onPickColor(ThemeItem item);
    }

    private final List<ThemeSection> sections;
    private final OnThemeColorRequested listener;

    public ThemeSectionAdapter(List<ThemeSection> sections, OnThemeColorRequested listener) {
        this.sections = sections;
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        ThemeSection section = sections.get(position);
        return section.getTitle().hashCode();
    }

    @NonNull
    @Override
    public SectionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme_section, parent, false);
        return new SectionVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionVH holder, int position) {
        ThemeSection section = sections.get(position);

        holder.title.setText(section.getTitle());
        holder.subtitle.setText(section.getSubtitle());

        if (holder.recyclerItems.getLayoutManager() == null) {
            holder.recyclerItems.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.recyclerItems.setNestedScrollingEnabled(false);
            holder.recyclerItems.setHasFixedSize(false);
            holder.recyclerItems.setItemAnimator(null);
        }

        ThemeItemAdapter adapter = (ThemeItemAdapter) holder.recyclerItems.getAdapter();
        if (adapter == null) {
            adapter = new ThemeItemAdapter(section.getItems(), (item, itemPos) -> listener.onPickColor(item));
            holder.recyclerItems.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        holder.recyclerItems.setVisibility(section.isExpanded() ? View.VISIBLE : View.GONE);
        holder.chevron.animate()
                .rotation(section.isExpanded() ? 180f : 0f)
                .setDuration(180)
                .start();

        holder.header.setOnClickListener(v -> {
            section.toggle();
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class SectionVH extends RecyclerView.ViewHolder {
        View header;
        TextView title, subtitle;
        ImageView chevron;
        RecyclerView recyclerItems;

        SectionVH(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.sectionHeader);
            title = itemView.findViewById(R.id.sectionTitle);
            subtitle = itemView.findViewById(R.id.sectionSubtitle);
            chevron = itemView.findViewById(R.id.sectionChevron);
            recyclerItems = itemView.findViewById(R.id.sectionRecyclerItems);
        }
    }
}