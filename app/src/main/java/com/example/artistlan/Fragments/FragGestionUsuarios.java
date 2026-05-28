package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.Admin.adapter.UsuarioAdminAdapter;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.CambiarRolRequestDTO;
import com.example.artistlan.Conector.model.PageResponseUsuariosDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.utils.CardThemeHelper;
import com.example.artistlan.utils.DialogThemeHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragGestionUsuarios extends Fragment {

    private static final String[] ROLES = {"USER", "ADMIN", "MODERADOR"};
    private static final int PAGE_SIZE = 10;
    private static final String SORT_DEFAULT = "idUsuario,desc";
    private static final long SEARCH_DEBOUNCE_MS = 400L;
    private static final long PROFILE_CLICK_THROTTLE_MS = 700L;

    private UsuarioApi usuarioApi;
    private UsuarioAdminAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private SearchView searchUsuarios;
    private View menuInferior;
    private Button btnCargarMasUsuarios;
    private LinearLayout layoutLoaderMasUsuarios;
    private ImageButton btnRegresar;
    private TextView tvTitulo;
    private TextView txtCargandoMas;
    private ProgressBar progressMasUsuarios;
    private ThemeManager themeManager;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private final List<UsuariosDTO> usuariosAcumulados = new ArrayList<>();

    private String textoBusquedaActual = "";
    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int requestToken = 0;
    private long ultimoClickPerfilMs = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_admin_gestion_usuarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);
        themeManager = new ThemeManager(requireContext());

        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        btnRegresar = view.findViewById(R.id.btnRegresarAdminUsuarios);
        recyclerView = view.findViewById(R.id.rvUsuariosAdmin);
        progressBar = view.findViewById(R.id.pbUsuarios);
        tvEstado = view.findViewById(R.id.tvEstadoUsuarios);
        searchUsuarios = view.findViewById(R.id.searchUsuariosAdmin);
        btnCargarMasUsuarios = view.findViewById(R.id.btnCargarMasUsuarios);
        layoutLoaderMasUsuarios = view.findViewById(R.id.layoutLoaderMasUsuarios);
        tvTitulo = view.findViewById(R.id.tvTituloGestionUsuarios);
        txtCargandoMas = view.findViewById(R.id.txtCargandoMasUsuarios);
        progressMasUsuarios = view.findViewById(R.id.progressMasUsuarios);

        menuInferior = requireActivity().findViewById(R.id.MenuInferiorFrame);
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }

        btnRegresar.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            boolean regreso = navController.popBackStack();
            if (!regreso) {
                try {
                    navController.navigate(R.id.fragMain);
                } catch (Exception ignored) {
                    // Evita cierre abrupto por fallback de back global.
                }
            }
        });

        adapter = new UsuarioAdminAdapter(this::mostrarDialogoRoles, this::abrirPerfilUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        if (btnCargarMasUsuarios != null) {
            btnCargarMasUsuarios.setOnClickListener(v -> {
                if (isLoading || isLastPage) {
                    return;
                }
                cargarPagina(nextPageToLoad);
            });
        }

        configurarBuscadorUsuarios();
        aplicarTemaVisual();
        reiniciarYCargarPrimeraPagina();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.VISIBLE);
        }
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menuInferior != null) {
            menuInferior.setVisibility(View.GONE);
        }
    }

    private void reiniciarYCargarPrimeraPagina() {
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }

        requestToken++;
        nextPageToLoad = 0;
        isLastPage = false;
        isLoading = false;

        usuariosAcumulados.clear();
        adapter.actualizar(new ArrayList<>());
        mostrarEstadoMensaje(null);
        mostrarBotonCargarMas(false, false);
        mostrarLoaderMasUsuarios(false);

        cargarPagina(0);
    }

    private void cargarPagina(int pageObjetivo) {
        if (isLoading || (isLastPage && pageObjetivo > 0)) {
            return;
        }

        isLoading = true;
        if (pageObjetivo == 0) {
            mostrarLoadingInicial(true);
            mostrarLoaderMasUsuarios(false);
            mostrarBotonCargarMas(false, false);
        } else {
            mostrarLoadingInicial(false);
            mostrarLoaderMasUsuarios(true);
            mostrarBotonCargarMas(false, false);
        }

        final int tokenLocal = ++requestToken;
        String queryParam = textoBusquedaActual.isEmpty() ? null : textoBusquedaActual;

        usuarioApi.getUsuariosPaginados(
                queryParam,
                null,
                null,
                null,
                pageObjetivo,
                PAGE_SIZE,
                SORT_DEFAULT
        ).enqueue(new Callback<PageResponseUsuariosDTO>() {
            @Override
            public void onResponse(@NonNull Call<PageResponseUsuariosDTO> call, @NonNull Response<PageResponseUsuariosDTO> response) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                mostrarLoadingInicial(false);
                mostrarLoaderMasUsuarios(false);

                if (!response.isSuccessful() || response.body() == null) {
                    if (pageObjetivo > 0) {
                        mostrarBotonCargarMas(true, true);
                    }
                    mostrarError("No se pudieron cargar los usuarios.");
                    return;
                }

                PageResponseUsuariosDTO pageResponse = response.body();
                List<UsuariosDTO> nuevos = pageResponse.getContent();

                if (pageObjetivo == 0) {
                    usuariosAcumulados.clear();
                }
                usuariosAcumulados.addAll(nuevos);

                if (pageObjetivo == 0) {
                    adapter.actualizar(new ArrayList<>(usuariosAcumulados));
                } else {
                    adapter.agregarItems(nuevos);
                }

                nextPageToLoad = pageObjetivo + 1;
                isLastPage = pageResponse.isLast();

                if (usuariosAcumulados.isEmpty()) {
                    String mensaje = textoBusquedaActual.isEmpty()
                            ? "No hay usuarios para gestionar."
                            : "No se encontraron usuarios para \"" + textoBusquedaActual + "\".";
                    mostrarEstadoMensaje(mensaje);
                    mostrarBotonCargarMas(false, false);
                } else {
                    mostrarEstadoMensaje(null);
                    mostrarBotonCargarMas(!isLastPage, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponseUsuariosDTO> call, @NonNull Throwable t) {
                if (!isAdded() || tokenLocal != requestToken) {
                    return;
                }

                isLoading = false;
                mostrarLoadingInicial(false);
                mostrarLoaderMasUsuarios(false);
                if (pageObjetivo > 0) {
                    mostrarBotonCargarMas(true, true);
                } else {
                    mostrarBotonCargarMas(false, false);
                }
                mostrarError("Error de conexi\u00F3n al listar usuarios.");
            }
        });
    }

    private void configurarBuscadorUsuarios() {
        if (searchUsuarios == null) {
            return;
        }

        searchUsuarios.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                actualizarBusqueda(query, false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                actualizarBusqueda(newText, true);
                return true;
            }
        });
    }

    private void aplicarTemaVisual() {
        if (themeManager == null) {
            return;
        }
        CardThemeHelper.applyFilterButton(btnRegresar, themeManager);
        CardThemeHelper.applyPrimaryBubbleButton(btnCargarMasUsuarios, themeManager);
        CardThemeHelper.tintProgress(progressBar, themeManager);
        CardThemeHelper.tintProgress(progressMasUsuarios, themeManager);
        ThemeApplier.applyTextPrimary(tvTitulo, themeManager);
        ThemeApplier.applyTextSecondary(tvEstado, themeManager);
        ThemeApplier.applyTextSecondary(txtCargandoMas, themeManager);
        if (searchUsuarios != null) {
            CardThemeHelper.applyFilterSurface(searchUsuarios, themeManager);
            TextView searchText = searchUsuarios.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchText != null) {
                searchText.setTextColor(themeManager.color(ThemeKeys.TEXT_PRIMARY));
                searchText.setHintTextColor(themeManager.color(ThemeKeys.INPUT_HINT));
            }
        }
    }

    private void abrirPerfilUsuario(@Nullable UsuariosDTO usuario) {
        if (!isAdded() || usuario == null || usuario.getIdUsuario() == null || usuario.getIdUsuario() <= 0) {
            return;
        }
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoClickPerfilMs < PROFILE_CLICK_THROTTLE_MS) {
            return;
        }
        ultimoClickPerfilMs = ahora;

        int idUsuarioSeleccionado = usuario.getIdUsuario();
        NavController navController = NavHostFragment.findNavController(this);
        if (idUsuarioSeleccionado == obtenerUsuarioSesionId()) {
            navController.navigate(R.id.fragVerPerfil);
            return;
        }
        Bundle args = new Bundle();
        args.putInt("idArtista", idUsuarioSeleccionado);
        navController.navigate(R.id.fragVerPerfilPublico, args);
    }

    private int obtenerUsuarioSesionId() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("idUsuario", prefs.getInt("id", -1));
    }

    private void actualizarBusqueda(String texto, boolean conDebounce) {
        String nuevoTexto = texto != null ? texto.trim() : "";
        if (nuevoTexto.equals(textoBusquedaActual)) {
            return;
        }

        textoBusquedaActual = nuevoTexto;
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }

        pendingSearchRunnable = this::reiniciarYCargarPrimeraPagina;
        if (conDebounce) {
            searchHandler.postDelayed(pendingSearchRunnable, SEARCH_DEBOUNCE_MS);
        } else {
            searchHandler.post(pendingSearchRunnable);
        }
    }

    private void mostrarDialogoRoles(UsuariosDTO usuario) {
        ThemeManager tm = new ThemeManager(requireContext());
        LinearLayout dialogContent = new LinearLayout(requireContext());
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setPadding(dpToPx(18), dpToPx(18), dpToPx(18), dpToPx(16));
        dialogContent.setBackground(DialogThemeHelper.createDialogBackground(requireContext()));

        TextView title = new TextView(requireContext());
        title.setText("Cambiar rol");
        title.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        title.setTextSize(24f);
        title.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        dialogContent.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(12);
        content.setPadding(padding, padding, padding, padding);
        content.setBackground(DialogThemeHelper.createLightGlassFieldBackground(requireContext()));

        TextView label = new TextView(requireContext());
        label.setText("Selecciona el nuevo rol");
        label.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        label.setTextSize(12f);
        label.setAllCaps(true);
        label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
        content.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Spinner spinnerRol = new Spinner(requireContext());
        spinnerRol.setAdapter(DialogThemeHelper.createLightGlassComboAdapter(requireContext(), Arrays.asList(ROLES)));
        DialogThemeHelper.applyLightGlassComboStyle(spinnerRol, requireContext());
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.topMargin = dpToPx(10);
        content.addView(spinnerRol, spinnerParams);

        String rolActual = usuario.getRol() == null ? "USER" : usuario.getRol();
        for (int i = 0; i < ROLES.length; i++) {
            if (ROLES[i].equalsIgnoreCase(rolActual)) {
                spinnerRol.setSelection(i);
                break;
            }
        }

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        contentParams.topMargin = dpToPx(14);
        dialogContent.addView(content, contentParams);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionsParams.topMargin = dpToPx(16);
        Button btnCancelar = new Button(requireContext());
        btnCancelar.setText("Cancelar");
        btnCancelar.setAllCaps(false);
        Button btnContinuar = new Button(requireContext());
        btnContinuar.setText("Continuar");
        btnContinuar.setAllCaps(false);
        CardThemeHelper.applySecondaryBubbleButton(btnCancelar, tm);
        CardThemeHelper.applyPrimaryBubbleButton(btnContinuar, tm);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dpToPx(46), 1f);
        LinearLayout.LayoutParams continueParams = new LinearLayout.LayoutParams(0, dpToPx(46), 1f);
        continueParams.leftMargin = dpToPx(10);
        actions.addView(btnCancelar, cancelParams);
        actions.addView(btnContinuar, continueParams);
        dialogContent.addView(actions, actionsParams);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogContent)
                .create();
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnContinuar.setOnClickListener(v -> {
            Object seleccionado = spinnerRol.getSelectedItem();
            dialog.dismiss();
            confirmarCambioRol(usuario, seleccionado == null ? "USER" : seleccionado.toString());
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        DialogThemeHelper.applyFieldDialogWindowSize(dialog, requireContext());
    }

    private void confirmarCambioRol(UsuariosDTO usuario, String rolNuevo) {
        ThemeManager tm = new ThemeManager(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(18), dpToPx(18), dpToPx(18), dpToPx(16));
        content.setBackground(DialogThemeHelper.createDialogBackground(requireContext()));

        TextView title = new TextView(requireContext());
        title.setText("Confirmar cambio");
        title.setTextColor(tm.color(ThemeKeys.TEXT_PRIMARY));
        title.setTextSize(24f);
        title.setTypeface(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD));
        content.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView message = new TextView(requireContext());
        message.setText("¿Cambiar rol de este usuario?");
        message.setTextColor(tm.color(ThemeKeys.TEXT_SECONDARY));
        message.setTextSize(14f);
        message.setLineSpacing(dpToPx(3), 1f);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageParams.topMargin = dpToPx(12);
        content.addView(message, messageParams);

        LinearLayout actions = new LinearLayout(requireContext());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actionsParams.topMargin = dpToPx(18);
        Button btnCancelar = new Button(requireContext());
        btnCancelar.setText("Cancelar");
        btnCancelar.setAllCaps(false);
        Button btnConfirmar = new Button(requireContext());
        btnConfirmar.setText("Confirmar");
        btnConfirmar.setAllCaps(false);
        CardThemeHelper.applySecondaryBubbleButton(btnCancelar, tm);
        CardThemeHelper.applyPrimaryBubbleButton(btnConfirmar, tm);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dpToPx(46), 1f);
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(0, dpToPx(46), 1f);
        confirmParams.leftMargin = dpToPx(10);
        actions.addView(btnCancelar, cancelParams);
        actions.addView(btnConfirmar, confirmParams);
        content.addView(actions, actionsParams);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(content)
                .create();
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();
            cambiarRol(usuario, rolNuevo);
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        DialogThemeHelper.applyDialogWindowSize(dialog, requireContext());
    }

    private void cambiarRol(UsuariosDTO usuario, String rolNuevo) {
        Integer idUsuario = usuario.getIdUsuario();
        int adminId = obtenerAdminId();
        if (idUsuario == null || adminId <= 0) {
            mostrarError("No se pudo validar el administrador actual.");
            return;
        }

        mostrarLoadingInicial(true);
        usuarioApi.cambiarRol(idUsuario, adminId, new CambiarRolRequestDTO(rolNuevo))
                .enqueue(new Callback<UsuariosDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UsuariosDTO> call, @NonNull Response<UsuariosDTO> response) {
                        mostrarLoadingInicial(false);
                        if (response.isSuccessful()) {
                            actualizarSesionSiCorresponde(usuario, rolNuevo);
                            adapter.actualizarRolUsuario(idUsuario, rolNuevo);
                            mostrarSnackbar("Rol actualizado a " + rolNuevo + ".");
                        } else if (response.code() == 403) {
                            mostrarError("No tienes permisos para cambiar roles.");
                        } else {
                            mostrarError("No se pudo actualizar el rol.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UsuariosDTO> call, @NonNull Throwable t) {
                        mostrarLoadingInicial(false);
                        mostrarError("Error de conexi\u00F3n al actualizar rol.");
                    }
                });
    }

    private int obtenerAdminId() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("id", -1);
    }

    private void actualizarSesionSiCorresponde(UsuariosDTO usuarioEditado, String rolNuevo) {
        if (usuarioEditado == null || usuarioEditado.getIdUsuario() == null) {
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        int idSesion = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (idSesion <= 0 || idSesion != usuarioEditado.getIdUsuario()) {
            return;
        }

        prefs.edit()
                .putString("rol", rolNuevo)
                .putString("modo", rolNuevo)
                .apply();

        if (requireActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) requireActivity()).refrescarUIRolActual();
        }
    }

    private void mostrarLoadingInicial(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
        }
    }

    private void mostrarLoaderMasUsuarios(boolean mostrar) {
        if (layoutLoaderMasUsuarios != null) {
            layoutLoaderMasUsuarios.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    private void mostrarBotonCargarMas(boolean mostrar, boolean reintento) {
        if (btnCargarMasUsuarios == null) {
            return;
        }
        btnCargarMasUsuarios.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (mostrar) {
            btnCargarMasUsuarios.setText(reintento ? "Reintentar cargar m\u00E1s usuarios" : "Cargar m\u00E1s usuarios");
        }
    }

    private void mostrarEstadoMensaje(String mensaje) {
        if (tvEstado == null) {
            return;
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            tvEstado.setText("");
            tvEstado.setVisibility(View.GONE);
            return;
        }
        tvEstado.setText(mensaje);
        tvEstado.setVisibility(View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        mostrarEstadoMensaje(mensaje);
        mostrarSnackbar(mensaje);
    }

    private void mostrarSnackbar(String mensaje) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
}
