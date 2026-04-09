package com.example.artistlan.Fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.model.ResolverSolicitudRequestDTO;
import com.example.artistlan.Conector.model.SolicitudDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.SolicitudesAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSolicitudesMensajes extends Fragment implements SolicitudesAdapter.Listener {

    public static final int MODO_RECIBIDAS = 0;
    public static final int MODO_ENVIADAS = 1;

    private enum ModoSolicitudes {
        RECIBIDAS,
        ENVIADAS
    }

    private RecyclerView recyclerSolicitudes;
    private ProgressBar progressSolicitudes;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;
    private TextView btnMarcarTodasLeidas;
    private TextView btnRecargar;
    private View segmentContainer;
    private View segmentIndicator;
    private Button btnRecibidas;
    private Button btnEnviadas;
    private View layoutAccionesLocal;

    private SolicitudesAdapter adapter;
    private SolicitudesApi solicitudesApi;
    private int idUsuario = -1;
    private ModoSolicitudes modoActual = ModoSolicitudes.RECIBIDAS;
    private Integer modoExternoPendiente = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_solicitudes_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        solicitudesApi = RetrofitClient.getClient().create(SolicitudesApi.class);

        recyclerSolicitudes = view.findViewById(R.id.recyclerSolicitudesMensajes);
        progressSolicitudes = view.findViewById(R.id.progressSolicitudesMensajes);
        emptyState = view.findViewById(R.id.layoutSolicitudesVacia);
        emptyTitle = view.findViewById(R.id.tvSolicitudesVaciaTitulo);
        emptySubtitle = view.findViewById(R.id.tvSolicitudesVaciaSubtitulo);
        btnMarcarTodasLeidas = view.findViewById(R.id.btnSolicitudesMarcarTodasLeidas);
        btnRecargar = view.findViewById(R.id.btnSolicitudesRecargar);

        segmentContainer = view.findViewById(R.id.segmentContainerSolicitudes);
        segmentIndicator = view.findViewById(R.id.segmentIndicatorSolicitudes);
        btnRecibidas = view.findViewById(R.id.btnSolicitudesRecibidas);
        btnEnviadas = view.findViewById(R.id.btnSolicitudesEnviadas);
        layoutAccionesLocal = view.findViewById(R.id.layoutSolicitudesAccionesLocal);

        recyclerSolicitudes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SolicitudesAdapter(this);
        recyclerSolicitudes.setAdapter(adapter);

        if (layoutAccionesLocal != null) {
            layoutAccionesLocal.setVisibility(View.GONE);
        }
        btnMarcarTodasLeidas.setVisibility(View.GONE);
        btnRecargar.setOnClickListener(v -> cargarSolicitudes());

        configurarSegmento();
        ModoSolicitudes inicial = modoExternoPendiente != null && modoExternoPendiente == MODO_ENVIADAS
                ? ModoSolicitudes.ENVIADAS
                : ModoSolicitudes.RECIBIDAS;
        modoExternoPendiente = null;
        view.post(() -> seleccionarModo(inicial, false));
    }

    private void configurarSegmento() {
        btnEnviadas.setOnClickListener(v -> seleccionarModo(ModoSolicitudes.ENVIADAS, true));
        btnRecibidas.setOnClickListener(v -> seleccionarModo(ModoSolicitudes.RECIBIDAS, true));
    }

    private void seleccionarModo(ModoSolicitudes modo, boolean animar) {
        if (!vistaListaInicializada()) {
            modoExternoPendiente = (modo == ModoSolicitudes.ENVIADAS) ? MODO_ENVIADAS : MODO_RECIBIDAS;
            modoActual = modo;
            return;
        }
        this.modoActual = modo;
        adapter.setModoLista(modo == ModoSolicitudes.RECIBIDAS
                ? SolicitudesAdapter.ModoLista.RECIBIDAS
                : SolicitudesAdapter.ModoLista.ENVIADAS);

        moverIndicador(modo == ModoSolicitudes.RECIBIDAS, animar);
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.artistlan_menu_text_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.artistlan_menu_text_secondary);
        btnEnviadas.setTextColor(modo == ModoSolicitudes.ENVIADAS ? selectedColor : defaultColor);
        btnRecibidas.setTextColor(modo == ModoSolicitudes.ENVIADAS ? defaultColor : selectedColor);
        cargarSolicitudes();
    }

    private void moverIndicador(boolean izquierda, boolean animar) {
        if (segmentContainer == null || segmentIndicator == null) {
            return;
        }
        int contWidth = segmentContainer.getWidth();
        int mitad = contWidth / 2;
        int nuevoInicio = izquierda ? mitad : 0;

        if (!animar) {
            segmentIndicator.setX(nuevoInicio);
            segmentIndicator.getLayoutParams().width = mitad;
            segmentIndicator.requestLayout();
            return;
        }

        ValueAnimator animator = ValueAnimator.ofInt((int) segmentIndicator.getX(), nuevoInicio);
        animator.setDuration(220);
        animator.addUpdateListener(a -> {
            int val = (int) a.getAnimatedValue();
            segmentIndicator.setX(val);
            segmentIndicator.getLayoutParams().width = mitad;
            segmentIndicator.requestLayout();
        });
        animator.start();
    }

    private void cargarSolicitudes() {
        if (!vistaListaInicializada()) {
            return;
        }
        if (idUsuario <= 0) {
            mostrarVacio("Sin sesion activa", "Inicia sesion para ver tus solicitudes.");
            return;
        }

        progressSolicitudes.setVisibility(View.VISIBLE);
        recyclerSolicitudes.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        Call<List<SolicitudDTO>> call = modoActual == ModoSolicitudes.RECIBIDAS
                ? solicitudesApi.obtenerSolicitudesRecibidas(idUsuario)
                : solicitudesApi.obtenerSolicitudesEnviadas(idUsuario);

        call.enqueue(new Callback<List<SolicitudDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<SolicitudDTO>> call, @NonNull Response<List<SolicitudDTO>> response) {
                if (!isAdded()) {
                    return;
                }
                progressSolicitudes.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    mostrarVacio("No se pudieron cargar solicitudes", "Verifica tu conexion y vuelve a intentar.");
                    return;
                }

                List<SolicitudDTO> solicitudes = ordenarPorFecha(response.body());
                if (solicitudes.isEmpty()) {
                    if (modoActual == ModoSolicitudes.RECIBIDAS) {
                        mostrarVacio("No tienes solicitudes recibidas", "Cuando recibas solicitudes de compra apareceran aqui.");
                    } else {
                        mostrarVacio("No has enviado solicitudes", "Las solicitudes que envies se mostraran aqui.");
                    }
                    return;
                }

                adapter.submitList(solicitudes);
                recyclerSolicitudes.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                refrescarBadge();
            }

            @Override
            public void onFailure(@NonNull Call<List<SolicitudDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progressSolicitudes.setVisibility(View.GONE);
                mostrarVacio("Error de conexion", "No fue posible cargar solicitudes.");
            }
        });
    }

    private List<SolicitudDTO> ordenarPorFecha(List<SolicitudDTO> source) {
        List<SolicitudDTO> salida = new ArrayList<>(source);
        Collections.sort(salida, new Comparator<SolicitudDTO>() {
            @Override
            public int compare(SolicitudDTO o1, SolicitudDTO o2) {
                return Long.compare(
                        MensajeUiUtils.toEpochMillis(o2 != null ? o2.getFecha() : null),
                        MensajeUiUtils.toEpochMillis(o1 != null ? o1.getFecha() : null)
                );
            }
        });
        return salida;
    }

    private void mostrarVacio(String titulo, String subtitulo) {
        adapter.submitList(new ArrayList<>());
        recyclerSolicitudes.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyTitle.setText(titulo);
        emptySubtitle.setText(subtitulo);
    }

    @Override
    public void onDetalle(@NonNull SolicitudDTO item) {
        if (item.getIdSolicitud() == null) {
            mostrarDetalle(item);
            return;
        }

        solicitudesApi.obtenerSolicitudPorId(item.getIdSolicitud(), idUsuario).enqueue(new Callback<SolicitudDTO>() {
            @Override
            public void onResponse(@NonNull Call<SolicitudDTO> call, @NonNull Response<SolicitudDTO> response) {
                if (!isAdded()) {
                    return;
                }
                SolicitudDTO detalle = response.isSuccessful() && response.body() != null ? response.body() : item;
                mostrarDetalle(detalle);
            }

            @Override
            public void onFailure(@NonNull Call<SolicitudDTO> call, @NonNull Throwable t) {
                if (isAdded()) {
                    mostrarDetalle(item);
                }
            }
        });
    }

    private void mostrarDetalle(@NonNull SolicitudDTO item) {
        boolean esRecibida = modoActual == ModoSolicitudes.RECIBIDAS;
        StringBuilder detalle = new StringBuilder();
        detalle.append("Estado: ").append(item.getEstadoVisual()).append("\n")
                .append("Fecha solicitud: ").append(MensajeUiUtils.formatearFechaCorta(item.getFecha())).append("\n")
                .append(esRecibida ? "De: " : "Para: ")
                .append(item.getNombreActorContextual(esRecibida)).append("\n")
                .append("Obra: ").append(item.getTituloSeguro()).append("\n")
                .append(esRecibida ? "Mensaje comprador: " : "Tu mensaje: ").append(item.getMensajeSeguro());

        appendBloqueSiTieneTexto(detalle, "Motivo rechazo", item.getMotivoRechazo());
        appendLineaSiTieneTexto(detalle, "Fecha respuesta", MensajeUiUtils.formatearFechaCorta(item.getFechaRespuesta()));
        appendLineaSiTieneTexto(detalle, "Expiracion reserva", MensajeUiUtils.formatearFechaCorta(item.getFechaExpiracionReserva()));

        if (item.getReferenciaTipo() != null
                && !item.getReferenciaTipo().trim().isEmpty()
                && item.getReferenciaId() != null) {
            detalle.append("\n").append(MensajeUiUtils.etiquetaReferencia(item.getReferenciaTipo(), item.getReferenciaId()));
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getTituloSeguro())
                .setMessage(detalle.toString())
                .setPositiveButton("Cerrar", null);

        Integer destino = resolverDestinoReferencia(item.getReferenciaTipo());
        if (destino != null) {
            builder.setNeutralButton("Ir a referencia", (dialog, which) -> navegarADestinoSeguro(destino));
        }
        builder.show();
    }

    private void appendLineaSiTieneTexto(@NonNull StringBuilder builder, @NonNull String etiqueta, @Nullable String valor) {
        if (valor == null || valor.trim().isEmpty() || "Fecha no disponible".equalsIgnoreCase(valor.trim())) {
            return;
        }
        builder.append("\n").append(etiqueta).append(": ").append(valor.trim());
    }

    private void appendBloqueSiTieneTexto(@NonNull StringBuilder builder, @NonNull String etiqueta, @Nullable String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return;
        }
        builder.append("\n").append(etiqueta).append(":\n").append(valor.trim());
    }

    private Integer resolverDestinoReferencia(String referenciaTipo) {
        if (referenciaTipo == null || referenciaTipo.trim().isEmpty()) {
            return null;
        }
        String tipo = referenciaTipo.trim().toLowerCase(Locale.ROOT);
        if (tipo.contains("obra")) {
            return R.id.fragArte;
        }
        return null;
    }

    private void navegarADestinoSeguro(int destinationId) {
        if (getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).navegarDesdeCentroMensajes(destinationId, null);
        }
    }

    @Override
    public void onAceptar(@NonNull SolicitudDTO item) {
        if (modoActual != ModoSolicitudes.RECIBIDAS || !item.correspondeAlVendedor(idUsuario)) {
            Toast.makeText(requireContext(), "Solo el vendedor puede aceptar esta solicitud.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!item.puedeSerResueltaPorVendedor()) {
            Toast.makeText(requireContext(), "Esta solicitud ya no puede ser aceptada.", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Aceptar solicitud")
                .setMessage("Al aceptar, otras solicitudes pendientes de esta obra pueden cerrarse automaticamente.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialog, which) -> ejecutarAceptar(item))
                .show();
    }

    @Override
    public void onRechazar(@NonNull SolicitudDTO item) {
        if (modoActual != ModoSolicitudes.RECIBIDAS || !item.correspondeAlVendedor(idUsuario)) {
            Toast.makeText(requireContext(), "Solo el vendedor puede rechazar esta solicitud.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!item.puedeSerResueltaPorVendedor()) {
            Toast.makeText(requireContext(), "Esta solicitud ya no puede ser rechazada.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText inputMotivo = new EditText(requireContext());
        inputMotivo.setHint("Motivo opcional");
        inputMotivo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputMotivo.setMinLines(2);
        inputMotivo.setMaxLines(4);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rechazar solicitud")
                .setMessage("Puedes agregar un motivo opcional.")
                .setView(inputMotivo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Rechazar", (dialog, which) -> {
                    String motivo = inputMotivo.getText() != null ? inputMotivo.getText().toString().trim() : null;
                    ejecutarRechazar(item, (motivo == null || motivo.isEmpty()) ? null : motivo);
                })
                .show();
    }

    @Override
    public void onCancelar(@NonNull SolicitudDTO item) {
        if (modoActual != ModoSolicitudes.ENVIADAS || !item.correspondeAlComprador(idUsuario)) {
            Toast.makeText(requireContext(), "Solo el comprador puede cancelar su solicitud.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!item.puedeSerCanceladaPorComprador()) {
            Toast.makeText(requireContext(), "Esta solicitud ya no puede cancelarse.", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancelar solicitud")
                .setMessage("Vas a cancelar esta solicitud de compra.")
                .setNegativeButton("Volver", null)
                .setPositiveButton("Cancelar solicitud", (dialog, which) -> ejecutarCancelar(item))
                .show();
    }

    private void ejecutarAceptar(@NonNull SolicitudDTO item) {
        ResolverSolicitudRequestDTO body = new ResolverSolicitudRequestDTO(idUsuario);
        solicitudesApi.aceptarSolicitud(item.getIdSolicitud(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo aceptar la solicitud.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(requireContext(), "Solicitud aceptada.", Toast.LENGTH_SHORT).show();
                refrescarBadge();
                cargarSolicitudes();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al aceptar solicitud.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ejecutarRechazar(@NonNull SolicitudDTO item, @Nullable String motivo) {
        ResolverSolicitudRequestDTO body = new ResolverSolicitudRequestDTO(idUsuario, motivo);
        solicitudesApi.rechazarSolicitud(item.getIdSolicitud(), body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo rechazar la solicitud.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(requireContext(), "Solicitud rechazada.", Toast.LENGTH_SHORT).show();
                refrescarBadge();
                cargarSolicitudes();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al rechazar solicitud.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ejecutarCancelar(@NonNull SolicitudDTO item) {
        solicitudesApi.cancelarSolicitud(item.getIdSolicitud(), idUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo cancelar la solicitud.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(requireContext(), "Solicitud cancelada.", Toast.LENGTH_SHORT).show();
                refrescarBadge();
                cargarSolicitudes();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al cancelar solicitud.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void revisarVacio() {
        if (!adapter.getItems().isEmpty()) {
            return;
        }
        if (modoActual == ModoSolicitudes.RECIBIDAS) {
            mostrarVacio("No tienes solicitudes recibidas", "Cuando recibas solicitudes de compra apareceran aqui.");
        } else {
            mostrarVacio("No has enviado solicitudes", "Las solicitudes que envies se mostraran aqui.");
        }
    }

    private void refrescarBadge() {
        if (getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).refrescarBadgeMensajes();
        }
        if (getParentFragment() instanceof FragCentroMensajes) {
            ((FragCentroMensajes) getParentFragment()).refrescarResumenContadores();
        }
    }

    public void recargarDesdeHeader() {
        if (!vistaListaInicializada()) {
            return;
        }
        cargarSolicitudes();
    }

    public void seleccionarModoExterno(int modo) {
        if (!isAdded()) {
            modoExternoPendiente = modo;
            return;
        }
        ModoSolicitudes destino = modo == MODO_ENVIADAS ? ModoSolicitudes.ENVIADAS : ModoSolicitudes.RECIBIDAS;
        if (!vistaListaInicializada()) {
            modoExternoPendiente = modo;
            return;
        }
        if (segmentContainer != null) {
            segmentContainer.post(() -> seleccionarModo(destino, false));
        } else {
            seleccionarModo(destino, false);
        }
    }

    private boolean vistaListaInicializada() {
        return isAdded()
                && getView() != null
                && adapter != null
                && recyclerSolicitudes != null
                && progressSolicitudes != null
                && emptyState != null
                && btnRecibidas != null
                && btnEnviadas != null;
    }
}
