package com.example.tourney.repositories

import android.content.Context
import com.example.tourney.entities.Tournament
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

    /**
     * Carga los torneos desde la base de datos si no han sido cargados aún.
     */
    fun loadFromDatabase(context: Context) {
        if (!isLoaded) {
            val dao = TournamentsDao(context)
            tournaments = dao.getAllTournaments()
            isLoaded = true
        }
    }

    fun getTournaments(): MutableList<Tournament> {
        return tournaments
    }

    fun addTournament(tournament: Tournament) {
        if (!tournaments.any { it.id == tournament.id }) {
            tournaments.add(0, tournament) // Añadir al principio para que salga el primero en la lista
        }
    }

    fun removeTournament(id: Long) {
        tournaments.removeIf { it.id == id }
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
