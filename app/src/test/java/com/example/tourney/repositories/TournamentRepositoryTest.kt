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
        // Reset the list for clean testing state
        repository.setTournaments(mutableListOf())
    }

    @Test
    fun testSingletonInstance() {
        val instance1 = TournamentRepository.getInstance()
        val instance2 = TournamentRepository.getInstance()
        assertSame(instance1, instance2)
    }

    @Test
    fun testAddAndGetTournaments() {
        val tournament = Tournament(1L, "T1", "G1", "C1", mutableListOf(), 2, "D", "L", "P", 111)
        repository.insertTournament(tournament)

        val tournaments = repository.getTournaments()
        assertEquals(1, tournaments.size)
        assertEquals(tournament, tournaments[0])
    }

    @Test
    fun testRemoveTournamentByObject() {
        val tournament = Tournament(1L, "T1", "G1", "C1", mutableListOf(), 2, "D", "L", "P", 111)
        repository.insertTournament(tournament)
        
        repository.removeTournament(tournament)
        assertTrue(repository.getTournaments().isEmpty())
    }

    @Test
    fun testRemoveTournamentById() {
        val tournament = Tournament(2L, "T2", "G2", "C2", mutableListOf(), 2, "D", "L", "P", 222)
        repository.insertTournament(tournament)
        
        repository.removeTournament(2L)
        assertTrue(repository.getTournaments().isEmpty())
    }

    @Test
    fun testSearchTournamentByCode() {
        val tournament = Tournament(3L, "T3", "G3", "C3", mutableListOf(), 2, "D", "L", "P", 333)
        repository.insertTournament(tournament)

        val found = repository.searchTournamentByCode(333)
        assertNotNull(found)
        assertEquals(tournament, found)

        val notFound = repository.searchTournamentByCode(999)
        assertNull(notFound)
    }

    @Test
    fun testSearchTournamentById() {
        val tournament = Tournament(4L, "T4", "G4", "C4", mutableListOf(), 2, "D", "L", "P", 444)
        repository.insertTournament(tournament)

        val found = repository.searchTournamentById(4L)
        assertNotNull(found)
        assertEquals(tournament, found)

        val notFound = repository.searchTournamentById(99L)
        assertNull(notFound)
    }

    @Test
    fun testSearchTournamentListByIds() {
        val t1 = Tournament(5L, "T5", "G5", "C5", mutableListOf(), 2, "D", "L", "P", 555)
        val t2 = Tournament(6L, "T6", "G6", "C6", mutableListOf(), 2, "D", "L", "P", 666)
        
        repository.insertTournament(t1)
        repository.insertTournament(t2)

        val foundList = repository.searchTournamentListByIds(mutableListOf(5L, 6L))
        assertEquals(2, foundList.size)
        assertTrue(foundList.containsAll(listOf(t1, t2)))
    }
}
