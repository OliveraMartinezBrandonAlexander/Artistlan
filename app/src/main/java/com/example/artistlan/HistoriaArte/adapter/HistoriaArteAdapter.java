package com.example.artistlan.HistoriaArte.adapter;

import android.animation.LayoutTransition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.HistoriaArte.model.HistoriaArteItem;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;

import java.util.List;

public class HistoriaArteAdapter extends RecyclerView.Adapter<HistoriaArteAdapter.HistoriaArteViewHolder> {

    private final List<HistoriaArteItem> items;

    public HistoriaArteAdapter(List<HistoriaArteItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public HistoriaArteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historia_arte, parent, false);

        return new HistoriaArteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoriaArteViewHolder holder, int position) {
        HistoriaArteItem item = items.get(position);
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());

        CardThemeHelper.applyFlatCard(holder.layoutCard, tm);
        CardThemeHelper.applyChip(holder.tvCategoria, tm);
        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvResumen, tm);
        ThemeApplier.applyTextPrimary(holder.tvContenido, tm);
        ThemeApplier.applySecondaryButton(holder.tvVerMas, tm);
        holder.tvCategoria.setText(item.getCategoria());
        holder.tvTitulo.setText(item.getTitulo());
        holder.tvResumen.setText(item.getResumen());
        holder.tvContenido.setText(item.getContenido());

        boolean expandido = item.isExpandido();

        holder.tvContenido.setVisibility(expandido ? View.VISIBLE : View.GONE);
        holder.tvVerMas.setText(expandido ? "Ver menos" : "Ver más");

        holder.itemView.setOnClickListener(v -> toggleItem(item, holder));
        holder.tvVerMas.setOnClickListener(v -> toggleItem(item, holder));

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(16f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .start();
    }

    private void toggleItem(@NonNull HistoriaArteItem item, @NonNull HistoriaArteViewHolder holder) {
        int position = holder.getBindingAdapterPosition();

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        item.setExpandido(!item.isExpandido());
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoriaArteViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutCard;
        TextView tvCategoria;
        TextView tvTitulo;
        TextView tvResumen;
        TextView tvContenido;
        TextView tvVerMas;

        public HistoriaArteViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutCard = itemView.findViewById(R.id.layoutHistoriaCard);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaHistoriaArte);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistoriaArte);
            tvResumen = itemView.findViewById(R.id.tvResumenHistoriaArte);
            tvContenido = itemView.findViewById(R.id.tvContenidoHistoriaArte);
            tvVerMas = itemView.findViewById(R.id.tvVerMasHistoriaArte);

            if (layoutCard != null) {
                layoutCard.setLayoutTransition(new LayoutTransition());
            }
        }
    }
}
