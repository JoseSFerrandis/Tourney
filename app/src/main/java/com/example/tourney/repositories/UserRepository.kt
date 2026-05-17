package com.example.tourney.repositories

import android.content.Context
import com.example.tourney.entities.User
import com.example.tourney.models.LoginModel
import com.example.tourney.models.NewUserModel
import com.example.tourney.tools.APIService
import com.example.tourney.tools.Security
import com.example.tourney.tools.UsersDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit
import com.example.models.PasswordModel
import com.example.tourney.entities.Tournament
import com.example.tourney.models.RememberPasswordModel
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import retrofit2.Response


class UserRepository(private val dao: UsersDao, private val api: APIService) {
    fun insertNewUser(user: NewUserModel, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000){
                    val newUser = api.insertNewUser(user)
                    // TODO: Quitar dao en producción
                    dao.insertNewUser(
                        User(
                            id = 0,
                            nickname = user.nickname,
                            email = user.email,
                            password = user.passwordHash,
                            photo = user.photo
                        )
                    )
                    withContext(Dispatchers.Main) { onSuccess() } // Toast de éxito y navegación a login
                    println("Usuario insertado correctamente")
                }
            } catch (e: Exception) {
                println("Error al insertar usuario")
                withContext(Dispatchers.Main) { onError(e) } // Toast de error
                e.printStackTrace()
            }
        }
    }

    fun loginUser(email: String, password: String, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                withTimeout(5000){
                    val loginResponse = api.loginUser(LoginModel(email, password))
                    User.actualUser = User(
                        loginResponse.user.id,
                        loginResponse.user.nickname,
                        loginResponse.user.email,
                        "",
                        loginResponse.user.photo
                    )

                    // Guardar el token en SharedPreferences (encriptado)
                    val sharedPreferences = Security().getEncryptedSharedPreferences(context)
                    sharedPreferences.edit { putString("token", loginResponse.token) }

                    // Cargar torneos pasando el contexto para recuperar el token
                    loadShowableTournaments(context)

                    withContext(Dispatchers.Main) { onSuccess() }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main) { onError(e) }
                e.printStackTrace()
            }
        }
    }

    fun checkPassword(password: String, context: Context, onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                withTimeout(5000) {
                    val sharedPreferences = Security().getEncryptedSharedPreferences(context)
                    val token = sharedPreferences.getString("token", "") ?: ""
                    val bearerToken = "Bearer $token"

                    val response = api.checkPassword(bearerToken, PasswordModel(password))
                    withContext(Dispatchers.Main) { onSuccess(response.isSuccessful) }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main) { onError(e) }
                e.printStackTrace()
            }
        }
    }

    fun rememberPassword(email: String, nickname: String, onSuccess: () -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                withTimeout(5000){
                    val response = api.rememberPassword(RememberPasswordModel(email, nickname))
                    if (response.isSuccessful) withContext(Dispatchers.Main) { onSuccess() }
                    else withContext(Dispatchers.Main) { onError(Exception("No se ha encontrado ningún usuario con esos datos")) }
                }
            }catch (e: Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
            }
        }
    }

    fun updatePassword(password: String, context: Context, onSuccess: (updated: Boolean) -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                withTimeout(5000){
                    val sharedPreferences = Security().getEncryptedSharedPreferences(context)
                    val token = sharedPreferences.getString("token", "") ?: ""
                    val bearerToken = "Bearer $token"

                    val response = api.updatePassword(bearerToken, PasswordModel(password))
                    withContext(Dispatchers.Main) { onSuccess(response.isSuccessful) }
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    fun updatePassword(email: String, passwordHash: String, onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = api.updatePassword(LoginModel(email, passwordHash))
                    withContext(Dispatchers.Main) { onSuccess(response.isSuccessful) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e) }
                e.printStackTrace()
            }
        }
    }

    fun updateAvatar(avatarId: Int, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val sharedPreferences = Security().getEncryptedSharedPreferences(context)
                    val token = sharedPreferences.getString("token", "") ?: ""
                    val bearerToken = "Bearer $token"

                    val response = api.updateAvatar(bearerToken, avatarId)
                    if (response.isSuccessful) withContext(Dispatchers.Main) { onSuccess() }
                    else withContext(Dispatchers.Main) { onError(Exception("No se ha podido actualizar el avatar")) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    suspend fun getCreatedTournamentsList(token: String): List<Tournament> = api.getCreatedTournaments(token)
    suspend fun getJoinedTournamentsList(token: String): List<Tournament> = api.getJoinedTournaments(token)
    suspend fun getFollowingTournamentsList(token: String): List<Tournament> = api.getFollowingTournaments(token)


    fun loadShowableTournaments(context: Context) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val sharedPreferences = Security().getEncryptedSharedPreferences(context)
            val token = sharedPreferences.getString("token", "") ?: ""
            val bearerToken = "Bearer $token"

            val createdTournamentList = async { getCreatedTournamentsList(bearerToken) }
            val joinedTournamentList = async { getJoinedTournamentsList(bearerToken) }
            val followingTournamentList = async { getFollowingTournamentsList(bearerToken) }

            val created = createdTournamentList.await()
            val joined = joinedTournamentList.await()
            val following = followingTournamentList.await()

            User.actualUser?.showableTournamentList?.clear()
            User.actualUser?.showableTournamentList?.addAll(created.map { it.id })
            User.actualUser?.showableTournamentList?.addAll(joined.map { it.id })
            User.actualUser?.showableTournamentList?.addAll(following.map { it.id })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object{
        private var instance: UserRepository? = null
        fun getInstance(dao: UsersDao, api: APIService): UserRepository{
            if(instance == null){
                instance = UserRepository(dao, api)
            }
            return instance!!
        }
    }
}