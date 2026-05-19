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
        this.tournaments.addAll(tournaments)
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
        if(User.actualUser?.logged == false){
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
        TournamentsDao(context).deleteTournament(id)
        removeTournament(id)
        User.actualUser?.let {
            it.removeShowableTournament(id)
            it.removeFollowingTournament(id)
            it.removeJoinedTournament(id)
        }
    }

    fun searchTournamentByCode(code: Int): Tournament? {
        return tournaments.find { it.code == code }
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
}
