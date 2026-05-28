package com.example.artistlan.HistoriaArte.adapter;

import android.animation.LayoutTransition;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.HistoriaArte.model.HistoriaArteItem;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;

import java.util.List;

public class HistoriaArteAdapter extends RecyclerView.Adapter<HistoriaArteAdapter.HistoriaArteViewHolder> {

    private List<HistoriaArteItem> items;

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

        CardThemeHelper.applyGradientGlassCard(holder.layoutCard, tm, 20);
        CardThemeHelper.applyChip(holder.tvCategoria, tm);
        CardThemeHelper.applyChip(holder.tvPeriodo, tm);
        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvResumen, tm);
        ThemeApplier.applyTextSecondary(holder.tvDato, tm);
        ThemeApplier.applyTextPrimary(holder.tvContenido, tm);
        ThemeApplier.applySecondaryButton(holder.tvVerMas, tm);
        ThemeApplier.applyIcon(holder.ivIcono, tm, ThemeKeys.ICON_ACTIVE);
        holder.ivIcono.setBackgroundTintList(ColorStateList.valueOf(tm.color(ThemeKeys.CARD_CHIP_BG)));
        holder.ivIcono.setImageResource(resolveIcon(item));
        holder.tvCategoria.setText(item.getCategoria());
        holder.tvPeriodo.setText(item.getPeriodo());
        holder.tvTitulo.setText(item.getTitulo());
        holder.tvResumen.setText(item.getResumen());
        holder.tvDato.setText(item.getDatoClave().isEmpty()
                ? "Dato importante: ayuda a entender cómo cambia la relación entre técnica, contexto y mirada."
                : "Dato importante: " + item.getDatoClave());
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

    public void actualizarItems(@NonNull List<HistoriaArteItem> nuevosItems) {
        items = nuevosItems;
        notifyDataSetChanged();
    }

    private int resolveIcon(@NonNull HistoriaArteItem item) {
        if (item.getIconResId() != 0) {
            return item.getIconResId();
        }
        String title = (item.getTitulo() + " " + item.getCategoria()).toLowerCase();
        if (title.contains("prehist") || title.contains("rupestre")) return R.drawable.ic_art_cave;
        if (title.contains("egip")) return R.drawable.ic_art_pyramid;
        if (title.contains("grieg") || title.contains("roman") || title.contains("neocl")) return R.drawable.ic_art_column;
        if (title.contains("renacimiento") || title.contains("pintura") || title.contains("dibujo")
                || title.contains("óleo") || title.contains("oleo") || title.contains("acuarela")) return R.drawable.ic_art_palette;
        if (title.contains("impresionismo") || title.contains("sol") || title.contains("paisaje")) return R.drawable.ic_art_sun;
        if (title.contains("abstract") || title.contains("cubismo") || title.contains("minimalismo")
                || title.contains("vanguard") || title.contains("moderno")) return R.drawable.ic_art_abstract;
        if (title.contains("contempor") || title.contains("digital") || title.contains("instalación")
                || title.contains("instalacion") || title.contains("performance")) return R.drawable.ic_art_gallery;
        return R.drawable.ic_history_art;
    }

    static class HistoriaArteViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutCard;
        ImageView ivIcono;
        TextView tvCategoria;
        TextView tvPeriodo;
        TextView tvTitulo;
        TextView tvResumen;
        TextView tvDato;
        TextView tvContenido;
        TextView tvVerMas;

        public HistoriaArteViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutCard = itemView.findViewById(R.id.layoutHistoriaCard);
            ivIcono = itemView.findViewById(R.id.ivIconoHistoriaArte);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaHistoriaArte);
            tvPeriodo = itemView.findViewById(R.id.tvPeriodoHistoriaArte);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistoriaArte);
            tvResumen = itemView.findViewById(R.id.tvResumenHistoriaArte);
            tvDato = itemView.findViewById(R.id.tvDatoHistoriaArte);
            tvContenido = itemView.findViewById(R.id.tvContenidoHistoriaArte);
            tvVerMas = itemView.findViewById(R.id.tvVerMasHistoriaArte);

            if (layoutCard != null) {
                layoutCard.setLayoutTransition(new LayoutTransition());
            }
        }
    }
}
