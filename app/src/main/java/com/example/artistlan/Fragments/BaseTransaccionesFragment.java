package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.TransaccionApi;
import com.example.artistlan.Conector.model.TransaccionDetalleDTO;
import com.example.artistlan.Conector.model.TransaccionResumenDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.TransaccionAdapter;
import com.example.artistlan.pagos.PagoSyncManager;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseTransaccionesFragment extends Fragment {

    private RecyclerView recyclerTransacciones;
    private TextView tvTransaccionesVacias;
    private ProgressBar progressTransacciones;
    private TransaccionAdapter adapter;
    private TransaccionApi transaccionApi;
    private int idUsuarioLogueado = -1;
    private long ultimaVersionPagoCargada = 0L;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @LayoutRes
    protected abstract int getLayoutResId();

    protected abstract Call<List<TransaccionResumenDTO>> crearLlamada(TransaccionApi api, int idUsuario);

    protected abstract String getMensajeVacio();

    protected abstract String getMensajeError();

    protected abstract TransaccionAdapter.TipoLista getTipoLista();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        transaccionApi = RetrofitClient.getClient().create(TransaccionApi.class);
        ultimaVersionPagoCargada = PagoSyncManager.getLastCaptureAt(requireContext());

        recyclerTransacciones = view.findViewById(R.id.recyclerTransacciones);
        tvTransaccionesVacias = view.findViewById(R.id.tvTransaccionesVacias);
        progressTransacciones = view.findViewById(R.id.progressTransacciones);

        recyclerTransacciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransacciones.setHasFixedSize(true);

        adapter = new TransaccionAdapter(
                requireContext(),
                getTipoLista(),
                new ArrayList<>(),
                this::onTransaccionClick
        );
        recyclerTransacciones.setAdapter(adapter);

        cargarTransacciones();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdded()) {
            return;
        }
        long ultimaVersionGlobal = PagoSyncManager.getLastCaptureAt(requireContext());
        if (ultimaVersionGlobal > ultimaVersionPagoCargada) {
            ultimaVersionPagoCargada = ultimaVersionGlobal;
            cargarTransacciones();
        }
    }

    public void recargarDespuesDePago() {
        if (!isAdded()) {
            return;
        }
        ultimaVersionPagoCargada = PagoSyncManager.getLastCaptureAt(requireContext());
        cargarTransacciones();
    }

    private void cargarTransacciones() {
        if (idUsuarioLogueado <= 0) {
            mostrarEstadoVacio(getString(R.string.transacciones_sin_sesion));
            return;
        }

        progressTransacciones.setVisibility(View.VISIBLE);
        recyclerTransacciones.setVisibility(View.GONE);
        tvTransaccionesVacias.setVisibility(View.GONE);

        crearLlamada(transaccionApi, idUsuarioLogueado).enqueue(new Callback<List<TransaccionResumenDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<TransaccionResumenDTO>> call, @NonNull Response<List<TransaccionResumenDTO>> response) {
                if (!isAdded()) {
                    return;
                }

                progressTransacciones.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : getMensajeError() + " (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    mostrarEstadoMensaje(getMensajeError());
                    return;
                }

                if (response.code() == 204) {
                    mostrarEstadoVacio(getMensajeVacio());
                    return;
                }

                List<TransaccionResumenDTO> transacciones = response.body();
                if (transacciones == null || transacciones.isEmpty()) {
                    mostrarEstadoVacio(getMensajeVacio());
                    return;
                }

                adapter.actualizarLista(transacciones);
                recyclerTransacciones.setVisibility(View.VISIBLE);
                tvTransaccionesVacias.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<List<TransaccionResumenDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }

                progressTransacciones.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                mostrarEstadoMensaje(getMensajeError());
            }
        });
    }

    private void onTransaccionClick(TransaccionResumenDTO item, int position) {
        if (item == null || item.getIdTransaccion() == null || item.getIdTransaccion() <= 0) {
            Toast.makeText(requireContext(), "No se pudo abrir el detalle de la transaccion", Toast.LENGTH_SHORT).show();
            return;
        }
        cargarDetalleTransaccion(item);
    }

    private void cargarDetalleTransaccion(TransaccionResumenDTO resumen) {
        String tipoOrigen = resumen != null ? resumen.getTipoOrigen() : null;
        if (tipoOrigen == null || tipoOrigen.trim().isEmpty()) {
            Toast.makeText(requireContext(), "No se pudo identificar el tipo de transaccion.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressTransacciones.setVisibility(View.VISIBLE);
        transaccionApi.obtenerDetalleTransaccion(
                idUsuarioLogueado,
                tipoOrigen,
                resumen.getIdTransaccion()
        ).enqueue(new Callback<TransaccionDetalleDTO>() {
            @Override
            public void onResponse(@NonNull Call<TransaccionDetalleDTO> call, @NonNull Response<TransaccionDetalleDTO> response) {
                if (!isAdded()) {
                    return;
                }
                progressTransacciones.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se pudo cargar el detalle (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                mostrarDialogoDetalle(resumen, response.body());
            }

            @Override
            public void onFailure(@NonNull Call<TransaccionDetalleDTO> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progressTransacciones.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de red al cargar detalle: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoDetalle(TransaccionResumenDTO resumen, TransaccionDetalleDTO detalle) {
        if (!isAdded()) {
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_transaccion_detalle, null, false);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        ImageView imgObra = dialogView.findViewById(R.id.imgDetalleObra);
        TextView tvTitulo = dialogView.findViewById(R.id.tvDetalleTituloObra);
        TextView tvMonto = dialogView.findViewById(R.id.tvDetalleMonto);
        TextView tvEstado = dialogView.findViewById(R.id.tvDetalleEstado);
        TextView tvOrderId = dialogView.findViewById(R.id.tvDetallePaypalOrderId);
        TextView tvCaptureId = dialogView.findViewById(R.id.tvDetallePaypalCaptureId);
        TextView tvFechaCreacion = dialogView.findViewById(R.id.tvDetalleFechaCreacion);
        TextView tvFechaCaptura = dialogView.findViewById(R.id.tvDetalleFechaCaptura);
        ImageView ivComprador = dialogView.findViewById(R.id.ivDetalleComprador);
        TextView tvCompradorUsuario = dialogView.findViewById(R.id.tvDetalleCompradorUsuario);
        TextView tvCompradorSecundario = dialogView.findViewById(R.id.tvDetalleCompradorSecundario);
        ImageView ivVendedor = dialogView.findViewById(R.id.ivDetalleVendedor);
        TextView tvVendedorUsuario = dialogView.findViewById(R.id.tvDetalleVendedorUsuario);
        TextView tvVendedorSecundario = dialogView.findViewById(R.id.tvDetalleVendedorSecundario);

        String titulo = safe(detalle != null ? detalle.getTituloObra() : null, resumen != null ? resumen.getTituloObra() : null, "Obra sin titulo");
        String imagen = safe(detalle != null ? detalle.getImagenObra() : null, resumen != null ? resumen.getImagenObra() : null, "");
        Double monto = detalle != null ? detalle.getMonto() : null;
        if (monto == null && resumen != null) {
            monto = resumen.getPrecio();
        }

        String estadoRaw = safe(detalle != null ? detalle.getEstado() : null, resumen != null ? resumen.getEstado() : null, "No disponible");
        String paypalOrderId = safe(detalle != null ? detalle.getPaypalOrderId() : null, "-", "-");
        String paypalCaptureId = safe(detalle != null ? detalle.getPaypalCaptureId() : null, "-", "-");
        String fechaCreacion = formatearFechaHora(safe(detalle != null ? detalle.getFechaCreacion() : null, resumen != null ? resumen.getFechaTransaccion() : null, "-"));
        String fechaCaptura = formatearFechaHora(safe(detalle != null ? detalle.getFechaCaptura() : null, "-", "-"));

        String compradorUsuario = safe(detalle != null ? detalle.getUsuarioComprador() : null, "No disponible");
        String compradorNombre = safe(detalle != null ? detalle.getNombreComprador() : null, "");
        String vendedorUsuario = safe(detalle != null ? detalle.getUsuarioVendedor() : null, "No disponible");
        String vendedorNombre = safe(detalle != null ? detalle.getNombreVendedor() : null, "");

        tvTitulo.setText(titulo);
        tvMonto.setText(monto != null ? currencyFormatter.format(monto) : "No disponible");
        tvEstado.setText(formatearEstadoVisual(estadoRaw));
        tvOrderId.setText(paypalOrderId);
        tvCaptureId.setText(paypalCaptureId);
        tvFechaCreacion.setText(fechaCreacion);
        tvFechaCaptura.setText(fechaCaptura);
        tvCompradorUsuario.setText(compradorUsuario);
        tvVendedorUsuario.setText(vendedorUsuario);
        bindSecundarioNombre(tvCompradorSecundario, compradorNombre, compradorUsuario);
        bindSecundarioNombre(tvVendedorSecundario, vendedorNombre, vendedorUsuario);

        Glide.with(dialogView)
                .load(imagen)
                .placeholder(R.drawable.imagencargaobras)
                .error(R.drawable.imagencargaobras)
                .centerCrop()
                .into(imgObra);

        Glide.with(dialogView)
                .load(detalle != null ? detalle.getFotoComprador() : null)
                .placeholder(R.drawable.cuenta)
                .error(R.drawable.cuenta)
                .circleCrop()
                .into(ivComprador);

        Glide.with(dialogView)
                .load(detalle != null ? detalle.getFotoVendedor() : null)
                .placeholder(R.drawable.cuenta)
                .error(R.drawable.cuenta)
                .circleCrop()
                .into(ivVendedor);

        Button btnCerrar = dialogView.findViewById(R.id.btnDetalleCerrar);
        Button btnAbrirPaypal = dialogView.findViewById(R.id.btnDetalleAbrirPaypal);
        Button btnContactar = dialogView.findViewById(R.id.btnDetalleContactar);
        Button btnReportar = dialogView.findViewById(R.id.btnDetalleReportar);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnAbrirPaypal.setOnClickListener(v -> abrirPaypalWeb());
        btnReportar.setOnClickListener(v -> Toast.makeText(requireContext(),
                "Esta funcion estara disponible proximamente",
                Toast.LENGTH_SHORT).show());
        btnContactar.setOnClickListener(v -> contactarContraparte(detalle));

        dialog.show();
    }

    private void contactarContraparte(TransaccionDetalleDTO detalle) {
        if (detalle == null) {
            Toast.makeText(requireContext(), "No hay datos de contacto disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        ContactoInfo contacto = getTipoLista() == TransaccionAdapter.TipoLista.COMPRAS
                ? new ContactoInfo(detalle.getNombreVendedor(), detalle.getUsuarioVendedor(), detalle.getCorreoVendedor(), detalle.getTelefonoVendedor())
                : new ContactoInfo(detalle.getNombreComprador(), detalle.getUsuarioComprador(), detalle.getCorreoComprador(), detalle.getTelefonoComprador());

        if (!TextUtils.isEmpty(contacto.correo)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + contacto.correo));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Artistlan - Seguimiento de transaccion");
            iniciarIntentSeguro(intent, "No se pudo abrir correo para contactar");
            return;
        }

        if (!TextUtils.isEmpty(contacto.telefono)) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contacto.telefono));
            iniciarIntentSeguro(intent, "No se pudo abrir telefono para contactar");
            return;
        }

        String alias = safe(contacto.usuario, contacto.nombre, "");
        if (!alias.isEmpty()) {
            Toast.makeText(requireContext(), "Contacto disponible: " + alias, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(requireContext(), "No hay datos de contacto disponibles", Toast.LENGTH_SHORT).show();
    }

    private void abrirPaypalWeb() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com"));
        iniciarIntentSeguro(intent, "No se pudo abrir PayPal");
    }

    private void iniciarIntentSeguro(Intent intent, String mensajeError) {
        try {
            startActivity(intent);
        } catch (Exception ignored) {
            Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_SHORT).show();
        }
    }

    private String formatearFechaHora(String fechaRaw) {
        if (fechaRaw == null || fechaRaw.trim().isEmpty() || "-".equals(fechaRaw.trim())) {
            return "-";
        }

        String valor = fechaRaw.trim();
        Date fecha = intentarParseo(valor,
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd");

        if (fecha == null) {
            return valor.replace('T', ' ');
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "MX")).format(fecha);
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

    private String formatearEstadoVisual(String estadoRaw) {
        if (estadoRaw == null || estadoRaw.trim().isEmpty()) {
            return "No disponible";
        }
        String estado = estadoRaw.trim().toLowerCase(Locale.ROOT).replace("_", " ");
        if (estado.contains("pend")) return "Pendiente";
        if (estado.contains("aprob") || estado.contains("acept")) return "Aprobada";
        if (estado.contains("complet")) return "Completada";
        if (estado.contains("cancel")) return "Cancelada";
        if (estado.contains("rechaz")) return "Rechazada";
        return Character.toUpperCase(estado.charAt(0)) + estado.substring(1);
    }

    private String safe(String primary, String secondary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) {
            return primary.trim();
        }
        if (secondary != null && !secondary.trim().isEmpty()) {
            return secondary.trim();
        }
        return fallback;
    }

    private String safe(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return fallback;
    }

    private void bindSecundarioNombre(TextView view, String nombre, String usuario) {
        if (view == null) {
            return;
        }
        String nombreTrim = nombre != null ? nombre.trim() : "";
        String usuarioTrim = usuario != null ? usuario.trim() : "";
        if (nombreTrim.isEmpty() || nombreTrim.equalsIgnoreCase(usuarioTrim)) {
            view.setVisibility(View.GONE);
            view.setText("");
            return;
        }
        view.setText(nombreTrim);
        view.setVisibility(View.VISIBLE);
    }

    private void mostrarEstadoVacio(String mensaje) {
        mostrarEstadoMensaje(mensaje);
    }

    private void mostrarEstadoMensaje(String mensaje) {
        adapter.actualizarLista(new ArrayList<>());
        recyclerTransacciones.setVisibility(View.GONE);
        tvTransaccionesVacias.setText(mensaje);
        tvTransaccionesVacias.setVisibility(View.VISIBLE);
    }

    private static class ContactoInfo {
        final String nombre;
        final String usuario;
        final String correo;
        final String telefono;

        ContactoInfo(String nombre, String usuario, String correo, String telefono) {
            this.nombre = nombre;
            this.usuario = usuario;
            this.correo = correo;
            this.telefono = telefono;
        }
    }
}
