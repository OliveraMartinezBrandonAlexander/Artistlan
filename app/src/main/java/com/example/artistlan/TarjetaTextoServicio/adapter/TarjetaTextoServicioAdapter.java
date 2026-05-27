package com.example.artistlan.TarjetaTextoServicio.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Fragments.DialogReportarContenido;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.LikeUiHelper;
import com.example.artistlan.utils.ReporteUiPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TarjetaTextoServicioAdapter extends RecyclerView.Adapter<TarjetaTextoServicioAdapter.ViewHolder> {
    private static final String PAYLOAD_LIKE_STATE = "payload_like_state";

    public interface OnLikeClickListener { void onLikeClick(TarjetaTextoServicioItem servicioItem, int position); }
    public interface OnEditClickListener { void onEditClick(TarjetaTextoServicioItem servicioItem, int position); }
    public interface OnDeleteClickListener { void onDeleteClick(TarjetaTextoServicioItem servicioItem, int position); }
    public interface OnAuthorClickListener { void onAuthorClick(TarjetaTextoServicioItem servicioItem, int position); }
    public interface OnCardClickListener { void onCardClick(TarjetaTextoServicioItem servicioItem, int position); }

    private static final long LIKE_BUTTON_COOLDOWN_MS = 500L;
    private OnLikeClickListener onLikeClickListener;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnAuthorClickListener onAuthorClickListener;
    private OnCardClickListener onCardClickListener;
    private final List<TarjetaTextoServicioItem> listaServicios;
    private final List<TarjetaTextoServicioItem> listaOriginal;
    private final Context context;
    private int tarjetaExpandida = -1;
    private int lastAnimatedPosition = -1;
    private long lastAuthorClickMs = 0L;
    private Integer currentUserId;
    private boolean entryAnimationsEnabled = true;
    private boolean portfolioHeaderEnabled = false;

    public TarjetaTextoServicioAdapter(List<TarjetaTextoServicioItem> listaServicios, Context context) {
        this.listaServicios = listaServicios;
        this.listaOriginal = new ArrayList<>(listaServicios);
        this.context = context;
    }

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) { this.onLikeClickListener = onLikeClickListener; }
    public void setOnEditClickListener(OnEditClickListener onEditClickListener) { this.onEditClickListener = onEditClickListener; notifyDataSetChanged(); }
    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) { this.onDeleteClickListener = onDeleteClickListener; notifyDataSetChanged(); }
    public void setOnAuthorClickListener(OnAuthorClickListener onAuthorClickListener) { this.onAuthorClickListener = onAuthorClickListener; }
    public void setOnCardClickListener(OnCardClickListener onCardClickListener) { this.onCardClickListener = onCardClickListener; }
    public void setEntryAnimationsEnabled(boolean enabled) { this.entryAnimationsEnabled = enabled; }
    public void setPortfolioHeaderEnabled(boolean enabled) {
        this.portfolioHeaderEnabled = enabled;
        notifyDataSetChanged();
    }
    public void setCurrentUserId(Integer currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoservicio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        TarjetaTextoServicioItem servicio = listaServicios.get(position);
        resetItemVisualState(holder.itemView);
        if (entryAnimationsEnabled) {
            animateFeedEntry(holder, position);
        }
        ThemeApplier.applyTextPrimary(holder.titulo, tm);
        ThemeApplier.applyTextSecondary(holder.autor, tm);
        ThemeApplier.applyTextSecondary(holder.descripcion, tm);
        ThemeApplier.applyTextSecondary(holder.contacto, tm);
        ThemeApplier.applyTextSecondary(holder.tipoContacto, tm);
        ThemeApplier.applyTextSecondary(holder.tecnicas, tm);
        ThemeApplier.applyTextPrimary(holder.precioRango, tm);
        ThemeApplier.applyTextSecondary(holder.categoria, tm);
        ThemeApplier.applyTextPrimary(holder.tvPublicationTitle, tm);
        CardThemeHelper.applyPrimaryBubbleButton(holder.btnContactar, tm);
        CardThemeHelper.applySecondaryBubbleButton(holder.btnReportarServicio, tm);
        CardThemeHelper.applyFlatCard(holder.layoutServicioCard, tm);
        CardThemeHelper.applyFilterSurface(holder.publicationHeader, tm);
        CardThemeHelper.applyFilterButton(holder.btnMoreOptions, tm);
        CardThemeHelper.applyChip(holder.categoria, tm);
        CardThemeHelper.applyChip(holder.precioRango, tm);
        CardThemeHelper.applyChip(holder.tvBadgeServicio, tm);

        holder.autor.setText(safe(servicio.getAutor(), "Autor"));
        holder.titulo.setText(safe(servicio.getTitulo(), "Servicio"));
        holder.descripcion.setText("Descripción: " + safe(servicio.getDescripcion(), "Sin descripción"));
        holder.contacto.setText("Contacto: " + safe(servicio.getContacto(), "No disponible"));
        holder.tipoContacto.setText("Tipo de contacto: " + safe(servicio.getTipoContacto(), "N/A"));
        holder.tecnicas.setText("Técnicas: " + safe(servicio.getTecnicas(), "No especificadas"));
        holder.precioRango.setText(formatearPrecioRango(servicio.getPrecioMin(), servicio.getPrecioMax()));
        holder.categoria.setText("Categoría: " + safe(servicio.getCategoria(), "Sin categoría"));
        boolean esServicioPropio = servicio.getIdUsuario() != null
                && getCurrentUserId() != null
                && servicio.getIdUsuario().equals(getCurrentUserId());
        holder.tvBadgeServicio.setVisibility(esServicioPropio ? View.VISIBLE : View.GONE);
        holder.tvBadgeServicio.setText("Mi servicio");
        configurarEncabezadoPublicacion(holder, servicio);
        bindLikeUi(holder, servicio, true);
        holder.btnLike.setOnClickListener(v -> {
            if (onLikeClickListener == null) {
                return;
            }
            animatePress(v);
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                TarjetaTextoServicioItem currentServicio = listaServicios.get(adapterPosition);
                boolean favoritoAntesDelClick = currentServicio.isFavorito();
                int likesAntesDelClick = currentServicio.getLikes();

                onLikeClickListener.onLikeClick(currentServicio, adapterPosition);

                if (favoritoAntesDelClick != currentServicio.isFavorito()
                        || likesAntesDelClick != currentServicio.getLikes()) {
                    animateLikeButton(holder.btnLike, currentServicio.isFavorito());
                }
            }
        });

        String fotoAutor = servicio.getFotoPerfilAutor();
        if (!isRemoteUrlValida(fotoAutor)) {
            Glide.with(holder.itemView.getContext()).clear(holder.imgAutor);
            holder.imgAutor.setImageResource(R.drawable.fotoperfilprueba);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(fotoAutor.trim())
                    .placeholder(R.drawable.fotoperfilprueba)
                    .error(R.drawable.fotoperfilprueba)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .circleCrop()
                    .into(holder.imgAutor);
        }

        holder.imgAutor.setOnClickListener(v -> {
            animatePress(v);
            dispatchAuthorClick(holder);
        });
        holder.autor.setOnClickListener(v -> dispatchAuthorClick(holder));
        boolean expandido = (tarjetaExpandida == position);
        holder.descripcion.setMaxLines(expandido ? Integer.MAX_VALUE : 2);
        animarVista(holder.expandedSection, expandido);
        configurarMenuOpciones(holder);

        holder.btnContactar.setVisibility(esServicioPropio ? View.GONE : View.VISIBLE);
        holder.btnContactar.setEnabled(!esServicioPropio && !TextUtils.isEmpty(servicio.getContacto()));
        holder.btnContactar.setOnClickListener(v -> {
            animatePress(v);
            contactar(servicio);
        });
        configurarBotonReportar(holder, servicio);

        View.OnClickListener toggleExpandListener = v -> {
            int previous = tarjetaExpandida;
            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) return;
            if (onCardClickListener != null) {
                onCardClickListener.onCardClick(listaServicios.get(currentPosition), currentPosition);
                return;
            }
            if (previous == currentPosition) tarjetaExpandida = -1;
            else {
                tarjetaExpandida = currentPosition;
                if (previous != -1) notifyItemChanged(previous);
            }
            notifyItemChanged(currentPosition);
        };
        holder.itemView.setOnClickListener(toggleExpandListener);
        holder.layoutServicioCard.setOnClickListener(toggleExpandListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_LIKE_STATE)) {
            if (position < 0 || position >= listaServicios.size()) {
                return;
            }
            bindLikeUi(holder, listaServicios.get(position), false);
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    private void bindLikeUi(@NonNull ViewHolder holder, @NonNull TarjetaTextoServicioItem servicio, boolean forceVisualState) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        LikeUiHelper.bind(
                holder.btnLike,
                holder.likes,
                servicio.isFavorito(),
                servicio.getLikes(),
                tm.color(ThemeKeys.LIKE_ACTIVE),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
    }

    private void dispatchAuthorClick(@NonNull ViewHolder holder) {
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION || onAuthorClickListener == null) {
            return;
        }
        long ahora = SystemClock.elapsedRealtime();
        if (ahora - lastAuthorClickMs < LIKE_BUTTON_COOLDOWN_MS) {
            return;
        }
        lastAuthorClickMs = ahora;
        View authorClickView = holder.imgAutor != null ? holder.imgAutor : holder.autor;
        if (authorClickView != null) {
            authorClickView.setEnabled(false);
            authorClickView.postDelayed(() -> authorClickView.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
        }
        onAuthorClickListener.onAuthorClick(listaServicios.get(adapterPosition), adapterPosition);
    }

    private String formatearPrecioRango(Double min, Double max) {
        if (min == null && max == null) return "Precio: A convenir";
        if (min != null && max != null) return String.format(Locale.getDefault(), "Precio: $%,.2f - $%,.2f", min, max);
        if (min != null) return String.format(Locale.getDefault(), "Precio desde: $%,.2f", min);
        return String.format(Locale.getDefault(), "Precio hasta: $%,.2f", max);
    }

    private void contactar(TarjetaTextoServicioItem servicio) {
        String tipo = safe(servicio.getTipoContacto(), "OTRO").toUpperCase(Locale.ROOT);
        String contacto = safe(servicio.getContacto(), "").trim();
        if (contacto.isEmpty()) {
            Toast.makeText(context, "Este servicio no tiene contacto disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent;
            switch (tipo) {
                case "EMAIL":
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + contacto));
                    break;
                case "WHATSAPP":
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + contacto.replaceAll("[^0-9]", "")));
                    break;
                case "TELEFONO":
                    intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contacto));
                    break;
                case "INSTAGRAM":
                    String user = contacto.startsWith("@") ? contacto.substring(1) : contacto;
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + user));
                    break;
                default:
                    intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_TEXT, "Contacto de servicio: " + contacto);
                    break;
            }
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir la app de contacto", Toast.LENGTH_SHORT).show();
        }
    }
    @Override public int getItemCount() { return listaServicios != null ? listaServicios.size() : 0; }

    private String safe(String v, String def) { return (v == null || v.trim().isEmpty()) ? def : v; }

    private boolean isRemoteUrlValida(String valor) {
        if (valor == null) {
            return false;
        }
        String trimmed = valor.trim();
        return !trimmed.isEmpty() && (trimmed.startsWith("http://") || trimmed.startsWith("https://"));
    }

    private void configurarBotonReportar(ViewHolder holder, TarjetaTextoServicioItem servicio) {
        boolean mostrarReportar = puedeReportarseServicio(servicio);
        holder.btnReportarServicio.setVisibility(mostrarReportar ? View.VISIBLE : View.GONE);
        if (!mostrarReportar) {
            holder.btnReportarServicio.setOnClickListener(null);
            return;
        }

        holder.btnReportarServicio.setOnClickListener(v -> {
            animatePress(v);
            mostrarDialogoReporteServicio(servicio);
        });
    }

    private boolean puedeReportarseServicio(TarjetaTextoServicioItem servicio) {
        Integer usuarioActual = getCurrentUserId();
        String rolActual = getCurrentUserRole();
        if (!ReporteUiPermissions.puedeMostrarReportar(usuarioActual, rolActual)) {
            return false;
        }
        if (servicio == null || servicio.getIdServicio() == null || servicio.getIdServicio() <= 0) {
            return false;
        }
        return servicio.getIdUsuario() == null || !servicio.getIdUsuario().equals(usuarioActual);
    }

    private Integer getCurrentUserId() {
        if (currentUserId != null && currentUserId > 0) {
            return currentUserId;
        }
        return ReporteUiPermissions.resolveCurrentUserId(context);
    }

    private String getCurrentUserRole() {
        return ReporteUiPermissions.resolveCurrentUserRole(context);
    }

    private void mostrarDialogoReporteServicio(TarjetaTextoServicioItem servicio) {
        Integer usuarioActual = getCurrentUserId();
        if (!(context instanceof FragmentActivity) || usuarioActual == null || usuarioActual <= 0) {
            Toast.makeText(context, "No se pudo abrir el formulario de reporte.", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogReportarContenido dialog = DialogReportarContenido.newInstance(
                "SERVICIO",
                servicio.getIdServicio(),
                usuarioActual,
                servicio.getTitulo()
        );
        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "DialogReportarContenido");
    }


    private void configurarMenuOpciones(ViewHolder holder) {
        boolean mostrarMenu = portfolioHeaderEnabled && (onEditClickListener != null || onDeleteClickListener != null);
        holder.btnMoreOptions.setVisibility(mostrarMenu ? View.VISIBLE : View.GONE);
        if (!mostrarMenu) { holder.btnMoreOptions.setOnClickListener(null); return; }
        holder.btnMoreOptions.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            mostrarPopupOpciones(holder.btnMoreOptions, onEditClickListener != null, onDeleteClickListener != null, opcion -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return;
                }

                TarjetaTextoServicioItem servicio = listaServicios.get(currentPosition);
                if (opcion == 1 && onEditClickListener != null) {
                    onEditClickListener.onEditClick(servicio, currentPosition);
                    return;
                }
                if (opcion == 2 && onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(servicio, currentPosition);
                }
            });
        });
    }

    private void configurarEncabezadoPublicacion(@NonNull ViewHolder holder, @NonNull TarjetaTextoServicioItem servicio) {
        holder.publicationHeader.setVisibility(portfolioHeaderEnabled ? View.VISIBLE : View.GONE);
        holder.tvPublicationTitle.setText("Editar: " + safe(servicio.getTitulo(), "Sin título"));
    }

    private interface OpcionMenuCallback {
        void onSeleccion(int opcion);
    }

    private void mostrarPopupOpciones(@NonNull View anchor, boolean puedeEditar, boolean puedeEliminar, @NonNull OpcionMenuCallback callback) {
        ThemeManager tm = new ThemeManager(anchor.getContext());
        LinearLayout container = new LinearLayout(anchor.getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPx(anchor, 8), dpToPx(anchor, 8), dpToPx(anchor, 8), dpToPx(anchor, 8));
        CardThemeHelper.applyFilterSurface(container, tm);

        PopupWindow popupWindow = new PopupWindow(container, dpToPx(anchor, 148), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(dpToPx(anchor, 10));

        if (puedeEditar) {
            container.addView(crearBotonMenu(anchor, tm, "Editar", () -> {
                popupWindow.dismiss();
                callback.onSeleccion(1);
            }));
        }
        if (puedeEliminar) {
            Button borrar = crearBotonMenu(anchor, tm, "Borrar", () -> {
                popupWindow.dismiss();
                callback.onSeleccion(2);
            });
            if (container.getChildCount() > 0) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(anchor, 38)
                );
                params.topMargin = dpToPx(anchor, 6);
                borrar.setLayoutParams(params);
            }
            container.addView(borrar);
        }

        popupWindow.showAsDropDown(anchor, -dpToPx(anchor, 112), dpToPx(anchor, 6));
    }

    private Button crearBotonMenu(@NonNull View anchor, @NonNull ThemeManager tm, @NonNull String texto, @NonNull Runnable action) {
        Button button = new Button(anchor.getContext());
        button.setText(texto);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dpToPx(anchor, 10), 0, dpToPx(anchor, 10), 0);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(anchor, 38)
        ));
        CardThemeHelper.applyFilterActionButton(button, tm);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private int dpToPx(@NonNull View view, int dp) {
        return Math.round(dp * view.getResources().getDisplayMetrics().density);
    }

    private void animateLikeButton(ImageButton btnLike, boolean targetLiked) {
        ThemeManager tm = new ThemeManager(btnLike.getContext());
        LikeUiHelper.animateChange(
                btnLike,
                targetLiked,
                tm.color(ThemeKeys.LIKE_ACTIVE),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
    }

    public void filtrar(String texto) {
        List<TarjetaTextoServicioItem> listaFiltrada = new ArrayList<>();
        if (texto == null || texto.isEmpty()) listaFiltrada.addAll(listaOriginal);
        else {
            texto = texto.toLowerCase();
            for (TarjetaTextoServicioItem servicio : listaOriginal){
                if (servicio.getTitulo() != null && servicio.getTitulo().toLowerCase().contains(texto)) listaFiltrada.add(servicio);
            }
        }
        int oldSize = listaServicios.size();
        listaServicios.clear();
        listaServicios.addAll(listaFiltrada);
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!listaFiltrada.isEmpty()) notifyItemRangeInserted(0, listaFiltrada.size());
    }

    public void actualizarLista(List<TarjetaTextoServicioItem> nuevaLista) {
        int oldSize = listaServicios.size();
        listaOriginal.clear();
        listaOriginal.addAll(nuevaLista);
        listaServicios.clear();
        listaServicios.addAll(nuevaLista);
        tarjetaExpandida = -1;
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize);
        if (!nuevaLista.isEmpty()) notifyItemRangeInserted(0, nuevaLista.size());
    }

    public void agregarItems(List<TarjetaTextoServicioItem> nuevosItems) {
        if (nuevosItems == null || nuevosItems.isEmpty()) {
            return;
        }
        int start = listaServicios.size();
        listaServicios.addAll(nuevosItems);
        listaOriginal.addAll(nuevosItems);
        notifyItemRangeInserted(start, nuevosItems.size());
    }

    public void removeItemAt(int position) {
        if (position < 0 || position >= listaServicios.size()) return;
        TarjetaTextoServicioItem item = listaServicios.remove(position);
        listaOriginal.remove(item);
        if (tarjetaExpandida == position) {
            tarjetaExpandida = -1;
        } else if (tarjetaExpandida > position) {
            tarjetaExpandida--;
        }
        notifyItemRemoved(position);
    }

    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < listaServicios.size()) notifyItemChanged(position);
    }

    public void notifyLikeChangedPartial(int position) {
        if (position >= 0 && position < listaServicios.size()) {
            notifyItemChanged(position, PAYLOAD_LIKE_STATE);
        }
    }


    private void animateFeedEntry(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position <= lastAnimatedPosition) {
            return;
        }
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(24f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(220)
                .start();
        lastAnimatedPosition = position;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        resetItemVisualState(holder.itemView);
    }

    private void resetItemVisualState(@NonNull View view) {
        view.animate().cancel();
        view.clearAnimation();
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    private void animatePress(@NonNull View view) {
        view.animate().cancel();
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(
                () -> view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        ).start();
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

        TextView titulo, descripcion, contacto, tipoContacto, tecnicas, autor, categoria, precioRango, likes, tvBadgeServicio, tvPublicationTitle;
        ImageView imgAutor;
        ImageButton btnLike;
        ImageButton btnMoreOptions;
        View expandedSection, layoutServicioCard, publicationHeader;
        Button btnContactar, btnReportarServicio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            autor = itemView.findViewById(R.id.autor);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            contacto = itemView.findViewById(R.id.contacto);
            tipoContacto = itemView.findViewById(R.id.tipoContacto);
            tecnicas = itemView.findViewById(R.id.tecnicas);
            categoria = itemView.findViewById(R.id.categoria);
            precioRango = itemView.findViewById(R.id.precioRango);
            tvBadgeServicio = itemView.findViewById(R.id.tvBadgeServicio);
            tvPublicationTitle = itemView.findViewById(R.id.tvPublicationTitle);
            imgAutor = itemView.findViewById(R.id.imgAutor);
            likes = itemView.findViewById(R.id.likes);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            expandedSection = itemView.findViewById(R.id.expanded_section);
            layoutServicioCard = itemView.findViewById(R.id.layoutServicioCard);
            publicationHeader = itemView.findViewById(R.id.publicationHeader);
            btnContactar = itemView.findViewById(R.id.btnContactar);
            btnReportarServicio = itemView.findViewById(R.id.btnReportarServicio);
        }
    }
}
