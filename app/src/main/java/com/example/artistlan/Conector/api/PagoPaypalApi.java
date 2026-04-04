package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CapturarOrdenPaypalResponseDTO;
import com.example.artistlan.Conector.model.CrearOrdenPaypalResponseDTO;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PagoPaypalApi {

    @POST("pagos/paypal/crear-orden/{idObra}")
    Call<CrearOrdenPaypalResponseDTO> crearOrden(
            @Path("idObra") int idObra,
            @Query("compradorId") int compradorId
    );

    @POST("pagos/paypal/capturar/{paypalOrderId}")
    Call<CapturarOrdenPaypalResponseDTO> capturarOrden(@Path("paypalOrderId") String paypalOrderId);
}
