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
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragGestionUsuarios extends Fragment {

    private static final String[] ROLES = {"USER", "ADMIN", "MODERADOR"};
    private static final int PAGE_SIZE = 10;
    private static final String SORT_DEFAULT = "idUsuario,desc";
    private static final long SEARCH_DEBOUNCE_MS = 400L;

    private UsuarioApi usuarioApi;
    private UsuarioAdminAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private SearchView searchUsuarios;
    private View menuInferior;
    private Button btnCargarMasUsuarios;
    private LinearLayout layoutLoaderMasUsuarios;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;
    private final List<UsuariosDTO> usuariosAcumulados = new ArrayList<>();

    private String textoBusquedaActual = "";
    private int nextPageToLoad = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int requestToken = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_frag_admin_gestion_usuarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        usuarioApi = RetrofitClient.getClient().create(UsuarioApi.class);

        ImageButton btnRegresar = view.findViewById(R.id.btnRegresarAdminUsuarios);
        recyclerView = view.findViewById(R.id.rvUsuariosAdmin);
        progressBar = view.findViewById(R.id.pbUsuarios);
        tvEstado = view.findViewById(R.id.tvEstadoUsuarios);
        searchUsuarios = view.findViewById(R.id.searchUsuariosAdmin);
        btnCargarMasUsuarios = view.findViewById(R.id.btnCargarMasUsuarios);
        layoutLoaderMasUsuarios = view.findViewById(R.id.layoutLoaderMasUsuarios);

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

        adapter = new UsuarioAdminAdapter(this::mostrarDialogoRoles);
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
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar rol")
                .setItems(ROLES, (dialog, which) -> confirmarCambioRol(usuario, ROLES[which]))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarCambioRol(UsuariosDTO usuario, String rolNuevo) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar cambio")
                .setMessage("\u00BFCambiar rol de este usuario?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> cambiarRol(usuario, rolNuevo))
                .show();
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
}
