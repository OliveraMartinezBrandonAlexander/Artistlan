package com.example.artistlan.TarjetaTextoObra.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TarjetaTextoObraAdapter extends RecyclerView.Adapter<TarjetaTextoObraAdapter.ViewHolder> {

    public interface OnLikeClickListener {
        void onLikeClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnPrimaryActionClickListener {
        void onPrimaryActionClick(TarjetaTextoObraItem obraItem, int position);
    }

    public interface OnSecondaryActionClickListener {
        void onSecondaryActionClick(TarjetaTextoObraItem obraItem, int position);
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

    private final List<TarjetaTextoObraItem> listaObras;
    private final List<TarjetaTextoObraItem> listaOriginal;
    private final Context context;
    private final Set<Integer> ownedObraIds = new HashSet<>();

    private ModoTarjetaObra modoTarjeta;
    private int tarjetaExpandida = -1;

    public TarjetaTextoObraAdapter(List<TarjetaTextoObraItem> listaObras, Context context) {
        this(listaObras, context, ModoTarjetaObra.EXPLORAR);
    }

    public TarjetaTextoObraAdapter(List<TarjetaTextoObraItem> listaObras, Context context, ModoTarjetaObra modoTarjeta) {
        this.listaObras = listaObras;
        this.listaOriginal = new ArrayList<>(listaObras);
        this.context = context;
        this.modoTarjeta = modoTarjeta != null ? modoTarjeta : ModoTarjetaObra.EXPLORAR;
    }

    public void setModoTarjeta(ModoTarjetaObra modoTarjeta) {
        this.modoTarjeta = modoTarjeta != null ? modoTarjeta : ModoTarjetaObra.EXPLORAR;
        notifyDataSetChanged();
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

    public void setOwnedObraIds(Set<Integer> ownedObraIds) {
        this.ownedObraIds.clear();
        if (ownedObraIds != null) {
            this.ownedObraIds.addAll(ownedObraIds);
        }
        notifyDataSetChanged();
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
        TarjetaTextoObraItem obra = listaObras.get(position);

        holder.titulo.setText(obra.getTitulo());
        holder.autor.setText(obra.getNombreAutor());
        holder.descripcion.setText(obra.getDescripcion());
        holder.estadoResumen.setText("Estado: " + formatearEstadoVisual(obra.getEstado()));
        holder.estado.setVisibility(View.GONE);
        holder.tecnica.setText("Tecnica: " + safeText(obra.getTecnicas(), "N/A"));
        holder.medidas.setText(formatearMedidas(obra.getMedidas()));
        if (debeOcultarPrecio(obra)) {
            holder.precio.setVisibility(View.GONE);
            holder.precio.setText("");
        } else {
            holder.precio.setVisibility(View.VISIBLE);
            holder.precio.setText(obra.getPrecio() != null
                    ? "Precio: $ " + String.format("%,.2f", obra.getPrecio())
                    : "Precio: N/A");
        }
        holder.categoria.setText(safeText(obra.getNombreCategoria(), "Sin categoria"));
        holder.likes.setText(String.valueOf(obra.getLikes()));

        holder.btnLike.setImageResource(obra.isUserLiked() ? R.drawable.ic_heart_red : R.drawable.ic_heart_purple);
        holder.btnLike.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), LIKE_BUTTON_COOLDOWN_MS);
            animateLikeButton(holder.btnLike, obra.isUserLiked());

            int adapterPosition = holder.getAdapterPosition();
            if (onLikeClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                onLikeClickListener.onLikeClick(listaObras.get(adapterPosition), adapterPosition);
            }
        });

        String fotoPerfil = obra.getFotoPerfilAutor();
        Glide.with(context)
                .load((fotoPerfil != null && !fotoPerfil.isEmpty()) ? fotoPerfil : R.drawable.fotoperfilprueba)
                .placeholder(R.drawable.fotoperfilprueba)
                .circleCrop()
                .into(holder.imgAutor);

        String imagenObra = obra.getImagen1();
        if (imagenObra != null && !imagenObra.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagenObra)
                    .placeholder(R.drawable.imagencargaobras)
                    .error(R.drawable.imagencargaobras)
                    .into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(R.drawable.imagencargaobras);
        }

        boolean expandido = position == tarjetaExpandida;
        animarVista(holder.expandedSection, expandido);
        obra.setExpandido(expandido);

        configurarBotones(holder, obra);
        configurarMenuOpciones(holder, obra);

        holder.itemView.setOnClickListener(v -> {
            int previousExpanded = tarjetaExpandida;
            int currentPosition = holder.getAdapterPosition();
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
        });

        holder.btnAccionPrincipal.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
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
            int adapterPosition = holder.getAdapterPosition();
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

    private void configurarBotones(ViewHolder holder, TarjetaTextoObraItem obra) {
        boolean mostrarPrincipal = debeMostrarBotonPrincipal(obra);
        boolean mostrarSecundario = modoTarjeta == ModoTarjetaObra.CARRITO;

        holder.btnAccionPrincipal.setVisibility(mostrarPrincipal ? View.VISIBLE : View.GONE);
        holder.btnAccionSecundaria.setVisibility(mostrarSecundario ? View.VISIBLE : View.GONE);

        if (!mostrarPrincipal && !mostrarSecundario) {
            holder.actionsContainer.setVisibility(View.GONE);
            return;
        }

        holder.actionsContainer.setVisibility(View.VISIBLE);
        holder.btnAccionPrincipal.setText(modoTarjeta == ModoTarjetaObra.CARRITO ? "Comprar" : "Solicitar compra");
        holder.btnAccionSecundaria.setText("Quitar del carrito");
    }

    private void configurarMenuOpciones(ViewHolder holder, TarjetaTextoObraItem obra) {
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
            int adapterPosition = holder.getAdapterPosition();
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
                int currentPosition = holder.getAdapterPosition();
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
        boolean bloqueada = estadoNormalizado.contains("vendida") || estadoNormalizado.contains("reservad");
        return esVenta && !bloqueada;
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
        return estado.trim().toLowerCase(Locale.ROOT).replace("_", " ");
    }

    private String formatearEstadoVisual(String estadoRaw) {
        String estado = normalizarEstado(estadoRaw);
        if (estado.isEmpty()) {
            return "N/A";
        }

        if (estado.contains("exhib")) {
            return "En exhibicion";
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

    private void animateLikeButton(ImageButton btnLike, boolean wasLiked) {
        btnLike.animate().cancel();
        btnLike.setScaleX(0.82f);
        btnLike.setScaleY(0.82f);
        btnLike.setAlpha(0.75f);

        btnLike.animate()
                .scaleX(1.24f)
                .scaleY(1.24f)
                .alpha(1f)
                .setDuration(140)
                .withEndAction(() -> {
                    btnLike.setImageResource(wasLiked ? R.drawable.ic_heart_purple : R.drawable.ic_heart_red);
                    btnLike.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(220)
                            .setInterpolator(new OvershootInterpolator(2.8f))
                            .start();
                })
                .start();
    }

    private void animarVista(View view, boolean expandir) {
        if (expandir) {
            view.setVisibility(View.VISIBLE);
            view.setScaleY(0f);
            view.setAlpha(0f);

            view.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start();
        } else {
            view.animate()
                    .alpha(0f)
                    .scaleY(0f)
                    .setDuration(160)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, descripcion, estadoResumen, estado, tecnica, medidas, precio, categoria, likes, autor;
        ImageView imgAutor, imgObra;
        ImageButton btnLike, btnMoreOptions;
        View expandedSection, actionsContainer;
        Button btnAccionPrincipal, btnAccionSecundaria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAutor = itemView.findViewById(R.id.imgAutor);
            imgObra = itemView.findViewById(R.id.imgObra);
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
            btnAccionPrincipal = itemView.findViewById(R.id.btnAccionPrincipal);
            btnAccionSecundaria = itemView.findViewById(R.id.btnAccionSecundaria);
        }
    }

    public void filtrar(String texto) {
        List<TarjetaTextoObraItem> listaFiltrada = new ArrayList<>();

        if (texto == null || texto.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String filtro = texto.toLowerCase();
            for (TarjetaTextoObraItem obra : listaOriginal) {
                if (obra.getTitulo() != null && obra.getTitulo().toLowerCase().contains(filtro)) {
                    listaFiltrada.add(obra);
                }
            }
        }
        int oldSize = listaObras.size();
        listaObras.clear();
        listaObras.addAll(listaFiltrada);
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
        listaOriginal.addAll(nuevaLista);
        listaObras.clear();
        listaObras.addAll(nuevaLista);
        tarjetaExpandida = -1;
        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (!nuevaLista.isEmpty()) {
            notifyItemRangeInserted(0, nuevaLista.size());
        }
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
}

