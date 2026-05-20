package com.example.artistlan.Fragments;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ReporteApi;
import com.example.artistlan.Conector.model.CrearReporteRequestDTO;
import com.example.artistlan.Conector.model.ReporteDetalleDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.utils.DialogThemeHelper;
import com.example.artistlan.utils.ReporteUiPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogReportarContenido extends DialogFragment {

    public interface OnReporteEnviadoListener {
        void onReporteEnviado();
    }

    private static final String ARG_TIPO_OBJETIVO = "arg_tipo_objetivo";
    private static final String ARG_ID_OBJETIVO = "arg_id_objetivo";
    private static final String ARG_ID_USUARIO_REPORTANTE = "arg_id_usuario_reportante";
    private static final String ARG_TITULO_OBJETIVO = "arg_titulo_objetivo";

    private static final String TIPO_OBRA = "OBRA";
    private static final String TIPO_SERVICIO = "SERVICIO";
    private static final String TIPO_USUARIO = "USUARIO";
    private static final String MOTIVO_PLACEHOLDER = "Selecciona un motivo";
    private static final String MOTIVO_OTRO = "Otro motivo";
    private static final int INVALID_ID = -1;

    private ReporteApi reporteApi;
    private Spinner spinnerMotivo;
    private EditText etDescripcion;
    private TextView tvMensaje;
    private TextView tvTituloObjetivo;
    private OnReporteEnviadoListener onReporteEnviadoListener;

    public static DialogReportarContenido newInstance(
            @NonNull String tipoObjetivo,
            @Nullable Integer idObjetivo,
            @Nullable Integer idUsuarioReportante,
            @Nullable String tituloObjetivo
    ) {
        DialogReportarContenido dialog = new DialogReportarContenido();
        Bundle args = new Bundle();
        args.putString(ARG_TIPO_OBJETIVO, tipoObjetivo);
        args.putInt(ARG_ID_OBJETIVO, idObjetivo != null ? idObjetivo : INVALID_ID);
        args.putInt(ARG_ID_USUARIO_REPORTANTE, idUsuarioReportante != null ? idUsuarioReportante : INVALID_ID);
        args.putString(ARG_TITULO_OBJETIVO, tituloObjetivo);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnReporteEnviadoListener(@Nullable OnReporteEnviadoListener listener) {
        this.onReporteEnviadoListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        reporteApi = RetrofitClient.getClient().create(ReporteApi.class);

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_reportar_contenido, null, false);

        spinnerMotivo = view.findViewById(R.id.spinnerMotivoReporte);
        etDescripcion = view.findViewById(R.id.etDescripcionReporte);
        tvMensaje = view.findViewById(R.id.tvMensajeReporte);
        tvTituloObjetivo = view.findViewById(R.id.tvTituloObjetivoReporte);

        configurarVista();

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(obtenerTituloDialogo())
                .setView(view)
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .setPositiveButton("Enviar reporte", null)
                .create();

        dialog.setOnShowListener(d -> {
            ThemeManager tm = new ThemeManager(requireContext());
            DialogThemeHelper.styleAlertDialog(dialog, requireContext());

            ThemeApplier.applyTextSecondary(tvMensaje, tm);
            ThemeApplier.applyTextPrimary(tvTituloObjetivo, tm);
            ThemeApplier.applyInput(etDescripcion, tm);

            if (view.getBackground() != null) {
                view.getBackground().setColorFilter(
                        tm.color(ThemeKeys.DIALOG_BG),
                        PorterDuff.Mode.SRC_ATOP
                );
            }

            Button btnCancelar = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button btnEnviar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (btnCancelar != null) {
                ThemeApplier.applySecondaryButton(btnCancelar, tm);
            }

            if (btnEnviar != null) {
                ThemeApplier.applyPrimaryButton(btnEnviar, tm);
                btnEnviar.setOnClickListener(v -> intentarEnviarReporte(dialog));
            }
        });

        return dialog;
    }

    private void configurarVista() {
        tvMensaje.setText("Selecciona el motivo del reporte y agrega una descripción si lo consideras necesario.");

        String tituloObjetivo = getTituloObjetivo();

        if (!TextUtils.isEmpty(tituloObjetivo)) {
            tvTituloObjetivo.setText("Contenido: " + tituloObjetivo);
            tvTituloObjetivo.setVisibility(View.VISIBLE);
        } else {
            tvTituloObjetivo.setVisibility(View.GONE);
        }

        List<String> motivos = new ArrayList<>();
        motivos.add(MOTIVO_PLACEHOLDER);
        motivos.add("Contenido ofensivo o inapropiado");
        motivos.add("Posible plagio o falta de autoría");
        motivos.add("Información falsa o engañosa");
        motivos.add("Spam o contenido repetido");
        motivos.add("Comportamiento sospechoso");
        motivos.add(MOTIVO_OTRO);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                motivos
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMotivo.setAdapter(adapter);
    }

    private void intentarEnviarReporte(@NonNull AlertDialog dialog) {
        CrearReporteRequestDTO request = construirRequestValidado();

        if (request == null) {
            return;
        }

        enviarReporte(dialog, request);
    }

    @Nullable
    private CrearReporteRequestDTO construirRequestValidado() {
        String tipoObjetivo = normalizarTipoObjetivo();

        int idObjetivo = getArguments() != null
                ? getArguments().getInt(ARG_ID_OBJETIVO, INVALID_ID)
                : INVALID_ID;

        int idUsuarioReportante = getArguments() != null
                ? getArguments().getInt(ARG_ID_USUARIO_REPORTANTE, INVALID_ID)
                : INVALID_ID;

        if (idUsuarioReportante <= 0) {
            Toast.makeText(
                    requireContext(),
                    "No se pudo identificar al usuario que reporta.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        String rolActual = ReporteUiPermissions.resolveCurrentUserRole(requireContext());

        if (ReporteUiPermissions.esRolAdminOModerador(rolActual)) {
            Toast.makeText(
                    requireContext(),
                    "Los administradores y moderadores no pueden crear reportes desde este flujo.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        if (TextUtils.isEmpty(tipoObjetivo)) {
            Toast.makeText(
                    requireContext(),
                    "El tipo de contenido a reportar no es válido.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        if (idObjetivo <= 0) {
            Toast.makeText(
                    requireContext(),
                    "El contenido seleccionado no es válido.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        if (spinnerMotivo.getSelectedItemPosition() <= 0 || spinnerMotivo.getSelectedItem() == null) {
            Toast.makeText(
                    requireContext(),
                    "Selecciona un motivo para el reporte.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        String motivo = spinnerMotivo.getSelectedItem().toString().trim();
        String descripcion = etDescripcion.getText() != null
                ? etDescripcion.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(motivo) || MOTIVO_PLACEHOLDER.equals(motivo)) {
            Toast.makeText(
                    requireContext(),
                    "Selecciona un motivo para el reporte.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        if (MOTIVO_OTRO.equals(motivo) && TextUtils.isEmpty(descripcion)) {
            etDescripcion.setError("Describe el motivo del reporte.");
            etDescripcion.requestFocus();
            return null;
        }

        CrearReporteRequestDTO request = new CrearReporteRequestDTO();
        request.setTipoObjetivo(tipoObjetivo);
        request.setIdUsuarioReportante(idUsuarioReportante);
        request.setMotivo(motivo);
        request.setDescripcion(TextUtils.isEmpty(descripcion) ? null : descripcion);

        if (TIPO_OBRA.equals(tipoObjetivo)) {
            request.setIdObra(idObjetivo);
        } else if (TIPO_SERVICIO.equals(tipoObjetivo)) {
            request.setIdServicio(idObjetivo);
        } else if (TIPO_USUARIO.equals(tipoObjetivo)) {
            request.setIdUsuarioReportado(idObjetivo);
        } else {
            Toast.makeText(
                    requireContext(),
                    "El tipo de contenido a reportar no es válido.",
                    Toast.LENGTH_LONG
            ).show();
            return null;
        }

        return request;
    }

    private void enviarReporte(@NonNull AlertDialog dialog, @NonNull CrearReporteRequestDTO request) {
        setDialogLoading(dialog, true);

        reporteApi.crearReporte(request).enqueue(new Callback<ReporteDetalleDTO>() {
            @Override
            public void onResponse(
                    @NonNull Call<ReporteDetalleDTO> call,
                    @NonNull Response<ReporteDetalleDTO> response
            ) {
                if (!isAdded()) {
                    return;
                }

                setDialogLoading(dialog, false);

                if (response.isSuccessful()) {
                    Toast.makeText(
                            requireContext(),
                            "Reporte enviado. El equipo de moderación lo revisará.",
                            Toast.LENGTH_LONG
                    ).show();

                    dismissAllowingStateLoss();

                    if (onReporteEnviadoListener != null) {
                        onReporteEnviadoListener.onReporteEnviado();
                    }

                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                int code = response.code();
                String message;

                if (code == 409) {
                    message = backendMessage != null
                            ? backendMessage
                            : "Ya existe un reporte activo o el contenido no puede reportarse.";
                } else if (code == 403) {
                    message = backendMessage != null
                            ? backendMessage
                            : "No puedes reportar este contenido.";
                } else if (code == 404) {
                    message = "El contenido ya no está disponible.";
                } else if (code == 400) {
                    message = backendMessage != null
                            ? backendMessage
                            : "Revisa los datos del reporte.";
                } else {
                    message = "No se pudo enviar el reporte. Inténtalo más tarde.";
                }

                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(
                    @NonNull Call<ReporteDetalleDTO> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) {
                    return;
                }

                setDialogLoading(dialog, false);

                Toast.makeText(
                        requireContext(),
                        "Error de conexión al enviar reporte.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setDialogLoading(@NonNull AlertDialog dialog, boolean loading) {
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (positiveButton != null) {
            positiveButton.setEnabled(!loading);
            positiveButton.setText(loading ? "Enviando..." : "Enviar reporte");
        }

        if (negativeButton != null) {
            negativeButton.setEnabled(!loading);
        }

        if (spinnerMotivo != null) {
            spinnerMotivo.setEnabled(!loading);
        }

        if (etDescripcion != null) {
            etDescripcion.setEnabled(!loading);
        }
    }

    @Nullable
    private String normalizarTipoObjetivo() {
        String tipoObjetivo = getArguments() != null
                ? getArguments().getString(ARG_TIPO_OBJETIVO)
                : null;

        if (TextUtils.isEmpty(tipoObjetivo)) {
            return null;
        }

        String normalizado = tipoObjetivo.trim().toUpperCase(Locale.ROOT);

        if (TIPO_OBRA.equals(normalizado)
                || TIPO_SERVICIO.equals(normalizado)
                || TIPO_USUARIO.equals(normalizado)) {
            return normalizado;
        }

        return null;
    }

    @NonNull
    private String obtenerTituloDialogo() {
        String tipoObjetivo = normalizarTipoObjetivo();

        if (TIPO_OBRA.equals(tipoObjetivo)) {
            return "Reportar obra";
        }

        if (TIPO_SERVICIO.equals(tipoObjetivo)) {
            return "Reportar servicio";
        }

        if (TIPO_USUARIO.equals(tipoObjetivo)) {
            return "Reportar usuario";
        }

        return "Reportar contenido";
    }

    @Nullable
    private String getTituloObjetivo() {
        return getArguments() != null
                ? getArguments().getString(ARG_TITULO_OBJETIVO)
                : null;
    }
}
