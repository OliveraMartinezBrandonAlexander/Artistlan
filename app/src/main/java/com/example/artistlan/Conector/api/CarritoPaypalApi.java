package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CapturarOrdenPaypalCarritoResponseDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalCarritoResponseDTO;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CarritoPaypalApi {

    @POST("carrito/paypal/crear-orden/{idUsuario}")
    Call<CrearOrdenPaypalCarritoResponseDTO> crearOrdenCarrito(@Path("idUsuario") int idUsuario);

    @POST("carrito/paypal/capturar/{paypalOrderId}")
    Call<CapturarOrdenPaypalCarritoResponseDTO> capturarOrdenCarrito(@Path("paypalOrderId") String paypalOrderId);
}
