package com.example.artistlan.Carrusel.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.artistlan.Carrusel.model.ObraCarruselItem;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.LikeUiHelper;
import com.example.artistlan.utils.LikeStateManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Locale;

public class CarruselAdapter extends RecyclerView.Adapter<CarruselAdapter.CarruselViewHolder> {
    private static final String PAYLOAD_LIKE_STATE = "payload_like_state";

    public interface OnCarruselActionListener {
        void onLike(ObraCarruselItem item, int position);
        void onAuthor(ObraCarruselItem item, int position);
        void onExpandedChanged(boolean expanded);
    }

    private final List<ObraCarruselItem> lista;
    private final Context context;
    private Long expandedItemKey;
    private OnCarruselActionListener onCarruselActionListener;

    public CarruselAdapter(List<ObraCarruselItem> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }
    public void setOnCarruselActionListener(OnCarruselActionListener listener) {
        this.onCarruselActionListener = listener;
    }

    @NonNull
    @Override
    public CarruselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrusel, parent, false);
        return new CarruselViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull CarruselViewHolder holder, int position) {
        ObraCarruselItem item = lista.get(position);
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        aplicarTema(holder, tm);

        // Imagen de la obra
        if (item.getImagenUrl() != null && !item.getImagenUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImagenUrl())
                    .placeholder(item.getImagen())
                    .error(item.getImagen())
                    .apply(RequestOptions.fitCenterTransform())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(item.getImagen());
        }

        // Foto de perfil del autor
        if (item.getAutorFotoUrl() != null && !item.getAutorFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getAutorFotoUrl())
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgAutor);
        } else {
            holder.imgAutor.setImageResource(R.drawable.fotoperfilprueba);
        }

        holder.tvTitulo.setText(item.getTitulo());
        holder.tvDescripcion.setText(item.getDescripcion());
        holder.tvAutor.setText(item.getAutor());
        resetDoubleTapHeart(holder);
        LikeStateManager.applyTo(item);
        bindExpandedUi(holder, item, position, tm);
        bindLikeUi(holder, item, true, tm);
        configureImageGestures(holder);
        animateShine(holder);
        View.OnClickListener expandClickListener = v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                toggleExpanded(adapterPosition);
            }
        };
        holder.itemView.setOnClickListener(expandClickListener);
        holder.layoutCarruselCard.setOnClickListener(expandClickListener);
        holder.imgAutor.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (onCarruselActionListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onCarruselActionListener.onAuthor(lista.get(adapterPosition), adapterPosition);
            }
        });
        holder.btnLikeCarrusel.setOnClickListener(v -> {
            LikeUiHelper.animatePress(v);
            triggerLike(holder, false);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull CarruselViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_LIKE_STATE)) {
            if (position < 0 || position >= lista.size()) {
                return;
            }
            bindLikeUi(holder, lista.get(position), false, new ThemeManager(holder.itemView.getContext()));
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < lista.size()) {
            notifyItemChanged(position);
        }
    }

    private void toggleExpanded(int adapterPosition) {
        ObraCarruselItem item = lista.get(adapterPosition);
        Long previousKey = expandedItemKey;
        Long selectedKey = resolveItemKey(item, adapterPosition);

        if (selectedKey.equals(expandedItemKey)) {
            expandedItemKey = null;
        } else {
            expandedItemKey = selectedKey;
        }
        dispatchExpandedState();

        if (previousKey != null && !previousKey.equals(selectedKey)) {
            int previousPosition = findPositionByKey(previousKey);
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
        }
        notifyItemChanged(adapterPosition);
    }

    private void bindExpandedUi(@NonNull CarruselViewHolder holder, @NonNull ObraCarruselItem item, int position, @NonNull ThemeManager tm) {
        boolean expanded = resolveItemKey(item, position).equals(expandedItemKey);
        holder.layoutInfoExpandida.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.tvDescripcion.setMaxLines(expanded ? 2 : 1);
        holder.tvInfoCompleta.setText(resolveInfoText(item, tm));
        holder.layoutCarruselCard.animate().cancel();
        holder.layoutCarruselCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(220)
                .start();
        if (expanded) {
            holder.layoutInfoExpandida.setAlpha(0f);
            holder.layoutInfoExpandida.setTranslationY(-8f);
            holder.layoutInfoExpandida.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(180)
                    .start();
        } else {
            holder.layoutInfoExpandida.animate().cancel();
            holder.layoutInfoExpandida.setAlpha(1f);
            holder.layoutInfoExpandida.setTranslationY(0f);
        }
    }

    public void collapseExpanded() {
        if (expandedItemKey == null) {
            return;
        }
        Long previousKey = expandedItemKey;
        expandedItemKey = null;
        dispatchExpandedState();
        int previousPosition = findPositionByKey(previousKey);
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        } else {
            notifyDataSetChanged();
        }
    }

    public boolean hasExpandedItem() {
        return expandedItemKey != null;
    }

    public void updateLikeStateById(int idObra, boolean liked, int likesCount) {
        for (int i = 0; i < lista.size(); i++) {
            ObraCarruselItem item = lista.get(i);
            if (item.getIdObra() != null && item.getIdObra() == idObra) {
                item.setUserLiked(liked);
                item.setLikesCount(Math.max(0, likesCount));
                notifyLikeChangedPartial(i);
            }
        }
    }

    public void refreshTheme() {
        notifyDataSetChanged();
    }

    private int findPositionByKey(@NonNull Long key) {
        for (int i = 0; i < lista.size(); i++) {
            if (key.equals(resolveItemKey(lista.get(i), i))) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    private Long resolveItemKey(@NonNull ObraCarruselItem item, int position) {
        if (item.getIdObra() != null && item.getIdObra() > 0) {
            return item.getIdObra().longValue();
        }
        return -1000L - position;
    }

    private void dispatchExpandedState() {
        if (onCarruselActionListener != null) {
            onCarruselActionListener.onExpandedChanged(expandedItemKey != null);
        }
    }

    @NonNull
    private CharSequence resolveInfoText(@NonNull ObraCarruselItem item, @NonNull ThemeManager tm) {
        String descripcion = item.getDescripcion() == null || item.getDescripcion().trim().isEmpty()
                ? "Sin descripcion disponible"
                : item.getDescripcion().trim();
        String autor = item.getAutor() == null || item.getAutor().trim().isEmpty()
                ? "Autor no disponible"
                : item.getAutor().trim();
        int labelColor = tm.color(ThemeKeys.ACCENT_PRIMARY_LIGHT);
        int valueColor = tm.color(ThemeKeys.TEXT_PRIMARY);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        appendField(builder, "Descripción: ", descripcion, labelColor, valueColor);
        appendField(builder, "Estado: ", safeText(item.getEstado(), "N/A"), labelColor, valueColor);
        appendField(builder, "Técnica: ", safeText(item.getTecnicas(), "N/A"), labelColor, valueColor);
        appendField(builder, "Medidas: ", formatearMedidas(item.getMedidas()), labelColor, valueColor);
        appendField(builder, "Precio: ", formatearPrecio(item.getPrecio()), labelColor, valueColor);
        appendField(builder, "Tipo de arte: ", safeText(item.getTipoArte(), "N/A"), labelColor, valueColor);
        appendField(builder, "Autor: ", autor, labelColor, valueColor);
        appendField(builder, "Likes: ", String.valueOf(item.getLikesCount()), labelColor, valueColor);
        return builder;
    }

    private void appendField(
            @NonNull SpannableStringBuilder builder,
            @NonNull String label,
            @NonNull String value,
            int labelColor,
            int valueColor
    ) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        int labelStart = builder.length();
        builder.append(label);
        builder.setSpan(new ForegroundColorSpan(labelColor), labelStart, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int valueStart = builder.length();
        builder.append(value);
        builder.setSpan(new ForegroundColorSpan(valueColor), valueStart, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void notifyLikeChangedPartial(int position) {
        if (position >= 0 && position < lista.size()) {
            notifyItemChanged(position, PAYLOAD_LIKE_STATE);
        }
    }

    private void bindLikeUi(
            @NonNull CarruselViewHolder holder,
            @NonNull ObraCarruselItem item,
            boolean forceVisualState,
            @NonNull ThemeManager tm
    ) {
        LikeUiHelper.bind(
                holder.btnLikeCarrusel,
                holder.tvLikesCarrusel,
                item.isUserLiked(),
                item.getLikesCount(),
                tm.color(ThemeKeys.LIKE_ACTIVE),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
    }

    private void aplicarTema(@NonNull CarruselViewHolder holder, @NonNull ThemeManager tm) {
        ThemeApplier.applyCardContainer(holder.layoutCarruselCard, tm);
        holder.tvTitulo.setTextColor(tm.color(ThemeKeys.ACCENT_PRIMARY_LIGHT));
        holder.tvAutor.setTextColor(tm.color(ThemeKeys.ACCENT_SECONDARY_LIGHT));
        ThemeApplier.applyTextPrimary(holder.tvLikesCarrusel, tm);
        ThemeApplier.applyTextPrimary(holder.tvInfoCompleta, tm);
        ThemeApplier.applyTextSecondary(holder.tvDescripcion, tm);
        if (holder.imgObra.getBackground() != null) {
            holder.imgObra.getBackground().setColorFilter(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), PorterDuff.Mode.SRC_ATOP);
        }
        holder.imgObra.setStrokeColor(ColorStateList.valueOf(tm.color(ThemeKeys.ACCOUNT_GLASS_STROKE)));
        holder.imgObra.setStrokeWidth(1f);
        holder.imgAutor.setStrokeColor(ColorStateList.valueOf(tm.color(ThemeKeys.ACCOUNT_GLASS_STROKE)));
        CardThemeHelper.applyFlatCard(holder.layoutCarruselCard, tm);
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private String formatearMedidas(String medidas) {
        if (medidas == null || medidas.trim().isEmpty()) {
            return "N/A";
        }
        String limpia = medidas.trim();
        return limpia.toLowerCase(Locale.ROOT).endsWith("cm") ? limpia : limpia + " cm";
    }

    private String formatearPrecio(Double precio) {
        if (precio == null || precio <= 0) {
            return "N/A";
        }
        return "$ " + String.format(Locale.getDefault(), "%,.2f", precio);
    }

    private void configureImageGestures(@NonNull CarruselViewHolder holder) {
        GestureDetector detector = new GestureDetector(
                holder.imgObra.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        int adapterPosition = holder.getBindingAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            toggleExpanded(adapterPosition);
                        }
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
                        LikeUiHelper.animateInstagramHeart(
                                holder.imgDoubleTapHeartCarrusel,
                                tm.color(ThemeKeys.LIKE_ACTIVE)
                        );
                        triggerLike(holder, true);
                        return true;
                    }
                }
        );
        holder.imgObra.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return true;
        });
    }

    private void triggerLike(@NonNull CarruselViewHolder holder, boolean onlyLikeIfNeeded) {
        int adapterPosition = holder.getBindingAdapterPosition();
        if (onCarruselActionListener == null || adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }

        ObraCarruselItem currentItem = lista.get(adapterPosition);
        LikeStateManager.applyTo(currentItem);
        if (onlyLikeIfNeeded && currentItem.isUserLiked()) {
            ThemeManager tm = new ThemeManager(holder.itemView.getContext());
            LikeUiHelper.animateChange(
                    holder.btnLikeCarrusel,
                    true,
                    tm.color(ThemeKeys.LIKE_ACTIVE),
                    tm.color(ThemeKeys.TEXT_SECONDARY)
            );
            return;
        }

        boolean likedAntesDelClick = currentItem.isUserLiked();
        int likesAntesDelClick = currentItem.getLikesCount();
        onCarruselActionListener.onLike(currentItem, adapterPosition);
        if (likedAntesDelClick != currentItem.isUserLiked()
                || likesAntesDelClick != currentItem.getLikesCount()) {
            ThemeManager tm = new ThemeManager(holder.itemView.getContext());
            LikeUiHelper.animateChange(
                    holder.btnLikeCarrusel,
                    currentItem.isUserLiked(),
                    tm.color(ThemeKeys.LIKE_ACTIVE),
                    tm.color(ThemeKeys.TEXT_SECONDARY)
            );
        }
    }

    private void animateShine(@NonNull CarruselViewHolder holder) {
        holder.viewCarouselShine.animate().cancel();
        holder.viewCarouselShine.setAlpha(0f);
        holder.viewCarouselShine.postDelayed(() -> {
            if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            float distance = Math.max(holder.itemView.getWidth(), holder.itemView.getHeight()) * 0.72f;
            holder.viewCarouselShine.setTranslationX(-distance);
            holder.viewCarouselShine.setTranslationY(distance * 0.36f);
            holder.viewCarouselShine.animate()
                    .alpha(0.18f)
                    .translationX(distance)
                    .translationY(-distance * 0.36f)
                    .setDuration(1500)
                    .withEndAction(() -> holder.viewCarouselShine.animate()
                            .alpha(0f)
                            .setDuration(240)
                            .start())
                    .start();
        }, 420);
    }

    private void resetDoubleTapHeart(@NonNull CarruselViewHolder holder) {
        if (holder.imgDoubleTapHeartCarrusel == null) {
            return;
        }
        holder.imgDoubleTapHeartCarrusel.animate().cancel();
        holder.imgDoubleTapHeartCarrusel.clearAnimation();
        holder.imgDoubleTapHeartCarrusel.setVisibility(View.GONE);
        holder.imgDoubleTapHeartCarrusel.setAlpha(0f);
        holder.imgDoubleTapHeartCarrusel.setScaleX(1f);
        holder.imgDoubleTapHeartCarrusel.setScaleY(1f);
    }

    public static class CarruselViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgObra, imgAutor;
        ImageView imgDoubleTapHeartCarrusel;
        TextView tvTitulo, tvDescripcion, tvAutor, tvLikesCarrusel, tvInfoCompleta;
        View layoutCarruselCard, layoutInfoExpandida;
        View viewCarouselShine;
        ImageButton btnLikeCarrusel;

        public CarruselViewHolder(@NonNull View itemView) {
            super(itemView);
            imgObra       = itemView.findViewById(R.id.imgObra);
            imgDoubleTapHeartCarrusel = itemView.findViewById(R.id.imgDoubleTapHeartCarrusel);
            layoutCarruselCard = itemView.findViewById(R.id.layoutCarruselCard);
            imgAutor      = itemView.findViewById(R.id.imgAutor);
            tvTitulo      = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvAutor       = itemView.findViewById(R.id.tvAutor);
            tvLikesCarrusel = itemView.findViewById(R.id.tvLikesCarrusel);
            tvInfoCompleta = itemView.findViewById(R.id.tvInfoCompletaCarrusel);
            layoutInfoExpandida = itemView.findViewById(R.id.layoutInfoExpandidaCarrusel);
            viewCarouselShine = itemView.findViewById(R.id.viewCarouselShine);
            btnLikeCarrusel = itemView.findViewById(R.id.btnLikeCarrusel);
        }
    }
}
