package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.TransaccionResumenDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TransaccionApi {

    @GET("transacciones/compras/{idUsuario}")
    Call<List<TransaccionResumenDTO>> obtenerComprasPorUsuario(@Path("idUsuario") int idUsuario);

    @GET("transacciones/ventas/{idUsuario}")
    Call<List<TransaccionResumenDTO>> obtenerVentasPorUsuario(@Path("idUsuario") int idUsuario);
}
