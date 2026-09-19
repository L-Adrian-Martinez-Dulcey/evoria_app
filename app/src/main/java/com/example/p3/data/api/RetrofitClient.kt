package com.example.p3.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // URL base de la API; Retrofit necesita que termine en "/" para construir bien las rutas.
    private const val BASE_URL = "https://6a8ee8baa12b7de8cc0f2245.mockapi.io/"

    // Registra las peticiones HTTP para depurar y comprobar la comunicación con la API.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Cliente HTTP con timeouts para evitar llamadas colgadas y controlar la conexión.
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    // Crea el servicio de la API solo cuando se necesita por primera vez.
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            // Convierte JSON a objetos Kotlin y viceversa con Gson.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
