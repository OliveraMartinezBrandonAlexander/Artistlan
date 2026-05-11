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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.model.PageResponseSolicitudDTO;
import com.example.artistlan.Conector.model.ResolverSolicitudRequestDTO;
import com.example.artistlan.Conector.model.SolicitudDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.adapter.SolicitudesAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragSolicitudesMensajes extends Fragment implements SolicitudesAdapter.Listener {

    public static final int MODO_RECIBIDAS = 0;
    public static final int MODO_ENVIADAS = 1;

    private static final int PAGE_SIZE = 10;
    private static final String SORT_DEFAULT = "fechaCreacion,desc";

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
    private TextView btnCargarMasSolicitudes;
    private View layoutLoaderMasSolicitudes;

    private SolicitudesAdapter adapter;
    private SolicitudesApi solicitudesApi;
    private int idUsuario = -1;
    private ModoSolicitudes modoActual = ModoSolicitudes.RECIBIDAS;
    private Integer modoExternoPendiente = null;

    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int requestToken = 0;
    private String estadoFiltroActual = null;
    private final List<SolicitudDTO> solicitudesAcumuladas = new ArrayList<>();
    private final Set<Integer> idsSolicitudesCargadas = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_solicitudes_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

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
        btnCargarMasSolicitudes = view.findViewById(R.id.btnCargarMasSolicitudes);
        layoutLoaderMasSolicitudes = view.findViewById(R.id.layoutLoaderMasSolicitudes);

        recyclerSolicitudes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SolicitudesAdapter(this);
        recyclerSolicitudes.setAdapter(adapter);

        if (layoutAccionesLocal != null) {
            layoutAccionesLocal.setVisibility(View.GONE);
        }
        if (btnMarcarTodasLeidas != null) {
            btnMarcarTodasLeidas.setVisibility(View.GONE);
        }
        if (btnRecargar != null) {
            btnRecargar.setOnClickListener(v -> cargarSolicitudes());
        }
        if (btnCargarMasSolicitudes != null) {
            btnCargarMasSolicitudes.setOnClickListener(v -> {
                if (isLoading || isLastPage) {
                    return;
                }
                cargarPagina(nextPageToLoad);
            });
        }

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
        animator.setInterpolator(new FastOutSlowInInterpolator());
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
            mostrarVacio("Sin sesi\u00F3n activa", "Inicia sesi\u00F3n para ver tus solicitudes.");
            return;
        }
        reiniciarYCargarPrimeraPagina();
    }

    private void reiniciarYCargarPrimeraPagina() {
        requestToken++;
        nextPageToLoad = 0;
        isLastPage = false;
        isLoading = false;

        solicitudesAcumuladas.clear();
        idsSolicitudesCargadas.clear();
        adapter.submitList(new ArrayList<>());

        mostrarBotonCargarMas(false, false);
        mostrarLoaderMasSolicitudes(false);
        emptyState.setVisibility(View.GONE);

        cargarPagina(0);
    }

    private void cargarPagina(int pageObjetivo) {
        if (isLoading || (isLastPage && pageObjetivo > 0)) {
            return;
        }

        isLoading = true;
        if (pageObjetivo == 0) {
            progressSolicitudes.setVisibility(View.VISIBLE);
            recyclerSolicitudes.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
            mostrarLoaderMasSolicitudes(false);
            mostrarBotonCargarMas(false, false);
        } else {
            progressSolicitudes.setVisibility(View.GONE);
            recyclerSolicitudes.setVisibility(View.VISIBLE);
            mostrarLoaderMasSolicitudes(true);
            mostrarBotonCargarMas(false, false);
        }

        final int tokenLocal = ++requestToken;
        Call<PageResponseSolicitudDTO> call = modoActual == ModoSolicitudes.RECIBIDAS
                ? solicitudesApi.obtenerSolicitudesRecibidasPaginadas(idUsuario, pageObjetivo, PAGE_SIZE, SORT_DEFAULT, estadoFiltroActual)
                : solicitudesApi.obtenerSolicitudesEnviadasPaginadas(idUsuario, pageObjetivo, PAGE_SIZE, SORT_DEFAULT, estadoFiltroActual);

        call.enqueue(new Callback<PageResponseSolicitudDTO>() {
            @Override
            public void onResponse(@NonNull Call<PageResponseSolicitudDTO> call, @NonNull Response<PageResponseSolicitudDTO> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                progressSolicitudes.setVisibility(View.GONE);
                mostrarLoaderMasSolicitudes(false);

                if (!response.isSuccessful() || response.body() == null) {
                    if (pageObjetivo == 0) {
                        mostrarVacio("No se pudieron cargar solicitudes", "Verifica tu conexi\u00F3n y vuelve a intentar.");
                    } else {
                        mostrarBotonCargarMas(true, true);
                        Toast.makeText(requireContext(), "No se pudo cargar m\u00E1s solicitudes.", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                PageResponseSolicitudDTO pageResponse = response.body();
                List<SolicitudDTO> nuevos = sanitizar(pageResponse.getContent());

                if (pageObjetivo == 0) {
                    solicitudesAcumuladas.clear();
                    idsSolicitudesCargadas.clear();
                }

                List<SolicitudDTO> agregados = new ArrayList<>();
                for (SolicitudDTO item : nuevos) {
                    Integer idSolicitud = item.getIdSolicitud();
                    if (idSolicitud != null && idsSolicitudesCargadas.contains(idSolicitud)) {
                        continue;
                    }
                    if (idSolicitud != null) {
                        idsSolicitudesCargadas.add(idSolicitud);
                    }
                    solicitudesAcumuladas.add(item);
                    agregados.add(item);
                }

                if (pageObjetivo == 0) {
                    adapter.submitList(new ArrayList<>(solicitudesAcumuladas));
                } else {
                    adapter.agregarItems(agregados);
                }

                nextPageToLoad = pageObjetivo + 1;
                isLastPage = pageResponse.isLast();

                if (solicitudesAcumuladas.isEmpty()) {
                    mostrarVacio(tituloVacioPorModo(), subtituloVacioPorModo());
                    mostrarBotonCargarMas(false, false);
                } else {
                    recyclerSolicitudes.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    mostrarBotonCargarMas(!isLastPage, false);
                }

                refrescarBadge();
            }

            @Override
            public void onFailure(@NonNull Call<PageResponseSolicitudDTO> call, @NonNull Throwable t) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }
                isLoading = false;
                progressSolicitudes.setVisibility(View.GONE);
                mostrarLoaderMasSolicitudes(false);
                if (pageObjetivo == 0) {
                    mostrarVacio("Error de conexi\u00F3n", "No fue posible cargar solicitudes.");
                } else {
                    mostrarBotonCargarMas(true, true);
                    Toast.makeText(requireContext(), "Error de red al cargar m\u00E1s solicitudes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<SolicitudDTO> sanitizar(List<SolicitudDTO> source) {
        List<SolicitudDTO> salida = new ArrayList<>();
        if (source == null) {
            return salida;
        }
        for (SolicitudDTO item : source) {
            if (item != null) {
                salida.add(item);
            }
        }
        return salida;
    }

    private String tituloVacioPorModo() {
        return modoActual == ModoSolicitudes.RECIBIDAS
                ? "No tienes solicitudes recibidas"
                : "No has enviado solicitudes";
    }

    private String subtituloVacioPorModo() {
        return modoActual == ModoSolicitudes.RECIBIDAS
                ? "Cuando recibas solicitudes de compra aparecer\u00E1n aqu\u00ED."
                : "Las solicitudes que env\u00EDes se mostrar\u00E1n aqu\u00ED.";
    }

    private void mostrarBotonCargarMas(boolean mostrar, boolean reintento) {
        if (btnCargarMasSolicitudes == null) {
            return;
        }
        btnCargarMasSolicitudes.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            btnCargarMasSolicitudes.setText(reintento
                    ? "Reintentar cargar m\u00E1s solicitudes"
                    : "Cargar m\u00E1s solicitudes");
        }
    }

    private void mostrarLoaderMasSolicitudes(boolean mostrar) {
        if (layoutLoaderMasSolicitudes == null) {
            return;
        }
        layoutLoaderMasSolicitudes.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarVacio(String titulo, String subtitulo) {
        adapter.submitList(new ArrayList<>());
        solicitudesAcumuladas.clear();
        idsSolicitudesCargadas.clear();
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
                .append("Fecha de solicitud: ").append(MensajeUiUtils.formatearFechaCorta(item.getFecha())).append("\n")
                .append(esRecibida ? "De: " : "Para: ")
                .append(item.getNombreActorContextual(esRecibida)).append("\n")
                .append("Obra: ").append(item.getTituloSeguro()).append("\n")
                .append(esRecibida ? "Mensaje comprador: " : "Tu mensaje: ").append(item.getMensajeSeguro());

        appendBloqueSiTieneTexto(detalle, "Motivo rechazo", item.getMotivoRechazo());
        appendLineaSiTieneTexto(detalle, "Fecha respuesta", MensajeUiUtils.formatearFechaCorta(item.getFechaRespuesta()));
        appendLineaSiTieneTexto(detalle, "Expiraci\u00F3n de reserva", MensajeUiUtils.formatearFechaCorta(item.getFechaExpiracionReserva()));

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
                .setMessage("Al aceptar, otras solicitudes pendientes de esta obra pueden cerrarse autom\u00E1ticamente.")
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
                item.marcarComoAtendida(true);
                adapter.notificarCambioPorId(item.getIdSolicitud());
                Toast.makeText(requireContext(), "Solicitud aceptada.", Toast.LENGTH_SHORT).show();
                refrescarBadge();
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
                item.marcarComoAtendida(false);
                adapter.notificarCambioPorId(item.getIdSolicitud());
                Toast.makeText(requireContext(), "Solicitud rechazada.", Toast.LENGTH_SHORT).show();
                refrescarBadge();
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
                item.marcarComoCancelada();
                adapter.notificarCambioPorId(item.getIdSolicitud());
                Toast.makeText(requireContext(), "Solicitud cancelada.", Toast.LENGTH_SHORT).show();
                refrescarBadge();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al cancelar solicitud.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
