package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.CarritoApi;
import com.example.artistlan.Conector.api.CarritoPaypalApi;
import com.example.artistlan.Conector.api.PagoPaypalApi;
import com.example.artistlan.Conector.model.CarritoDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalCarritoResponseDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalResponseDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.CarritoObraAdapter;
import com.example.artistlan.pagos.PagoPaypalSessionManager;
import com.example.artistlan.pagos.PagoSyncManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragCarrito extends Fragment {

    private ImageButton btnVolverExplorar;
    private Button btnComprarCarrito;
    private RecyclerView recyclerViewCarrito;
    private TextView tvCarritoVacio;
    private TextView tvCarritoVacioSub;
    private TextView tvResumenCantidad;
    private TextView tvResumenTotal;
    private LinearLayout layoutCarritoVacio;
    private View menuInferior;
    private ProgressBar progressCarrito;
    private CarritoObraAdapter adapter;
    private CarritoApi carritoApi;
    private PagoPaypalApi pagoPaypalApi;
    private CarritoPaypalApi carritoPaypalApi;
    private int idUsuarioLogueado = -1;
    private final Set<Integer> obrasEnEliminacion = new HashSet<>();
    private boolean compraEnProceso = false;
    private long ultimaVersionPagoRefrescada = 0L;
    private long ultimoIntentoCompraMs = 0L;
    private static final long COMPRA_TAP_GUARD_MS = 1200L;

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
        pagoPaypalApi = RetrofitClient.getClient().create(PagoPaypalApi.class);
        carritoPaypalApi = RetrofitClient.getClient().create(CarritoPaypalApi.class);
        ultimaVersionPagoRefrescada = PagoSyncManager.getLastCaptureAt(requireContext());

        btnVolverExplorar = view.findViewById(R.id.btnVolverExplorar);
        btnComprarCarrito = view.findViewById(R.id.btnComprarCarrito);
        recyclerViewCarrito = view.findViewById(R.id.recyclerCarrito);
        tvCarritoVacio = view.findViewById(R.id.tvCarritoVacio);
        tvCarritoVacioSub = view.findViewById(R.id.tvCarritoVacioSub);
        tvResumenCantidad = view.findViewById(R.id.tvResumenCantidad);
        tvResumenTotal = view.findViewById(R.id.tvResumenTotal);
        layoutCarritoVacio = view.findViewById(R.id.layoutCarritoVacio);
        progressCarrito = view.findViewById(R.id.progressCarrito);
        menuInferior = requireActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        btnVolverExplorar.setOnClickListener(v -> volverAExplorar());
        btnComprarCarrito.setOnClickListener(v -> prepararCompraCarrito());

        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CarritoObraAdapter(requireContext());
        adapter.setOnComprarClickListener(this::prepararCompraObra);
        adapter.setOnContactarClickListener(this::consultarContactoVendedor);
        adapter.setOnQuitarClickListener(this::confirmarQuitarDelCarrito);
        recyclerViewCarrito.setAdapter(adapter);

        actualizarResumen(0, 0d);
        actualizarEstadoBotonComprar(false);
        cargarCarrito();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
        if (PagoPaypalSessionManager.shouldCaptureOnReturn(requireContext())
                && !PagoPaypalSessionManager.hasApprovalFromDeepLink(requireContext())) {
            PagoPaypalSessionManager.clear(requireContext());
        }
        long ultimaVersionCaptura = PagoSyncManager.getLastCaptureAt(requireContext());
        if (ultimaVersionCaptura > ultimaVersionPagoRefrescada) {
            ultimaVersionPagoRefrescada = ultimaVersionCaptura;
        }
        cargarCarrito();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
    }

    private void volverAExplorar() {
        NavController navController = androidx.navigation.fragment.NavHostFragment.findNavController(this);
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == R.id.fragExplorar) {
            return;
        }

        boolean regreso = navController.popBackStack();
        if (regreso) {
            return;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .build();
        try {
            navController.navigate(R.id.fragExplorar, null, navOptions);
        } catch (Exception ignored) {
            // evitar cierre de actividad por fallback de back global
        }
    }

    private void cargarCarrito() {
        if (idUsuarioLogueado <= 0) {
            adapter.submitList(new ArrayList<>());
            actualizarEstadoVacio(true, "Debes iniciar sesion para ver tu carrito", "Inicia sesion para consultar tus reservas.");
            actualizarResumen(0, 0d);
            actualizarEstadoBotonComprar(false);
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

                if (!response.isSuccessful() && response.code() != 204) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se pudo cargar el carrito (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    adapter.submitList(new ArrayList<>());
                    actualizarEstadoVacio(true, "No se pudo cargar tu carrito", "Intentalo nuevamente en unos segundos.");
                    actualizarResumen(0, 0d);
                    actualizarEstadoBotonComprar(false);
                    return;
                }

                List<CarritoDTO> lista = normalizarItems(response.body());
                boolean vacio = lista.isEmpty();
                adapter.submitList(lista);
                actualizarEstadoBotonComprar(!vacio);

                if (vacio) {
                    actualizarEstadoVacio(true, "Tu carrito esta vacio", "Cuando reserves obras apareceran aqui.");
                    actualizarResumen(0, 0d);
                    return;
                }

                actualizarEstadoVacio(false, "", "");
                double totalLocal = calcularTotalLocal(lista);
                actualizarResumen(lista.size(), totalLocal);
                cargarTotalDesdeBackend(lista.size(), totalLocal);
            }

            @Override
            public void onFailure(@NonNull Call<List<CarritoDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progressCarrito.setVisibility(View.GONE);
                adapter.submitList(new ArrayList<>());
                actualizarEstadoVacio(true, "No se pudo conectar con el carrito", "Revisa tu conexion e intentalo de nuevo.");
                actualizarResumen(0, 0d);
                actualizarEstadoBotonComprar(false);
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<CarritoDTO> normalizarItems(List<CarritoDTO> body) {
        List<CarritoDTO> salida = new ArrayList<>();
        if (body == null) {
            return salida;
        }
        for (CarritoDTO dto : body) {
            if (dto == null || dto.getIdObra() == null) {
                continue;
            }
            salida.add(dto);
        }
        return salida;
    }

    private void cargarTotalDesdeBackend(int cantidadItems, double totalFallback) {
        carritoApi.obtenerTotalCarrito(idUsuarioLogueado).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    actualizarResumen(cantidadItems, totalFallback);
                    return;
                }
                double total = parseTotal(response.body(), totalFallback);
                actualizarResumen(cantidadItems, total);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                actualizarResumen(cantidadItems, totalFallback);
            }
        });
    }

    private double parseTotal(JsonElement body, double fallback) {
        if (body == null || body.isJsonNull()) {
            return fallback;
        }
        try {
            if (body.isJsonPrimitive()) {
                return parseDouble(body.getAsJsonPrimitive(), fallback);
            }
            if (body.isJsonObject()) {
                JsonObject object = body.getAsJsonObject();
                Double total = getAsDouble(object,
                        "total", "montoTotal", "totalCarrito", "importeTotal", "totalAcumulado");
                return total != null ? total : fallback;
            }
        } catch (Exception ignored) {
            return fallback;
        }
        return fallback;
    }

    private void confirmarQuitarDelCarrito(CarritoDTO item, int position) {
        Integer idObra = item.getIdObra();
        if (idObra == null) {
            Toast.makeText(requireContext(), "No se pudo identificar la obra a eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (obrasEnEliminacion.contains(idObra)) {
            Toast.makeText(requireContext(), "Ya se esta quitando esta obra del carrito", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = safe(item.getTitulo(), "esta obra");
        new AlertDialog.Builder(requireContext())
                .setTitle("Quitar del carrito")
                .setMessage("Si quitas \"" + titulo + "\" del carrito perderas su reserva y la obra volvera a estar disponible para otros compradores.\n\nDeseas continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Quitar", (dialog, which) -> quitarDelCarrito(idObra))
                .show();
    }

    private void quitarDelCarrito(int idObra) {
        if (idUsuarioLogueado <= 0) {
            return;
        }
        obrasEnEliminacion.add(idObra);
        carritoApi.eliminarDelCarrito(idUsuarioLogueado, idObra).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                obrasEnEliminacion.remove(idObra);
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful()) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se pudo quitar del carrito (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(requireContext(), "Obra eliminada del carrito", Toast.LENGTH_SHORT).show();
                cargarCarrito();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                obrasEnEliminacion.remove(idObra);
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), "Error de red al quitar del carrito", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void consultarContactoVendedor(CarritoDTO item, int position) {
        Integer idObra = item.getIdObra();
        if (idUsuarioLogueado <= 0 || idObra == null) {
            Toast.makeText(requireContext(), "No se pudo cargar el contacto del artista", Toast.LENGTH_SHORT).show();
            return;
        }

        progressCarrito.setVisibility(View.VISIBLE);
        carritoApi.obtenerContactoVendedor(idUsuarioLogueado, idObra).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (!isAdded()) {
                    return;
                }
                progressCarrito.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se pudo cargar el contacto (" + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                ContactoVendedorInfo info = parseContacto(response.body());
                if (info == null || !info.hasData()) {
                    Toast.makeText(requireContext(), "Este artista no tiene contacto disponible", Toast.LENGTH_SHORT).show();
                    return;
                }

                mostrarDialogoContacto(info);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                progressCarrito.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de red al cargar el contacto", Toast.LENGTH_LONG).show();
            }
        });
    }

    private ContactoVendedorInfo parseContacto(JsonElement body) {
        ContactoVendedorInfo info = new ContactoVendedorInfo();
        if (body == null || body.isJsonNull()) {
            return info;
        }
        if (body.isJsonPrimitive()) {
            info.contacto = body.getAsString();
            return info;
        }
        if (!body.isJsonObject()) {
            return info;
        }

        JsonObject root = body.getAsJsonObject();
        JsonObject vendedorObj = getFirstObject(root, "vendedor", "usuario", "artista", "datosVendedor");
        JsonObject contactoObj = getFirstObject(root, "contacto", "datosContacto", "contactoVendedor");

        info.nombre = firstNonBlank(root, "nombreVendedor", "nombreArtista", "nombre");
        if (isBlank(info.nombre) && vendedorObj != null) {
            info.nombre = firstNonBlank(vendedorObj, "nombreVendedor", "nombreArtista", "nombre", "nombreCompleto");
        }

        info.usuario = firstNonBlank(root, "usuarioVendedor", "usuarioOrigen", "usuario", "username", "login");
        if (isBlank(info.usuario) && vendedorObj != null) {
            info.usuario = firstNonBlank(vendedorObj, "usuario", "username", "login");
        }

        info.tipoContacto = firstNonBlank(root, "tipoContacto", "canal", "tipo");
        info.contacto = firstNonBlank(root, "contacto", "valorContacto", "datoContacto", "medioContacto");
        if (contactoObj != null) {
            if (isBlank(info.tipoContacto)) {
                info.tipoContacto = firstNonBlank(contactoObj, "tipoContacto", "tipo", "canal");
            }
            if (isBlank(info.contacto)) {
                info.contacto = firstNonBlank(contactoObj, "contacto", "valor", "dato");
            }
        }

        info.correo = firstNonBlank(root, "correo", "email", "correoVendedor", "emailVendedor");
        if (isBlank(info.correo) && vendedorObj != null) {
            info.correo = firstNonBlank(vendedorObj, "correo", "email");
        }

        info.telefono = firstNonBlank(root, "telefono", "telefonoVendedor", "whatsapp", "celular");
        if (isBlank(info.telefono) && vendedorObj != null) {
            info.telefono = firstNonBlank(vendedorObj, "telefono", "celular", "whatsapp");
        }

        if (isBlank(info.contacto)) {
            info.contacto = !isBlank(info.correo) ? info.correo : info.telefono;
        }
        if (isBlank(info.tipoContacto)) {
            info.tipoContacto = inferirTipo(info.contacto);
        }
        return info;
    }

    private void mostrarDialogoContacto(ContactoVendedorInfo info) {
        ContactoDialogHelper.mostrarDialogoContacto(
                this,
                "Contacto del artista",
                info.nombre,
                info.usuario,
                info.tipoContacto,
                info.contacto,
                info.correo,
                info.telefono
        );
    }

    private void prepararCompraObra(CarritoDTO item, int position) {
        if (item == null || item.getIdObra() == null || item.getIdObra() <= 0) {
            Toast.makeText(requireContext(), "No se pudo identificar la obra a comprar", Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarDialogoConfirmarCompra(() -> iniciarCompraObra(item));
    }

    private void prepararCompraCarrito() {
        if (adapter == null || adapter.getItemCount() == 0) {
            Toast.makeText(requireContext(), "Tu carrito esta vacio", Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarDialogoConfirmarCompra(this::iniciarCompraCarrito);
    }

    public void recargarDespuesDePago() {
        if (!isAdded()) {
            return;
        }
        ultimaVersionPagoRefrescada = PagoSyncManager.getLastCaptureAt(requireContext());
        cargarCarrito();
    }

    private void mostrarDialogoConfirmarCompra(Runnable onConfirmar) {
        if (!isAdded()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar compra")
                .setMessage("Estas a punto de comprar esta obra.\n\nTu pago se procesara de forma segura mediante PayPal.\nSi lo deseas, puedes contactar al vendedor antes de continuar.\nDespues podras dar seguimiento en tu historial.\n\nDeseas continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    if (onConfirmar != null) {
                        onConfirmar.run();
                    }
                })
                .show();
    }

    private void iniciarCompraObra(CarritoDTO item) {
        if (!puedeLanzarCompraAhora()) {
            return;
        }
        if (compraEnProceso) {
            Toast.makeText(requireContext(), "Ya estamos procesando una compra", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idUsuarioLogueado <= 0) {
            Toast.makeText(requireContext(), "Debes iniciar sesion para comprar", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer idObra = item.getIdObra();
        if (idObra == null || idObra <= 0) {
            Toast.makeText(requireContext(), "No se pudo identificar la obra a comprar", Toast.LENGTH_SHORT).show();
            return;
        }

        setCompraEnProceso(true);
        pagoPaypalApi.crearOrden(idObra, idUsuarioLogueado).enqueue(new Callback<CrearOrdenPaypalResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<CrearOrdenPaypalResponseDTO> call, @NonNull Response<CrearOrdenPaypalResponseDTO> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    setCompraEnProceso(false);
                    String backendMessage = resolveCompraErrorMessage(response, "No se pudo iniciar la compra de la obra");
                    Toast.makeText(requireContext(), backendMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                CrearOrdenPaypalResponseDTO body = response.body();
                String orderId = safe(body.getPaypalOrderId(), "");
                String approveLink = safe(body.getApproveLink(), "");
                if (orderId.isEmpty() || approveLink.isEmpty()) {
                    setCompraEnProceso(false);
                    Toast.makeText(requireContext(), "No se recibio un enlace valido de PayPal", Toast.LENGTH_LONG).show();
                    return;
                }

                PagoPaypalSessionManager.savePendingOrder(requireContext(), orderId, idObra, idUsuarioLogueado);
                PagoPaypalSessionManager.markApprovalOpened(requireContext());
                abrirPaypal(approveLink);
                setCompraEnProceso(false);
            }

            @Override
            public void onFailure(@NonNull Call<CrearOrdenPaypalResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                setCompraEnProceso(false);
                Toast.makeText(requireContext(), "Error de red al iniciar compra: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarCompraCarrito() {
        if (!puedeLanzarCompraAhora()) {
            return;
        }
        if (compraEnProceso) {
            Toast.makeText(requireContext(), "Ya estamos procesando una compra", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idUsuarioLogueado <= 0) {
            Toast.makeText(requireContext(), "Debes iniciar sesion para comprar", Toast.LENGTH_SHORT).show();
            return;
        }

        setCompraEnProceso(true);
        carritoPaypalApi.crearOrdenCarrito(idUsuarioLogueado).enqueue(new Callback<CrearOrdenPaypalCarritoResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<CrearOrdenPaypalCarritoResponseDTO> call, @NonNull Response<CrearOrdenPaypalCarritoResponseDTO> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    setCompraEnProceso(false);
                    String backendMessage = resolveCompraErrorMessage(response, "No se pudo iniciar la compra del carrito");
                    Toast.makeText(requireContext(), backendMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                CrearOrdenPaypalCarritoResponseDTO body = response.body();
                String orderId = safe(body.getPaypalOrderId(), "");
                String approveLink = safe(body.getApproveLink(), "");
                if (orderId.isEmpty() || approveLink.isEmpty()) {
                    setCompraEnProceso(false);
                    Toast.makeText(requireContext(), "No se recibio un enlace valido de PayPal", Toast.LENGTH_LONG).show();
                    return;
                }

                PagoPaypalSessionManager.savePendingOrder(requireContext(), orderId, -1, idUsuarioLogueado);
                PagoPaypalSessionManager.markApprovalOpened(requireContext());
                abrirPaypal(approveLink);
                setCompraEnProceso(false);
            }

            @Override
            public void onFailure(@NonNull Call<CrearOrdenPaypalCarritoResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                setCompraEnProceso(false);
                Toast.makeText(requireContext(), "Error de red al iniciar compra: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirPaypal(String approveLink) {
        if (!isAdded()) {
            return;
        }
        ultimoIntentoCompraMs = SystemClock.elapsedRealtime();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(approveLink));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se pudo abrir PayPal en este dispositivo", Toast.LENGTH_LONG).show();
        }
    }

    private void setCompraEnProceso(boolean enProceso) {
        compraEnProceso = enProceso;
        progressCarrito.setVisibility(enProceso ? View.VISIBLE : View.GONE);
        btnComprarCarrito.setEnabled(!enProceso && adapter != null && adapter.getItemCount() > 0);
        btnComprarCarrito.setAlpha(btnComprarCarrito.isEnabled() ? 1f : 0.55f);
    }

    private boolean puedeLanzarCompraAhora() {
        long ahora = SystemClock.elapsedRealtime();
        if (ahora - ultimoIntentoCompraMs < COMPRA_TAP_GUARD_MS) {
            Toast.makeText(requireContext(), "Espera un momento antes de volver a intentar", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String resolveCompraErrorMessage(Response<?> response, String fallback) {
        int code = response != null ? response.code() : -1;
        String backendMessage = ApiErrorParser.extractMessage(response);
        if (code == 409) {
            String normalized = backendMessage == null ? "" : backendMessage.toLowerCase(Locale.ROOT);
            if (normalized.contains("order_not_approved")
                    || normalized.contains("not approved")
                    || normalized.contains("cancelad")
                    || normalized.contains("no aprob")) {
                return "El pago no fue aprobado en PayPal. Puedes intentarlo nuevamente.";
            }
            return "Hubo un conflicto al procesar el pago en PayPal. Intenta nuevamente.";
        }
        if (backendMessage != null && !backendMessage.trim().isEmpty()) {
            return backendMessage;
        }
        if (code == 404) {
            return "La reserva ya no esta disponible para esta obra";
        }
        if (code >= 500) {
            return "El servidor no pudo procesar la compra en este momento";
        }
        return fallback + (code > 0 ? " (" + code + ")" : "");
    }

    private void actualizarEstadoVacio(boolean mostrarVacio, String titulo, String subtitulo) {
        layoutCarritoVacio.setVisibility(mostrarVacio ? View.VISIBLE : View.GONE);
        recyclerViewCarrito.setVisibility(mostrarVacio ? View.GONE : View.VISIBLE);
        if (mostrarVacio) {
            tvCarritoVacio.setText(safe(titulo, "Tu carrito esta vacio"));
            tvCarritoVacioSub.setText(safe(subtitulo, ""));
        }
    }

    private void actualizarResumen(int cantidadItems, double total) {
        tvResumenCantidad.setText("Items: " + Math.max(0, cantidadItems));
        tvResumenTotal.setText(String.format(Locale.US, "Total: $ %,.2f", Math.max(0d, total)));
    }

    private void actualizarEstadoBotonComprar(boolean habilitado) {
        btnComprarCarrito.setEnabled(habilitado);
        btnComprarCarrito.setAlpha(habilitado ? 1f : 0.55f);
    }

    private double calcularTotalLocal(List<CarritoDTO> items) {
        double total = 0d;
        if (items == null) {
            return total;
        }
        for (CarritoDTO item : items) {
            Double precio = item.getPrecio();
            if (precio != null && precio > 0) {
                total += precio;
            }
        }
        return total;
    }

    private static Double getAsDouble(JsonObject object, String... keys) {
        if (object == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!object.has(key)) {
                continue;
            }
            JsonElement value = object.get(key);
            if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
                continue;
            }
            Double parsed = parseDouble(value.getAsJsonPrimitive(), null);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static Double parseDouble(JsonPrimitive primitive, Double fallback) {
        try {
            if (primitive.isNumber()) {
                return primitive.getAsDouble();
            }
            if (primitive.isString()) {
                String raw = primitive.getAsString();
                if (raw == null) {
                    return fallback;
                }
                String normalized = raw.replace("$", "").replace(",", "").trim();
                if (normalized.isEmpty()) {
                    return fallback;
                }
                return Double.parseDouble(normalized);
            }
        } catch (Exception ignored) {
            return fallback;
        }
        return fallback;
    }

    private static JsonObject getFirstObject(JsonObject root, String... keys) {
        if (root == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!root.has(key)) {
                continue;
            }
            JsonElement value = root.get(key);
            if (value != null && value.isJsonObject()) {
                return value.getAsJsonObject();
            }
        }
        return null;
    }

    private static String firstNonBlank(JsonObject object, String... keys) {
        if (object == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!object.has(key)) {
                continue;
            }
            JsonElement value = object.get(key);
            if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
                continue;
            }
            String text = value.getAsString();
            if (text != null && !text.trim().isEmpty()) {
                return text.trim();
            }
        }
        return null;
    }

    private static String inferirTipo(String contacto) {
        if (contacto == null || contacto.trim().isEmpty()) {
            return "No especificado";
        }
        String value = contacto.trim();
        if (value.contains("@")) {
            return "EMAIL";
        }
        if (value.matches(".*\\d{7,}.*")) {
            return "TELEFONO";
        }
        if (value.contains("instagram.com") || value.startsWith("@")) {
            return "INSTAGRAM";
        }
        return "OTRO";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static class ContactoVendedorInfo {
        String nombre;
        String usuario;
        String tipoContacto;
        String contacto;
        String correo;
        String telefono;

        boolean hasData() {
            return !isBlank(nombre)
                    || !isBlank(usuario)
                    || !isBlank(contacto)
                    || !isBlank(correo)
                    || !isBlank(telefono);
        }
    }
}
