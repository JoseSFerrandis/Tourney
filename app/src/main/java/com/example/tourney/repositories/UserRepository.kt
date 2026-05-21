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
import com.example.tourney.models.EmailAndNickname
import com.example.tourney.tools.AppDatabaseHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout


class UserRepository(private val dao: UsersDao, private val api: APIService) {
    fun insertNewUser(user: NewUserModel, onSuccess: (succeed: Boolean) -> Unit, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000){
                    val newUser = api.insertNewUser(user)
                    /*dao.insertNewUser(
                        User(
                            id = 0,
                            nickname = user.nickname,
                            email = user.email,
                            password = user.password,
                            photo = user.photo
                        )
                    )*/
                    withContext(Dispatchers.Main) { onSuccess(newUser.id != -1L) } // Toast de éxito y navegación a login
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
                        loginResponse.user.photo,
                        logged = true
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
                    val response = api.rememberPassword(EmailAndNickname(email, nickname))
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

    fun editAccount(email: String, nickname: String, context: Context, onSuccess: (result: Int) -> Unit, onError: (Exception) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val sharedPreferences = Security().getEncryptedSharedPreferences(context)
                    val token = sharedPreferences.getString("token", "") ?: ""
                    val bearerToken = "Bearer $token"

                    val response = api.editAccount(bearerToken, EmailAndNickname(email, nickname))
                    withContext(Dispatchers.Main) { onSuccess(
                        when{
                            response.isSuccessful -> 0  // Ok
                            response.code() == 409 -> 1  // Conflict
                            else -> 2  // Bad request
                        }
                    ) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }

    }

    fun followTournament(tournamentId: Long, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        updateTournamentRelation(
            context = context,
            localAction = {
                User.actualUser?.addFollowingTournament(tournamentId)
                User.actualUser?.email?.let { email ->
                    dao.updateFollowingTournamentList(email, User.actualUser?.followingTournamentList.toCsv())
                }
            },
            remoteAction = { token -> api.followTournament(token, tournamentId) },
            memoryAction = { User.actualUser?.addFollowingTournament(tournamentId) },
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun unfollowTournament(tournamentId: Long, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        updateTournamentRelation(
            context = context,
            localAction = {
                User.actualUser?.removeFollowingTournament(tournamentId)
                User.actualUser?.email?.let { email ->
                    dao.updateFollowingTournamentList(email, User.actualUser?.followingTournamentList.toCsv())
                }
            },
            remoteAction = { token -> api.unfollowTournament(token, tournamentId) },
            memoryAction = { User.actualUser?.removeFollowingTournament(tournamentId) },
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun joinTournament(tournamentId: Long, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        updateTournamentRelation(
            context = context,
            localAction = {
                User.actualUser?.addJoinedTournament(tournamentId)
                User.actualUser?.email?.let { email ->
                    dao.updateJoinedTournamentList(email, User.actualUser?.joinedTournamentList.toCsv())
                }
            },
            remoteAction = { token -> api.joinTournament(token, tournamentId) },
            memoryAction = { User.actualUser?.addJoinedTournament(tournamentId) },
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun leaveTournament(tournamentId: Long, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        updateTournamentRelation(
            context = context,
            localAction = {
                User.actualUser?.removeJoinedTournament(tournamentId)
                User.actualUser?.email?.let { email ->
                    dao.updateJoinedTournamentList(email, User.actualUser?.joinedTournamentList.toCsv())
                }
            },
            remoteAction = { token -> api.leaveTournament(token, tournamentId) },
            memoryAction = { User.actualUser?.removeJoinedTournament(tournamentId) },
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun removeJoinedTournamentRelation(userId: Long, tournamentId: Long, context: Context, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        if (User.actualUser?.logged != true) {
            dao.removeTournamentRelation(userId, tournamentId, AppDatabaseHelper.REL_TYPE_JOINED)
            onSuccess()
            return
        }

        // El backend no tiene por quÃ© permitir que un creador quite la relaciÃ³n de otro usuario
        // con el token actual. Persistimos la lista del torneo desde TournamentRepository y, si es
        // el usuario actual, sincronizamos tambiÃ©n su relaciÃ³n remota.
        if (User.actualUser?.id == userId) {
            leaveTournament(tournamentId, context, onSuccess, onError)
        } else {
            onSuccess()
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

            TournamentRepository.getInstance(context).addTournamentsToMemory(created.toMutableList())
            TournamentRepository.getInstance(context).addTournamentsToMemory(joined.toMutableList())
            TournamentRepository.getInstance(context).addTournamentsToMemory(following.toMutableList())

            User.actualUser?.showableTournamentList?.clear()
            User.actualUser?.showableTournamentList?.addAll(created.map { it.id })
            User.actualUser?.joinedTournamentList?.clear()
            User.actualUser?.joinedTournamentList?.addAll(joined.map { it.id })
            User.actualUser?.followingTournamentList?.clear()
            User.actualUser?.followingTournamentList?.addAll(following.map { it.id })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateTournamentRelation(
        context: Context,
        localAction: () -> Unit,
        remoteAction: suspend (String) -> retrofit2.Response<Unit>,
        memoryAction: () -> Unit,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (User.actualUser?.logged != true) {
            localAction()
            onSuccess()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = remoteAction(getBearerToken(context))
                    if (response.isSuccessful) {
                        memoryAction()
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) { onError(Exception("No se ha podido actualizar la relaciÃ³n con el torneo")) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexiÃ³n con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    private fun getBearerToken(context: Context): String {
        val sharedPreferences = Security().getEncryptedSharedPreferences(context)
        val token = sharedPreferences.getString("token", "") ?: ""
        return "Bearer $token"
    }

    private fun MutableList<Long>?.toCsv(): String {
        return this?.joinToString(",") ?: ""
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
