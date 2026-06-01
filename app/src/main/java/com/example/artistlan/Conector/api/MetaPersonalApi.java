package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.MetaPersonalCancelRequestDTO;
import com.example.artistlan.Conector.model.MetaPersonalDTO;
import com.example.artistlan.Conector.model.MetaPersonalRequestDTO;
import com.example.artistlan.Conector.model.MetaPersonalResumenDTO;
import com.example.artistlan.Conector.model.MetaPersonalUpdateDTO;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MetaPersonalApi {

    @GET("metas/mis-metas")
    Call<List<MetaPersonalDTO>> obtenerMisMetas();

    @GET("metas/resumen")
    Call<MetaPersonalResumenDTO> obtenerResumenMisMetas();

    @POST("metas")
    Call<MetaPersonalDTO> crearMeta(@Body MetaPersonalRequestDTO request);

    @PUT("metas/{idMeta}")
    Call<MetaPersonalDTO> actualizarMeta(@Path("idMeta") int idMeta, @Body MetaPersonalUpdateDTO request);

    @PATCH("metas/{idMeta}/cancelar")
    Call<MetaPersonalDTO> cancelarMeta(@Path("idMeta") int idMeta, @Body MetaPersonalCancelRequestDTO request);
}
