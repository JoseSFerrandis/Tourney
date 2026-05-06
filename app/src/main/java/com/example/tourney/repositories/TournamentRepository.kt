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

    /**
     * Obtiene la lista completa de torneos cargados en memoria.
     */
    fun getTournaments(): MutableList<Tournament> { return tournaments }

    /**
     * Añade un torneo a la lista en memoria.
     */
    fun addTournament(tournament: Tournament) {
        if (!tournaments.any { it.id == tournament.id }) {
            tournaments.add(0, tournament) // Añadir al principio para que salga el primero en la lista
        }
    }

    /**
     * Elimina un torneo de la lista en memoria.
     */
    fun removeTournament(id: Long) {
        tournaments.removeIf { it.id == id }
    }

    /**
     * Borra un torneo tanto de la base de datos como de la memoria,
     * y actualiza las listas del usuario actual.
     */
    fun deleteTournament(context: Context, id: Long) {
        // 1. Borrar de la Base de Datos (Limpieza automática de relaciones vía CASCADE)
        TournamentsDao(context).deleteTournament(id)

        // 2. Borrar de la memoria local del repositorio
        removeTournament(id)

        // 3. Sincronizar el usuario actual (si estaba inscrito o lo seguía)
        User.actualUser?.let {
            it.removeShowableTournament(id)
            it.removeFollowingTournament(id)
            it.removeJoinedTournament(id)
        }
    }

    /**
     * Busca un torneo por su código de invitación.
     */
    fun searchTournamentByCode(code: Int): Tournament? {
        return tournaments.find { it.code == code }
    }

    /**
     * Busca un torneo por su ID único.
     */
    fun searchTournamentById(id: Long): Tournament? {
        return tournaments.find { it.id == id }
    }

    /**
     * Filtra la lista de torneos devolviendo solo los que coincidan con los IDs proporcionados.
     */
    fun searchTournamentListByIds(ids: List<Long>): MutableList<Tournament> {
        return tournaments.filter { ids.contains(it.id) }.toMutableList()
    }
    
    /**
     * Limpia la lista de torneos y marca el repositorio para volver a cargar datos.
     */
    fun clear() {
        tournaments.clear()
        isLoaded = false
    }
}
