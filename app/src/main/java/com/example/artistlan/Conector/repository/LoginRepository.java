package com.example.artistlan.Conector.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.artistlan.Conector.api.UsuarioApi;
import com.example.artistlan.Conector.model.UsuariosDTO;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginRepository {

    private final UsuarioApi api;

    public LoginRepository(UsuarioApi api) {
        this.api = api;
    }

    public LiveData<UsuariosDTO> login(String usuario, String correo, String contrasena) {
        MutableLiveData<UsuariosDTO> data = new MutableLiveData<>();

        api.login(usuario, correo, contrasena).enqueue(new Callback<UsuariosDTO>() {
            @Override
            public void onResponse(Call<UsuariosDTO> call, Response<UsuariosDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<UsuariosDTO> call, Throwable t) {
                data.setValue(null);
            }
        });

        return data;
    }
}