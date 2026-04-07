package com.example.artistlan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.model.TransaccionResumenDTO;
import com.example.artistlan.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.ViewHolder> {

    public enum TipoLista {
        COMPRAS,
        VENTAS
    }

    private final Context context;
    private final TipoLista tipoLista;
    private final List<TransaccionResumenDTO> transacciones;
    private final NumberFormat currencyFormatter;
    private final Locale localeSalida = new Locale("es", "MX");

    public TransaccionAdapter(Context context, TipoLista tipoLista, List<TransaccionResumenDTO> transacciones) {
        this.context = context;
        this.tipoLista = tipoLista;
        this.transacciones = transacciones != null ? transacciones : new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(localeSalida);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaccion_resumen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransaccionResumenDTO item = transacciones.get(position);

        holder.tvTituloObra.setText(textoSeguro(item.getTituloObra(), context.getString(R.string.transaccion_titulo_fallback)));
        holder.tvNombrePersona.setText(obtenerNombreRelacionado(item));
        holder.tvFecha.setText(formatearFecha(item.getFechaTransaccion()));
        holder.tvPrecio.setText(formatearPrecio(item.getPrecio()));

        holder.tvRolPersona.setText(tipoLista == TipoLista.COMPRAS
                ? context.getString(R.string.transaccion_label_vendedor)
                : context.getString(R.string.transaccion_label_comprador));

        String estado = item.getEstado();
        if (estado != null && !estado.trim().isEmpty()) {
            holder.tvEstado.setText(formatearEstadoVisual(estado));
            holder.tvEstado.setVisibility(View.VISIBLE);
        } else {
            holder.tvEstado.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView)
                .load(item.getImagenObra())
                .placeholder(R.drawable.imagencargaobras)
                .error(R.drawable.imagencargaobras)
                .centerCrop()
                .into(holder.imgObra);
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    public void actualizarLista(List<TransaccionResumenDTO> nuevaLista) {
        transacciones.clear();
        if (nuevaLista != null) {
            transacciones.addAll(nuevaLista);
        }
        notifyDataSetChanged();
    }

    private String obtenerNombreRelacionado(TransaccionResumenDTO item) {
        String nombre = tipoLista == TipoLista.COMPRAS
                ? primerTexto(item.getNombreVendedor(), item.getNombreArtista())
                : primerTexto(item.getNombreComprador(), item.getNombreArtista());

        return textoSeguro(nombre, context.getString(R.string.transaccion_persona_fallback));
    }

    private String primerTexto(String... candidatos) {
        if (candidatos == null) {
            return null;
        }
        for (String candidato : candidatos) {
            if (candidato != null && !candidato.trim().isEmpty()) {
                return candidato.trim();
            }
        }
        return null;
    }

    private String formatearPrecio(Double precio) {
        if (precio == null) {
            return context.getString(R.string.transaccion_precio_fallback);
        }
        return currencyFormatter.format(precio);
    }

    private String formatearFecha(String fechaRaw) {
        if (fechaRaw == null || fechaRaw.trim().isEmpty()) {
            return context.getString(R.string.transaccion_fecha_fallback);
        }

        String valor = fechaRaw.trim();
        String fechaNormalizada = normalizarFecha(valor);
        Date fecha = intentarParseo(valor,
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy"
        );

        if (fecha == null && !fechaNormalizada.equals(valor)) {
            fecha = intentarParseo(fechaNormalizada,
                    "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                    "yyyy-MM-dd'T'HH:mm:ssX",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss"
            );
        }

        if (fecha == null) {
            return valor;
        }

        return new SimpleDateFormat("dd MMM yyyy", localeSalida).format(fecha);
    }

    private String normalizarFecha(String valor) {
        int puntoIndex = valor.indexOf('.');
        if (puntoIndex < 0) {
            return valor;
        }

        int finFraccion = puntoIndex + 1;
        while (finFraccion < valor.length() && Character.isDigit(valor.charAt(finFraccion))) {
            finFraccion++;
        }

        String prefijo = valor.substring(0, puntoIndex);
        String fraccion = valor.substring(puntoIndex + 1, finFraccion);
        String sufijo = valor.substring(finFraccion);

        if (fraccion.isEmpty()) {
            return prefijo + sufijo;
        }

        String fraccionNormalizada = fraccion.length() > 3
                ? fraccion.substring(0, 3)
                : completarConCeros(fraccion, 3);

        return prefijo + "." + fraccionNormalizada + normalizarZonaHoraria(sufijo);
    }

    private String completarConCeros(String valor, int longitudObjetivo) {
        StringBuilder builder = new StringBuilder(valor);
        while (builder.length() < longitudObjetivo) {
            builder.append('0');
        }
        return builder.toString();
    }

    private String normalizarZonaHoraria(String sufijo) {
        if (sufijo == null || sufijo.isEmpty()) {
            return sufijo;
        }
        if (sufijo.matches("[+-]\\d{4}")) {
            return sufijo.substring(0, 3) + ":" + sufijo.substring(3);
        }
        return sufijo;
    }

    private Date intentarParseo(String valor, String... patrones) {
        for (String patron : patrones) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(patron, Locale.US);
                parser.setLenient(true);
                return parser.parse(valor);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String textoSeguro(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private String formatearEstadoVisual(String estadoRaw) {
        String estado = estadoRaw == null ? "" : estadoRaw.trim().toLowerCase(Locale.ROOT).replace("_", " ");
        if (estado.isEmpty()) {
            return "";
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
        return Character.toUpperCase(estado.charAt(0)) + estado.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgObra;
        private final TextView tvTituloObra;
        private final TextView tvRolPersona;
        private final TextView tvNombrePersona;
        private final TextView tvFecha;
        private final TextView tvPrecio;
        private final TextView tvEstado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgObra = itemView.findViewById(R.id.imgTransaccionObra);
            tvTituloObra = itemView.findViewById(R.id.tvTransaccionTitulo);
            tvRolPersona = itemView.findViewById(R.id.tvTransaccionRolPersona);
            tvNombrePersona = itemView.findViewById(R.id.tvTransaccionNombrePersona);
            tvFecha = itemView.findViewById(R.id.tvTransaccionFecha);
            tvPrecio = itemView.findViewById(R.id.tvTransaccionPrecio);
            tvEstado = itemView.findViewById(R.id.tvTransaccionEstado);
        }
    }
}
