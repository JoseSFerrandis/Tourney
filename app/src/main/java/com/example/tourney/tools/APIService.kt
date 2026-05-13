package com.example.tourney.tools

import com.example.models.LoginResponse
import com.example.tourney.entities.Tournament
import com.example.tourney.models.LoginRequest
import com.example.tourney.models.NewUserModel
import com.example.tourney.models.UserModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface APIService {
    @POST("/newUser")
    suspend fun insertNewUser(@Body user: NewUserModel): UserModel

    @POST("/login")
    //suspend fun loginUser(@Body login: LoginRequest): Map<String, String>
    suspend fun loginUser(@Body login: LoginRequest): LoginResponse

    @GET("/getUser")
    suspend fun getUserById(): UserModel

    @GET("/user/getCreatedTournaments")
    suspend fun getCreatedTournaments(): List<Tournament>

    @GET("/user/getJoinedTournaments")
    suspend fun getJoinedTournaments(): List<Tournament>

    @GET("/user/getFollowingTournaments")
    suspend fun getFollowingTournaments(): List<Tournament>

    @POST("/insertTournament")
    suspend fun insertTournament(@Body tournament: Tournament): Tournament






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