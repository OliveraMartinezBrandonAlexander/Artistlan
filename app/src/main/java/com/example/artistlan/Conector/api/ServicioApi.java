package com.example.artistlan.Conector.api;
import com.example.artistlan.Conector.model.ServicioDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
public interface ServicioApi {

    //Generales
    @GET("servicios")
    Call<List<ServicioDTO>> obtenerTodos();

    @GET("servicios/{id}")
    Call<ServicioDTO> obtenerPorId(@Path("id") int id);

    @POST("servicios")
    Call<ServicioDTO> crear(@Body ServicioDTO servicio);

    @PUT("servicios/{id}")
    Call<ServicioDTO> actualizar(@Path("id") int id, @Body ServicioDTO servicio);

    @DELETE("servicios/{id}")
    Call<Void> eliminar(@Path("id") int id);

    //Personales
    @GET("portafolioPersonal/{usuarioId}")
    Call<List<ServicioDTO>> obtenerServiciosDeUsuario(@Path("usuarioId") int usuarioId);

    @POST("portafolioPersonal/{usuarioId}")
    Call<ServicioDTO> crearServicioDeUsuario(@Path("usuarioId") int usuarioId, @Body ServicioDTO servicio);

    @PUT("portafolioPersonal/{idServicio}")
    Call<ServicioDTO> actualizarServicioUsuario(@Path("idServicio") int idServicio,
                                                @Body ServicioDTO servicio);
    @DELETE("portafolioPersonal/{idServicio}")
    Call<Void> eliminarServicioUsuario(@Path("idServicio") int idServicio);
}