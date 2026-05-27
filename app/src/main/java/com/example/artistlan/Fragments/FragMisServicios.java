package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.FavoritosApi;
import com.example.artistlan.Conector.api.ServicioApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.ServicioDTO;
import com.example.artistlan.Conector.model.ValidarPasswordRequestDTO;
import com.example.artistlan.Conector.model.ValidarPasswordResponseDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.TarjetaTextoServicio.adapter.TarjetaTextoServicioAdapter;
import com.example.artistlan.TarjetaTextoServicio.model.TarjetaTextoServicioItem;
import com.example.artistlan.utils.DialogThemeHelper;
import com.example.artistlan.utils.LottieFeedbackDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragMisServicios extends Fragment {

    private static final long LIKE_THROTTLE_MS = 500L;
    private static final String TAG_CRUD = "ServicioCrudDebug";
    private static final String TAG_REFRESH = "RefreshMiArteDebug";
    private RecyclerView recyclerMisServicios;
    private TextView tvEmptyMisServicios;
    private ProgressBar progressMisServicios;
    private TarjetaTextoServicioAdapter adapter;
    private FavoritosApi favoritosApi;
    private int idUsuarioLogueado = -1;
    private final Map<Integer, Long> lastLikeClickByServicio = new HashMap<>();
    private final Set<Integer> likesEnVuelo = new HashSet<>();
    private final Set<Integer> serviciosEnEliminacion = new HashSet<>();
    private LottieFeedbackDialog feedbackDialog;
    private SessionManager sessionManager;
    private UsuarioApi usuarioApi;
    private boolean debeRecargarEnResume = true;
    private boolean isLoading = false;
    private boolean validacionPasswordEnCurso = false;
    private int requestToken = 0;
    private int ultimoColorTemaAplicado = Integer.MIN_VALUE;
    private static final Map<Integer, List<TarjetaTextoServicioItem>> serviciosCachePorUsuario = new HashMap<>();

    public static final String ARG_MODO_EDICION = "modo_edicion";
    public static final String ARG_SERVICIO_ID = "servicio_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_mis_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        recyclerMisServicios = view.findViewById(R.id.recyclerMisServicios);
        tvEmptyMisServicios = view.findViewById(R.id.tvEmptyMisServicios);
        progressMisServicios = view.findViewById(R.id.progressMisServicios);
        recyclerMisServicios.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMisServicios.setItemAnimator(null);
        ThemeApplier.applyTextPrimary(view.findViewById(R.id.tvTituloMisServicios), new ThemeManager(requireContext()));
        ThemeApplier.applyTextSecondary(tvEmptyMisServicios, new ThemeManager(requireContext()));
        adapter = new TarjetaTextoServicioAdapter(new ArrayList<>(), requireContext());
        adapter.setEntryAnimationsEnabled(false);
        adapter.setPortfolioHeaderEnabled(true);
        adapter.setOwnershipBadgeEnabled(false);
        adapter.setCurrentUserId(idUsuarioLogueado);
        adapter.setOnLikeClickListener(this::toggleLikeServicio);
        adapter.setOnEditClickListener(this::editarServicio);
        adapter.setOnDeleteClickListener(this::confirmarEliminacionServicio);
        recyclerMisServicios.setAdapter(adapter);
        actualizarEstadoVacio(false);
        feedbackDialog = new LottieFeedbackDialog(requireContext());

        favoritosApi = RetrofitClient.getClient().create(FavoritosApi.class);
        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);
        sessionManager = new SessionManager(requireContext());
        getParentFragmentManager().setFragmentResultListener(
                FragPortafolio.RESULT_KEY_PORTAFOLIO_REFRESH,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String target = result.getString(FragPortafolio.RESULT_EXTRA_TARGET, "");
                    boolean guardado = result.getBoolean(FragPortafolio.RESULT_EXTRA_GUARDADO, true);
                    String modo = result.getString(FragPortafolio.RESULT_EXTRA_MODO, "");
                    if (!FragPortafolio.TARGET_SERVICIOS.equals(target)) {
                        Log.d(TAG_REFRESH, "Resultado ignorado por servicios target=" + target
                                + " guardado=" + guardado
                                + " modo=" + modo
                                + " obrasNoSeTocan=true");
                        return;
                    }
                    Log.d(TAG_REFRESH, "Resultado recibido servicios guardado=" + guardado
                            + " modo=" + modo
                            + " sizeAntes=" + (adapter != null ? adapter.getItemCount() : -1));
                    if (!guardado) {
                        debeRecargarEnResume = false;
                        boolean cacheRestaurada = restaurarServiciosCacheSiExiste();
                        Log.d(TAG_REFRESH, "Regreso servicios sin guardar cacheRestaurada=" + cacheRestaurada
                                + " sizeDespues=" + (adapter != null ? adapter.getItemCount() : -1));
                        return;
                    }
                    FragPortafolio.marcarRefreshPendiente(FragPortafolio.TARGET_SERVICIOS);
                    debeRecargarEnResume = true;
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        debeRecargarEnResume = false;
                        ensureDataLoadedForCurrentState();
                    }
                }
        );
        boolean refreshPendiente = FragPortafolio.hasRefreshPendiente(FragPortafolio.TARGET_SERVICIOS);
        if (debeRecargarEnResume || refreshPendiente) {
            ensureDataLoadedForCurrentState();
        }
    }

    @Override
    public void onDestroyView() {
        if (feedbackDialog != null) {
            feedbackDialog.release();
            feedbackDialog = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshThemeOnly();
        boolean refreshPendiente = FragPortafolio.hasRefreshPendiente(FragPortafolio.TARGET_SERVICIOS);
        if ((debeRecargarEnResume || refreshPendiente) && isAdded()) {
            ensureDataLoadedForCurrentState();
        }
    }

    public void refreshThemeOnly() {
        if (!isAdded()) {
            return;
        }
        ThemeManager tm = new ThemeManager(requireContext());
        int colorActual = tm.color(ThemeKeys.ACCENT_PRIMARY);
        if (colorActual == ultimoColorTemaAplicado) {
            return;
        }
        ultimoColorTemaAplicado = colorActual;
        View view = getView();
        if (view != null) {
            ThemeModuleStyler.styleFragment(this, view);
        }
        ThemeApplier.applyTextPrimary(view != null ? view.findViewById(R.id.tvTituloMisServicios) : null, tm);
        ThemeApplier.applyTextSecondary(tvEmptyMisServicios, tm);
        if (adapter != null && adapter.getItemCount() > 0) {
            adapter.notifyDataSetChanged();
        }
    }

    public void ensureDataLoadedForCurrentState() {
        if (!isAdded() || recyclerMisServicios == null || adapter == null) {
            return;
        }
        if (recyclerMisServicios.getAdapter() != adapter) {
            recyclerMisServicios.setAdapter(adapter);
        }
        actualizarEstadoVacio(false);
        boolean refreshPendiente = FragPortafolio.hasRefreshPendiente(FragPortafolio.TARGET_SERVICIOS);
        if (refreshPendiente) {
            debeRecargarEnResume = false;
            if (!isLoading) {
                FragPortafolio.limpiarRefreshPendiente(FragPortafolio.TARGET_SERVICIOS);
                cargarServiciosDelUsuario();
            }
            return;
        }
        if (adapter.getItemCount() > 0) {
            return;
        }
        sincronizarUsuarioActual();
        if (restaurarServiciosCacheSiExiste()) {
            return;
        }
        if (!isLoading) {
            debeRecargarEnResume = false;
            cargarServiciosDelUsuario();
        }
    }

    private List<TarjetaTextoServicioItem> convertirDTOaItem(List<ServicioDTO> dtoList, Set<Integer> serviciosFavoritos) {
        List<TarjetaTextoServicioItem> items = new ArrayList<>();
        List<ServicioDTO> ordenados = dtoList != null ? new ArrayList<>(dtoList) : new ArrayList<>();
        ordenados.sort((a, b) -> Integer.compare(safeServicioId(b), safeServicioId(a)));

        for (ServicioDTO dto : ordenados) {
            Integer idServicio = dto.getIdServicio();
            boolean esFavoritoReal = idServicio != null && serviciosFavoritos.contains(idServicio);
            items.add(new TarjetaTextoServicioItem(
                    idServicio,
                    dto.getIdUsuario(),
                    dto.getTitulo(),
                    dto.getDescripcion(),
                    dto.getContacto(),
                    dto.getTipoContacto(),
                    dto.getTecnicas(),
                    dto.getNombreUsuario(),
                    dto.getCategoria(),
                    dto.getFotoPerfilAutor(),
                    dto.getPrecioMin(),
                    dto.getPrecioMax(),
                    dto.getLikes() != null ? dto.getLikes() : 0,
                    esFavoritoReal,
                    false
            ));
        }

        return items;
    }

    private boolean isLikeActionBlocked(int idServicio) {
        long now = SystemClock.elapsedRealtime();
        Long last = lastLikeClickByServicio.get(idServicio);
        if (last != null && now - last < LIKE_THROTTLE_MS) {
            return true;
        }
        lastLikeClickByServicio.put(idServicio, now);
        return false;
    }

    private void toggleLikeServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (idUsuarioLogueado <= 0 || servicioItem.getIdServicio() == null) return;
        if (isLikeActionBlocked(servicioItem.getIdServicio()) || likesEnVuelo.contains(servicioItem.getIdServicio())) return;
        likesEnVuelo.add(servicioItem.getIdServicio());

        final boolean favoritoAnterior = servicioItem.isFavorito();
        final int likesAnterior = servicioItem.getLikes();
        servicioItem.setFavorito(!favoritoAnterior);
        servicioItem.setLikes(Math.max(0, likesAnterior + (favoritoAnterior ? -1 : 1)));
        adapter.notifyLikeChangedPartial(position);

        FavoritoDTO dto = new FavoritoDTO();
        dto.idUsuario = idUsuarioLogueado;
        dto.idServicio = servicioItem.getIdServicio();

        Call<Void> call = favoritoAnterior ? favoritosApi.eliminarFavorito(dto) : favoritosApi.agregarFavorito(dto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                likesEnVuelo.remove(servicioItem.getIdServicio());
                if (response.isSuccessful()) {
                    return;
                }

                if (!favoritoAnterior && response.code() == 409) {
                    servicioItem.setFavorito(true);
                    refreshLikeCount(servicioItem, position);
                    return;
                }

                servicioItem.setFavorito(favoritoAnterior);
                servicioItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "No se pudo actualizar favorito (" + response.code() + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                likesEnVuelo.remove(servicioItem.getIdServicio());
                servicioItem.setFavorito(favoritoAnterior);
                servicioItem.setLikes(likesAnterior);
                adapter.notifyLikeChanged(position);
                Toast.makeText(requireContext(), "Error de red al actualizar favorito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshLikeCount(TarjetaTextoServicioItem item, int position) {
        Integer idServicio = item.getIdServicio();
        if (idServicio == null) return;

        favoritosApi.likesServicio(idServicio).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                item.setLikes(Math.max(0, response.body()));
                adapter.notifyLikeChanged(position);
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                // si falla, mantenemos valor optimista
            }
        });
    }

    private void refreshLikeCounts(List<TarjetaTextoServicioItem> items) {
        for (int i = 0; i < items.size(); i++) {
            refreshLikeCount(items.get(i), i);
        }
    }

    private void editarServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (!isAdded() || servicioItem.getIdServicio() == null) {
            return;
        }

        Log.d(TAG_CRUD, "Editar servicio click usuarioId=" + idUsuarioLogueado
                + " idServicio=" + servicioItem.getIdServicio()
                + " position=" + position
                + " titulo=" + servicioItem.getTitulo());
        Bundle args = new Bundle();
        args.putBoolean(ARG_MODO_EDICION, true);
        args.putInt(ARG_SERVICIO_ID, servicioItem.getIdServicio());
        NavHostFragment.findNavController(this).navigate(R.id.fragSubirServicio, args);
    }

    private void confirmarEliminacionServicio(TarjetaTextoServicioItem servicioItem, int position) {
        if (!isAdded()) {
            return;
        }

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(24);
        contenedor.setPadding(padding, dpToPx(8), padding, 0);

        TextView mensaje = new TextView(requireContext());
        mensaje.setText("Ingresa tu contraseña para continuar.");
        contenedor.addView(mensaje);

        EditText etContrasena = new EditText(requireContext());
        etContrasena.setHint("Contraseña");
        etContrasena.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etContrasena.setSingleLine(true);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.topMargin = dpToPx(12);
        contenedor.addView(etContrasena, inputParams);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar contraseña")
                .setView(contenedor)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", null)
                .create();
        dialog.setOnDismissListener(d -> etContrasena.setText(""));
        dialog.show();
        DialogThemeHelper.styleAlertDialog(dialog, requireContext());

        Button btnConfirmar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnCancelar = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        btnConfirmar.setOnClickListener(v -> {
            if (validacionPasswordEnCurso) {
                return;
            }
            String contrasena = etContrasena.getText() != null ? etContrasena.getText().toString().trim() : "";
            if (contrasena.isEmpty()) {
                etContrasena.setError("Ingresa tu contraseña");
                etContrasena.requestFocus();
                return;
            }
            validacionPasswordEnCurso = true;
            setEstadoDialogoValidacionPassword(etContrasena, btnConfirmar, btnCancelar, false);
            validarPasswordActual(contrasena, new PasswordValidationCallback() {
                @Override
                public void onValid() {
                    validacionPasswordEnCurso = false;
                    if (!isAdded()) {
                        return;
                    }
                    dialog.dismiss();
                    eliminarServicio(servicioItem, position);
                }

                @Override
                public void onInvalid(String mensajeError) {
                    validacionPasswordEnCurso = false;
                    if (!isAdded()) {
                        return;
                    }
                    etContrasena.setText("");
                    etContrasena.requestFocus();
                    setEstadoDialogoValidacionPassword(etContrasena, btnConfirmar, btnCancelar, true);
                    Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String mensajeError) {
                    validacionPasswordEnCurso = false;
                    if (!isAdded()) {
                        return;
                    }
                    setEstadoDialogoValidacionPassword(etContrasena, btnConfirmar, btnCancelar, true);
                    Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void validarPasswordActual(String contrasena, PasswordValidationCallback callback) {
        if (usuarioApi == null || sessionManager == null) {
            callback.onError("No se pudo validar la contraseña");
            return;
        }
        String token = sessionManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            callback.onError("No se pudo validar la contraseña");
            return;
        }
        usuarioApi.validarPassword(
                "Bearer " + token.trim(),
                new ValidarPasswordRequestDTO(contrasena)
        ).enqueue(new Callback<ValidarPasswordResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ValidarPasswordResponseDTO> call, @NonNull Response<ValidarPasswordResponseDTO> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isValida()) {
                    callback.onValid();
                    return;
                }
                if (response.code() == 403) {
                    callback.onInvalid("Contraseña incorrecta");
                    return;
                }
                callback.onError("No se pudo validar la contraseña");
            }

            @Override
            public void onFailure(@NonNull Call<ValidarPasswordResponseDTO> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                callback.onError("Inténtalo de nuevo");
            }
        });
    }

    private void setEstadoDialogoValidacionPassword(EditText etContrasena, Button btnConfirmar, Button btnCancelar, boolean habilitado) {
        if (etContrasena != null) {
            etContrasena.setEnabled(habilitado);
        }
        if (btnConfirmar != null) {
            btnConfirmar.setEnabled(habilitado);
        }
        if (btnCancelar != null) {
            btnCancelar.setEnabled(habilitado);
        }
    }

    private void eliminarServicio(TarjetaTextoServicioItem servicioItem, int position) {
        Integer idServicio = servicioItem.getIdServicio();
        if (idUsuarioLogueado <= 0 || idServicio == null) {
            Toast.makeText(requireContext(), "Error de usuario.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (serviciosEnEliminacion.contains(idServicio)) {
            Toast.makeText(requireContext(), "Ya se est\u00E1 procesando la eliminaci\u00F3n de este servicio", Toast.LENGTH_SHORT).show();
            return;
        }
        serviciosEnEliminacion.add(idServicio);
        mostrarFeedbackCarga("Eliminando servicio...");
        Log.d(TAG_CRUD, "Borrar servicio request DELETE portafolioPersonal/{usuarioId}/{idServicio}"
                + " usuarioId=" + idUsuarioLogueado
                + " idServicio=" + idServicio
                + " position=" + position
                + " sizeAntes=" + (adapter != null ? adapter.getItemCount() : -1));

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        api.eliminarServicioUsuario(idUsuarioLogueado, idServicio).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                serviciosEnEliminacion.remove(idServicio);
                if (!isAdded()) {
                    return;
                }

                int code = response.code();
                Log.d(TAG_CRUD, "Borrar servicio response code=" + code
                        + " successful=" + response.isSuccessful()
                        + " idServicio=" + idServicio);
                if (response.isSuccessful()) {
                    eliminarServicioDeCache(idServicio);
                    if (adapter != null && position >= 0 && position < adapter.getItemCount()) {
                        adapter.removeItemAt(position);
                        actualizarEstadoVacio(adapter.getItemCount() == 0);
                    }
                    mostrarFeedbackExito("Servicio eliminado", null);
                    cargarServiciosDelUsuario(true, idServicio);
                    return;
                }
                if (code == 403) {
                    mostrarFeedbackError("No se pudo eliminar el servicio");
                    Toast.makeText(requireContext(), "No puedes eliminar este servicio", Toast.LENGTH_LONG).show();
                    return;
                }
                if (code == 404) {
                    mostrarFeedbackError("No se pudo eliminar el servicio");
                    Toast.makeText(requireContext(), "El servicio ya no existe", Toast.LENGTH_LONG).show();
                    return;
                }
                if (code == 409) {
                    mostrarFeedbackError("No se pudo eliminar el servicio");
                    String backendMessage = ApiErrorParser.extractMessage(response);
                    Toast.makeText(requireContext(),
                            backendMessage != null ? backendMessage : "No se puede eliminar este servicio",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String backendMessage = ApiErrorParser.extractMessage(response);
                Log.w(TAG_CRUD, "Borrar servicio error code=" + code + " idServicio=" + idServicio + " error=" + backendMessage);
                mostrarFeedbackError("No se pudo eliminar el servicio");
                Toast.makeText(requireContext(),
                        backendMessage != null ? backendMessage : "No se pudo eliminar el servicio (" + code + ")",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                serviciosEnEliminacion.remove(idServicio);
                Log.e(TAG_CRUD, "Borrar servicio failure idServicio=" + idServicio, t);
                if (isAdded()) {
                    mostrarFeedbackError("No se pudo eliminar el servicio");
                    Toast.makeText(requireContext(), "Error de conexión al eliminar el servicio", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cargarServiciosDelUsuario() {
        cargarServiciosDelUsuario(false);
    }

    private void cargarServiciosDelUsuario(boolean permitirVaciarLista) {
        cargarServiciosDelUsuario(permitirVaciarLista, null);
    }

    private void cargarServiciosDelUsuario(boolean permitirVaciarLista, @Nullable Integer idEliminadoEsperado) {
        final int tokenLocal = ++requestToken;
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (adapter != null) {
            adapter.setCurrentUserId(idUsuarioLogueado);
        }

        if (idUsuarioLogueado == -1) {
            debeRecargarEnResume = true;
            isLoading = false;
            actualizarEstadoVacio(true);
            return;
        }
        isLoading = true;
        actualizarEstadoVacio(true);

        int sizeAntes = adapter != null ? adapter.getItemCount() : -1;
        Log.d(TAG_REFRESH, "Cargar servicios start token=" + tokenLocal
                + " usuarioId=" + idUsuarioLogueado
                + " permitirVaciar=" + permitirVaciarLista
                + " esperadoEliminado=" + idEliminadoEsperado
                + " sizeAntes=" + sizeAntes
                + " obrasNoSeTocan=true");
        restaurarServiciosCacheSiExiste();

        ServicioApi api = RetrofitClient.getClient().create(ServicioApi.class);
        Log.d(TAG_REFRESH, "GET portafolioPersonal/{usuarioId} usuarioId=" + idUsuarioLogueado);
        Call<List<ServicioDTO>> call = api.obtenerServiciosDeUsuario(idUsuarioLogueado);
        call.enqueue(new Callback<List<ServicioDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ServicioDTO>> call, @NonNull Response<List<ServicioDTO>> response) {
                if (!isAdded()) {
                    if (tokenLocal == requestToken) {
                        isLoading = false;
                    }
                    return;
                }
                if (tokenLocal != requestToken) {
                    if (tokenLocal != requestToken) {
                        Log.d(TAG_REFRESH, "Ignorada respuesta vieja servicios token=" + tokenLocal
                                + " actual=" + requestToken
                                + " sizeActual=" + (adapter != null ? adapter.getItemCount() : -1));
                    }
                    return;
                }
                Log.d(TAG_REFRESH, "Cargar servicios response code=" + response.code()
                        + " successful=" + response.isSuccessful()
                        + " bodySize=" + (response.body() != null ? response.body().size() : -1));
                if (response.code() == 204) {
                    isLoading = false;
                    if (!permitirVaciarLista && restaurarServiciosCacheSiExiste()) {
                        Log.w(TAG_REFRESH, "Respuesta servicios 204 protegida con cache usuarioId=" + idUsuarioLogueado);
                        debeRecargarEnResume = false;
                        return;
                    }
                    adapter.actualizarLista(new ArrayList<>());
                    serviciosCachePorUsuario.remove(idUsuarioLogueado);
                    actualizarEstadoVacio(true);
                    if (idEliminadoEsperado != null) {
                        Log.d(TAG_CRUD, "Verificacion borrado servicio id=" + idEliminadoEsperado
                                + " siguePresente=false sizeDespues=0 code=204");
                    }
                    debeRecargarEnResume = false;
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    isLoading = false;
                    actualizarEstadoVacio(true);
                    return;
                }

                List<ServicioDTO> dtos = response.body();
                if (dtos.isEmpty()) {
                    isLoading = false;
                    if (!permitirVaciarLista && restaurarServiciosCacheSiExiste()) {
                        Log.w(TAG_REFRESH, "Respuesta servicios vacia protegida con cache usuarioId=" + idUsuarioLogueado);
                        debeRecargarEnResume = false;
                        return;
                    }
                    adapter.actualizarLista(new ArrayList<>());
                    serviciosCachePorUsuario.remove(idUsuarioLogueado);
                    actualizarEstadoVacio(true);
                    if (idEliminadoEsperado != null) {
                        Log.d(TAG_CRUD, "Verificacion borrado servicio id=" + idEliminadoEsperado
                                + " siguePresente=false sizeDespues=0");
                    }
                    debeRecargarEnResume = false;
                    return;
                }
                cargarFavoritosServiciosDeUsuario(serviciosFavoritos -> {
                    if (!isAdded() || tokenLocal != requestToken) {
                        return;
                    }
                    isLoading = false;
                    List<TarjetaTextoServicioItem> items = convertirDTOaItem(dtos, serviciosFavoritos);
                    guardarServiciosCache(items);
                    aplicarServiciosEnUi(items);
                    FragPortafolio.limpiarRefreshPendiente(FragPortafolio.TARGET_SERVICIOS);
                    if (idEliminadoEsperado != null) {
                        boolean siguePresente = contieneServicio(items, idEliminadoEsperado);
                        Log.d(TAG_CRUD, "Verificacion borrado servicio id=" + idEliminadoEsperado
                                + " siguePresente=" + siguePresente
                                + " sizeDespues=" + items.size());
                    }
                    debeRecargarEnResume = false;
                    refreshLikeCounts(items);
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<ServicioDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    if (tokenLocal == requestToken) {
                        isLoading = false;
                    }
                    return;
                }
                if (tokenLocal == requestToken) {
                    isLoading = false;
                    debeRecargarEnResume = true;
                    actualizarEstadoVacio(true);
                    Log.e(TAG_REFRESH, "Cargar servicios failure token=" + tokenLocal + " usuarioId=" + idUsuarioLogueado, t);
                    Toast.makeText(requireContext(), "Error de red/API: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sincronizarUsuarioActual() {
        if (!isAdded()) {
            return;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (adapter != null) {
            adapter.setCurrentUserId(idUsuarioLogueado);
        }
    }

    public static synchronized void invalidarCacheUsuario(int idUsuario) {
        serviciosCachePorUsuario.remove(idUsuario);
    }

    private boolean restaurarServiciosCacheSiExiste() {
        List<TarjetaTextoServicioItem> cachedItems = serviciosCachePorUsuario.get(idUsuarioLogueado);
        if (cachedItems == null || cachedItems.isEmpty()) {
            return false;
        }
        Log.d(TAG_REFRESH, "Restaurando cache servicios usuarioId=" + idUsuarioLogueado + " size=" + cachedItems.size());
        aplicarServiciosEnUi(cachedItems);
        return true;
    }

    private void guardarServiciosCache(List<TarjetaTextoServicioItem> items) {
        serviciosCachePorUsuario.put(idUsuarioLogueado, ordenarServiciosMasRecientesPrimero(items));
    }

    private void aplicarServiciosEnUi(List<TarjetaTextoServicioItem> items) {
        List<TarjetaTextoServicioItem> ordenados = ordenarServiciosMasRecientesPrimero(items);
        adapter.actualizarLista(ordenados);
        actualizarEstadoVacio(ordenados.isEmpty());
        Log.d(TAG_REFRESH, "Aplicar servicios UI size=" + ordenados.size()
                + " emptyVisible=" + ordenados.isEmpty()
                + " obrasNoSeTocan=true");
    }

    private boolean contieneServicio(List<TarjetaTextoServicioItem> items, int idServicio) {
        if (items == null) return false;
        for (TarjetaTextoServicioItem item : items) {
            if (item != null && item.getIdServicio() != null && item.getIdServicio() == idServicio) {
                return true;
            }
        }
        return false;
    }

    private void eliminarServicioDeCache(int idServicio) {
        List<TarjetaTextoServicioItem> cachedItems = serviciosCachePorUsuario.get(idUsuarioLogueado);
        if (cachedItems != null) {
            cachedItems.removeIf(item -> item != null && item.getIdServicio() != null && item.getIdServicio() == idServicio);
        }
    }

    private List<TarjetaTextoServicioItem> ordenarServiciosMasRecientesPrimero(@Nullable List<TarjetaTextoServicioItem> source) {
        List<TarjetaTextoServicioItem> ordenados = source != null ? new ArrayList<>(source) : new ArrayList<>();
        ordenados.sort((a, b) -> Integer.compare(
                b != null && b.getIdServicio() != null ? b.getIdServicio() : -1,
                a != null && a.getIdServicio() != null ? a.getIdServicio() : -1
        ));
        return ordenados;
    }

    private int safeServicioId(@Nullable ServicioDTO dto) {
        return dto != null && dto.getIdServicio() != null ? dto.getIdServicio() : -1;
    }

    private interface FavoritosServiciosCallback {
        void onResult(Set<Integer> serviciosFavoritos);
    }

    private void cargarFavoritosServiciosDeUsuario(FavoritosServiciosCallback callback) {
        favoritosApi.obtenerFavoritosUsuario(idUsuarioLogueado).enqueue(new Callback<List<FavoritoDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<FavoritoDTO>> call, @NonNull Response<List<FavoritoDTO>> response) {
                Set<Integer> serviciosFavoritos = new HashSet<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (FavoritoDTO favorito : response.body()) {
                        if (favorito.idServicio != null) {
                            serviciosFavoritos.add(favorito.idServicio);
                        }
                    }
                }
                callback.onResult(serviciosFavoritos);
            }
            @Override
            public void onFailure(@NonNull Call<List<FavoritoDTO>> call, @NonNull Throwable t) {
                callback.onResult(new HashSet<>());
                }
        });
    }

    private void mostrarFeedbackCarga(String mensaje) {
        if (feedbackDialog != null) {
            feedbackDialog.showLoading(mensaje);
        }
    }

    private void mostrarFeedbackExito(String mensaje, @Nullable Runnable onDismiss) {
        if (feedbackDialog != null) {
            feedbackDialog.showSuccess(mensaje, onDismiss);
        } else if (onDismiss != null) {
            onDismiss.run();
        }
    }

    private void mostrarFeedbackError(String mensaje) {
        if (feedbackDialog != null) {
            feedbackDialog.showError(mensaje);
        }
    }

    private void actualizarEstadoVacio(boolean mostrar) {
        if (tvEmptyMisServicios == null || recyclerMisServicios == null) {
            return;
        }
        boolean tieneDatos = adapter != null && adapter.getItemCount() > 0;
        boolean mostrarLoaderCentral = isLoading && !tieneDatos;
        boolean mostrarEmpty = !mostrarLoaderCentral && !tieneDatos && mostrar;

        if (progressMisServicios != null) {
            progressMisServicios.setVisibility(mostrarLoaderCentral ? View.VISIBLE : View.GONE);
        }
        recyclerMisServicios.setVisibility(tieneDatos ? View.VISIBLE : View.GONE);
        tvEmptyMisServicios.setVisibility(mostrarEmpty ? View.VISIBLE : View.GONE);
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private interface PasswordValidationCallback {
        void onValid();
        void onInvalid(String mensajeError);
        void onError(String mensajeError);
    }
}

