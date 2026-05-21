package com.example.tourney.repositories

import android.content.Context
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User
import com.example.tourney.models.TournamentModel
import com.example.tourney.tools.APIService
import com.example.tourney.tools.Security
import com.example.tourney.tools.TournamentsDao
import com.example.tourney.tools.UsersDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class TournamentRepository (private val tournamentsDao: TournamentsDao, private val usersDao: UsersDao, private val api: APIService) {
    private var tournaments: MutableList<Tournament> = mutableListOf()
    private var isLoaded = false

    companion object {
        private var instance: TournamentRepository? = null

        fun getInstance(context: Context): TournamentRepository {
            if (instance == null) {
                instance = TournamentRepository(
                    TournamentsDao(context),
                    UsersDao(context),
                    APIService.getInstance()
                )
            }
            return instance!!
        }
    }

    fun addTournamentsToMemory(tournaments: MutableList<Tournament>){
        tournaments.forEach { tournament ->
            if (this.tournaments.none { it.id == tournament.id }) {
                this.tournaments.add(tournament)
            }
        }
    }

    fun loadFromDatabase(context: Context) {
        if (!isLoaded) {
            val dao = TournamentsDao(context)
            tournaments = dao.getAllTournaments()
            isLoaded = true
        }
    }

    fun getTournaments(): MutableList<Tournament> { return tournaments }

    fun insertTournament(tournament: Tournament, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if(User.actualUser?.logged != true){
            tournament.id = tournamentsDao.insertTournament(tournament)
            if (!tournaments.any { it.id == tournament.id }){
                onSuccess()

                User.actualUser?.addShowableTournament(tournament.id)
                tournaments.add(0, tournament)
            }else onError(Exception("El torneo ya existe en la base de datos local"))
        }else{
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    withTimeout(5000){
                        val sharedPreferences = Security().getEncryptedSharedPreferences(context)
                        val token = sharedPreferences.getString("token", "") ?: ""
                        val bearerToken = "Bearer $token"

                        val tournamentModel = TournamentModel(
                            id = 0,
                            name = tournament.name,
                            game = tournament.game,
                            creatorId = tournament.creatorId,
                            creatorNickname = tournament.creatorNickname,
                            participantList = tournament.participantList,
                            maxParticipants = tournament.maxParticipants,
                            date = tournament.date,
                            location = tournament.location,
                            prize = tournament.prize,
                            code = tournament.code,
                            type = tournament.type,
                            tournamentStatus = tournament.tournamentStatus,
                            thumbnail = tournament.thumbnail
                        )
                        val response = api.insertTournament(bearerToken, tournamentModel)
                        if(response.isSuccessful){
                            tournament.id = response.body()?.id ?: -1
                            User.actualUser?.addShowableTournament(tournament.id)
                            tournaments.add(0, tournament)
                            withContext(Dispatchers.Main) { onSuccess() }
                        }else{
                            withContext(Dispatchers.Main) { onError(Exception("No se ha podido insertar el torneo en la base de datos remota")) }
                        }
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                    e.printStackTrace()
                }
            }
        }
    }

    fun updateTournament(tournament: Tournament, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (User.actualUser?.logged != true) {
            if (tournamentsDao.updateTournament(tournament)) {
                updateTournamentInList(tournament)
                onSuccess()
            } else {
                onError(Exception("Error al guardar los cambios en la base de datos local"))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = api.updateTournament(getBearerToken(context), tournament.toModel())
                    if (response.isSuccessful) {
                        updateTournamentInList(tournament)
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) { onError(Exception("No se ha podido actualizar el torneo")) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    fun updateParticipants(tournament: Tournament, context: Context, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        if (User.actualUser?.logged != true) {
            if (tournamentsDao.updateParticipants(tournament)) {
                updateTournamentInList(tournament)
                onSuccess()
            } else {
                onError(Exception("Error al actualizar participantes en la base de datos local"))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = api.updateParticipants(getBearerToken(context), tournament.id, tournament.participantList)
                    if (response.isSuccessful) {
                        updateTournamentInList(tournament)
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) { onError(Exception("No se han podido actualizar los participantes")) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    fun updateTournamentThumbnail(id: Long, thumbnail: Int, context: Context, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val tournament = searchTournamentById(id)
        if (User.actualUser?.logged != true) {
            val rows = tournamentsDao.updateTournamentThumbnail(id, thumbnail)
            if (rows > 0) {
                tournament?.thumbnail = thumbnail
                onSuccess()
            } else {
                onError(Exception("Error al actualizar la portada en la base de datos local"))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = api.updateTournamentThumbnail(getBearerToken(context), id, thumbnail)
                    if (response.isSuccessful) {
                        tournament?.thumbnail = thumbnail
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) { onError(Exception("No se ha podido actualizar la portada")) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    /**
     * Actualiza un torneo en la lista de memoria manteniendo su posición original.
     */
    fun updateTournamentInList(tournament: Tournament) {
        val index = tournaments.indexOfFirst { it.id == tournament.id }
        if (index != -1) {
            tournaments[index] = tournament
        }
    }

    fun removeTournament(id: Long) {
        tournaments.removeIf { it.id == id }
    }

    fun deleteTournament(context: Context, id: Long) {
        deleteTournament(context, id, {}, {})
    }

    fun deleteTournament(context: Context, id: Long, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (User.actualUser?.logged != true) {
            TournamentsDao(context).deleteTournament(id)
            removeTournamentFromMemoryAndUser(id)
            onSuccess()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = api.deleteTournament(getBearerToken(context), id)
                    if (response.isSuccessful) {
                        removeTournamentFromMemoryAndUser(id)
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) { onError(Exception("No se ha podido eliminar el torneo")) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    fun searchTournamentByCode(code: Int): Tournament? {
        return tournaments.find { it.code == code }
    }

    fun searchTournamentByCode(
        code: Int,
        context: Context,
        onSuccess: (Tournament) -> Unit,
        onNotFound: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (User.actualUser?.logged != true) {
            searchTournamentByCode(code)?.let(onSuccess) ?: onNotFound()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeout(5000) {
                    val response = api.getTournamentByCode(getBearerToken(context), code)
                    val tournament = response.body()
                    if (response.isSuccessful && tournament != null) {
                        if (tournaments.none { it.id == tournament.id }) {
                            tournaments.add(tournament)
                        }
                        withContext(Dispatchers.Main) { onSuccess(tournament) }
                    } else if (response.code() == 404) {
                        withContext(Dispatchers.Main) { onNotFound() }
                    } else {
                        withContext(Dispatchers.Main) { onError(Exception("No se ha podido buscar el torneo")) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(Exception("No se pudo establecer conexión con el servidor. Vuelve a intentarlo")) }
                e.printStackTrace()
            }
        }
    }

    fun searchTournamentById(id: Long): Tournament? {
        return tournaments.find { it.id == id }
    }

    fun searchTournamentListByIds(ids: List<Long>): MutableList<Tournament> {
        return tournaments.filter { ids.contains(it.id) }.toMutableList()
    }
    
    fun clear() {
        tournaments.clear()
        isLoaded = false
    }

    private fun removeTournamentFromMemoryAndUser(id: Long) {
        removeTournament(id)
        User.actualUser?.let {
            it.removeShowableTournament(id)
            it.removeFollowingTournament(id)
            it.removeJoinedTournament(id)
        }
    }

    private fun getBearerToken(context: Context): String {
        val sharedPreferences = Security().getEncryptedSharedPreferences(context)
        val token = sharedPreferences.getString("token", "") ?: ""
        return "Bearer $token"
    }

    private fun Tournament.toModel(): TournamentModel {
        updateMatchesFromView()
        return TournamentModel(
            id = id,
            name = name,
            game = game,
            creatorId = creatorId,
            creatorNickname = creatorNickname,
            participantList = participantList,
            maxParticipants = maxParticipants,
            date = date,
            location = location,
            prize = prize,
            code = code,
            type = type,
            tournamentStatus = tournamentStatus,
            thumbnail = thumbnail,
            matches = matches
        )
    }
}
