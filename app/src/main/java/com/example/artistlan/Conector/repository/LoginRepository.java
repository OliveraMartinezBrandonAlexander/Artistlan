package com.example.artistlan.Conector.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.LoginRequestDTO;
import com.example.artistlan.Conector.model.LoginResponseDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRepository {

    private final UsuarioApi api;

    public LoginRepository(UsuarioApi api) {
        this.api = api;
    }

    public LiveData<LoginResponseDTO> login(String usuario, String correo, String contrasena) {
        MutableLiveData<LoginResponseDTO> data = new MutableLiveData<>();
        String usuarioOCorreo = (usuario != null && !usuario.trim().isEmpty()) ? usuario.trim() : (correo != null ? correo.trim() : "");
        LoginRequestDTO request = new LoginRequestDTO(usuarioOCorreo, contrasena);

        api.login(request).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                data.setValue(null);
            }
        });

        return data;
    }
}
