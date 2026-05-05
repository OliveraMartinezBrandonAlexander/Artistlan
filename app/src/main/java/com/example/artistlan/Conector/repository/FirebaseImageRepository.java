package com.example.artistlan.Conector.repository;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.ActualizarFotoPerfilRequestDTO;
import com.example.artistlan.Conector.model.ActualizarImagenObraRequestDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import android.content.Context;

import com.example.artistlan.utils.ImageOptimizerUtil;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FirebaseImageRepository {

    public interface ImagenListener {
        void onSuccess(String urlFinal);
        void onError(String mensajeError);
    }

    private final StorageReference storageRef;
    private final UsuarioApi usuarioApi;
    private final ObraApi obraApi;
    private final Context appContext;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    public FirebaseImageRepository() {
        this(null);
    }

    public FirebaseImageRepository(Context context) {
        appContext = context != null ? context.getApplicationContext() : null;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Retrofit retrofit = RetrofitClient.getClient();
        usuarioApi = retrofit.create(UsuarioApi.class);
        obraApi = retrofit.create(ObraApi.class);
    }

    public void subirFotoPerfilYGuardarEnBD(int idUsuario, Uri imagenLocal, ImagenListener listener) {
        if (imagenLocal == null) {
            listener.onError("Uri de imagen nula");
            return;
        }
        if (appContext == null) {
            listener.onError("No hay contexto para optimizar imagen.");
            return;
        }

        String ruta = "usuarios/" + idUsuario + "/perfil_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storageRef.child(ruta);

        optimizeAndUpload(ref, imagenLocal, ImageOptimizerUtil.ImageType.PROFILE, listener, url -> {
            ActualizarFotoPerfilRequestDTO body = new ActualizarFotoPerfilRequestDTO(url);
            usuarioApi.actualizarFotoPerfil(idUsuario, body).enqueue(new Callback<UsuariosDTO>() {
                @Override
                public void onResponse(@NonNull Call<UsuariosDTO> call, @NonNull Response<UsuariosDTO> response) {
                    if (response.isSuccessful()) {
                        listener.onSuccess(url);
                    } else {
                        listener.onError("Error HTTP al guardar foto: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UsuariosDTO> call, @NonNull Throwable t) {
                    listener.onError("Fallo en la petición: " + t.getMessage());
                }
            });
        });
    }

    public void subirImagenObraYActualizarEnBD(int idObra, Uri imagenLocal, ImagenListener listener) {
        if (imagenLocal == null) {
            listener.onError("Uri de imagen nula");
            return;
        }
        if (appContext == null) {
            listener.onError("No hay contexto para optimizar imagen.");
            return;
        }

        String ruta = "obras/" + idObra + "/imagen1_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storageRef.child(ruta);

        optimizeAndUpload(ref, imagenLocal, ImageOptimizerUtil.ImageType.ARTWORK, listener, url -> {
            ActualizarImagenObraRequestDTO body = new ActualizarImagenObraRequestDTO(url);
            obraApi.actualizarImagen1(idObra, body).enqueue(new Callback<ObraDTO>() {
                @Override
                public void onResponse(@NonNull Call<ObraDTO> call, @NonNull Response<ObraDTO> response) {
                    if (response.isSuccessful()) {
                        listener.onSuccess(url);
                    } else {
                        listener.onError("Error HTTP al guardar imagen1: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ObraDTO> call, @NonNull Throwable t) {
                    listener.onError("Fallo en la petición: " + t.getMessage());
                }
            });
        });
    }

    public void subirImagenSolo(int idUsuario, Uri imagenLocal, ImagenListener listener) {
        if (imagenLocal == null) {
            listener.onError("Uri de imagen nula");
            return;
        }
        if (appContext == null) {
            listener.onError("No hay contexto para optimizar imagen.");
            return;
        }

        String ruta = "obras/" + idUsuario + "/temp_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storageRef.child(ruta);
        optimizeAndUpload(ref, imagenLocal, ImageOptimizerUtil.ImageType.ARTWORK, listener, listener::onSuccess);
    }

    private interface UrlConsumer { void accept(String url); }

    private void optimizeAndUpload(StorageReference ref, Uri imagenLocal, ImageOptimizerUtil.ImageType imageType,
                                   ImagenListener listener, UrlConsumer onUrlReady) {
        imageExecutor.execute(() -> {
            try {
                byte[] optimized = ImageOptimizerUtil.optimizeToJpeg(appContext, imagenLocal, imageType);
                StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
                ref.putBytes(optimized, metadata)
                        .addOnSuccessListener(taskSnapshot ->
                                ref.getDownloadUrl().addOnSuccessListener(uri -> onUrlReady.accept(uri.toString()))
                                        .addOnFailureListener(e -> listener.onError("Error al obtener URL de descarga: " + e.getMessage())))
                        .addOnFailureListener(e -> listener.onError("Error al subir a Firebase: " + e.getMessage()));
            } catch (IOException e) {
                listener.onError("No se pudo optimizar la imagen seleccionada.");
            } catch (OutOfMemoryError e) {
                listener.onError("La imagen es demasiado grande para procesarse. Intenta con otra imagen.");
            }
        });
    }
}
