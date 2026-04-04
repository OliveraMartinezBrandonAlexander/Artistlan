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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CarritoApi;
import com.example.artistlan.Conector.api.CarritoPaypalApi;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.PagoPaypalApi;
import com.example.artistlan.Conector.model.CarritoDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalCarritoResponseDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalResponseDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.R;
import com.example.artistlan.TarjetaTextoObra.adapter.TarjetaTextoObraAdapter;
import com.example.artistlan.TarjetaTextoObra.model.ModoTarjetaObra;
import com.example.artistlan.TarjetaTextoObra.model.TarjetaTextoObraItem;
import com.example.artistlan.pagos.PagoPaypalSessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragCarrito extends Fragment {

    private ImageButton btnVolverExplorar;
    private Button btnComprarCarrito;
    private RecyclerView recyclerViewCarrito;
    private TextView tvCarritoVacio;
    private ProgressBar progressCarrito;
    private TarjetaTextoObraAdapter adapter;

    private CarritoApi carritoApi;
    private CarritoPaypalApi carritoPaypalApi;
    private FavoritosApi favoritosApi;
    private PagoPaypalApi pagoPaypalApi;

    private int idUsuarioLogueado = -1;
    private boolean creandoOrden = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_carrito, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));

        carritoApi = RetrofitClient.getClient().create(CarritoApi.class);
        carritoPaypalApi = RetrofitClient.getClient().create(CarritoPaypalApi.class);
        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        pagoPaypalApi = RetrofitClient.getClient().create(PagoPaypalApi.class);

        btnVolverExplorar = view.findViewById(R.id.btnVolverExplorar);
        btnComprarCarrito = view.findViewById(R.id.btnComprarCarrito);
        recyclerViewCarrito = view.findViewById(R.id.recyclerCarrito);
        tvCarritoVacio = view.findViewById(R.id.tvCarritoVacio);
        progressCarrito = view.findViewById(R.id.progressCarrito);

        btnVolverExplorar.setOnClickListener(v -> volverAExplorar());
        btnComprarCarrito.setOnClickListener(v -> iniciarCompraCarrito());

        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TarjetaTextoObraAdapter(new ArrayList<>(), requireContext(), ModoTarjetaObra.CARRITO);
        adapter.setOnLikeClickListener(this::toggleLikeObra);
        adapter.setOnPrimaryActionClickListener(this::iniciarCompraObra);
        adapter.setOnSecondaryActionClickListener(this::eliminarObraDelCarrito);
        recyclerViewCarrito.setAdapter(adapter);

        cargarCarrito();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarCarrito();
    }

    private void volverAExplorar() {
        NavController navController = androidx.navigation.fragment.NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.fragExplorar) {
            return;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .build();
        navController.navigate(R.id.fragExplorar, null, navOptions);
    }

    private void cargarCarrito() {
        if (idUsuarioLogueado <= 0) {
            mostrarListaVacia("Debes iniciar sesion para ver tu carrito");
            return;
        }

        progressCarrito.setVisibility(View.VISIBLE);
        carritoApi.obtenerCarritoUsuario(idUsuarioLogueado).enqueue(new Callback<List<CarritoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<CarritoDTO>> call, @NonNull Response<List<CarritoDTO>> response) {
                if (!isAdded()) {
                    return;
                }

                progressCarrito.setVisibility(View.GONE);
                if (response.code() == 204 || response.body() == null || response.body().isEmpty()) {
                    mostrarListaVacia("Tu carrito esta vacio");
                    return;
                }

                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "No se pudo cargar el carrito (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    mostrarListaVacia("No se pudo cargar tu carrito");
                    return;
                }

                List<TarjetaTextoObraItem> items = new ArrayList<>();
                for (CarritoDTO dto : response.body()) {
                    Integer idObra = dto.getIdObra();
                    if (idObra == null) {
                        continue;
                    }
                    items.add(new TarjetaTextoObraItem(
                            idObra,
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
                            false,
                            false
                    ));
                }

                adapter.actualizarLista(items);
                adapter.setOwnedObraIds(new HashSet<>());
                actualizarEstadoVacio(items.isEmpty());
            }

            @Override
            public void onFailure(@NonNull Call<List<CarritoDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progressCarrito.setVisibility(View.GONE);
                mostrarListaVacia("No se pudo conectar con el carrito");
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void eliminarObraDelCarrito(TarjetaTextoObraItem obraItem, int position) {
        if (idUsuarioLogueado <= 0) {
            return;
        }

        carritoApi.eliminarDelCarrito(idUsuarioLogueado, obraItem.getIdObra()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "No se pudo quitar del carrito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                adapter.removeItemAt(position);
                actualizarEstadoVacio(adapter.getItemCount() == 0);
                Toast.makeText(getContext(), "Obra eliminada del carrito", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Error de red al quitar del carrito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarCompraObra(TarjetaTextoObraItem obraItem, int position) {
        if (creandoOrden) {
            return;
        }
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
                if (!isAdded()) {
                    return;
                }

                manejarRespuestaCreacionOrden(response, obraItem.getIdObra());
            }

            @Override
            public void onFailure(@NonNull Call<CrearOrdenPaypalResponseDTO> call, @NonNull Throwable t) {
                creandoOrden = false;
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Error de red al crear la orden: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarCompraCarrito() {
        if (creandoOrden) {
            return;
        }
        if (idUsuarioLogueado <= 0) {
            Toast.makeText(getContext(), "Debes iniciar sesion para comprar", Toast.LENGTH_SHORT).show();
            return;
        }
        if (adapter == null || adapter.getItemCount() == 0) {
            Toast.makeText(getContext(), "Tu carrito esta vacio", Toast.LENGTH_SHORT).show();
            return;
        }

        creandoOrden = true;
        carritoPaypalApi.crearOrdenCarrito(idUsuarioLogueado).enqueue(new Callback<CrearOrdenPaypalCarritoResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<CrearOrdenPaypalCarritoResponseDTO> call, @NonNull Response<CrearOrdenPaypalCarritoResponseDTO> response) {
                creandoOrden = false;
                if (!isAdded()) {
                    return;
                }

                manejarRespuestaCreacionOrdenCarrito(response);
            }

            @Override
            public void onFailure(@NonNull Call<CrearOrdenPaypalCarritoResponseDTO> call, @NonNull Throwable t) {
                creandoOrden = false;
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(getContext(), "Error de red al crear la orden: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void manejarRespuestaCreacionOrden(Response<CrearOrdenPaypalResponseDTO> response, int obraId) {
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

        PagoPaypalSessionManager.savePendingOrder(requireContext(), paypalOrderId, obraId, idUsuarioLogueado);
        abrirApproveLink(approveLink);
    }

    private void manejarRespuestaCreacionOrdenCarrito(Response<CrearOrdenPaypalCarritoResponseDTO> response) {
        if (!response.isSuccessful() || response.body() == null) {
            Toast.makeText(getContext(), "No se pudo crear la orden de PayPal (" + response.code() + ")", Toast.LENGTH_LONG).show();
            return;
        }

        CrearOrdenPaypalCarritoResponseDTO body = response.body();
        String paypalOrderId = body.getPaypalOrderId();
        String approveLink = body.getApproveLink();

        if (paypalOrderId == null || paypalOrderId.trim().isEmpty() || approveLink == null || approveLink.trim().isEmpty()) {
            Toast.makeText(getContext(), "La respuesta del backend no contiene la orden de PayPal", Toast.LENGTH_LONG).show();
            return;
        }

        PagoPaypalSessionManager.savePendingOrder(requireContext(), paypalOrderId, -1, idUsuarioLogueado);
        abrirApproveLink(approveLink);
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

    private void mostrarListaVacia(String mensaje) {
        adapter.actualizarLista(new ArrayList<>());
        actualizarEstadoVacio(true);
        tvCarritoVacio.setText(mensaje);
    }

    private void actualizarEstadoVacio(boolean mostrarVacio) {
        tvCarritoVacio.setVisibility(mostrarVacio ? View.VISIBLE : View.GONE);
        recyclerViewCarrito.setVisibility(mostrarVacio ? View.GONE : View.VISIBLE);
        btnComprarCarrito.setVisibility(mostrarVacio ? View.GONE : View.VISIBLE);
    }
}
