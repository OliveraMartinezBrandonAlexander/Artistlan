package com.example.artistlan.Conector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient; // Nueva importación
import okhttp3.logging.HttpLoggingInterceptor; // Nueva importación
import java.util.concurrent.TimeUnit; // Nueva importación

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {

            //Interceptor para ver logs de la conexión
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Cliente HTTP con logging y timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS) // Añadido timeout
                    .build();

            //Gson más tolerante con respuestas no estrictamente válidas
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            //
            // Retrofit con el cliente OkHttpClient
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}