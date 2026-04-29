package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.TwoFactorResendRequest;
import com.example.artistlan.Conector.model.TwoFactorResponse;
import com.example.artistlan.Conector.model.TwoFactorVerifyActivationRequest;
import com.example.artistlan.Conector.model.TwoFactorVerifyLoginRequest;
import com.example.artistlan.Conector.model.TwoFactorVerifyLoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface Auth2FAApi {

    @POST("auth/2fa/verify-login")
    Call<TwoFactorVerifyLoginResponse> verifyLogin(@Body TwoFactorVerifyLoginRequest body);

    @POST("auth/2fa/resend")
    Call<TwoFactorResponse> resend(@Body TwoFactorResendRequest body);

    @POST("auth/2fa/request-activation")
    Call<TwoFactorResponse> requestActivation(@Header("Authorization") String authorization);

    @POST("auth/2fa/verify-activation")
    Call<TwoFactorResponse> verifyActivation(@Header("Authorization") String authorization,
                                             @Body TwoFactorVerifyActivationRequest body);

    @POST("auth/2fa/disable")
    Call<TwoFactorResponse> disable(@Header("Authorization") String authorization);
}
