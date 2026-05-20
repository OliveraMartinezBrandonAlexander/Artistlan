package com.example.artistlan.TarjetaTextoObra.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Fragments.DialogReportarContenido;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.LikeUiHelper;
import com.example.artistlan.utils.LikeStateManager;
import com.example.artistlan.utils.ReporteUiPermissions;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TarjetaTextoObraAdapter extends RecyclerView.Adapter<TarjetaTextoObraAdapter.ViewHolder> {
    private static final String TAG_REPORTE_DEBUG = "ReporteUiDebug";
    private static final boolean ENABLE_REPORTE_UI_DEBUG_LOGS = false;
    private static final String PAYLOAD_LIKE_STATE = "payload_like_state";

    public interface OnLikeClickListener {
        void onLikeClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnPrimaryActionClickListener {
        void onPrimaryActionClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnSecondaryActionClickListener {
        void onSecondaryActionClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnAuthorClickListener {
        void onAuthorClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnCardClickListener {
        void onCardClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnComprarClickListener {
        void onComprarClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnEditClickListener {
        void onEditClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(TarjetaTextoObraItem obraItem, int position);
    }

    private static final long LIKE_BUTTON_COOLDOWN_MS = 500L;

    private OnLikeClickListener onLikeClickListener;
    private OnPrimaryActionClickListener onPrimaryActionClickListener;
    private OnSecondaryActionClickListener onSecondaryActionClickListener;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnAuthorClickListener onAuthorClickListener;
    private OnCardClickListener onCardClickListener;

    private final List<TarjetaTextoObraItem> listaObras;
    private final List<TarjetaTextoObraItem> listaOriginal;
    private final Context context;
    private final Set<Integer> ownedObraIds = new HashSet<>();

    private Integer currentUserId;
    private ModoTarjetaObra modoTarjeta;
    private int tarjetaExpandida = -1;
    private int lastAnimatedPosition = -1;
    private boolean entryAnimationsEnabled = true;

    public TarjetaTextoObraAdapter(List<TarjetaTextoObraItem> listaObras, Context context) {
        this(listaObras, context, ModoTarjetaObra.EXPLORAR);
    }

    public TarjetaTextoObraAdapter(
            List<TarjetaTextoObraItem> listaObras,
            Context context,
            ModoTarjetaObra modoTarjeta
    ) {
        this.listaObras = listaObras != null ? listaObras : new ArrayList<>();
        this.listaOriginal = new ArrayList<>(this.listaObras);
        this.context = context;
        this.modoTarjeta = modoTarjeta != null ? modoTarjeta : ModoTarjetaObra.EXPLORAR;
        this.currentUserId = resolveCurrentUserId();
    }

    public void setModoTarjeta(ModoTarjetaObra modoTarjeta) {
        this.modoTarjeta = modoTarjeta != null ? modoTarjeta : ModoTarjetaObra.EXPLORAR;
        notifyDataSetChanged();
    }

    public void setEntryAnimationsEnabled(boolean enabled) {
        this.entryAnimationsEnabled = enabled;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.onLikeClickListener = listener;
    }

    public void setOnPrimaryActionClickListener(OnPrimaryActionClickListener listener) {
        this.onPrimaryActionClickListener = listener;
    }

    public void setOnSecondaryActionClickListener(OnSecondaryActionClickListener listener) {
        this.onSecondaryActionClickListener = listener;
    }

    public void setOnComprarClickListener(OnComprarClickListener listener) {
        this.onPrimaryActionClickListener = listener == null ? null : listener::onComprarClick;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
        notifyDataSetChanged();
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
        notifyDataSetChanged();
    }

    public void setOnAuthorClickListener(OnAuthorClickListener listener) {
        this.onAuthorClickListener = listener;
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.onCardClickListener = listener;
    }

    public void setOwnedObraIds(Set<Integer> ownedObraIds) {
        setOwnedObraIds(ownedObraIds, true);
    }

    public void setOwnedObraIds(Set<Integer> ownedObraIds, boolean notifyChanges) {
        this.ownedObraIds.clear();

        if (ownedObraIds != null) {
            this.ownedObraIds.addAll(ownedObraIds);
        }

        if (notifyChanges) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjetatextoobra, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        TarjetaTextoObraItem obra = listaObras.get(position);

        if (entryAnimationsEnabled) {
            animateFeedEntry(holder, position);
        }

        aplicarTema(holder, tm);
        llenarDatosBasicos(holder, obra);
        configurarLike(holder, tm);
        cargarImagenAutor(holder, obra);
        cargarImagenObra(holder, obra);
        configurarGestosImagen(holder);

        boolean expandido = position == tarjetaExpandida;
        obra.setExpandido(expandido);
        animarVista(holder.expandedSection, expandido);

        configurarBotones(holder, obra);
        configurarMenuOpciones(holder, obra);
        configurarBotonReportar(holder, obra);
        configurarExpansion(holder);
        configurarAcciones(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains(PAYLOAD_LIKE_STATE)) {
            if (position < 0 || position >= listaObras.size()) {
                return;
            }
            bindLikeUi(holder, listaObras.get(position), false, new ThemeManager(holder.itemView.getContext()));
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    private void aplicarTema(@NonNull ViewHolder holder, @NonNull ThemeManager tm) {
        ThemeApplier.applyTextPrimary(holder.titulo, tm);
        ThemeApplier.applyTextSecondary(holder.autor, tm);
        ThemeApplier.applyTextSecondary(holder.descripcion, tm);
        ThemeApplier.applyTextSecondary(holder.estado, tm);
        ThemeApplier.applyTextSecondary(holder.tecnica, tm);
        ThemeApplier.applyTextSecondary(holder.medidas, tm);
        ThemeApplier.applyTextPrimary(holder.precio, tm);
        ThemeApplier.applyPrimaryButton(holder.btnAccionPrincipal, tm);
        ThemeApplier.applySecondaryButton(holder.btnAccionSecundaria, tm);
        ThemeApplier.applySecondaryButton(holder.btnReportarObra, tm);
        CardThemeHelper.applyFlatCard(holder.layoutObraCard, tm);
        CardThemeHelper.applyChip(holder.categoria, tm);
        CardThemeHelper.applyStatusChip(holder.estadoResumen, null);
    }

    private void llenarDatosBasicos(@NonNull ViewHolder holder, @NonNull TarjetaTextoObraItem obra) {
        holder.titulo.setText(safeText(obra.getTitulo(), "Obra sin título"));
        holder.autor.setText(safeText(obra.getNombreAutor(), "Autor"));
        holder.descripcion.setText(safeText(obra.getDescripcion(), "Sin descripción"));
        holder.estadoResumen.setText(formatearEstadoVisual(obra.getEstado()));
        CardThemeHelper.applyStatusChip(holder.estadoResumen, obra.getEstado());
        holder.estado.setText("Estado: " + formatearEstadoVisual(obra.getEstado()));
        holder.estado.setVisibility(View.GONE);
        holder.tecnica.setText("Técnica: " + safeText(obra.getTecnicas(), "N/A"));
        holder.medidas.setText(formatearMedidas(obra.getMedidas()));
        holder.categoria.setText(safeText(obra.getNombreCategoria(), "Sin categoría"));
        holder.likes.setText(String.valueOf(obra.getLikes()));
        resetDoubleTapHeart(holder);

        if (debeOcultarPrecio(obra)) {
            holder.precio.setVisibility(View.GONE);
            holder.precio.setText("");
        } else {
            holder.precio.setVisibility(View.VISIBLE);
            holder.precio.setText(obra.getPrecio() != null
                    ? "Precio: $ " + String.format(Locale.getDefault(), "%,.2f", obra.getPrecio())
                    : "Precio: N/A");
        }
    }

    private void configurarLike(@NonNull ViewHolder holder, @NonNull ThemeManager tm) {
        int adapterPosition = holder.getBindingAdapterPosition();

        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }

        TarjetaTextoObraItem obra = listaObras.get(adapterPosition);
        LikeStateManager.applyTo(obra);
        bindLikeUi(holder, obra, true, tm);
        holder.btnLike.setOnClickListener(v -> {
            LikeUiHelper.animatePress(v);
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
            triggerLike(holder, false);
        });
    }

    private void bindLikeUi(
            @NonNull ViewHolder holder,
            @NonNull TarjetaTextoObraItem obra,
            boolean forceVisualState,
            @NonNull ThemeManager tm
    ) {
        LikeUiHelper.bind(
                holder.btnLike,
                holder.likes,
                obra.isUserLiked(),
                obra.getLikes(),
                tm.color(ThemeKeys.LIKE_ACTIVE),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
    }

    private void cargarImagenAutor(@NonNull ViewHolder holder, @NonNull TarjetaTextoObraItem obra) {
        String fotoPerfil = obra.getFotoPerfilAutor();

        Glide.with(holder.itemView.getContext())
                .load((fotoPerfil != null && !fotoPerfil.trim().isEmpty())
                        ? fotoPerfil
                        : R.drawable.fotoperfilprueba)
                .placeholder(R.drawable.fotoperfilprueba)
                .error(R.drawable.fotoperfilprueba)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(holder.imgAutor);

        holder.imgAutor.setOnClickListener(v -> {
            animatePress(v);

            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (onAuthorClickListener != null) {
                onAuthorClickListener.onAuthorClick(listaObras.get(adapterPosition), adapterPosition);
            }
        });
    }

    private void cargarImagenObra(@NonNull ViewHolder holder, @NonNull TarjetaTextoObraItem obra) {
        String imagenObra = obra.getImagen1();

        if (imagenObra != null && !imagenObra.trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagenObra)
                    .placeholder(R.drawable.imagencargaobras)
                    .error(R.drawable.imagencargaobras)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(R.drawable.imagencargaobras);
        }
    }

    private void configurarExpansion(@NonNull ViewHolder holder) {
        View.OnClickListener toggleExpandListener = v -> {
            if (!dispatchCardClick(holder)) {
                toggleExpansion(holder);
            }
        };

        holder.itemView.setOnClickListener(toggleExpandListener);

        if (holder.layoutObraCard != null) {
            holder.layoutObraCard.setOnClickListener(toggleExpandListener);
        }
    }

    private void toggleExpansion(@NonNull ViewHolder holder) {
        int previousExpanded = tarjetaExpandida;
        int currentPosition = holder.getBindingAdapterPosition();

        if (currentPosition == RecyclerView.NO_POSITION) {
            return;
        }

        if (previousExpanded == currentPosition) {
            tarjetaExpandida = -1;
        } else {
            tarjetaExpandida = currentPosition;

            if (previousExpanded != -1) {
                notifyItemChanged(previousExpanded);
            }
        }

        notifyItemChanged(currentPosition);
    }

    private void configurarGestosImagen(@NonNull ViewHolder holder) {
        GestureDetector detector = new GestureDetector(
                holder.imgObra.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        if (!dispatchCardClick(holder)) {
                            toggleExpansion(holder);
                        }
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(@NonNull MotionEvent e) {
                        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
                        LikeUiHelper.animateInstagramHeart(
                                holder.imgDoubleTapHeart,
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

    private boolean dispatchCardClick(@NonNull ViewHolder holder) {
        if (onCardClickListener == null) {
            return false;
        }
        int adapterPosition = holder.getBindingAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return true;
        }
        onCardClickListener.onCardClick(listaObras.get(adapterPosition), adapterPosition);
        return true;
    }

    private void triggerLike(@NonNull ViewHolder holder, boolean onlyLikeIfNeeded) {
        if (onLikeClickListener == null) {
            return;
        }

        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) {
            return;
        }

        TarjetaTextoObraItem currentObra = listaObras.get(currentPosition);
        LikeStateManager.applyTo(currentObra);
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        if (onlyLikeIfNeeded && currentObra.isUserLiked()) {
            LikeUiHelper.animateChange(
                    holder.btnLike,
                    true,
                    tm.color(ThemeKeys.LIKE_ACTIVE),
                    tm.color(ThemeKeys.TEXT_SECONDARY)
            );
            return;
        }

        boolean likedAntesDelClick = currentObra.isUserLiked();
        int likesAntesDelClick = currentObra.getLikes();

        onLikeClickListener.onLikeClick(currentObra, currentPosition);

        if (likedAntesDelClick != currentObra.isUserLiked()
                || likesAntesDelClick != currentObra.getLikes()) {
            LikeUiHelper.animateChange(
                    holder.btnLike,
                    currentObra.isUserLiked(),
                    tm.color(ThemeKeys.LIKE_ACTIVE),
                    tm.color(ThemeKeys.TEXT_SECONDARY)
            );
        }
    }

    private void resetDoubleTapHeart(@NonNull ViewHolder holder) {
        if (holder.imgDoubleTapHeart == null) {
            return;
        }
        holder.imgDoubleTapHeart.animate().cancel();
        holder.imgDoubleTapHeart.clearAnimation();
        holder.imgDoubleTapHeart.setVisibility(View.GONE);
        holder.imgDoubleTapHeart.setAlpha(0f);
        holder.imgDoubleTapHeart.setScaleX(1f);
        holder.imgDoubleTapHeart.setScaleY(1f);
    }

    private void configurarAcciones(@NonNull ViewHolder holder) {
        holder.btnAccionPrincipal.setOnClickListener(v -> {
            animatePress(v);

            int adapterPosition = holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION || onPrimaryActionClickListener == null) {
                return;
            }

            TarjetaTextoObraItem selectedObra = listaObras.get(adapterPosition);

            if (!debeMostrarBotonPrincipal(selectedObra)) {
                return;
            }

            onPrimaryActionClickListener.onPrimaryActionClick(selectedObra, adapterPosition);
        });

        holder.btnAccionSecundaria.setOnClickListener(v -> {
            animatePress(v);

            int adapterPosition = holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION || onSecondaryActionClickListener == null) {
                return;
            }

            onSecondaryActionClickListener.onSecondaryActionClick(listaObras.get(adapterPosition), adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
    }

    private void configurarBotones(@NonNull ViewHolder holder, @NonNull TarjetaTextoObraItem obra) {
        boolean mostrarPrincipal = debeMostrarBotonPrincipal(obra);
        boolean mostrarSecundario = modoTarjeta == ModoTarjetaObra.CARRITO;

        holder.btnAccionPrincipal.setVisibility(mostrarPrincipal ? View.VISIBLE : View.GONE);
        holder.btnAccionSecundaria.setVisibility(mostrarSecundario ? View.VISIBLE : View.GONE);

        if (!mostrarPrincipal && !mostrarSecundario) {
            holder.actionsContainer.setVisibility(View.GONE);
            return;
        }

        holder.actionsContainer.setVisibility(View.VISIBLE);
        holder.btnAccionPrincipal.setText(modoTarjeta == ModoTarjetaObra.CARRITO
                ? "Comprar"
                : "Solicitar compra");
        holder.btnAccionSecundaria.setText("Quitar del carrito");
    }

    private void configurarBotonReportar(@NonNull ViewHolder holder, @NonNull TarjetaTextoObraItem obra) {
        boolean mostrarReportar = puedeReportarseObra(obra);

        holder.btnReportarObra.setVisibility(mostrarReportar ? View.VISIBLE : View.GONE);

        if (!mostrarReportar) {
            holder.btnReportarObra.setOnClickListener(null);
            return;
        }

        holder.btnReportarObra.setOnClickListener(v -> {
            animatePress(v);
            mostrarDialogoReporteObra(obra);
        });
    }

    private void configurarMenuOpciones(@NonNull ViewHolder holder, @NonNull TarjetaTextoObraItem obra) {
        boolean esModoMisObras = modoTarjeta == ModoTarjetaObra.MIS_OBRAS;
        boolean hayAcciones = onEditClickListener != null || onDeleteClickListener != null;

        if (!esModoMisObras || !hayAcciones) {
            holder.btnMoreOptions.setVisibility(View.GONE);
            holder.btnMoreOptions.setOnClickListener(null);
            return;
        }

        boolean puedeEditar = onEditClickListener != null && puedeModificarObra(obra);
        boolean puedeEliminar = onDeleteClickListener != null && puedeEliminarObra(obra);
        boolean habilitarMenu = puedeEditar || puedeEliminar;

        holder.btnMoreOptions.setVisibility(View.VISIBLE);
        holder.btnMoreOptions.setEnabled(habilitarMenu);
        holder.btnMoreOptions.setAlpha(habilitarMenu ? 1f : 0.45f);

        if (!habilitarMenu) {
            holder.btnMoreOptions.setOnClickListener(null);
            return;
        }

        holder.btnMoreOptions.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            PopupMenu popupMenu = new PopupMenu(context, holder.btnMoreOptions);

            if (puedeEditar) {
                popupMenu.getMenu().add(0, 1, 0, "Modificar");
            }

            if (puedeEliminar) {
                popupMenu.getMenu().add(0, 2, 1, "Eliminar");
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                int currentPosition = holder.getBindingAdapterPosition();

                if (currentPosition == RecyclerView.NO_POSITION) {
                    return false;
                }

                TarjetaTextoObraItem obraSeleccionada = listaObras.get(currentPosition);

                if (item.getItemId() == 1 && onEditClickListener != null) {
                    onEditClickListener.onEditClick(obraSeleccionada, currentPosition);
                    return true;
                }

                if (item.getItemId() == 2 && onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(obraSeleccionada, currentPosition);
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private boolean debeMostrarBotonPrincipal(TarjetaTextoObraItem obra) {
        boolean esPropia = ownedObraIds.contains(obra.getIdObra());
        boolean cumpleReglas = cumpleReglasVisualesSolicitud(obra);
        boolean puedeComprar = obra != null && obra.isPuedeSolicitarCompra();

        if (!puedeComprar) {
            puedeComprar = puedeComprarse(obra);
        }

        if (modoTarjeta == ModoTarjetaObra.MIS_OBRAS) {
            return false;
        }

        if (modoTarjeta == ModoTarjetaObra.CARRITO) {
            return !esPropia && cumpleReglas && puedeComprar;
        }

        return !esPropia && cumpleReglas && puedeComprar;
    }

    private boolean cumpleReglasVisualesSolicitud(TarjetaTextoObraItem obra) {
        String estado = normalizarEstado(obra != null ? obra.getEstado() : null);

        if (estado.isEmpty()) {
            return true;
        }

        return !estado.contains("exhib")
                && !estado.contains("reservad")
                && !estado.contains("vendid");
    }

    private boolean puedeComprarse(TarjetaTextoObraItem obra) {
        if (obra == null) {
            return false;
        }

        if (obra.getPrecio() == null || obra.getPrecio() <= 0) {
            return false;
        }

        String estadoNormalizado = normalizarEstado(obra.getEstado());
        boolean esVenta = estadoNormalizado.contains("venta");
        boolean bloqueada = estadoNormalizado.contains("vendida")
                || estadoNormalizado.contains("reservad");

        return esVenta && !bloqueada;
    }

    private boolean puedeReportarseObra(TarjetaTextoObraItem obra) {
        if (obra == null || obra.getIdObra() <= 0) {
            logReporteDebug("puedeReportarseObra=false -> obra nula o idObra invalido");
            return false;
        }

        Integer usuarioActual = currentUserId != null ? currentUserId : resolveCurrentUserId();
        String rolActual = resolveCurrentUserRole();
        Integer idAutorObra = obra.getIdAutor();
        boolean esObraPropiaPorAutor = idAutorObra != null && usuarioActual != null && idAutorObra.equals(usuarioActual);
        boolean esObraPropiaPorOwnedIds = ownedObraIds.contains(obra.getIdObra());
        boolean esObraPropia = esObraPropiaPorAutor || esObraPropiaPorOwnedIds;
        String estado = normalizarEstado(obra.getEstado());
        boolean estadoBloqueado = estado.contains("reservad") || estado.contains("vendid");
        boolean puedeMostrarSegunRol = ReporteUiPermissions.puedeMostrarReportar(usuarioActual, rolActual);
        boolean esModoMisObras = modoTarjeta == ModoTarjetaObra.MIS_OBRAS;
        boolean puedeReportar = puedeMostrarSegunRol && !esModoMisObras && !esObraPropia && !estadoBloqueado;

        SharedPreferences prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        String rolPrefs = prefs.getString("rol", null);
        String modoPrefs = prefs.getString("modo", null);
        logReporteDebug("obraId=" + obra.getIdObra()
                + ", rolDetectado=" + rolActual
                + ", rolPrefs=" + rolPrefs
                + ", modoDetectado=" + modoPrefs
                + ", idUsuarioActual=" + usuarioActual
                + ", idUsuarioObra/idAutor=" + idAutorObra
                + ", esObraPropia=" + esObraPropia
                + ", esObraPropiaPorAutor=" + esObraPropiaPorAutor
                + ", esObraPropiaPorOwnedIds=" + esObraPropiaPorOwnedIds
                + ", estadoObra=" + obra.getEstado()
                + ", estadoNormalizado=" + estado
                + ", editable=" + obra.isEditable()
                + ", eliminable=" + obra.isEliminable()
                + ", puedeSolicitarCompra=" + obra.isPuedeSolicitarCompra()
                + ", modoTarjeta=" + modoTarjeta
                + ", puedeMostrarSegunRol=" + puedeMostrarSegunRol
                + ", puedeReportar=" + puedeReportar);

        return puedeReportar;
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    private String formatearMedidas(String medidasCrudas) {
        if (medidasCrudas == null || medidasCrudas.trim().isEmpty()) {
            return "Medidas: N/A";
        }

        String medidas = medidasCrudas.trim();

        if (medidas.toLowerCase(Locale.ROOT).endsWith("cm")) {
            return "Medidas: " + medidas;
        }

        return "Medidas: " + medidas + " cm";
    }

    private boolean debeOcultarPrecio(TarjetaTextoObraItem obra) {
        if (obra == null || obra.getEstado() == null) {
            return false;
        }

        String estado = obra.getEstado().trim().toLowerCase(Locale.ROOT);
        return estado.contains("exhib");
    }

    private boolean puedeModificarObra(TarjetaTextoObraItem obra) {
        if (obra == null || !obra.isEditable()) {
            return false;
        }

        String estado = normalizarEstado(obra.getEstado());
        return !estado.contains("reservad") && !estado.contains("vendida");
    }

    private boolean puedeEliminarObra(TarjetaTextoObraItem obra) {
        if (obra == null || !obra.isEliminable()) {
            return false;
        }

        String estado = normalizarEstado(obra.getEstado());
        return !estado.contains("reservad") && !estado.contains("vendida");
    }

    private String normalizarEstado(String estado) {
        if (estado == null) {
            return "";
        }

        String sinAcentos = Normalizer.normalize(estado, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return sinAcentos.trim().toLowerCase(Locale.ROOT).replace("_", " ");
    }

    private Integer resolveCurrentUserId() {
        return ReporteUiPermissions.resolveCurrentUserId(context);
    }

    private String resolveCurrentUserRole() {
        return ReporteUiPermissions.resolveCurrentUserRole(context);
    }

    private void mostrarDialogoReporteObra(TarjetaTextoObraItem obra) {
        Integer usuarioActual = currentUserId != null ? currentUserId : resolveCurrentUserId();

        if (!(context instanceof FragmentActivity) || usuarioActual == null || usuarioActual <= 0) {
            Toast.makeText(context, "No se pudo abrir el formulario de reporte.", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogReportarContenido dialog = DialogReportarContenido.newInstance(
                "OBRA",
                obra.getIdObra(),
                usuarioActual,
                obra.getTitulo()
        );

        dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "DialogReportarContenido");
    }

    private String formatearEstadoVisual(String estadoRaw) {
        String estado = normalizarEstado(estadoRaw);

        if (estado.isEmpty()) {
            return "N/A";
        }

        if (estado.contains("exhib")) {
            return "En exhibición";
        }

        if (estado.contains("venta")) {
            return "En venta";
        }

        if (estado.contains("reservad")) {
            return "Reservada";
        }

        if (estado.contains("vendid")) {
            return "Vendida";
        }

        String[] palabras = estado.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.isEmpty()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(palabra.charAt(0)));

            if (palabra.length() > 1) {
                builder.append(palabra.substring(1));
            }
        }

        return builder.toString();
    }

    private void logReporteDebug(String message) {
        if (!ENABLE_REPORTE_UI_DEBUG_LOGS) {
            return;
        }
        if (context != null
                && context.getApplicationInfo() != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Log.d(TAG_REPORTE_DEBUG, message);
        }
    }

    private void animateFeedEntry(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position <= lastAnimatedPosition) {
            return;
        }

        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(18f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();

        lastAnimatedPosition = position;
    }

    private void animatePress(@NonNull View view) {
        LikeUiHelper.animatePress(view);
    }

    private void animarVista(View view, boolean expandir) {
        if (view == null) {
            return;
        }

        view.animate().cancel();

        if (expandir) {
            view.setVisibility(View.VISIBLE);
            view.setScaleY(0.96f);
            view.setAlpha(0f);

            view.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(130)
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .scaleY(0.96f)
                    .setDuration(120)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public void filtrar(String texto) {
        List<TarjetaTextoObraItem> listaFiltrada = new ArrayList<>();

        if (texto == null || texto.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String filtro = texto.toLowerCase(Locale.ROOT);

            for (TarjetaTextoObraItem obra : listaOriginal) {
                if (obra.getTitulo() != null && obra.getTitulo().toLowerCase(Locale.ROOT).contains(filtro)) {
                    listaFiltrada.add(obra);
                }
            }
        }

        int oldSize = listaObras.size();
        listaObras.clear();
        listaObras.addAll(listaFiltrada);
        tarjetaExpandida = -1;

        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }

        if (!listaFiltrada.isEmpty()) {
            notifyItemRangeInserted(0, listaFiltrada.size());
        }
    }

    public void actualizarLista(List<TarjetaTextoObraItem> nuevaLista) {
        int oldSize = listaObras.size();

        listaOriginal.clear();

        if (nuevaLista != null) {
            listaOriginal.addAll(nuevaLista);
        }

        listaObras.clear();

        if (nuevaLista != null) {
            listaObras.addAll(nuevaLista);
        }

        tarjetaExpandida = -1;

        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }

        if (nuevaLista != null && !nuevaLista.isEmpty()) {
            notifyItemRangeInserted(0, nuevaLista.size());
        }
    }

    public void agregarItems(List<TarjetaTextoObraItem> nuevosItems) {
        if (nuevosItems == null || nuevosItems.isEmpty()) {
            return;
        }
        int start = listaObras.size();
        listaObras.addAll(nuevosItems);
        listaOriginal.addAll(nuevosItems);
        notifyItemRangeInserted(start, nuevosItems.size());
    }

    public void removeItemAt(int position) {
        if (position < 0 || position >= listaObras.size()) {
            return;
        }

        TarjetaTextoObraItem item = listaObras.remove(position);
        listaOriginal.remove(item);

        if (tarjetaExpandida == position) {
            tarjetaExpandida = -1;
        } else if (tarjetaExpandida > position) {
            tarjetaExpandida--;
        }

        notifyItemRemoved(position);
    }

    public void notifyLikeChanged(int position) {
        if (position >= 0 && position < listaObras.size()) {
            notifyItemChanged(position);
        }
    }

    public void notifyLikeChangedPartial(int position) {
        if (position >= 0 && position < listaObras.size()) {
            notifyItemChanged(position, PAYLOAD_LIKE_STATE);
        }
    }

    public void updateLikeStateById(int idObra, boolean liked, int likesCount) {
        for (int i = 0; i < listaObras.size(); i++) {
            TarjetaTextoObraItem item = listaObras.get(i);
            if (item.getIdObra() == idObra) {
                item.setUserLiked(liked);
                item.setLikes(Math.max(0, likesCount));
                notifyLikeChangedPartial(i);
            }
        }
        for (TarjetaTextoObraItem item : listaOriginal) {
            if (item.getIdObra() == idObra) {
                item.setUserLiked(liked);
                item.setLikes(Math.max(0, likesCount));
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        TextView descripcion;
        TextView estadoResumen;
        TextView estado;
        TextView tecnica;
        TextView medidas;
        TextView precio;
        TextView categoria;
        TextView likes;
        TextView autor;

        ImageView imgAutor;
        ImageView imgObra;
        ImageView imgDoubleTapHeart;

        ImageButton btnLike;
        ImageButton btnMoreOptions;

        View expandedSection;
        View actionsContainer;
        View layoutObraCard;

        Button btnAccionPrincipal;
        Button btnAccionSecundaria;
        Button btnReportarObra;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAutor = itemView.findViewById(R.id.imgAutor);
            imgObra = itemView.findViewById(R.id.imgObra);
            imgDoubleTapHeart = itemView.findViewById(R.id.imgDoubleTapHeart);

            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            estadoResumen = itemView.findViewById(R.id.estadoResumen);
            estado = itemView.findViewById(R.id.estado);
            tecnica = itemView.findViewById(R.id.tecnica);
            medidas = itemView.findViewById(R.id.medidas);
            precio = itemView.findViewById(R.id.precio);
            autor = itemView.findViewById(R.id.autor);
            categoria = itemView.findViewById(R.id.categoria);
            likes = itemView.findViewById(R.id.likes);

            btnLike = itemView.findViewById(R.id.btnLike);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);

            expandedSection = itemView.findViewById(R.id.expanded_section);
            actionsContainer = itemView.findViewById(R.id.actionsContainer);
            layoutObraCard = itemView.findViewById(R.id.layoutObraCard);

            btnAccionPrincipal = itemView.findViewById(R.id.btnAccionPrincipal);
            btnAccionSecundaria = itemView.findViewById(R.id.btnAccionSecundaria);
            btnReportarObra = itemView.findViewById(R.id.btnReportarObra);
        }
    }
}
