package com.example.artistlan.Admin.adapter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Conector.model.AdminRankingItemDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRankingAdapter extends RecyclerView.Adapter<AdminRankingAdapter.ViewHolder> {

    private static final int PROGRESS_MAX = 1000;
    private static final int MIN_VISIBLE_PROGRESS = 72;

    private final List<AdminRankingItemDTO> items = new ArrayList<>();
    private String tipo = "OBRAS";
    private long maxTotal = 0L;

    public void actualizar(@Nullable List<AdminRankingItemDTO> nuevos, @Nullable String nuevoTipo) {
        items.clear();
        maxTotal = 0L;
        if (!TextUtils.isEmpty(nuevoTipo)) {
            tipo = nuevoTipo.trim().toUpperCase(Locale.ROOT);
        }
        if (nuevos != null) {
            items.addAll(nuevos);
            for (AdminRankingItemDTO item : nuevos) {
                if (item != null) {
                    maxTotal = Math.max(maxTotal, Math.max(0L, item.getTotal()));
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminRankingItemDTO item = items.get(position);
        long total = item != null ? Math.max(0L, item.getTotal()) : 0L;
        int progress = calcularProgress(total);

        holder.tvPosicion.setText((position + 1) + "\u00B0");
        holder.tvNombre.setText(obtenerTitulo(item));
        holder.tvTotal.setVisibility(View.GONE);
        holder.tvDetalle.setText(construirDetalle(total));

        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        CardThemeHelper.applyMessageCard(holder.cardRoot, tm, position == 0);
        CardThemeHelper.applySoftChip(holder.tvPosicion, tm);
        CardThemeHelper.applySoftChip(holder.tvTipoChip, tm);
        ThemeApplier.applyTextPrimary(holder.tvNombre, tm);
        ThemeApplier.applyTextSecondary(holder.tvSubtitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvDetalle, tm);
        ThemeApplier.applyTextSecondary(holder.tvAutorSecundario, tm);
        holder.layoutAutorSecundario.setBackground(null);
        CardThemeHelper.applyAvatarStroke(holder.imgRankingRect, tm);
        CardThemeHelper.applyAvatarStroke(holder.imgRankingCircle, tm);
        CardThemeHelper.applyAvatarStroke(holder.imgAutorSecundario, tm);

        configurarVisualPorTipo(holder, item, tm);

        int accent = (position % 2 == 0)
                ? tm.color(ThemeKeys.ACCENT_PRIMARY)
                : tm.color(ThemeKeys.ACCENT_SECONDARY);
        int track = ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 118);
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        holder.progressBar.setTrackThickness(Math.round(6f * density));
        holder.progressBar.setTrackCornerRadius(Math.round(6f * density));
        holder.progressBar.setIndicatorColor(accent);
        holder.progressBar.setTrackColor(track);
        holder.progressBar.setMax(PROGRESS_MAX);
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.progressBar.setProgressCompat(progress, false);
        holder.progressBar.setAlpha(total > 0L ? 0.96f : 0.82f);
        aplicarMetricaCompacta(holder.tvDetalle, tm);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.imgRankingRect);
        Glide.with(holder.itemView.getContext()).clear(holder.imgRankingCircle);
        Glide.with(holder.itemView.getContext()).clear(holder.imgAutorSecundario);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void configurarVisualPorTipo(@NonNull ViewHolder holder,
                                         @Nullable AdminRankingItemDTO item,
                                         @NonNull ThemeManager tm) {
        holder.imgRankingRect.setVisibility(View.GONE);
        holder.imgRankingCircle.setVisibility(View.GONE);
        holder.layoutAutorSecundario.setVisibility(View.GONE);
        holder.tvSubtitulo.setVisibility(View.VISIBLE);

        if ("SERVICIOS".equals(tipo)) {
            holder.tvTipoChip.setText("Servicio");
            holder.tvSubtitulo.setText(obtenerSubtituloPrincipalServicio(item));
            String autor = obtenerAutor(item);
            if (!TextUtils.isEmpty(autor) || !TextUtils.isEmpty(item != null ? item.getImagen() : null)) {
                holder.layoutAutorSecundario.setVisibility(View.VISIBLE);
                holder.tvAutorSecundario.setText(formatearPrestador(autor));
                cargarImagen(holder.imgAutorSecundario, item != null ? item.getImagen() : null,
                        R.drawable.fotoperfilprueba, true);
            }
            ajustarMargenInicio(holder.layoutTextos, 0);
            return;
        }

        ajustarMargenInicio(holder.layoutTextos, 12);

        if ("ARTISTAS".equals(tipo)) {
            holder.tvTipoChip.setText("Perfil");
            holder.tvSubtitulo.setText(obtenerSubtitulo(item));
            holder.imgRankingCircle.setVisibility(View.VISIBLE);
            cargarImagen(holder.imgRankingCircle, item != null ? item.getImagen() : null,
                    R.drawable.fotoperfilprueba, true);
            return;
        }

        holder.tvTipoChip.setText("Obra");
        holder.tvSubtitulo.setVisibility(View.GONE);
        holder.layoutAutorSecundario.setVisibility(View.VISIBLE);
        holder.tvAutorSecundario.setText(obtenerAutorObra(item));
        cargarImagen(holder.imgAutorSecundario, obtenerImagenAutor(item),
                R.drawable.fotoperfilprueba, true);
        holder.imgRankingRect.setVisibility(View.VISIBLE);
        cargarImagen(holder.imgRankingRect, item != null ? item.getImagen() : null,
                R.drawable.imagencargaobras, false);
    }

    private void cargarImagen(@NonNull ShapeableImageView imageView,
                              @Nullable String imagen,
                              int placeholder,
                              boolean circular) {
        if (TextUtils.isEmpty(imagen)) {
            Glide.with(imageView.getContext()).clear(imageView);
            imageView.setImageResource(placeholder);
            return;
        }

        if (circular) {
            Glide.with(imageView.getContext())
                    .load(imagen.trim())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        Glide.with(imageView.getContext())
                .load(imagen.trim())
                .placeholder(placeholder)
                .error(placeholder)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(imageView);
    }

    private void ajustarMargenInicio(@NonNull View view, int marginStartDp) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        int px = Math.round(marginStartDp * view.getResources().getDisplayMetrics().density);
        if (marginLayoutParams.getMarginStart() != px) {
            marginLayoutParams.setMarginStart(px);
            view.setLayoutParams(marginLayoutParams);
        }
    }

    private int calcularProgress(long total) {
        if (maxTotal <= 0L || total <= 0L) {
            return 0;
        }
        int progress = (int) Math.round((total * PROGRESS_MAX) / (double) maxTotal);
        return Math.min(PROGRESS_MAX, Math.max(MIN_VISIBLE_PROGRESS, progress));
    }

    @NonNull
    private String obtenerTitulo(@Nullable AdminRankingItemDTO item) {
        return obtenerNombre(item);
    }

    @NonNull
    private String obtenerNombre(@Nullable AdminRankingItemDTO item) {
        String nombre = item != null ? item.getNombre() : null;
        if (TextUtils.isEmpty(nombre)) {
            return "Sin nombre";
        }
        return nombre.trim();
    }

    @NonNull
    private String obtenerSubtitulo(@Nullable AdminRankingItemDTO item) {
        String valor;
        if ("ARTISTAS".equals(tipo)) {
            valor = item != null ? item.getSubtitulo() : null;
        } else {
            valor = item != null ? item.getAutor() : null;
        }

        if (TextUtils.isEmpty(valor) && item != null) {
            valor = item.getSubtitulo();
        }
        if (TextUtils.isEmpty(valor) && item != null) {
            valor = item.getDescripcionSecundaria();
        }
        if (TextUtils.isEmpty(valor)) {
            return "Sin informaci\u00F3n adicional";
        }
        return valor.trim();
    }

    @NonNull
    private String obtenerSubtituloPrincipalServicio(@Nullable AdminRankingItemDTO item) {
        String contactoFormateado = construirContactoServicio(item);
        if (!TextUtils.isEmpty(contactoFormateado)) {
            return contactoFormateado;
        }
        String autor = item != null ? item.getAutor() : null;
        if (!TextUtils.isEmpty(autor)) {
            return autor.trim();
        }
        String subtitulo = item != null ? item.getSubtitulo() : null;
        if (!TextUtils.isEmpty(subtitulo)) {
            return subtitulo.trim();
        }
        return "Servicio sin contacto visible";
    }

    @Nullable
    private String construirContactoServicio(@Nullable AdminRankingItemDTO item) {
        if (item == null) {
            return null;
        }
        String contacto = limpiarTexto(item.getContacto());
        if (TextUtils.isEmpty(contacto)) {
            return null;
        }
        String tipoContacto = limpiarTexto(item.getTipoContacto());
        if (TextUtils.isEmpty(tipoContacto)) {
            return "Contacto: " + contacto;
        }
        return "Contacto: " + normalizarTipoContacto(tipoContacto) + " \u00B7 " + contacto;
    }

    @NonNull
    private String obtenerAutor(@Nullable AdminRankingItemDTO item) {
        String autor = item != null ? item.getAutor() : null;
        if (TextUtils.isEmpty(autor)) {
            return "";
        }
        return autor.trim();
    }

    @Nullable
    private String obtenerImagenAutor(@Nullable AdminRankingItemDTO item) {
        return item != null ? limpiarTexto(item.getImagenAutor()) : null;
    }

    @NonNull
    private String obtenerAutorObra(@Nullable AdminRankingItemDTO item) {
        String autor = obtenerAutor(item);
        if (TextUtils.isEmpty(autor)) {
            return "Autor no disponible";
        }
        return autor;
    }

    @NonNull
    private String formatearPrestador(@Nullable String autor) {
        if (TextUtils.isEmpty(autor)) {
            return "Autor no disponible";
        }
        return autor.trim();
    }

    @Nullable
    private String limpiarTexto(@Nullable String valor) {
        if (TextUtils.isEmpty(valor)) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    @NonNull
    private String normalizarTipoContacto(@NonNull String tipo) {
        String valor = tipo.trim().toUpperCase(Locale.ROOT);
        switch (valor) {
            case "WHATSAPP":
                return "WhatsApp";
            case "INSTAGRAM":
                return "Instagram";
            case "EMAIL":
            case "CORREO":
                return "Correo";
            case "TELEFONO":
            case "TEL":
            case "PHONE":
                return "Tel\u00E9fono";
            default:
                return tipo.trim();
        }
    }

    @NonNull
    private String construirDetalle(long total) {
        long valor = Math.max(0L, total);
        if ("ARTISTAS".equals(tipo)) {
            return valor + " interacciones de popularidad";
        }
        return String.valueOf(valor);
    }

    private void aplicarMetricaCompacta(@NonNull TextView textView, @NonNull ThemeManager tm) {
        if ("ARTISTAS".equals(tipo)) {
            textView.setCompoundDrawablesRelative(null, null, null, null);
            textView.setCompoundDrawablePadding(0);
            return;
        }
        Drawable drawable = AppCompatResources.getDrawable(textView.getContext(), R.drawable.ic_like_filled);
        if (drawable == null) {
            textView.setCompoundDrawablesRelative(null, null, null, null);
            textView.setCompoundDrawablePadding(0);
            return;
        }
        Drawable tinted = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(tinted, tm.color(ThemeKeys.ACCENT_PRIMARY));
        int size = Math.round(16f * textView.getResources().getDisplayMetrics().density);
        tinted.setBounds(0, 0, size, size);
        textView.setCompoundDrawablesRelative(tinted, null, null, null);
        textView.setCompoundDrawablePadding(Math.round(6f * textView.getResources().getDisplayMetrics().density));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardRoot;
        final TextView tvPosicion;
        final TextView tvTipoChip;
        final ShapeableImageView imgRankingRect;
        final ShapeableImageView imgRankingCircle;
        final LinearLayout layoutTextos;
        final TextView tvNombre;
        final TextView tvSubtitulo;
        final LinearLayout layoutAutorSecundario;
        final ShapeableImageView imgAutorSecundario;
        final TextView tvAutorSecundario;
        final TextView tvTotal;
        final TextView tvDetalle;
        final LinearProgressIndicator progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardRankingRoot);
            tvPosicion = itemView.findViewById(R.id.tvRankingPosicion);
            tvTipoChip = itemView.findViewById(R.id.tvRankingTipoChip);
            imgRankingRect = itemView.findViewById(R.id.imgRankingRect);
            imgRankingCircle = itemView.findViewById(R.id.imgRankingCircle);
            layoutTextos = itemView.findViewById(R.id.layoutRankingTextos);
            tvNombre = itemView.findViewById(R.id.tvRankingNombre);
            tvSubtitulo = itemView.findViewById(R.id.tvRankingSubtitulo);
            layoutAutorSecundario = itemView.findViewById(R.id.layoutRankingAutorSecundario);
            imgAutorSecundario = itemView.findViewById(R.id.imgRankingAutorSecundario);
            tvAutorSecundario = itemView.findViewById(R.id.tvRankingAutorSecundario);
            tvTotal = itemView.findViewById(R.id.tvRankingTotal);
            tvDetalle = itemView.findViewById(R.id.tvRankingDetalle);
            progressBar = itemView.findViewById(R.id.progressRankingItem);
        }
    }
}

