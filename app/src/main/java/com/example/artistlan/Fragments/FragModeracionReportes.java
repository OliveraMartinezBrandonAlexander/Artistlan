package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.ApiErrorParser;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.ModeracionApi;
import com.example.artistlan.Conector.model.ReporteResumenDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.adapter.ReportesModeracionAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragModeracionReportes extends Fragment {

    private RecyclerView recyclerReportes;
    private Spinner spinnerEstado;
    private Spinner spinnerTipo;
    private Spinner spinnerPrioridad;
    private CheckBox checkSoloMios;
    private ProgressBar progressBar;
    private TextView tvVacio;
    private View contenedorFiltros;

    private ModeracionApi moderacionApi;
    private ReportesModeracionAdapter adapter;

    private int idUsuarioActual = -1;
    private String rolActual = "USER";
    private boolean filtrosListos = false;

    public FragModeracionReportes() {
        super(R.layout.fragment_frag_moderacion_reportes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ThemeModuleStyler.styleFragment(this, view);

        moderacionApi = RetrofitClient.getClient().create(ModeracionApi.class);
        cargarSesionActual();
        bindViews(view);
        configurarRecycler();
        configurarFiltros();
        validarPermisosYCargar();
    }

    private void cargarSesionActual() {
        SessionManager sessionManager = new SessionManager(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        idUsuarioActual = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        rolActual = prefs.getString("rol", "USER");
        if (rolActual == null || rolActual.trim().isEmpty()) {
            rolActual = "USER";
        }
        if (!sessionManager.isLoggedIn()) {
            idUsuarioActual = -1;
        }
    }

    private void bindViews(@NonNull View view) {
        recyclerReportes = view.findViewById(R.id.recyclerModeracionReportes);
        spinnerEstado = view.findViewById(R.id.spinnerEstadoModeracion);
        spinnerTipo = view.findViewById(R.id.spinnerTipoModeracion);
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridadModeracion);
        checkSoloMios = view.findViewById(R.id.checkSoloMiosModeracion);
        progressBar = view.findViewById(R.id.progressModeracionReportes);
        tvVacio = view.findViewById(R.id.tvModeracionVacio);
        contenedorFiltros = view.findViewById(R.id.contenedorFiltrosModeracion);
    }

    private void configurarRecycler() {
        recyclerReportes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReportesModeracionAdapter(new ArrayList<>(), reporte -> {
            if (reporte == null || reporte.getIdReporte() == null || reporte.getIdReporte() <= 0) {
                Toast.makeText(requireContext(), "No se encontr\u00f3 el reporte seleccionado.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle args = new Bundle();
            args.putInt("idReporte", reporte.getIdReporte());
            NavHostFragment.findNavController(this).navigate(R.id.fragDetalleReporteModeracion, args);
        });
        recyclerReportes.setAdapter(adapter);
    }

    private void configurarFiltros() {
        configurarSpinner(spinnerEstado, new String[]{"TODOS", "PENDIENTE", "EN_REVISION", "RESUELTO", "DESCARTADO"});
        configurarSpinner(spinnerTipo, new String[]{"TODOS", "OBRA", "SERVICIO", "USUARIO"});
        configurarSpinner(spinnerPrioridad, new String[]{"TODAS", "BAJA", "MEDIA", "ALTA"});

        AdapterView.OnItemSelectedListener recargaListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (filtrosListos) {
                    cargarReportes();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinnerEstado.setOnItemSelectedListener(recargaListener);
        spinnerTipo.setOnItemSelectedListener(recargaListener);
        spinnerPrioridad.setOnItemSelectedListener(recargaListener);
        checkSoloMios.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (filtrosListos) {
                cargarReportes();
            }
        });

        filtrosListos = true;
    }

    private void configurarSpinner(@NonNull Spinner spinner, @NonNull String[] valores) {
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                valores
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpinner);
    }

    private void validarPermisosYCargar() {
        if (!tienePermisoModeracion()) {
            mostrarSinPermisos();
            return;
        }
        cargarReportes();
    }

    private boolean tienePermisoModeracion() {
        return idUsuarioActual > 0 && ("ADMIN".equalsIgnoreCase(rolActual) || "MODERADOR".equalsIgnoreCase(rolActual));
    }

    private void mostrarSinPermisos() {
        if (contenedorFiltros != null) {
            contenedorFiltros.setVisibility(View.GONE);
        }
        if (recyclerReportes != null) {
            recyclerReportes.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (tvVacio != null) {
            tvVacio.setVisibility(View.VISIBLE);
            tvVacio.setText("No tienes permisos para ver moderaci\u00f3n");
        }
        Toast.makeText(requireContext(), "No tienes permisos para ver moderaci\u00f3n", Toast.LENGTH_LONG).show();
    }

    private void cargarReportes() {
        if (!tienePermisoModeracion()) {
            mostrarSinPermisos();
            return;
        }

        mostrarCarga(true);
        moderacionApi.listarReportes(
                idUsuarioActual,
                obtenerFiltroEstado(),
                obtenerFiltroPrioridad(),
                obtenerFiltroTipo(),
                checkSoloMios.isChecked()
        ).enqueue(new Callback<List<ReporteResumenDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReporteResumenDTO>> call, @NonNull Response<List<ReporteResumenDTO>> response) {
                mostrarCarga(false);

                if (response.code() == 204) {
                    mostrarListaVacia("No hay reportes para los filtros seleccionados.");
                    return;
                }

                if (response.isSuccessful()) {
                    List<ReporteResumenDTO> body = response.body();
                    if (body == null || body.isEmpty()) {
                        mostrarListaVacia("No hay reportes para los filtros seleccionados.");
                        return;
                    }
                    recyclerReportes.setVisibility(View.VISIBLE);
                    tvVacio.setVisibility(View.GONE);
                    adapter.actualizarLista(body);
                    return;
                }

                String backendMessage = ApiErrorParser.extractMessage(response);
                String mensaje;
                if (response.code() == 403) {
                    mensaje = backendMessage != null ? backendMessage : "No tienes permisos para ver moderaci\u00f3n.";
                } else if (response.code() == 404) {
                    mensaje = backendMessage != null ? backendMessage : "No se encontraron reportes.";
                } else {
                    mensaje = backendMessage != null ? backendMessage : "No se pudo cargar la bandeja de reportes.";
                }
                mostrarError(mensaje);
            }

            @Override
            public void onFailure(@NonNull Call<List<ReporteResumenDTO>> call, @NonNull Throwable t) {
                mostrarCarga(false);
                mostrarError("Error de conexi\u00f3n al cargar reportes.");
            }
        });
    }

    @Nullable
    private String obtenerFiltroEstado() {
        String valor = spinnerEstado.getSelectedItem() != null ? spinnerEstado.getSelectedItem().toString() : "TODOS";
        return "TODOS".equalsIgnoreCase(valor) ? null : valor;
    }

    @Nullable
    private String obtenerFiltroTipo() {
        String valor = spinnerTipo.getSelectedItem() != null ? spinnerTipo.getSelectedItem().toString() : "TODOS";
        return "TODOS".equalsIgnoreCase(valor) ? null : valor;
    }

    @Nullable
    private String obtenerFiltroPrioridad() {
        String valor = spinnerPrioridad.getSelectedItem() != null ? spinnerPrioridad.getSelectedItem().toString() : "TODAS";
        return "TODAS".equalsIgnoreCase(valor) ? null : valor;
    }

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        recyclerReportes.setVisibility(cargando ? View.GONE : View.VISIBLE);
        if (cargando) {
            tvVacio.setVisibility(View.GONE);
        }
    }

    private void mostrarListaVacia(@NonNull String mensaje) {
        adapter.actualizarLista(new ArrayList<>());
        recyclerReportes.setVisibility(View.GONE);
        tvVacio.setVisibility(View.VISIBLE);
        tvVacio.setText(mensaje);
    }

    private void mostrarError(@NonNull String mensaje) {
        adapter.actualizarLista(new ArrayList<>());
        recyclerReportes.setVisibility(View.GONE);
        tvVacio.setVisibility(View.VISIBLE);
        tvVacio.setText(mensaje);
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}
