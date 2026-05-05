package com.example.artistlan.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.ModeracionApi;
import com.example.artistlan.Conector.model.ReporteDetalleDTO;
import com.example.artistlan.Conector.model.ResolverReporteRequestDTO;
import com.example.artistlan.Conector.model.RespuestaModeracionDTO;
import com.example.artistlan.Conector.model.TomarReporteRequestDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.ModeracionUiMapper;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragDetalleReporteModeracion extends Fragment {

    private static final String ARG_ID_REPORTE = "idReporte";
    private static final Locale LOCALE_ES_MX = new Locale("es", "MX");
    private static final String ACTION_SUSPENDER_USUARIO = "SUSPENDER_USUARIO";

    private NestedScrollView detailScrollView;
    private View contenedorDetalle;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvIdReporte;
    private TextView tvTipoObjetivo;
    private TextView tvTituloObjetivo;
    private TextView tvDescripcionObjetivo;
    private ImageView imgObjetivo;
    private TextView tvReportante;
    private TextView tvUsuarioReportado;
    private TextView tvMotivo;
    private TextView tvDescripcionReporte;
    private TextView tvEstado;
    private TextView tvPrioridad;
    private TextView tvModeradorAsignado;
    private TextView tvFechaReporte;
    private TextView tvFechaInicioRevision;
    private TextView tvAccionResolucion;
    private TextView tvMensajeRespuesta;
    private TextView tvFechaResolucion;
    private Button btnTomarReporte;

    private View cardResolverReporte;
    private Spinner spinnerAccionResolver;
    private EditText etMensajeRespuesta;
    private EditText etMotivoAccion;
    private View contenedorFechaFinSuspension;
    private EditText etFechaFinSuspension;
    private Button btnResolverReporte;

    private ModeracionApi moderacionApi;
    private int idReporte = -1;
    private int idUsuarioActual = -1;
    private String rolActual = "USER";
    private ReporteDetalleDTO reporteActual;
    private final List<AccionResolucionOption> opcionesResolucionActuales = new ArrayList<>();
    private String fechaFinSuspensionBackend;
    private int previousSoftInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

    public FragDetalleReporteModeracion() {
        super(R.layout.fragment_frag_detalle_reporte_moderacion);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        moderacionApi = RetrofitClient.getClient().create(ModeracionApi.class);
        bindViews(view);
        cargarSesionActual();

        Bundle args = getArguments();
        idReporte = args != null ? args.getInt(ARG_ID_REPORTE, -1) : -1;

        btnTomarReporte.setOnClickListener(v -> tomarReporte());
        btnResolverReporte.setOnClickListener(v -> resolverReporte());
        etFechaFinSuspension.setOnClickListener(v -> abrirSelectorFechaFinSuspension());
        spinnerAccionResolver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                actualizarCampoFechaFinSuspension();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                actualizarCampoFechaFinSuspension();
            }
        });

        configurarScrollYTeclado();
        ocultarSeccionResolucion();
        validarPermisosYCargar();
    }

    private void bindViews(@NonNull View view) {
        detailScrollView = view.findViewById(R.id.scrollDetalleReporteModeracion);
        contenedorDetalle = view.findViewById(R.id.contenedorDetalleReporteModeracion);
        progressBar = view.findViewById(R.id.progressDetalleReporteModeracion);
        tvError = view.findViewById(R.id.tvErrorDetalleReporteModeracion);
        tvIdReporte = view.findViewById(R.id.tvIdDetalleReporteModeracion);
        tvTipoObjetivo = view.findViewById(R.id.tvTipoObjetivoDetalleModeracion);
        tvTituloObjetivo = view.findViewById(R.id.tvTituloObjetivoDetalleModeracion);
        tvDescripcionObjetivo = view.findViewById(R.id.tvDescripcionObjetivoDetalleModeracion);
        imgObjetivo = view.findViewById(R.id.imgObjetivoDetalleModeracion);
        tvReportante = view.findViewById(R.id.tvReportanteDetalleModeracion);
        tvUsuarioReportado = view.findViewById(R.id.tvUsuarioReportadoDetalleModeracion);
        tvMotivo = view.findViewById(R.id.tvMotivoDetalleModeracion);
        tvDescripcionReporte = view.findViewById(R.id.tvDescripcionReporteDetalleModeracion);
        tvEstado = view.findViewById(R.id.tvEstadoDetalleModeracion);
        tvPrioridad = view.findViewById(R.id.tvPrioridadDetalleModeracion);
        tvModeradorAsignado = view.findViewById(R.id.tvModeradorAsignadoDetalleModeracion);
        tvFechaReporte = view.findViewById(R.id.tvFechaReporteDetalleModeracion);
        tvFechaInicioRevision = view.findViewById(R.id.tvFechaInicioRevisionDetalleModeracion);
        tvAccionResolucion = view.findViewById(R.id.tvAccionResolucionDetalleModeracion);
        tvMensajeRespuesta = view.findViewById(R.id.tvMensajeRespuestaDetalleModeracion);
        tvFechaResolucion = view.findViewById(R.id.tvFechaResolucionDetalleModeracion);
        btnTomarReporte = view.findViewById(R.id.btnTomarReporteModeracion);

        cardResolverReporte = view.findViewById(R.id.cardResolverReporteModeracion);
        spinnerAccionResolver = view.findViewById(R.id.spinnerAccionResolverModeracion);
        etMensajeRespuesta = view.findViewById(R.id.etMensajeRespuestaModeracion);
        etMotivoAccion = view.findViewById(R.id.etMotivoAccionModeracion);
        contenedorFechaFinSuspension = view.findViewById(R.id.contenedorFechaFinSuspensionModeracion);
        etFechaFinSuspension = view.findViewById(R.id.etFechaFinSuspensionModeracion);
        btnResolverReporte = view.findViewById(R.id.btnResolverReporteModeracion);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity().getWindow() != null) {
            previousSoftInputMode = requireActivity().getWindow().getAttributes().softInputMode;
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onPause() {
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(previousSoftInputMode);
        }
        super.onPause();
    }

    private void cargarSesionActual() {
        SessionManager sessionManager = new SessionManager(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        idUsuarioActual = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        rolActual = prefs.getString("rol", "USER");

        if (rolActual == null || rolActual.trim().isEmpty()) {
            rolActual = "USER";
        }
        if (!sessionManager.isLoggedIn()) {
            idUsuarioActual = -1;
        }
    }

    private void validarPermisosYCargar() {
        if (!tienePermisoModeracion()) {
            mostrarError("No tienes permisos para ver moderaci\u00f3n.");
            return;
        }
        if (idReporte <= 0) {
            mostrarError("No se encontr\u00f3 el reporte solicitado.");
            return;
        }
        cargarDetalleReporte();
    }

    private boolean tienePermisoModeracion() {
        return idUsuarioActual > 0 && ("ADMIN".equalsIgnoreCase(rolActual) || "MODERADOR".equalsIgnoreCase(rolActual));
    }

    private void cargarDetalleReporte() {
        mostrarCarga(true);
        moderacionApi.obtenerDetalleReporte(idReporte, idUsuarioActual).enqueue(new Callback<ReporteDetalleDTO>() {
            @Override
            public void onResponse(@NonNull Call<ReporteDetalleDTO> call, @NonNull Response<ReporteDetalleDTO> response) {
                mostrarCarga(false);

                if (response.isSuccessful() && response.body() != null) {
                    reporteActual = response.body();
                    renderizarDetalle(reporteActual);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "No tienes permisos para ver este reporte.";
                } else if (response.code() == 404) {
                    mensaje = backendMessage != null ? backendMessage : "Reporte no encontrado.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo cargar el detalle del reporte.";
                }
                mostrarError(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<ReporteDetalleDTO> call, @NonNull Throwable t) {
                mostrarCarga(false);
                mostrarError("Error de conexi\u00f3n al cargar el detalle del reporte.");
            }
        });
    }

    private void renderizarDetalle(@NonNull ReporteDetalleDTO reporte) {
        contenedorDetalle.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        tvIdReporte.setText("Reporte #" + safeNumero(reporte.getIdReporte()));
        tvTipoObjetivo.setText("Tipo de objetivo: " + ModeracionUiMapper.formatTipoObjetivo(reporte.getTipoObjetivo()));
        tvTituloObjetivo.setText("Objetivo: " + safeText(reporte.getTituloObjetivo(), "Sin t\u00edtulo"));
        tvDescripcionObjetivo.setText("Descripci\u00f3n del contenido: " + safeText(reporte.getDescripcionObjetivo(), "Sin descripci\u00f3n del contenido"));
        tvReportante.setText("Reportante: " + safeText(reporte.getNombreUsuarioReportante(), "No disponible"));
        tvUsuarioReportado.setText("Due\u00f1o o usuario reportado: " + resolverUsuarioReportado(reporte));
        tvMotivo.setText("Motivo: " + safeText(reporte.getMotivo(), "Sin motivo"));
        tvDescripcionReporte.setText("Descripci\u00f3n del reporte: " + safeText(reporte.getDescripcion(), "Sin descripci\u00f3n adicional"));
        tvEstado.setText("Estado: " + ModeracionUiMapper.formatEstadoReporte(reporte.getEstado()));
        tvPrioridad.setText("Prioridad: " + ModeracionUiMapper.formatPrioridad(reporte.getPrioridad()));
        tvModeradorAsignado.setText("Moderador asignado: " + safeText(reporte.getNombreModeradorAsignado(), "Sin asignar"));
        tvFechaReporte.setText("Fecha del reporte: " + safeText(reporte.getFechaReporte(), "No disponible"));
        tvFechaInicioRevision.setText("Inicio de revisi\u00f3n: " + safeText(reporte.getFechaInicioRevision(), "A\u00fan no iniciado"));

        configurarCampoOpcional(
                tvAccionResolucion,
                "Acci\u00f3n de resoluci\u00f3n: ",
                mapearAccionResolucionVisible(reporte.getAccionResolucion())
        );
        configurarCampoOpcional(tvMensajeRespuesta, "Mensaje de respuesta: ", reporte.getMensajeRespuesta());
        configurarCampoOpcional(tvFechaResolucion, "Fecha de resoluci\u00f3n: ", reporte.getFechaResolucion());

        cargarImagenObjetivo(reporte.getImagenObjetivo());
        actualizarBotonTomar(reporte);
        actualizarSeccionResolucion(reporte);
    }

    private void cargarImagenObjetivo(@Nullable String imagenObjetivo) {
        if (TextUtils.isEmpty(imagenObjetivo)) {
            imgObjetivo.setVisibility(View.GONE);
            return;
        }

        imgObjetivo.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imagenObjetivo)
                .placeholder(R.drawable.imagencargaobras)
                .error(R.drawable.imagencargaobras)
                .into(imgObjetivo);
    }

    private void actualizarBotonTomar(@Nullable ReporteDetalleDTO reporte) {
        boolean puedeTomar = puedeTomarReporte(reporte);
        btnTomarReporte.setVisibility(puedeTomar ? View.VISIBLE : View.GONE);
        btnTomarReporte.setEnabled(puedeTomar);
        if (!puedeTomar) {
            btnTomarReporte.setText("Tomar reporte");
        }
    }

    private boolean puedeTomarReporte(@Nullable ReporteDetalleDTO reporte) {
        if (!tienePermisoModeracion() || reporte == null) {
            return false;
        }
        String estado = normalizarValor(reporte.getEstado());
        return "PENDIENTE".equals(estado) && reporte.getIdModeradorAsignado() == null;
    }

    private void tomarReporte() {
        if (!puedeTomarReporte(reporteActual)) {
            return;
        }

        TomarReporteRequestDTO request = new TomarReporteRequestDTO();
        request.setIdModerador(idUsuarioActual);
        request.setPrioridad(reporteActual != null ? reporteActual.getPrioridad() : null);

        setEstadoTomarReporte(true);
        moderacionApi.tomarReporte(idReporte, request).enqueue(new Callback<RespuestaModeracionDTO>() {
            @Override
            public void onResponse(@NonNull Call<RespuestaModeracionDTO> call, @NonNull Response<RespuestaModeracionDTO> response) {
                setEstadoTomarReporte(false);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Reporte tomado para revisi\u00f3n", Toast.LENGTH_LONG).show();
                    cargarDetalleReporte();
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "No puedes tomar este reporte.";
                } else if (response.code() == 404) {
                    mensaje = "Reporte no encontrado.";
                } else if (response.code() == 409) {
                    mensaje = backendMessage != null ? backendMessage : "El reporte ya no est\u00e1 disponible para tomar.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo tomar el reporte.";
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<RespuestaModeracionDTO> call, @NonNull Throwable t) {
                setEstadoTomarReporte(false);
                Toast.makeText(requireContext(), "Error de conexi\u00f3n al tomar el reporte.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarSeccionResolucion(@Nullable ReporteDetalleDTO reporte) {
        if (!puedeResolverReporte(reporte)) {
            ocultarSeccionResolucion();
            return;
        }

        cardResolverReporte.setVisibility(View.VISIBLE);
        configurarOpcionesResolucion(reporte);
        limpiarFormularioResolucion();
        actualizarCampoFechaFinSuspension();
        setEstadoResolverReporte(false);
    }

    private void ocultarSeccionResolucion() {
        cardResolverReporte.setVisibility(View.GONE);
        limpiarFormularioResolucion();
        opcionesResolucionActuales.clear();
    }

    private boolean puedeResolverReporte(@Nullable ReporteDetalleDTO reporte) {
        if (!tienePermisoModeracion() || reporte == null) {
            return false;
        }

        String estado = normalizarValor(reporte.getEstado());
        String accionResolucionActual = safeText(reporte.getAccionResolucion(), null);
        Integer idModeradorAsignado = reporte.getIdModeradorAsignado();

        return "EN_REVISION".equals(estado)
                && idModeradorAsignado != null
                && idModeradorAsignado == idUsuarioActual
                && accionResolucionActual == null;
    }

    private void configurarOpcionesResolucion(@NonNull ReporteDetalleDTO reporte) {
        opcionesResolucionActuales.clear();
        opcionesResolucionActuales.add(new AccionResolucionOption("Selecciona una acci\u00f3n", null));
        opcionesResolucionActuales.add(new AccionResolucionOption("Descartar reporte", "DESCARTAR_REPORTE"));

        boolean esUsuario = "USUARIO".equals(normalizarValor(reporte.getTipoObjetivo()));
        if (!esUsuario) {
            opcionesResolucionActuales.add(new AccionResolucionOption("Ocultar contenido", "OCULTAR_CONTENIDO"));
            opcionesResolucionActuales.add(new AccionResolucionOption("Retirar contenido", "ELIMINAR_CONTENIDO_LOGICO"));
        }

        opcionesResolucionActuales.add(new AccionResolucionOption("Advertir usuario", "ADVERTENCIA"));
        opcionesResolucionActuales.add(new AccionResolucionOption("Suspender usuario", ACTION_SUSPENDER_USUARIO));
        opcionesResolucionActuales.add(new AccionResolucionOption("Bloquear usuario permanentemente", "BLOQUEAR_USUARIO_PERMANENTE"));

        List<String> labels = new ArrayList<>();
        for (AccionResolucionOption option : opcionesResolucionActuales) {
            labels.add(option.visibleLabel);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccionResolver.setAdapter(adapter);
    }

    @Nullable
    private AccionResolucionOption getAccionSeleccionada() {
        int position = spinnerAccionResolver.getSelectedItemPosition();
        if (position < 0 || position >= opcionesResolucionActuales.size()) {
            return null;
        }
        return opcionesResolucionActuales.get(position);
    }

    private void actualizarCampoFechaFinSuspension() {
        AccionResolucionOption action = getAccionSeleccionada();
        boolean requiereFecha = action != null && ACTION_SUSPENDER_USUARIO.equals(action.backendAction);
        contenedorFechaFinSuspension.setVisibility(requiereFecha ? View.VISIBLE : View.GONE);
        if (!requiereFecha) {
            fechaFinSuspensionBackend = null;
            etFechaFinSuspension.setText("");
            etFechaFinSuspension.setError(null);
        }
    }

    private void limpiarFormularioResolucion() {
        spinnerAccionResolver.setSelection(0, false);
        etMensajeRespuesta.setText("");
        etMotivoAccion.setText("");
        fechaFinSuspensionBackend = null;
        etFechaFinSuspension.setText("");
        etMensajeRespuesta.setError(null);
        etMotivoAccion.setError(null);
        etFechaFinSuspension.setError(null);
        contenedorFechaFinSuspension.setVisibility(View.GONE);
    }

    private void resolverReporte() {
        if (!puedeResolverReporte(reporteActual)) {
            return;
        }

        AccionResolucionOption accion = getAccionSeleccionada();
        if (accion == null || accion.backendAction == null) {
            Toast.makeText(requireContext(), "Selecciona una acci\u00f3n para resolver el reporte.", Toast.LENGTH_LONG).show();
            return;
        }

        String mensajeRespuesta = valueOrEmpty(etMensajeRespuesta);
        if (TextUtils.isEmpty(mensajeRespuesta)) {
            etMensajeRespuesta.setError("Debes escribir un mensaje.");
            etMensajeRespuesta.requestFocus();
            return;
        }

        String motivoAccion = valueOrEmpty(etMotivoAccion);
        boolean requiereMotivo = !"DESCARTAR_REPORTE".equals(accion.backendAction);
        if (requiereMotivo && TextUtils.isEmpty(motivoAccion)) {
            etMotivoAccion.setError("Debes escribir un motivo.");
            etMotivoAccion.requestFocus();
            return;
        }

        if (ACTION_SUSPENDER_USUARIO.equals(accion.backendAction)) {
            if (TextUtils.isEmpty(fechaFinSuspensionBackend)) {
                etFechaFinSuspension.setError("Debes indicar la fecha de fin de suspensi\u00f3n.");
                scrollToField(etFechaFinSuspension);
                return;
            }
        }

        ResolverReporteRequestDTO request = new ResolverReporteRequestDTO();
        request.setIdModerador(idUsuarioActual);
        request.setAccion(accion.backendAction);
        request.setTipoRespuesta("PERSONALIZADA");
        request.setMensajeRespuesta(mensajeRespuesta);
        request.setMotivoAccion(TextUtils.isEmpty(motivoAccion) ? null : motivoAccion);
        request.setFechaFinSuspension(TextUtils.isEmpty(fechaFinSuspensionBackend) ? null : fechaFinSuspensionBackend);

        setEstadoResolverReporte(true);
        moderacionApi.resolverReporte(idReporte, request).enqueue(new Callback<RespuestaModeracionDTO>() {
            @Override
            public void onResponse(@NonNull Call<RespuestaModeracionDTO> call, @NonNull Response<RespuestaModeracionDTO> response) {
                setEstadoResolverReporte(false);

                if (response.isSuccessful()) {
                    String successMessage = null;
                    if (response.body() != null) {
                        successMessage = safeText(response.body().getMessage(), null);
                    }
                    Toast.makeText(
                            requireContext(),
                            successMessage != null ? successMessage : "Reporte resuelto correctamente",
                            Toast.LENGTH_LONG
                    ).show();
                    cargarDetalleReporte();
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 400) {
                    mensaje = backendMessage != null ? backendMessage : "Revisa los datos de la resoluci\u00f3n.";
                } else if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "No puedes resolver este reporte.";
                } else if (response.code() == 404) {
                    mensaje = "Reporte no encontrado.";
                } else if (response.code() == 409) {
                    mensaje = backendMessage != null ? backendMessage : "El reporte ya no puede resolverse.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo resolver el reporte.";
                }
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<RespuestaModeracionDTO> call, @NonNull Throwable t) {
                setEstadoResolverReporte(false);
                Toast.makeText(requireContext(), "Error de conexi\u00f3n al resolver reporte.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setEstadoTomarReporte(boolean tomando) {
        btnTomarReporte.setEnabled(!tomando);
        btnTomarReporte.setText(tomando ? "Tomando..." : "Tomar reporte");
    }

    private void setEstadoResolverReporte(boolean resolviendo) {
        btnResolverReporte.setEnabled(!resolviendo);
        btnResolverReporte.setText(resolviendo ? "Resolviendo..." : "Resolver reporte");
        spinnerAccionResolver.setEnabled(!resolviendo);
        etMensajeRespuesta.setEnabled(!resolviendo);
        etMotivoAccion.setEnabled(!resolviendo);
        etFechaFinSuspension.setEnabled(!resolviendo);
    }

    private void abrirSelectorFechaFinSuspension() {
        if (!ACTION_SUSPENDER_USUARIO.equals(getBackendActionSeleccionada())) {
            return;
        }

        Calendar baseCalendar = obtenerFechaFinSuspensionSeleccionadaOActual();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    baseCalendar.set(Calendar.YEAR, year);
                    baseCalendar.set(Calendar.MONTH, month);
                    baseCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    abrirSelectorHoraFinSuspension(baseCalendar);
                },
                baseCalendar.get(Calendar.YEAR),
                baseCalendar.get(Calendar.MONTH),
                baseCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void abrirSelectorHoraFinSuspension(@NonNull Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    actualizarFechaFinSuspensionSeleccionada(calendar);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    @NonNull
    private Calendar obtenerFechaFinSuspensionSeleccionadaOActual() {
        Calendar calendar = Calendar.getInstance();
        if (TextUtils.isEmpty(fechaFinSuspensionBackend)) {
            return calendar;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            formatter.setLenient(false);
            java.util.Date parsedDate = formatter.parse(fechaFinSuspensionBackend);
            if (parsedDate != null) {
                calendar.setTime(parsedDate);
            }
        } catch (ParseException ignored) {
            // fallback: usar fecha actual
        }
        return calendar;
    }

    private void actualizarFechaFinSuspensionSeleccionada(@NonNull Calendar calendar) {
        fechaFinSuspensionBackend = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                .format(calendar.getTime());
        String visibleValue = new SimpleDateFormat("dd/MM/yyyy HH:mm", LOCALE_ES_MX)
                .format(calendar.getTime());
        etFechaFinSuspension.setError(null);
        etFechaFinSuspension.setText(visibleValue);
    }

    @Nullable
    private String getBackendActionSeleccionada() {
        AccionResolucionOption action = getAccionSeleccionada();
        return action != null ? action.backendAction : null;
    }

    private void configurarScrollYTeclado() {
        if (detailScrollView == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(detailScrollView, (v, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeBottom
            );
            return insets;
        });

        configurarScrollEnFoco(etMensajeRespuesta);
        configurarScrollEnFoco(etMotivoAccion);
    }

    private void configurarScrollEnFoco(@Nullable View target) {
        if (target == null) {
            return;
        }
        target.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToField(v);
            }
        });
    }

    private void scrollToField(@NonNull View target) {
        if (detailScrollView == null) {
            return;
        }

        detailScrollView.post(() -> {
            Rect rect = new Rect();
            target.getDrawingRect(rect);
            detailScrollView.offsetDescendantRectToMyCoords(target, rect);
            detailScrollView.smoothScrollTo(0, Math.max(0, rect.top - dpToPx(24)));
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        if (cargando) {
            contenedorDetalle.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            btnTomarReporte.setVisibility(View.GONE);
            ocultarSeccionResolucion();
        }
    }

    private void mostrarError(@NonNull String mensaje) {
        contenedorDetalle.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        btnTomarReporte.setVisibility(View.GONE);
        ocultarSeccionResolucion();
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(mensaje);
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private void configurarCampoOpcional(@NonNull TextView textView, @NonNull String label, @Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            textView.setVisibility(View.GONE);
            return;
        }
        textView.setVisibility(View.VISIBLE);
        textView.setText(label + value.trim());
    }

    @Nullable
    private String mapearAccionResolucionVisible(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return ModeracionUiMapper.formatAccionResolucion(value);
    }

    @NonNull
    private String resolverUsuarioReportado(@NonNull ReporteDetalleDTO reporte) {
        String nombre = safeText(reporte.getNombreUsuarioDuenoObjetivo(), null);
        if (nombre != null) {
            return nombre;
        }
        if (reporte.getIdUsuarioReportado() != null) {
            return "Usuario #" + reporte.getIdUsuarioReportado();
        }
        return "No disponible";
    }

    private String valueOrEmpty(@Nullable EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private String safeText(@Nullable String value, @Nullable String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String safeNumero(@Nullable Integer value) {
        return value != null ? String.valueOf(value) : "N/A";
    }

    @NonNull
    private String normalizarValor(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinAcentos.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
    }

    private static class AccionResolucionOption {
        final String visibleLabel;
        final String backendAction;

        AccionResolucionOption(String visibleLabel, String backendAction) {
            this.visibleLabel = visibleLabel;
            this.backendAction = backendAction;
        }
    }
}
