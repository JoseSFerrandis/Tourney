package com.example.tourney.tools

import com.example.tourney.entities.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface APIService {
    //fun getUsers(): List<User>
    @POST("/newUser")
    suspend fun insertNewUser(@Body user: User): User

    companion object{
        private var apiService: APIService? = null
        // TODO: cambiar la URL
        private var url: String = "http://192.168.0.17:8080"

        fun getInstance(): APIService {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(APIService::class.java)
            }
            return apiService!!
        }
    }
}