package com.example.artistlan.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import com.example.artistlan.Conector.model.CarritoDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.CarritoObraAdapter;
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
    private int idUsuarioLogueado = -1;
    private final Set<Integer> obrasEnEliminacion = new HashSet<>();

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
        StringBuilder mensaje = new StringBuilder();
        appendLinea(mensaje, "Artista", info.nombre);
        appendLinea(mensaje, "Usuario", info.usuario);
        appendLinea(mensaje, "Contacto", info.contacto);
        appendLinea(mensaje, "Tipo", info.tipoContacto);
        appendLinea(mensaje, "Correo", info.correo);
        appendLinea(mensaje, "Telefono", info.telefono);

        String texto = mensaje.toString().trim();
        if (texto.isEmpty()) {
            texto = "No hay informacion de contacto disponible.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Contacto del artista")
                .setMessage(texto)
                .setNegativeButton("Cerrar", null);

        if (!isBlank(info.contacto)) {
            builder.setPositiveButton("Copiar contacto", (dialog, which) -> copiarTexto("Contacto del artista", info.contacto));
        }

        if (puedeAbrirContacto(info)) {
            builder.setNeutralButton("Contactar", (dialog, which) -> abrirContacto(info));
        }

        builder.show();
    }

    private boolean puedeAbrirContacto(ContactoVendedorInfo info) {
        if (isBlank(info.contacto)) {
            return false;
        }
        String tipo = safe(info.tipoContacto, "").toUpperCase(Locale.ROOT);
        return tipo.contains("MAIL")
                || tipo.contains("EMAIL")
                || tipo.contains("WHATS")
                || tipo.contains("TELEF")
                || tipo.contains("PHONE")
                || tipo.contains("INSTA");
    }

    private void abrirContacto(ContactoVendedorInfo info) {
        if (isBlank(info.contacto)) {
            return;
        }
        String tipo = safe(info.tipoContacto, "").toUpperCase(Locale.ROOT);
        String valor = info.contacto.trim();
        Intent intent;

        if (tipo.contains("MAIL") || tipo.contains("EMAIL") || valor.contains("@")) {
            intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + valor));
        } else if (tipo.contains("WHATS")) {
            String numero = valor.replaceAll("[^0-9]", "");
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + numero));
        } else if (tipo.contains("TELEF") || tipo.contains("PHONE")) {
            intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + valor));
        } else if (tipo.contains("INSTA")) {
            String user = valor.startsWith("@") ? valor.substring(1) : valor;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/" + user));
        } else {
            copiarTexto("Contacto del artista", valor);
            Toast.makeText(requireContext(), "Contacto copiado al portapapeles", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se pudo abrir la app de contacto", Toast.LENGTH_SHORT).show();
        }
    }

    private void copiarTexto(String label, String texto) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(requireContext(), "No se pudo acceder al portapapeles", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText(label, texto));
        Toast.makeText(requireContext(), "Contacto copiado", Toast.LENGTH_SHORT).show();
    }

    private void prepararCompraObra(CarritoDTO item, int position) {
        Toast.makeText(requireContext(), "Compra de obra disponible en el siguiente bloque", Toast.LENGTH_SHORT).show();
    }

    private void prepararCompraCarrito() {
        if (adapter == null || adapter.getItemCount() == 0) {
            Toast.makeText(requireContext(), "Tu carrito esta vacio", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(requireContext(), "Compra de carrito disponible en el siguiente bloque", Toast.LENGTH_SHORT).show();
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

    private static void appendLinea(StringBuilder builder, String label, String value) {
        if (builder == null || isBlank(value)) {
            return;
        }
        builder.append(label).append(": ").append(value.trim()).append('\n');
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
