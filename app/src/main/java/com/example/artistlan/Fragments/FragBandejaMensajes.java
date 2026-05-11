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
import com.example.artistlan.Conector.model.PageResponseNotificacionDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.adapter.NotificacionesAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragBandejaMensajes extends Fragment implements NotificacionesAdapter.Listener {

    private static final int PAGE_SIZE = 10;
    private static final String SORT_DEFAULT = "fechaCreacion,desc";

    private RecyclerView recyclerMensajes;
    private ProgressBar progressMensajes;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;
    private TextView btnMarcarTodasLeidas;
    private TextView btnRecargar;
    private View layoutAccionesLocal;
    private TextView btnCargarMasNotificaciones;
    private View layoutLoaderMasNotificaciones;

    private NotificacionesAdapter adapter;
    private NotificacionesApi notificacionesApi;
    private int idUsuario = -1;
    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int requestToken = 0;
    private final List<NotificacionDTO> notificacionesAcumuladas = new ArrayList<>();
    private final Set<Integer> idsNotificacionCargados = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_bandeja_mensajes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

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
        layoutAccionesLocal = view.findViewById(R.id.layoutBandejaAccionesLocal);
        btnCargarMasNotificaciones = view.findViewById(R.id.btnCargarMasNotificaciones);
        layoutLoaderMasNotificaciones = view.findViewById(R.id.layoutLoaderMasNotificaciones);

        recyclerMensajes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificacionesAdapter(this);
        recyclerMensajes.setAdapter(adapter);

        if (layoutAccionesLocal != null) {
            layoutAccionesLocal.setVisibility(View.GONE);
        }

        btnMarcarTodasLeidas.setOnClickListener(v -> confirmarMarcarTodas());
        btnRecargar.setOnClickListener(v -> cargarNotificaciones());
        if (btnCargarMasNotificaciones != null) {
            btnCargarMasNotificaciones.setOnClickListener(v -> {
                if (isLoading || isLastPage) {
                    return;
                }
                cargarPagina(nextPageToLoad);
            });
        }

        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        if (!vistaListaInicializada()) {
            return;
        }
        if (idUsuario <= 0) {
            mostrarVacio("Sin sesi\u00F3n activa", "Inicia sesi\u00F3n para ver tu bandeja de mensajes.");
            return;
        }
        reiniciarYCargarPrimeraPagina();
    }

    private void reiniciarYCargarPrimeraPagina() {
        requestToken++;
        nextPageToLoad = 0;
        isLastPage = false;
        isLoading = false;

        notificacionesAcumuladas.clear();
        idsNotificacionCargados.clear();
        adapter.submitList(new ArrayList<>());

        mostrarBotonCargarMas(false, false);
        mostrarLoaderMasNotificaciones(false);
        emptyState.setVisibility(View.GONE);

        cargarPagina(0);
    }

    private void cargarPagina(int pageObjetivo) {
        if (isLoading || (isLastPage && pageObjetivo > 0)) {
            return;
        }

        isLoading = true;
        if (pageObjetivo == 0) {
            progressMensajes.setVisibility(View.VISIBLE);
            recyclerMensajes.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            progressMensajes.setVisibility(View.GONE);
            recyclerMensajes.setVisibility(View.VISIBLE);
            mostrarLoaderMasNotificaciones(true);
            mostrarBotonCargarMas(false, false);
        }

        final int tokenLocal = ++requestToken;
        notificacionesApi.obtenerNotificacionesPaginadas(
                idUsuario,
                pageObjetivo,
                PAGE_SIZE,
                SORT_DEFAULT,
                null,
                null
        ).enqueue(new Callback<PageResponseNotificacionDTO>() {
            @Override
            public void onResponse(@NonNull Call<PageResponseNotificacionDTO> call, @NonNull Response<PageResponseNotificacionDTO> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                progressMensajes.setVisibility(View.GONE);
                mostrarLoaderMasNotificaciones(false);

                if (!response.isSuccessful() || response.body() == null) {
                    if (pageObjetivo == 0) {
                        mostrarVacio("No se pudo cargar la bandeja", "Intenta nuevamente en unos segundos.");
                    } else {
                        mostrarBotonCargarMas(true, true);
                        Toast.makeText(requireContext(), "No se pudo cargar m\u00E1s notificaciones.", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                PageResponseNotificacionDTO pageResponse = response.body();
                List<NotificacionDTO> nuevos = sanitizar(pageResponse.getContent());

                if (pageObjetivo == 0) {
                    notificacionesAcumuladas.clear();
                    idsNotificacionCargados.clear();
                }

                List<NotificacionDTO> agregados = new ArrayList<>();
                for (NotificacionDTO item : nuevos) {
                    Integer id = item.getIdNotificacion();
                    if (id != null && idsNotificacionCargados.contains(id)) {
                        continue;
                    }
                    if (id != null) {
                        idsNotificacionCargados.add(id);
                    }
                    notificacionesAcumuladas.add(item);
                    agregados.add(item);
                }

                if (pageObjetivo == 0) {
                    adapter.submitList(new ArrayList<>(notificacionesAcumuladas));
                } else {
                    adapter.agregarItems(agregados);
                }

                nextPageToLoad = pageObjetivo + 1;
                isLastPage = pageResponse.isLast();

                if (notificacionesAcumuladas.isEmpty()) {
                    mostrarVacio("Tu bandeja est\u00E1 al d\u00EDa", "Cuando recibas mensajes o alertas, aparecer\u00E1n aqu\u00ED.");
                    mostrarBotonCargarMas(false, false);
                } else {
                    recyclerMensajes.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);
                    mostrarBotonCargarMas(!isLastPage, false);
                }

                refrescarBadge();
            }

            @Override
            public void onFailure(@NonNull Call<PageResponseNotificacionDTO> call, @NonNull Throwable t) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                progressMensajes.setVisibility(View.GONE);
                mostrarLoaderMasNotificaciones(false);

                if (pageObjetivo == 0) {
                    mostrarVacio("Error de conexi\u00F3n", "No fue posible cargar tus mensajes.");
                } else {
                    mostrarBotonCargarMas(true, true);
                    Toast.makeText(requireContext(), "Error de conexi\u00F3n al cargar m\u00E1s notificaciones.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<NotificacionDTO> sanitizar(List<NotificacionDTO> source) {
        List<NotificacionDTO> salida = new ArrayList<>();
        if (source == null) {
            return salida;
        }
        for (NotificacionDTO item : source) {
            if (item != null) {
                salida.add(item);
            }
        }
        return salida;
    }

    private void mostrarBotonCargarMas(boolean mostrar, boolean reintento) {
        if (btnCargarMasNotificaciones == null) {
            return;
        }
        btnCargarMasNotificaciones.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            btnCargarMasNotificaciones.setText(reintento
                    ? "Reintentar cargar m\u00E1s notificaciones"
                    : "Cargar m\u00E1s notificaciones");
        }
    }

    private void mostrarLoaderMasNotificaciones(boolean mostrar) {
        if (layoutLoaderMasNotificaciones == null) {
            return;
        }
        layoutLoaderMasNotificaciones.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    private void mostrarVacio(String titulo, String subtitulo) {
        adapter.submitList(new ArrayList<>());
        notificacionesAcumuladas.clear();
        idsNotificacionCargados.clear();
        recyclerMensajes.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyTitle.setText(titulo);
        emptySubtitle.setText(subtitulo);
    }

    private void confirmarMarcarTodas() {
        if (!vistaListaInicializada()) {
            return;
        }
        if (adapter.getItems().isEmpty()) {
            Toast.makeText(requireContext(), "No hay mensajes para marcar.", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Marcar todo como le\u00EDdo")
                .setMessage("Se marcar\u00E1n como le\u00EDdos todos los mensajes de la bandeja.")
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
                for (NotificacionDTO item : notificacionesAcumuladas) {
                    item.setLeida(true);
                }
                refrescarBadge();
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
        detalle.append(MensajeUiUtils.formatearMensajeConMotivo(item.getMensajeSeguro())).append("\n\n")
                .append("Fecha: ").append(MensajeUiUtils.formatearFechaCorta(item.getFecha())).append("\n")
                .append("Origen: ").append(origenVisual);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getTituloSeguro())
                .setMessage(detalle.toString())
                .setPositiveButton("Cerrar", null);

        String ctaTexto = MensajeUiUtils.obtenerTextoCtaSemantico(item);
        Integer destino = MensajeUiUtils.obtenerDestinoSemantico(item);
        if (ctaTexto != null && destino != null) {
            builder.setNeutralButton(ctaTexto, (dialog, which) -> navegarSemantico(item, destino));
        }
        builder.show();
    }

    @Override
    public void onNavegar(@NonNull NotificacionDTO item) {
        Integer destino = MensajeUiUtils.obtenerDestinoSemantico(item);
        if (destino == null) {
            onDetalle(item);
            return;
        }
        navegarSemantico(item, destino);
    }

    private void navegarSemantico(@NonNull NotificacionDTO item, int destinationId) {
        Integer modoSolicitudes = MensajeUiUtils.obtenerModoSolicitudesSemantico(item);
        if (MensajeUiUtils.destinoSemanticoRequiereSolicitudesTab(item) || modoSolicitudes != null) {
            if (getParentFragment() instanceof FragCentroMensajes) {
                FragCentroMensajes parent = (FragCentroMensajes) getParentFragment();
                parent.seleccionarTab(1);
                if (modoSolicitudes != null) {
                    parent.seleccionarModoSolicitudes(modoSolicitudes);
                }
                return;
            }
            if (getActivity() instanceof ActFragmentoPrincipal) {
                ((ActFragmentoPrincipal) getActivity()).abrirCentroMensajes(
                        1,
                        modoSolicitudes != null ? modoSolicitudes : FragSolicitudesMensajes.MODO_RECIBIDAS
                );
                return;
            }
        }

        Integer tabTransacciones = MensajeUiUtils.obtenerTabTransaccionesSemantico(item);
        if (destinationId == R.id.fragTransacciones
                && tabTransacciones != null
                && getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).abrirTransacciones(tabTransacciones);
            return;
        }
        navegarADestinoSeguro(destinationId, null);
    }

    private void navegarADestinoSeguro(int destinationId, @Nullable Bundle args) {
        if (getActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) getActivity()).navegarDesdeCentroMensajes(destinationId, args);
        }
    }

    @Override
    public void onMarcarLeida(@NonNull NotificacionDTO item) {
        if (item.isLeida()) {
            return;
        }
        if (item.getIdNotificacion() == null) {
            Toast.makeText(requireContext(), "No se pudo identificar la notificaci\u00F3n.", Toast.LENGTH_SHORT).show();
            return;
        }

        notificacionesApi.marcarNotificacionComoLeida(idUsuario, item.getIdNotificacion()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "No se pudo marcar como le\u00EDdo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                item.setLeida(true);
                adapter.marcarComoLeida(item);
                refrescarBadge();
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
                .setMessage("Esta acci\u00F3n eliminar\u00E1 el mensaje de forma permanente.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarNotificacion(item))
                .show();
    }

    private void eliminarNotificacion(@NonNull NotificacionDTO item) {
        if (item.getIdNotificacion() == null) {
            Toast.makeText(requireContext(), "No se pudo identificar la notificaci\u00F3n.", Toast.LENGTH_SHORT).show();
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
                notificacionesAcumuladas.remove(item);
                idsNotificacionCargados.remove(item.getIdNotificacion());
                revisarVacio();
                refrescarBadge();
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
            mostrarVacio("Tu bandeja est\u00E1 al d\u00EDa", "Cuando recibas mensajes o alertas, aparecer\u00E1n aqu\u00ED.");
            mostrarBotonCargarMas(false, false);
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
        cargarNotificaciones();
    }

    public void marcarTodasDesdeHeader() {
        if (!vistaListaInicializada()) {
            return;
        }
        confirmarMarcarTodas();
    }

    private boolean vistaListaInicializada() {
        return isAdded()
                && getView() != null
                && adapter != null
                && recyclerMensajes != null
                && progressMensajes != null
                && emptyState != null;
    }
}
