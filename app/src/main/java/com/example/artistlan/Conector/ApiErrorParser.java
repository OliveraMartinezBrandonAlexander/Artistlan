package com.example.artistlan.Conector;

import com.example.artistlan.Conector.model.ErrorResponseDTO;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Response;

public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() {
    }

    public static String extractMessage(Response<?> response) {
        if (response == null || response.errorBody() == null) {
            return null;
        }
        try {
            String raw = response.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) {
                return null;
            }
            ErrorResponseDTO dto = GSON.fromJson(raw, ErrorResponseDTO.class);
            if (dto == null) {
                return null;
            }
            return dto.getMessage();
        } catch (IOException ignored) {
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}

