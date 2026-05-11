package com.example.tourney.tools

import com.example.tourney.entities.User
import com.example.tourney.models.NewUserModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface APIService {
    //fun getUsers(): List<User>
    @POST("/newUser")
    suspend fun insertNewUser(@Body user: NewUserModel): User

    companion object{
        private var apiService: APIService? = null
        // TODO: cambiar la URL
        private var url: String = "http://192.168.0.17:8080"

        fun getInstance(): APIService {
            if (apiService == null) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()

                apiService = Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(APIService::class.java)
            }
            return apiService!!
        }
    }
}