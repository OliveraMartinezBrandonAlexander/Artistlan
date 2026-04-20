package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragGestionUsuarios extends Fragment {

    private static final String[] ROLES = {"USER", "ADMIN", "MODERADOR"};

    private UsuarioApi usuarioApi;
    private UsuarioAdminAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEstado;
    private SearchView searchUsuarios;
    private View menuInferior;

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

        menuInferior = requireActivity().findViewById(R.id.MenuInferior);
        if (menuInferior != null) menuInferior.setVisibility(View.GONE);

        btnRegresar.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            boolean regreso = navController.popBackStack();
            if (!regreso) {
                try {
                    navController.navigate(R.id.fragMain);
                } catch (Exception ignored) {
                    // evitar cierre abrupto de activity por fallback de back global
                }
            }
        });

        adapter = new UsuarioAdminAdapter(this::mostrarDialogoRoles);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        configurarBuscadorUsuarios();

        cargarUsuarios();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (menuInferior != null) menuInferior.setVisibility(View.VISIBLE);
    }

    private void cargarUsuarios() {
        mostrarLoading(true);
        usuarioApi.getUsuarios().enqueue(new Callback<List<UsuariosDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<UsuariosDTO>> call, @NonNull Response<List<UsuariosDTO>> response) {
                mostrarLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    mostrarError("No se pudieron cargar los usuarios.");
                    return;
                }

                List<UsuariosDTO> lista = response.body();
                adapter.actualizar(lista);
                if (searchUsuarios != null) {
                    String query = searchUsuarios.getQuery() != null ? searchUsuarios.getQuery().toString() : "";
                    adapter.filtrarPorUsuario(query);
                    actualizarEstadoPorFiltro(query);
                }
                boolean vacio = lista.isEmpty();
                tvEstado.setVisibility(vacio ? View.VISIBLE : View.GONE);
                tvEstado.setText(vacio ? "No hay usuarios para gestionar." : "");
            }

            @Override
            public void onFailure(@NonNull Call<List<UsuariosDTO>> call, @NonNull Throwable t) {
                mostrarLoading(false);
                mostrarError("Error de conexión al listar usuarios.");
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
                adapter.filtrarPorUsuario(query);
                actualizarEstadoPorFiltro(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filtrarPorUsuario(newText);
                actualizarEstadoPorFiltro(newText);
                return true;
            }
        });
    }

    private void actualizarEstadoPorFiltro(String query) {
        if (adapter == null || tvEstado == null) {
            return;
        }
        if (adapter.getItemCount() > 0) {
            tvEstado.setVisibility(View.GONE);
            return;
        }

        if (query != null && !query.trim().isEmpty()) {
            tvEstado.setText("No se encontraron usuarios para \"" + query.trim() + "\".");
            tvEstado.setVisibility(View.VISIBLE);
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
                .setMessage("¿Cambiar rol de este usuario?")
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

        mostrarLoading(true);
        usuarioApi.cambiarRol(idUsuario, adminId, new CambiarRolRequestDTO(rolNuevo))
                .enqueue(new Callback<UsuariosDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<UsuariosDTO> call, @NonNull Response<UsuariosDTO> response) {
                        mostrarLoading(false);
                        if (response.isSuccessful()) {
                            actualizarSesionSiCorresponde(usuario, rolNuevo);
                            mostrarSnackbar("Rol actualizado a " + rolNuevo + ".");
                            cargarUsuarios();
                        } else if (response.code() == 403) {
                            mostrarError("No tienes permisos para cambiar roles.");
                        } else {
                            mostrarError("No se pudo actualizar el rol.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UsuariosDTO> call, @NonNull Throwable t) {
                        mostrarLoading(false);
                        mostrarError("Error de conexión al actualizar rol.");
                    }
                });
    }

    private int obtenerAdminId() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("id", -1);
    }

    private void actualizarSesionSiCorresponde(UsuariosDTO usuarioEditado, String rolNuevo) {
        if (usuarioEditado == null || usuarioEditado.getIdUsuario() == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        int idSesion = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        if (idSesion <= 0 || idSesion != usuarioEditado.getIdUsuario()) return;

        prefs.edit()
                .putString("rol", rolNuevo)
                .putString("modo", rolNuevo)
                .apply();

        if (requireActivity() instanceof ActFragmentoPrincipal) {
            ((ActFragmentoPrincipal) requireActivity()).refrescarUIRolActual();
        }
    }


    private void mostrarLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        tvEstado.setText(mensaje);
        tvEstado.setVisibility(View.VISIBLE);
        mostrarSnackbar(mensaje);
    }

    private void mostrarSnackbar(String mensaje) {
        View view = getView();
        if (view != null) Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
    }
}
