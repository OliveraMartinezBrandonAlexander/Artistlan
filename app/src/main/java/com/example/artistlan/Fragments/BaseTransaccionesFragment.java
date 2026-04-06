package com.example.artistlan.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.TransaccionApi;
import com.example.artistlan.Conector.model.TransaccionResumenDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.TransaccionAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseTransaccionesFragment extends Fragment {

    private RecyclerView recyclerTransacciones;
    private TextView tvTransaccionesVacias;
    private ProgressBar progressTransacciones;
    private TransaccionAdapter adapter;
    private TransaccionApi transaccionApi;
    private int idUsuarioLogueado = -1;

    @LayoutRes
    protected abstract int getLayoutResId();

    protected abstract Call<List<TransaccionResumenDTO>> crearLlamada(TransaccionApi api, int idUsuario);

    protected abstract String getMensajeVacio();

    protected abstract String getMensajeError();

    protected abstract TransaccionAdapter.TipoLista getTipoLista();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        transaccionApi = RetrofitClient.getClient().create(TransaccionApi.class);

        recyclerTransacciones = view.findViewById(R.id.recyclerTransacciones);
        tvTransaccionesVacias = view.findViewById(R.id.tvTransaccionesVacias);
        progressTransacciones = view.findViewById(R.id.progressTransacciones);

        recyclerTransacciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTransacciones.setHasFixedSize(true);

        adapter = new TransaccionAdapter(requireContext(), getTipoLista(), new ArrayList<>());
        recyclerTransacciones.setAdapter(adapter);

        cargarTransacciones();
    }

    private void cargarTransacciones() {
        if (idUsuarioLogueado <= 0) {
            mostrarEstadoVacio(getString(R.string.transacciones_sin_sesion));
            return;
        }

        progressTransacciones.setVisibility(View.VISIBLE);
        recyclerTransacciones.setVisibility(View.GONE);
        tvTransaccionesVacias.setVisibility(View.GONE);

        crearLlamada(transaccionApi, idUsuarioLogueado).enqueue(new Callback<List<TransaccionResumenDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<TransaccionResumenDTO>> call, @NonNull Response<List<TransaccionResumenDTO>> response) {
                if (!isAdded()) {
                    return;
                }

                progressTransacciones.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), getMensajeError() + " (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    mostrarEstadoMensaje(getMensajeError());
                    return;
                }

                if (response.code() == 204) {
                    mostrarEstadoVacio(getMensajeVacio());
                    return;
                }

                List<TransaccionResumenDTO> transacciones = response.body();
                if (transacciones == null || transacciones.isEmpty()) {
                    mostrarEstadoVacio(getMensajeVacio());
                    return;
                }

                adapter.actualizarLista(transacciones);
                recyclerTransacciones.setVisibility(View.VISIBLE);
                tvTransaccionesVacias.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<List<TransaccionResumenDTO>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }

                progressTransacciones.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                mostrarEstadoMensaje(getMensajeError());
            }
        });
    }

    private void mostrarEstadoVacio(String mensaje) {
        mostrarEstadoMensaje(mensaje);
    }

    private void mostrarEstadoMensaje(String mensaje) {
        adapter.actualizarLista(new ArrayList<>());
        recyclerTransacciones.setVisibility(View.GONE);
        tvTransaccionesVacias.setText(mensaje);
        tvTransaccionesVacias.setVisibility(View.VISIBLE);
    }
}
