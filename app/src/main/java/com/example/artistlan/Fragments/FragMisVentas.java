package com.example.artistlan.Fragments;

import com.example.artistlan.Conector.api.TransaccionApi;
import com.example.artistlan.Conector.model.TransaccionResumenDTO;
import com.example.artistlan.R;
import com.example.artistlan.adapter.TransaccionAdapter;

import java.util.List;

import retrofit2.Call;

public class FragMisVentas extends BaseTransaccionesFragment {

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_frag_mis_ventas;
    }

    @Override
    protected Call<List<TransaccionResumenDTO>> crearLlamada(TransaccionApi api, int idUsuario) {
        return api.obtenerVentasPorUsuario(idUsuario);
    }

    @Override
    protected String getMensajeVacio() {
        return getString(R.string.transacciones_vacias_ventas);
    }

    @Override
    protected String getMensajeError() {
        return getString(R.string.transacciones_error_ventas);
    }

    @Override
    protected TransaccionAdapter.TipoLista getTipoLista() {
        return TransaccionAdapter.TipoLista.VENTAS;
    }
}
