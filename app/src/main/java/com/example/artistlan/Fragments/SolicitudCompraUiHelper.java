package com.example.artistlan.Fragments;

import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.api.SolicitudesApi;
import com.example.artistlan.Conector.model.SolicitudCompraCrearRequestDTO;
import com.example.artistlan.Conector.model.SolicitudDTO;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class SolicitudCompraUiHelper {

    private SolicitudCompraUiHelper() {
    }

    public static void mostrarDialogoSolicitudCompra(
            @NonNull Fragment fragment,
            int idUsuario,
            @NonNull SolicitudesApi solicitudesApi,
            @NonNull TarjetaTextoObraItem obraItem,
            @Nullable Runnable onSuccess
    ) {
        if (!fragment.isAdded()) {
            return;
        }
        if (idUsuario <= 0) {
            Toast.makeText(fragment.requireContext(), "Debes iniciar sesion para solicitar compra", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText inputMensaje = new EditText(fragment.requireContext());
        inputMensaje.setHint("Mensaje opcional para el vendedor");
        inputMensaje.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputMensaje.setMinLines(2);
        inputMensaje.setMaxLines(4);

        new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle("Solicitar compra")
                .setMessage("Vas a enviar una solicitud de compra para \"" + obraItem.getTitulo() + "\".")
                .setView(inputMensaje)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar solicitud", (dialog, which) -> {
                    String mensaje = inputMensaje.getText() != null ? inputMensaje.getText().toString().trim() : "";
                    enviarSolicitudCompra(fragment, idUsuario, solicitudesApi, obraItem, mensaje.isEmpty() ? null : mensaje, onSuccess);
                })
                .show();
    }

    private static void enviarSolicitudCompra(
            @NonNull Fragment fragment,
            int idUsuario,
            @NonNull SolicitudesApi solicitudesApi,
            @NonNull TarjetaTextoObraItem obraItem,
            @Nullable String mensajeOpcional,
            @Nullable Runnable onSuccess
    ) {
        SolicitudCompraCrearRequestDTO requestDTO = new SolicitudCompraCrearRequestDTO(
                idUsuario,
                obraItem.getIdObra(),
                mensajeOpcional
        );

        solicitudesApi.crearSolicitudCompra(requestDTO).enqueue(new Callback<SolicitudDTO>() {
            @Override
            public void onResponse(@NonNull Call<SolicitudDTO> call, @NonNull Response<SolicitudDTO> response) {
                if (!fragment.isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(fragment.requireContext(), "Solicitud de compra enviada", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                Toast.makeText(
                        fragment.requireContext(),
                        backendMessage != null && !backendMessage.trim().isEmpty()
                                ? backendMessage
                                : "No se pudo solicitar compra (" + response.code() + ")",
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(@NonNull Call<SolicitudDTO> call, @NonNull Throwable t) {
                if (!fragment.isAdded()) {
                    return;
                }
                Toast.makeText(fragment.requireContext(), "Error de red al solicitar compra", Toast.LENGTH_LONG).show();
            }
        });
    }
}
