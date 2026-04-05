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
import retrofit2.http.Query;
public interface ServicioApi {

    @GET("servicios")
    Call<List<ServicioDTO>> obtenerTodos();

    @GET("servicios")
    Call<List<ServicioDTO>> obtenerTodos(@Query("usuarioId") Integer usuarioId);

    @GET("servicios/{id}")
    Call<ServicioDTO> obtenerPorId(@Path("id") int id);

    @GET("servicios/{id}")
    Call<ServicioDTO> obtenerPorId(@Path("id") int id, @Query("usuarioId") Integer usuarioId);

    @POST("servicios")
    Call<ServicioDTO> crear(@Body ServicioDTO servicio);

    @PUT("servicios/{id}")
    Call<ServicioDTO> actualizar(@Path("id") int id, @Body ServicioDTO servicio);

    @DELETE("servicios/{id}")
    Call<Void> eliminar(@Path("id") int id);

    @GET("portafolioPersonal/{usuarioId}")
    Call<List<ServicioDTO>> obtenerServiciosDeUsuario(@Path("usuarioId") int usuarioId);

    @POST("portafolioPersonal/{usuarioId}")
    Call<ServicioDTO> crearServicioDeUsuario(@Path("usuarioId") int usuarioId, @Body ServicioDTO servicio);

    @PUT("portafolioPersonal/{usuarioId}/{idServicio}")
    Call<ServicioDTO> actualizarServicioUsuario(
            @Path("usuarioId") int usuarioId,
            @Path("idServicio") int idServicio,
            @Body ServicioDTO servicio
    );

    @DELETE("portafolioPersonal/{usuarioId}/{idServicio}")
    Call<Void> eliminarServicioUsuario(
            @Path("usuarioId") int usuarioId,
            @Path("idServicio") int idServicio
    );
}
