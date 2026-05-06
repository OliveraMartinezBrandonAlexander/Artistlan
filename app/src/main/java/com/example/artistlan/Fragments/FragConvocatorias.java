package com.example.artistlan.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.BotonesMenuSuperior;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ConvocatoriaApi;
import com.example.artistlan.Conector.model.ConvocatoriaDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.adapter.ConvocatoriaHomeAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragConvocatorias extends Fragment {

    private RecyclerView rvCalendarioEventos;
    private ProgressBar pbCalendarioEventos;
    private TextView tvCalendarioEventosEstado;
    private ConvocatoriaHomeAdapter convocatoriaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_frag_convocatorias, container, false);
        ThemeModuleStyler.styleFragment(this, root);
        new BotonesMenuSuperior(this);

        rvCalendarioEventos = root.findViewById(R.id.rvCalendarioEventos);
        pbCalendarioEventos = root.findViewById(R.id.pbCalendarioEventos);
        tvCalendarioEventosEstado = root.findViewById(R.id.tvCalendarioEventosEstado);

        configurarLista();
        cargarConvocatorias();

        return root;
    }

    private void configurarLista() {
        convocatoriaAdapter = new ConvocatoriaHomeAdapter(this::openWebPage);
        rvCalendarioEventos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCalendarioEventos.setAdapter(convocatoriaAdapter);
    }

    private void cargarConvocatorias() {
        mostrarEstado(true, null);
        ConvocatoriaApi api = RetrofitClient.getClient().create(ConvocatoriaApi.class);
        api.getConvocatorias().enqueue(new Callback<List<ConvocatoriaDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ConvocatoriaDTO>> call, @NonNull Response<List<ConvocatoriaDTO>> response) {
                if (!isAdded()) return;
                mostrarEstado(false, null);

                if (!response.isSuccessful() || response.body() == null) {
                    mostrarEstado(false, "No se pudieron cargar las convocatorias.");
                    return;
                }

                List<ConvocatoriaDTO> convocatorias = response.body();
                convocatoriaAdapter.actualizar(convocatorias);

                if (convocatorias.isEmpty()) {
                    mostrarEstado(false, "No hay convocatorias activas por ahora.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ConvocatoriaDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mostrarEstado(false, "Error de conexión al cargar convocatorias.");
            }
        });
    }

    private void mostrarEstado(boolean loading, @Nullable String mensaje) {
        pbCalendarioEventos.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (mensaje == null || mensaje.isEmpty()) {
            tvCalendarioEventosEstado.setVisibility(View.GONE);
        } else {
            tvCalendarioEventosEstado.setVisibility(View.VISIBLE);
            tvCalendarioEventosEstado.setText(mensaje);
        }
    }

    private void openWebPage(String url) {
        try {
            Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("FragCalendarioEventos", "No se pudo abrir el navegador: " + e.getMessage());
        }
    }
}