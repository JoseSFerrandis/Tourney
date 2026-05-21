package com.example.tourney.repositories

import com.example.tourney.entities.Tournament
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TournamentRepositoryTest {

    private lateinit var repository: TournamentRepository

    @Before
    fun setUp() {
        repository = TournamentRepository.getInstance()
        repository.clear()
    }

    @Test
    fun testSingletonInstance() {
        val instance1 = TournamentRepository.getInstance()
        val instance2 = TournamentRepository.getInstance()
        assertSame(instance1, instance2)
    }

    @Test
    fun testAddAndGetTournaments() {
        val tournament = Tournament(1L, "T1", "G1", 100L, "Admin", mutableListOf(), 16, null, "Online", "100€", 111)
        repository.addTournament(tournament)

        val tournaments = repository.getTournaments()
        assertEquals(1, tournaments.size)
        assertEquals(tournament, tournaments[0])
    }

    @Test
    fun testRemoveTournamentById() {
        val tournament = Tournament(2L, "T2", "G2", 100L, "Admin", mutableListOf(), 16, null, null, null, 222)
        repository.addTournament(tournament)
        
        repository.removeTournament(2L)
        assertTrue(repository.getTournaments().isEmpty())
    }

    @Test
    fun testSearchTournamentByCode() {
        val tournament = Tournament(3L, "T3", "G3", 100L, "Admin", mutableListOf(), 16, null, null, null, 333)
        repository.addTournament(tournament)

        val found = repository.searchTournamentByCode(333)
        assertNotNull(found)
        assertEquals(tournament, found)
    }
}
