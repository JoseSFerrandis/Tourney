package com.example.tourney.repositories

import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User

class TournamentRepository {
    private val tournamentExamples = mutableListOf(
        Tournament(3, "Championship Dungeons & Dragons", "D&D 5e",  "Marquitos", mutableListOf(),12, "20 Ene 2026", "Tienda Gaming Local", "$1,500", 1),
        Tournament(1, "Copa League of Legends 2026", "League of Legends", "Marquitos", mutableListOf(), 32, "25 Ene 2026", "KOI", "$5,000", 777),
        Tournament(2, "Torneo Counter-Strike Relámpago", "CS:GO", "Marquitos", mutableListOf(), 8, "18 Ene 2026", "Cybercafé Central", "$2,000", 69),
        Tournament(4, "Torneo Valorant Summer", "Valorant", "Marquitos", mutableListOf(),32, "28 Ene 2026", "Online/Presencial", "$3,000", 1000)
    )

    private val userExamples = mutableListOf(
        User(1, "Marquitos", "marquitos@gmail.com", "password123", 1),
        User(2, "Pepito", "pepito@gmail.com", "password123", 1),
        User(3, "Juan", "juan@gmail.com", "password123", 1),
        User(4, "Pedro", "pedro@gmail.com", "password123", 1),
        User(5, "Jose", "jose@gmail.com", "password123", 1),
        User(6, "Esteban", "esteban@gmail.com", "password123", 1),
        User(7, "Sebastián", "sebastián@gmail.com", "password123", 1),
        User(8, "Julio", "julio@gmail.com", "password123", 1),
        User(9, "Marcos", "marcos@gmail.com", "password123", 1),
        User(10, "Erik", "erik@gmail.com", "password123", 1),
        User(11, "Javier", "javier@gmail.com", "password123", 1),
        User(12, "Lucas", "lucas@gmail.com", "password123", 1),
    )
    private var tournaments: MutableList<Tournament> = mutableListOf()

    init {
        // Inicializa datos
        tournamentExamples[0].participantList = userExamples
        setTournaments(tournamentExamples)
    }

    companion object {
        private val instance = TournamentRepository()
        fun getInstance(): TournamentRepository {
            return instance
        }
    }

    fun getTournaments() : MutableList<Tournament> { return tournaments }
    fun setTournaments(tournaments: MutableList<Tournament>){ this.tournaments = tournaments}

    fun addTournament(tournament: Tournament){ tournaments.add(tournament) }
    fun removeTournament(tournament: Tournament){ tournaments.remove(tournament) }
    fun removeTournament(id: Long){ tournaments.removeIf { it.id == id } }

    fun searchTournamentByCode(code: Int) : Tournament ? {
        tournaments.find { it.code == code }?.let { return it }
        return null
    }

    fun searchTournamentById(id: Long) : Tournament ? {
        tournaments.find { it.id == id }?.let { return it }
        return null
    }

    fun searchTournamentListByIds(ids: MutableList<Long>) : MutableList<Tournament> {
        val foundTournaments = mutableListOf<Tournament>()
        for (id in ids) {
            searchTournamentById(id)?.let { foundTournaments.add(it) }
        }
        return foundTournaments
    }
}