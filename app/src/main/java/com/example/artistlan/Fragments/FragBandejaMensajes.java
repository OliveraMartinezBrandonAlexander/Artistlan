package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.NotificacionesApi;
import com.example.artistlan.Conector.model.NotificacionDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.NotificacionesAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragBandejaMensajes extends Fragment implements NotificacionesAdapter.Listener {

    private RecyclerView recyclerMensajes;
    private ProgressBar progressMensajes;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;
    private TextView btnMarcarTodasLeidas;
    private TextView btnRecargar;

    private NotificacionesAdapter adapter;
    private NotificacionesApi notificacionesApi;
    private int idUsuario = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_bandeja_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        notificacionesApi = RetrofitClient.getClient().create(NotificacionesApi.class);

        recyclerMensajes = view.findViewById(R.id.recyclerBandejaMensajes);
        progressMensajes = view.findViewById(R.id.progressBandejaMensajes);
        emptyState = view.findViewById(R.id.layoutBandejaVacia);
        emptyTitle = view.findViewById(R.id.tvBandejaVaciaTitulo);
        emptySubtitle = view.findViewById(R.id.tvBandejaVaciaSubtitulo);
        btnMarcarTodasLeidas = view.findViewById(R.id.btnBandejaMarcarTodasLeidas);
        btnRecargar = view.findViewById(R.id.btnBandejaRecargar);

        recyclerMensajes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificacionesAdapter(this);
        recyclerMensajes.setAdapter(adapter);

        btnMarcarTodasLeidas.setOnClickListener(v -> confirmarMarcarTodas());
        btnRecargar.setOnClickListener(v -> cargarNotificaciones());

        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        if (idUsuario <= 0) {
            mostrarVacio("Sin sesion activa", "Inicia sesion para ver tu bandeja de mensajes.");
            return;
        }

        progressMensajes.setVisibility(View.VISIBLE);
        recyclerMensajes.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        notificacionesApi.obtenerNotificacionesPorUsuario(idUsuario).enqueue(new Callback<List<NotificacionDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificacionDTO>> call, @NonNull Response<List<NotificacionDTO>> response) {
                if (!isAdded()) {
                    return;
                }
                progressMensajes.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    mostrarVacio("No se pudo cargar la bandeja", "Intenta nuevamente en unos segundos.");
                    return;
                }

                List<NotificacionDTO> mensajes = filtrarMensajes(response.body());
                if (mensajes.isEmpty()) {
                    mostrarVacio("Tu bandeja esta al dia", "Cuando recibas mensajes o alertas, apareceran aqui.");
                    return;
                }

                adapter.submitList(mensajes);
                recyclerMensajes.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                refrescarBadge();
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificacionDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progressMensajes.setVisibility(View.GONE);
                mostrarVacio("Error de conexion", "No fue posible cargar tus mensajes.");
            }
        });
    }

    private List<NotificacionDTO> filtrarMensajes(List<NotificacionDTO> source) {
        List<NotificacionDTO> salida = new ArrayList<>();
        for (NotificacionDTO item : source) {
            if (item != null && !item.esSolicitudCreada()) {
                salida.add(item);
            }
        }
        Collections.sort(salida, new Comparator<NotificacionDTO>() {
            @Override
            public int compare(NotificacionDTO o1, NotificacionDTO o2) {
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
        recyclerMensajes.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyTitle.setText(titulo);
        emptySubtitle.setText(subtitulo);
    }

    private void confirmarMarcarTodas() {
        if (adapter.getItems().isEmpty()) {
            Toast.makeText(requireContext(), "No hay mensajes para marcar.", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Marcar todo como leido")
                .setMessage("Se marcaran como leidos todos los mensajes de la bandeja.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Marcar", (dialog, which) -> marcarTodasLeidas())
                .show();
    }

    private void marcarTodasLeidas() {
        notificacionesApi.marcarTodasComoLeidas(idUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo marcar toda la bandeja.", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.marcarTodasLeidas();
                refrescarBadge();
                cargarNotificaciones();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al marcar mensajes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDetalle(@NonNull NotificacionDTO item) {
        if (item.getIdNotificacion() == null) {
            mostrarDialogoDetalle(item);
            return;
        }

        notificacionesApi.obtenerNotificacionPorId(idUsuario, item.getIdNotificacion()).enqueue(new Callback<NotificacionDTO>() {
            @Override
            public void onResponse(@NonNull Call<NotificacionDTO> call, @NonNull Response<NotificacionDTO> response) {
                if (!isAdded()) {
                    return;
                }
                NotificacionDTO detalle = response.isSuccessful() && response.body() != null ? response.body() : item;
                mostrarDialogoDetalle(detalle);
            }

            @Override
            public void onFailure(@NonNull Call<NotificacionDTO> call, @NonNull Throwable t) {
                if (isAdded()) {
                    mostrarDialogoDetalle(item);
                }
            }
        });
    }

    private void mostrarDialogoDetalle(@NonNull NotificacionDTO item) {
        String usuarioOrigen = item.getUsuarioOrigenSeguro();
        String origenVisual = item.esDeSistema()
                ? "Sistema"
                : (usuarioOrigen != null && !usuarioOrigen.trim().isEmpty() ? usuarioOrigen.trim() : "Usuario");

        StringBuilder detalle = new StringBuilder();
        detalle.append(item.getMensajeSeguro()).append("\n\n")
                .append("Fecha: ").append(MensajeUiUtils.formatearFechaCorta(item.getFecha())).append("\n")
                .append("Origen: ").append(origenVisual);
        if (item.getReferenciaTipo() != null && !item.getReferenciaTipo().trim().isEmpty() && item.getReferenciaId() != null) {
            detalle.append("\n").append(MensajeUiUtils.etiquetaReferencia(item.getReferenciaTipo(), item.getReferenciaId()));
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getTituloSeguro())
                .setMessage(detalle.toString())
                .setPositiveButton("Cerrar", null);

        Integer destino = resolverDestinoReferencia(item.getReferenciaTipo(), item.getReferenciaId());
        if (destino != null) {
            builder.setNeutralButton("Ir a referencia", (dialog, which) -> navegarADestinoSeguro(destino));
        }
        builder.show();
    }

    private Integer resolverDestinoReferencia(String referenciaTipo, Integer referenciaId) {
        if (referenciaTipo == null || referenciaTipo.trim().isEmpty() || referenciaId == null) {
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
    public void onMarcarLeida(@NonNull NotificacionDTO item) {
        if (item.isLeida()) {
            return;
        }
        if (item.getIdNotificacion() == null) {
            Toast.makeText(requireContext(), "No se pudo identificar la notificacion.", Toast.LENGTH_SHORT).show();
            cargarNotificaciones();
            return;
        }

        notificacionesApi.marcarNotificacionComoLeida(idUsuario, item.getIdNotificacion()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo marcar como leido.", Toast.LENGTH_SHORT).show();
                    return;
                }
                item.setLeida(true);
                adapter.notifyDataSetChanged();
                refrescarBadge();
                cargarNotificaciones();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al marcar mensaje.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onEliminar(@NonNull NotificacionDTO item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar mensaje")
                .setMessage("Esta accion eliminara el mensaje de forma permanente.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarNotificacion(item))
                .show();
    }

    private void eliminarNotificacion(@NonNull NotificacionDTO item) {
        if (item.getIdNotificacion() == null) {
            Toast.makeText(requireContext(), "No se pudo identificar la notificacion.", Toast.LENGTH_SHORT).show();
            cargarNotificaciones();
            return;
        }

        notificacionesApi.eliminarNotificacion(idUsuario, item.getIdNotificacion()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo eliminar el mensaje.", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.removeItem(item);
                revisarVacio();
                refrescarBadge();
                cargarNotificaciones();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error de red al eliminar mensaje.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void revisarVacio() {
        if (adapter.getItems().isEmpty()) {
            mostrarVacio("Tu bandeja esta al dia", "Cuando recibas mensajes o alertas, apareceran aqui.");
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
}
