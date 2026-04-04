package com.example.artistlan.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.PagoPaypalApi;
import com.example.artistlan.Conector.model.CapturarOrdenPaypalResponseDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalResponseDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.pagos.PagoPaypalSessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragArte extends Fragment implements FilterableExplorarFragment {

    private RecyclerView recyclerViewObras;
    private TarjetaTextoObraAdapter adapter;
    private String categoriaFiltroActual = "";
    private ObraApi obraApi;
    private FavoritosApi favoritosApi;
    private PagoPaypalApi pagoPaypalApi;
    private int idUsuarioLogueado = -1;
    private boolean creandoOrden = false;
    private boolean capturandoPago = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_arte, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        intentarCapturarPagoPendienteComoFallback();
    }

    public void filtrarBusqueda(String texto) {
        if (adapter != null) adapter.filtrar(texto);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new BotonesMenuSuperior(this);
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        obraApi = RetrofitClient.getClient().create(ObraApi.class);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        pagoPaypalApi = RetrofitClient.getClient().create(PagoPaypalApi.class);
        configurarObras(view);
    }

    @Override
    public List<String> getFilterOptions() {
        return Arrays.asList(
                "Pintura", "Dibujo", "Escultura", "Fotografia", "Digital",
                "Acuarela", "Oleo", "Acrilico", "Grabado", "Ceramica",
                "Arte textil", "Collage", "Ilustracion", "Mural",
                "Arte abstracto", "Retrato", "Paisaje", "Arte conceptual"
        );
    }

    @Override
    public String getActiveFilter() {
        return categoriaFiltroActual;
    }

    @Override
    public void applyFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            clearFilter();
            return;
        }

        if (filter.equalsIgnoreCase(categoriaFiltroActual)) {
            categoriaFiltroActual = "";
            Toast.makeText(getContext(), "Filtro desactivado", Toast.LENGTH_SHORT).show();
        } else {
            categoriaFiltroActual = filter;
            Toast.makeText(getContext(), "Filtrando: " + filter, Toast.LENGTH_SHORT).show();
        }

        obtenerObrasDeAPI();
    }

    @Override
    public void clearFilter() {
        categoriaFiltroActual = "";
        Toast.makeText(getContext(), "Filtros borrados", Toast.LENGTH_SHORT).show();
        obtenerObrasDeAPI();
    }

    private void configurarObras(View view) {
        recyclerViewObras = view.findViewById(R.id.recyclerObras);
        recyclerViewObras.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext());
        adapter.setOnLikeClickListener(this::toggleLikeObra);
        adapter.setOnComprarClickListener(this::iniciarCompraObra);
        recyclerViewObras.setAdapter(adapter);
        obtenerObrasDeAPI();
    }

    private void iniciarCompraObra(TarjetaTextoObraItem obraItem, int position) {
        if (creandoOrden || capturandoPago) return;
        if (idUsuarioLogueado <= 0) {
            Toast.makeText(getContext(), "Debes iniciar sesion para comprar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (obraItem.getPrecio() == null || obraItem.getPrecio() <= 0) {
            Toast.makeText(getContext(), "Esta obra no esta disponible para compra", Toast.LENGTH_SHORT).show();
            return;
        }

        creandoOrden = true;
        pagoPaypalApi.crearOrden(obraItem.getIdObra(), idUsuarioLogueado).enqueue(new Callback<CrearOrdenPaypalResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<CrearOrdenPaypalResponseDTO> call, @NonNull Response<CrearOrdenPaypalResponseDTO> response) {
                creandoOrden = false;
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "No se pudo crear la orden de PayPal (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    return;
                }

                CrearOrdenPaypalResponseDTO body = response.body();
                String paypalOrderId = body.getPaypalOrderId();
                String approveLink = body.getApproveLink();

                if (paypalOrderId == null || paypalOrderId.trim().isEmpty() || approveLink == null || approveLink.trim().isEmpty()) {
                    Toast.makeText(getContext(), "La respuesta del backend no contiene la orden de PayPal", Toast.LENGTH_LONG).show();
                    return;
                }

                PagoPaypalSessionManager.savePendingOrder(requireContext(), paypalOrderId, obraItem.getIdObra(), idUsuarioLogueado);
                abrirApproveLink(approveLink);
            }

            @Override
            public void onFailure(@NonNull Call<CrearOrdenPaypalResponseDTO> call, @NonNull Throwable t) {
                creandoOrden = false;
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error de red al crear la orden: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirApproveLink(String approveLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(approveLink));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            PagoPaypalSessionManager.markApprovalOpened(requireContext());
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            PagoPaypalSessionManager.clear(requireContext());
            Toast.makeText(getContext(), "No se encontro una aplicacion para abrir PayPal", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            PagoPaypalSessionManager.clear(requireContext());
            Toast.makeText(getContext(), "No se pudo abrir el enlace de aprobacion", Toast.LENGTH_LONG).show();
        }
    }

    public void procesarRetornoPaypalDeepLink() {
        capturarPagoPendiente(true);
    }

    private void intentarCapturarPagoPendienteComoFallback() {
        capturarPagoPendiente(false);
    }

    private void capturarPagoPendiente(boolean triggeredByDeepLink) {
        if (!isAdded() || capturandoPago || creandoOrden) return;
        if (!PagoPaypalSessionManager.shouldCaptureOnReturn(requireContext())) return;
        if (!triggeredByDeepLink && !PagoPaypalSessionManager.hasApprovalFromDeepLink(requireContext())) return;

        String paypalOrderId = PagoPaypalSessionManager.getPendingOrderId(requireContext());
        if (paypalOrderId == null || paypalOrderId.trim().isEmpty()) {
            PagoPaypalSessionManager.clear(requireContext());
            return;
        }

        capturandoPago = true;
        pagoPaypalApi.capturarOrden(paypalOrderId).enqueue(new Callback<CapturarOrdenPaypalResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<CapturarOrdenPaypalResponseDTO> call, @NonNull Response<CapturarOrdenPaypalResponseDTO> response) {
                capturandoPago = false;
                if (!isAdded()) return;

                CapturarOrdenPaypalResponseDTO body = response.body();
                String backendMessage = body != null ? body.resolveUserMessage() : null;
                PagoPaypalSessionManager.clear(requireContext());

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(),
                            backendMessage != null ? backendMessage : "Pago capturado correctamente",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getContext(),
                        backendMessage != null ? backendMessage : "No se pudo capturar el pago (" + response.code() + ")",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<CapturarOrdenPaypalResponseDTO> call, @NonNull Throwable t) {
                capturandoPago = false;
                if (!isAdded()) return;
                PagoPaypalSessionManager.clear(requireContext());
                Toast.makeText(getContext(), "Error de red al capturar el pago: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLikeObra(TarjetaTextoObraItem obraItem, int position) {
        if (idUsuarioLogueado <= 0) return;
        final boolean favoritoAnterior = obraItem.isUserLiked();
        final int likesAnterior = obraItem.getLikes();
        obraItem.setUserLiked(!favoritoAnterior);
        obraItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        adapter.notifyLikeChanged(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idObra = obraItem.getIdObra();
        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    obraItem.setUserLiked(favoritoAnterior);
                    obraItem.setLikes(likesAnterior);
                    adapter.notifyLikeChanged(position);
                    Toast.makeText(getContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                obraItem.setUserLiked(favoritoAnterior);
                obraItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(getContext(), "Fallo de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerObrasDeAPI() {
        obraApi.obtenerTodasLasObras(idUsuarioLogueado > 0 ? idUsuarioLogueado : null).enqueue(new Callback<List<ObraDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ObraDTO>> call, @NonNull Response<List<ObraDTO>> response) {
                if (response.code() == 204) {
                    adapter.actualizarLista(new ArrayList<>());
                    adapter.setOwnedObraIds(new HashSet<>());
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                List<TarjetaTextoObraItem> items = new ArrayList<>();
                Set<Integer> ownedObraIds = new HashSet<>();
                for (ObraDTO dto : response.body()) {
                    if (!categoriaFiltroActual.isEmpty() && (dto.getNombreCategoria() == null || !categoriaFiltroActual.equalsIgnoreCase(dto.getNombreCategoria()))) {
                        continue;
                    }
                    if (dto.getIdUsuario() != null && dto.getIdUsuario() == idUsuarioLogueado && dto.getIdObra() != null) {
                        ownedObraIds.add(dto.getIdObra());
                    }
                    items.add(new TarjetaTextoObraItem(
                            dto.getIdObra(),
                            dto.getTitulo(),
                            dto.getDescripcion(),
                            dto.getEstado(),
                            dto.getPrecio(),
                            dto.getImagen1(),
                            dto.getImagen2(),
                            dto.getImagen3(),
                            dto.getTecnicas(),
                            dto.getMedidas(),
                            dto.getLikes() != null ? dto.getLikes() : 0,
                            dto.getNombreAutor(),
                            dto.getNombreCategoria(),
                            dto.getFotoPerfilAutor(),
                            Boolean.TRUE.equals(dto.getEsFavorito()),
                            false));
                }
                adapter.actualizarLista(items);
                adapter.setOwnedObraIds(ownedObraIds);
            }

            @Override
            public void onFailure(@NonNull Call<List<ObraDTO>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
