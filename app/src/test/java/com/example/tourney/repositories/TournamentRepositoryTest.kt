package com.example.tourney.repositories

import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TournamentRepositoryTest {

    private lateinit var repository: TournamentRepository

    @Before
    fun setUp() {
        repository = TournamentRepository.getInstance()
        // Reset the list for clean testing state using the existing clear() method
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
        // Corrected constructor: id, name, game, creatorId, creatorNickname, participantList, maxParticipants, date, location, prize, code
        val tournament = Tournament(1L, "T1", "G1", 100L, "Admin", mutableListOf(), 16, null, null, null, 111)
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

        val notFound = repository.searchTournamentByCode(999)
        assertNull(notFound)
    }

    @Test
    fun testSearchTournamentById() {
        val tournament = Tournament(4L, "T4", "G4", 100L, "Admin", mutableListOf(), 16, null, null, null, 444)
        repository.addTournament(tournament)

        val found = repository.searchTournamentById(4L)
        assertNotNull(found)
        assertEquals(tournament, found)

        val notFound = repository.searchTournamentById(99L)
        assertNull(notFound)
    }

    @Test
    fun testSearchTournamentListByIds() {
        val t1 = Tournament(5L, "T5", "G5", 100L, "Admin", mutableListOf(), 16, null, null, null, 555)
        val t2 = Tournament(6L, "T6", "G6", 100L, "Admin", mutableListOf(), 16, null, null, null, 666)
        
        repository.addTournament(t1)
        repository.addTournament(t2)

        val foundList = repository.searchTournamentListByIds(listOf(5L, 6L))
        assertEquals(2, foundList.size)
        assertTrue(foundList.contains(t1))
        assertTrue(foundList.contains(t2))
    }
}
