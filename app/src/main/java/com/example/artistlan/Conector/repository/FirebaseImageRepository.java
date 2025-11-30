package com.example.artistlan.Conector.repository;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.api.ObraApi;
import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.ActualizarFotoPerfilRequestDTO;
import com.example.artistlan.Conector.model.ActualizarImagenObraRequestDTO;
import com.example.artistlan.Conector.model.ObraDTO;
import com.example.artistlan.Conector.model.UsuariosDTO;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    public FirebaseImageRepository() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        Retrofit retrofit = RetrofitClient.getClient();
        usuarioApi = retrofit.create(UsuarioApi.class);
        obraApi = retrofit.create(ObraApi.class);
    }

    // ------- FOTO DE PERFIL -------
    public void subirFotoPerfilYGuardarEnBD(
            int idUsuario,
            Uri imagenLocal,
            ImagenListener listener
    ) {
        if (imagenLocal == null) {
            listener.onError("Uri de imagen nula");
            return;
        }

        String ruta = "usuarios/" + idUsuario + "/perfil_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storageRef.child(ruta);

        UploadTask uploadTask = ref.putFile(imagenLocal);

        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();

                            ActualizarFotoPerfilRequestDTO body =
                                    new ActualizarFotoPerfilRequestDTO(url);

                            usuarioApi.actualizarFotoPerfil(idUsuario, body)
                                    .enqueue(new Callback<UsuariosDTO>() {
                                        @Override
                                        public void onResponse(@NonNull Call<UsuariosDTO> call,
                                                               @NonNull Response<UsuariosDTO> response) {
                                            if (response.isSuccessful()) {
                                                listener.onSuccess(url);
                                            } else {
                                                listener.onError("Error HTTP al guardar foto: " + response.code());
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<UsuariosDTO> call,
                                                              @NonNull Throwable t) {
                                            listener.onError("Fallo en la petición: " + t.getMessage());
                                        }
                                    });
                        }).addOnFailureListener(e ->
                                listener.onError("Error al obtener URL de descarga: " + e.getMessage())
                        )
                )
                .addOnFailureListener(e ->
                        listener.onError("Error al subir a Firebase: " + e.getMessage())
                );
    }

    // ------- IMAGEN1 DE OBRA (OBRA YA EXISTENTE) -------
    public void subirImagenObraYActualizarEnBD(
            int idObra,
            Uri imagenLocal,
            ImagenListener listener
    ) {
        if (imagenLocal == null) {
            listener.onError("Uri de imagen nula");
            return;
        }

        String ruta = "obras/" + idObra + "/imagen1_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storageRef.child(ruta);

        UploadTask uploadTask = ref.putFile(imagenLocal);

        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            String url = uri.toString();

                            ActualizarImagenObraRequestDTO body =
                                    new ActualizarImagenObraRequestDTO(url);

                            obraApi.actualizarImagen1(idObra, body)
                                    .enqueue(new Callback<ObraDTO>() {
                                        @Override
                                        public void onResponse(@NonNull Call<ObraDTO> call,
                                                               @NonNull Response<ObraDTO> response) {
                                            if (response.isSuccessful()) {
                                                listener.onSuccess(url);
                                            } else {
                                                listener.onError("Error HTTP al guardar imagen1: " + response.code());
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<ObraDTO> call,
                                                              @NonNull Throwable t) {
                                            listener.onError("Fallo en la petición: " + t.getMessage());
                                        }
                                    });
                        }).addOnFailureListener(e ->
                                listener.onError("Error al obtener URL de descarga: " + e.getMessage())
                        )
                )
                .addOnFailureListener(e ->
                        listener.onError("Error al subir a Firebase: " + e.getMessage())
                );
    }
}
