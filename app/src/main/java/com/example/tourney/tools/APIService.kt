package com.example.tourney.tools

import com.example.models.LoginResponse
import com.example.models.PasswordModel
import com.example.tourney.entities.Tournament
import com.example.tourney.models.LoginModel
import com.example.tourney.models.NewUserModel
import com.example.tourney.models.EmailAndNickname
import com.example.tourney.models.IdModel
import com.example.tourney.models.TournamentModel
import com.example.tourney.models.UserModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface APIService {
    @POST("/newUser")
    suspend fun insertNewUser(@Body user: NewUserModel): UserModel

    @POST("/login")
    suspend fun loginUser(@Body login: LoginModel): LoginResponse

    @POST("/rememberPassword")
    suspend fun rememberPassword(@Body refreshPassword: EmailAndNickname): Response<Unit>
    @POST("/updatePassword")
    suspend fun updatePassword(@Body loginModel: LoginModel): Response<Unit>


    @GET("/getUser")
    suspend fun getUserById(@Header("Authorization") token: String): UserModel

    @GET("/user/getCreatedTournaments")
    suspend fun getCreatedTournaments(@Header("Authorization") token: String): List<Tournament>

    @GET("/user/getJoinedTournaments")
    suspend fun getJoinedTournaments(@Header("Authorization") token: String): List<Tournament>

    @GET("/user/getFollowingTournaments")
    suspend fun getFollowingTournaments(@Header("Authorization") token: String): List<Tournament>

    @GET("/user/updateAvatar/{avatarId}")
    suspend fun updateAvatar(@Header("Authorization") token: String, @Path("avatarId") avatarId: Int): Response<Unit>

    @POST("/user/checkPassword")
    suspend fun checkPassword(@Header("Authorization") token: String, @Body password: PasswordModel): Response<Unit>

    @POST("/user/updatePassword")
    suspend fun updatePassword(@Header("Authorization") token: String, @Body password: PasswordModel): Response<Unit>

    @POST("/user/editAccount")
    suspend fun editAccount(@Header("Authorization") token: String, @Body emailAndNickname: EmailAndNickname): Response<Unit>





    @POST("/tournament/createTournament")
    suspend fun insertTournament(@Header("Authorization") token: String, @Body tournament: TournamentModel): Response<IdModel>

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