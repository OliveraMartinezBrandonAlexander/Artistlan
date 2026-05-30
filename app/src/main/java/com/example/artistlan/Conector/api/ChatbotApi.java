package com.example.artistlan.Conector.api;

import com.example.artistlan.Conector.model.ChatbotRequestDTO;
import com.example.artistlan.Conector.model.ChatbotResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatbotApi {

    @POST("chatbot/message")
    Call<ChatbotResponseDTO> enviarMensaje(@Body ChatbotRequestDTO request);
}
