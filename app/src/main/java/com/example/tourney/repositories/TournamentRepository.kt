package com.example.tourney.repositories

import android.content.Context
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User
import com.example.tourney.tools.TournamentsDao

class TournamentRepository private constructor() {
    private var tournaments: MutableList<Tournament> = mutableListOf()
    private var isLoaded = false

    companion object {
        private val instance = TournamentRepository()
        fun getInstance(): TournamentRepository {
            return instance
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

    fun addTournament(tournament: Tournament) {
        if (!tournaments.any { it.id == tournament.id }) {
            tournaments.add(0, tournament)
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
