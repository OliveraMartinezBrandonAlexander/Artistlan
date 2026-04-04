package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CarritoApi;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.PagoPaypalApi;
import com.example.artistlan.Conector.model.CapturarOrdenPaypalResponseDTO;
import com.example.artistlan.Conector.model.CarritoDTO;
import com.example.artistlan.Conector.model.CarritoRequestDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
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
    private CarritoApi carritoApi;
    private PagoPaypalApi pagoPaypalApi;
    private int idUsuarioLogueado = -1;
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
        if (adapter != null) {
            adapter.filtrar(texto);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        obraApi = RetrofitClient.getClient().create(ObraApi.class);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        carritoApi = RetrofitClient.getClient().create(CarritoApi.class);
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

        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext(), ModoTarjetaObra.EXPLORAR);
        adapter.setOnLikeClickListener(this::toggleLikeObra);
        adapter.setOnPrimaryActionClickListener(this::agregarObraAlCarrito);
        recyclerViewObras.setAdapter(adapter);

        obtenerObrasDeAPI();
    }

    private void agregarObraAlCarrito(TarjetaTextoObraItem obraItem, int position) {
        if (idUsuarioLogueado <= 0) {
            Toast.makeText(getContext(), "Debes iniciar sesion para agregar al carrito", Toast.LENGTH_SHORT).show();
            return;
        }

        CarritoRequestDTO requestDTO = new CarritoRequestDTO(idUsuarioLogueado, obraItem.getIdObra());
        carritoApi.agregarAlCarrito(requestDTO).enqueue(new Callback<CarritoDTO>() {
            @Override
            public void onResponse(@NonNull Call<CarritoDTO> call, @NonNull Response<CarritoDTO> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Obra agregada al carrito", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.code() == 409) {
                    Toast.makeText(getContext(), "Esta obra ya esta en tu carrito", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getContext(), "No se pudo agregar al carrito (" + response.code() + ")", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<CarritoDTO> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Error de red al agregar al carrito", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void intentarCapturarPagoPendienteComoFallback() {
        capturarPagoPendiente(false);
    }

    private void capturarPagoPendiente(boolean triggeredByDeepLink) {
        if (!isAdded() || capturandoPago) {
            return;
        }
        if (!PagoPaypalSessionManager.shouldCaptureOnReturn(requireContext())) {
            return;
        }
        if (!triggeredByDeepLink && !PagoPaypalSessionManager.hasApprovalFromDeepLink(requireContext())) {
            return;
        }

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
                if (!isAdded()) {
                    return;
                }

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
                if (!isAdded()) {
                    return;
                }
                PagoPaypalSessionManager.clear(requireContext());
                Toast.makeText(getContext(), "Error de red al capturar el pago: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLikeObra(TarjetaTextoObraItem obraItem, int position) {
        if (idUsuarioLogueado <= 0) {
            return;
        }

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
                if (!isAdded()) {
                    return;
                }

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
                    if (!categoriaFiltroActual.isEmpty()
                            && (dto.getNombreCategoria() == null || !categoriaFiltroActual.equalsIgnoreCase(dto.getNombreCategoria()))) {
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
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
