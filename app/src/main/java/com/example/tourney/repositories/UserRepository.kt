package com.example.tourney.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.tourney.entities.User
import com.example.tourney.models.LoginRequest
import com.example.tourney.models.NewUserModel
import com.example.tourney.tools.APIService
import com.example.tourney.tools.SecurePreferences
import com.example.tourney.tools.UsersDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit
import com.example.tourney.entities.Tournament
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class UserRepository(private val dao: UsersDao, private val api: APIService) {
    fun insertNewUser(user: NewUserModel, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                val loginResponse = api.loginUser(LoginRequest(email, password))
                User.actualUser = User(loginResponse.user.id, loginResponse.user.nickname, loginResponse.user.email, "", loginResponse.user.photo)
                //dao.login(LoginRequest(email, password))
                withContext(Dispatchers.Main) { onSuccess() } // Toast de éxito y navegación a login

                // Guardar el token en SharedPreferences (encriptado)
                val sharedPreferences = SecurePreferences().getEncryptedSharedPreferences( context )
                sharedPreferences.edit { putString("token", loginResponse.token) }
            }catch (e: Exception){
                withContext(Dispatchers.Main) { onError(e) } // Toast de error
                e.printStackTrace()
            }
        }
    }

    suspend fun getCreatedTournamentsList(): List<Tournament> = api.getCreatedTournaments()
    suspend fun getJoinedTournamentsList(): List<Tournament> = api.getJoinedTournaments()
    suspend fun getFollowingTournamentsList(): List<Tournament> = api.getFollowingTournaments()


    fun loadShowableTournaments() = CoroutineScope(Dispatchers.IO).launch {
        val createdTournamentList = async { getCreatedTournamentsList() }
        val joinedTournamentList = async { getJoinedTournamentsList() }
        val followingTournamentList = async { getFollowingTournamentsList() }

        val created = createdTournamentList.await()
        val joined = joinedTournamentList.await()
        val following = followingTournamentList.await()

        User.actualUser?.showableTournamentList?.addAll(created.map { it.id })
        User.actualUser?.showableTournamentList?.addAll(joined.map { it.id })
        User.actualUser?.showableTournamentList?.addAll(following.map { it.id })
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