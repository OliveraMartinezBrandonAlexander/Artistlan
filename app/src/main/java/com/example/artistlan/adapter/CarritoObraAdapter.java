package com.example.artistlan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.model.CarritoDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CarritoObraAdapter extends RecyclerView.Adapter<CarritoObraAdapter.ViewHolder> {

    public interface OnComprarClickListener {
        void onComprar(CarritoDTO item, int position);
    }

    public interface OnContactarClickListener {
        void onContactar(CarritoDTO item, int position);
    }

    public interface OnQuitarClickListener {
        void onQuitar(CarritoDTO item, int position);
    }

    private final Context context;
    private final List<CarritoDTO> items = new ArrayList<>();
    private OnComprarClickListener onComprarClickListener;
    private OnContactarClickListener onContactarClickListener;
    private OnQuitarClickListener onQuitarClickListener;

    public CarritoObraAdapter(@NonNull Context context) {
        this.context = context;
    }

    public void setOnComprarClickListener(OnComprarClickListener listener) {
        this.onComprarClickListener = listener;
    }

    public void setOnContactarClickListener(OnContactarClickListener listener) {
        this.onContactarClickListener = listener;
    }

    public void setOnQuitarClickListener(OnQuitarClickListener listener) {
        this.onQuitarClickListener = listener;
    }

    public void submitList(@NonNull List<CarritoDTO> nuevosItems) {
        items.clear();
        items.addAll(nuevosItems);
        notifyDataSetChanged();
    }

    public List<CarritoDTO> getItems() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito_obra, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        CarritoDTO item = items.get(position);

        String imagen = safe(item.getImagen1(), "");
        if (!imagen.isEmpty()) {
            Glide.with(context)
                    .load(imagen)
                    .placeholder(R.drawable.imagencargaobras)
                    .error(R.drawable.imagencargaobras)
                    .into(holder.imgObra);
        } else {
            holder.imgObra.setImageResource(R.drawable.imagencargaobras);
        }

        ThemeApplier.applyTextPrimary(holder.tvTitulo, tm);
        ThemeApplier.applyTextSecondary(holder.tvArtista, tm);
        ThemeApplier.applyTextSecondary(holder.tvPrecio, tm);
        ThemeApplier.applyTextSecondary(holder.tvEstado, tm);
        ThemeApplier.applyTextSecondary(holder.tvReservaDetalle, tm);
        ThemeApplier.applyPrimaryButton(holder.btnComprar, tm);
        ThemeApplier.applySecondaryButton(holder.btnContactar, tm);
        ThemeApplier.applySecondaryButton(holder.btnQuitar, tm);
        ThemeApplier.applyCardContainer(holder.itemView, tm);

        holder.tvTitulo.setText(safe(item.getTitulo(), "Obra sin titulo"));
        holder.tvArtista.setText("Artista: " + safe(item.getNombreAutor(), "No disponible"));
        holder.tvPrecio.setText(formatoPrecio(item.getPrecio()));
        String estadoTexto = formatearEstado(item);
        String reservaInfo = construirReservaInfo(item);
        if (reservaInfo == null) {
            holder.tvEstado.setText("Estado: " + estadoTexto);
            holder.tvReservaDetalle.setVisibility(View.GONE);
        } else {
            holder.tvEstado.setText(reservaInfo);
            holder.tvReservaDetalle.setVisibility(View.VISIBLE);
            holder.tvReservaDetalle.setText("Estado: " + estadoTexto);
        }

        holder.btnComprar.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || onComprarClickListener == null) {
                return;
            }
            onComprarClickListener.onComprar(items.get(adapterPosition), adapterPosition);
        });

        holder.btnContactar.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || onContactarClickListener == null) {
                return;
            }
            onContactarClickListener.onContactar(items.get(adapterPosition), adapterPosition);
        });

        holder.btnQuitar.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || onQuitarClickListener == null) {
                return;
            }
            onQuitarClickListener.onQuitar(items.get(adapterPosition), adapterPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String safe(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private String formatoPrecio(Double precio) {
        if (precio == null) {
            return "Precio: No disponible";
        }
        return String.format(Locale.US, "Precio: $ %,.2f", precio);
    }

    private String formatearEstado(CarritoDTO item) {
        String estadoRaw = item != null ? item.getEstado() : null;
        if (estadoRaw == null || estadoRaw.trim().isEmpty()) {
            if (item != null && (item.getFechaExpiracionReserva() != null
                    || item.getTiempoRestanteReserva() != null
                    || item.getMinutosRestantesReserva() != null)) {
                return "Reservada";
            }
            return "No disponible";
        }
        String estado = estadoRaw.trim().toLowerCase(Locale.ROOT).replace("_", " ");
        if (estado.contains("reservad")) {
            return "Reservada";
        }
        if (estado.contains("pendiente")) {
            return "Pendiente";
        }
        if (estado.contains("venta")) {
            return "En venta";
        }
        if (estado.contains("exhib")) {
            return "En exhibicion";
        }
        if (estado.contains("vendid")) {
            return "Vendida";
        }
        return estadoRaw;
    }

    private String construirReservaInfo(CarritoDTO item) {
        String tiempoRestante = safe(item.getTiempoRestanteReserva(), "");
        if (!tiempoRestante.isEmpty()) {
            return "Tiempo restante: " + tiempoRestante;
        }

        Integer minutosRestantes = item.getMinutosRestantesReserva();
        if (minutosRestantes != null && minutosRestantes >= 0) {
            return "Tiempo restante: " + formatearMinutosRestantes(minutosRestantes);
        }

        String fechaExpiracion = safe(item.getFechaExpiracionReserva(), "");
        if (!fechaExpiracion.isEmpty()) {
            return "Reserva hasta: " + formatearFechaReserva(fechaExpiracion);
        }
        return null;
    }

    private String formatearMinutosRestantes(int minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        int horas = minutos / 60;
        int mins = minutos % 60;
        if (mins == 0) {
            return horas + " h";
        }
        return horas + " h " + mins + " min";
    }

    private String formatearFechaReserva(String raw) {
        String cleaned = raw.trim();
        String[] patrones = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mmXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm"
        };
        for (String patron : patrones) {
            try {
                SimpleDateFormat entrada = new SimpleDateFormat(patron, Locale.US);
                entrada.setLenient(false);
                Date parsed = entrada.parse(cleaned);
                if (parsed != null) {
                    return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(parsed);
                }
            } catch (Exception ignored) {
                // continuar al siguiente patron
            }
        }
        return cleaned.replace('T', ' ');
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgObra;
        final TextView tvTitulo;
        final TextView tvArtista;
        final TextView tvPrecio;
        final TextView tvEstado;
        final TextView tvReservaDetalle;
        final Button btnComprar;
        final Button btnContactar;
        final Button btnQuitar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgObra = itemView.findViewById(R.id.imgCarritoObra);
            tvTitulo = itemView.findViewById(R.id.tvCarritoTitulo);
            tvArtista = itemView.findViewById(R.id.tvCarritoArtista);
            tvPrecio = itemView.findViewById(R.id.tvCarritoPrecio);
            tvEstado = itemView.findViewById(R.id.tvCarritoEstado);
            tvReservaDetalle = itemView.findViewById(R.id.tvCarritoReservaDetalle);
            btnComprar = itemView.findViewById(R.id.btnCarritoComprar);
            btnContactar = itemView.findViewById(R.id.btnCarritoContactar);
            btnQuitar = itemView.findViewById(R.id.btnCarritoQuitar);
        }
    }
}
