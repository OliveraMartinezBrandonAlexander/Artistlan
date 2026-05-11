package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ActualizarFotoPerfilRequestDTO;
import com.example.artistlan.Conector.model.ArtistaDTO;
import com.example.artistlan.Conector.model.DesactivarCuentaRequestDTO;
import com.example.artistlan.Conector.model.LoginRequestDTO;
import com.example.artistlan.Conector.model.LoginResponseDTO;
import com.example.artistlan.Conector.model.PageResponseArtistaDTO;
import com.example.artistlan.Conector.model.PageResponseUsuariosDTO;
import com.example.artistlan.Conector.model.RespuestaModeracionDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.example.artistlan.Conector.model.CambiarRolRequestDTO;
import com.example.artistlan.Conector.model.FavoritoDTO;
import com.example.artistlan.Conector.model.PerfilPublicoArtistaDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UsuarioApi {
    @GET("usuarios")
    Call<List<ArtistaDTO>> getArtistas();

    @GET("usuarios")
    Call<List<ArtistaDTO>> getArtistas(@Query("usuarioId") Integer usuarioId);

    @GET("usuarios/artistas/paginado")
    Call<PageResponseArtistaDTO> getArtistasPaginados(
            @Query("usuarioId") Integer usuarioId,
            @Query("q") String q,
            @Query("categoria") String categoria,
            @Query("idCategoria") Integer idCategoria,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @POST("usuarios")
    Call<List<UsuariosDTO>> crearUsuarios(@Body List<UsuariosDTO> usuarios);

    @POST("usuarios/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO request);

    @GET("usuarios/{id}")
    Call<UsuariosDTO> obtenerUsuarioPorId(@Path("id") int id);

    @GET("usuarios/{id}")
    Call<UsuariosDTO> obtenerUsuarioPorId(@Path("id") int id, @Query("usuarioId") Integer usuarioId);

    @PUT("usuarios/{id}")
    Call<Void> actualizarUsuario(@Path("id") int id, @Body UsuariosDTO usuario);

    @GET("usuarios/existe")
    Call<String> existeUsuario(@Query("usuario") String usuario, @Query("correo") String correo);

    @PUT("usuarios/{id}/foto-perfil")
    Call<UsuariosDTO> actualizarFotoPerfil(
            @Path("id") int idUsuario,
            @Body ActualizarFotoPerfilRequestDTO body
    );
    @PUT("usuariosusuario/{id}")
    Call<Void> actualizarUsuario(@Path("id") Integer id, @Body UsuariosDTO usuario);

    @GET("usuarios/{id}/categoria")
    Call<UsuariosDTO> obtenerCategoriaUsuario(@Path("id") int idUsuario);

    @DELETE("usuariosusuario/{id}")
    Call<Void> eliminarUsuario(@Path("id") int idUsuario);

    @POST("usuarios/{idUsuario}/desactivar-cuenta")
    Call<RespuestaModeracionDTO> desactivarCuenta(
            @Path("idUsuario") Integer idUsuario,
            @Body DesactivarCuentaRequestDTO request
    );

    @GET("usuarios/favoritos/{usuarioId}")
    Call<List<FavoritoDTO>> favoritosAlterno(@Path("usuarioId") int usuarioId);

    @GET("usuarios")
    Call<List<UsuariosDTO>> getUsuarios();

    @GET("usuarios/paginado")
    Call<PageResponseUsuariosDTO> getUsuariosPaginados(
            @Query("q") String q,
            @Query("rol") String rol,
            @Query("estadoCuenta") String estadoCuenta,
            @Query("idCategoria") Integer idCategoria,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @PUT("usuarios/{id}/rol")
    Call<UsuariosDTO> cambiarRol(
            @Path("id") int idUsuario,
            @Query("adminId") int adminId,
            @Body CambiarRolRequestDTO body
    );
    @GET("artistas/{idArtista}/publico")
    Call<PerfilPublicoArtistaDTO> obtenerPerfilPublicoArtista(
            @Path("idArtista") int idArtista,
            @Query("usuarioConsulta") Integer usuarioConsulta
    );
}
