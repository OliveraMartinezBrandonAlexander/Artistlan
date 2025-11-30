package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.CategoriaDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoriaApi {

    @GET("categorias")
    Call<List<CategoriaDTO>> obtenerCategorias();

}