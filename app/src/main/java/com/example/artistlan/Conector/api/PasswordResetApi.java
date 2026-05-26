package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.PasswordResetConfirmRequestDTO;
import com.example.artistlan.Conector.model.PasswordResetRequestDTO;
import com.example.artistlan.Conector.model.PasswordResetResendRequestDTO;
import com.example.artistlan.Conector.model.PasswordResetResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PasswordResetApi {

    @POST("auth/password-reset/request")
    Call<PasswordResetResponseDTO> requestReset(@Body PasswordResetRequestDTO body);

    @POST("auth/password-reset/confirm")
    Call<PasswordResetResponseDTO> confirmReset(@Body PasswordResetConfirmRequestDTO body);

    @POST("auth/password-reset/resend")
    Call<PasswordResetResponseDTO> resend(@Body PasswordResetResendRequestDTO body);
}
