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
        val tournament = createTestTournament(1L, 111)
        repository.addTournament(tournament)

        val tournaments = repository.getTournaments()
        assertEquals(1, tournaments.size)
        assertEquals(tournament.id, tournaments[0].id)
    }

    @Test
    fun testRemoveTournamentById() {
        val tournament = createTestTournament(2L, 222)
        repository.addTournament(tournament)
        
        repository.removeTournament(2L)
        assertTrue(repository.getTournaments().isEmpty())
    }

    @Test
    fun testSearchTournamentByCode() {
        val tournament = createTestTournament(3L, 333)
        repository.addTournament(tournament)

        val found = repository.searchTournamentByCode(333)
        assertNotNull(found)
        assertEquals(tournament.id, found?.id)

        val notFound = repository.searchTournamentByCode(999)
        assertNull(notFound)
    }

    @Test
    fun testSearchTournamentById() {
        val tournament = createTestTournament(4L, 444)
        repository.addTournament(tournament)

        val found = repository.searchTournamentById(4L)
        assertNotNull(found)
        assertEquals(tournament.id, found?.id)

        val notFound = repository.searchTournamentById(99L)
        assertNull(notFound)
    }

    @Test
    fun testSearchTournamentListByIds() {
        val t1 = createTestTournament(5L, 555)
        val t2 = createTestTournament(6L, 666)
        
        repository.addTournament(t1)
        repository.addTournament(t2)

        val foundList = repository.searchTournamentListByIds(listOf(5L, 6L))
        assertEquals(2, foundList.size)
        assertTrue(foundList.any { it.id == 5L })
        assertTrue(foundList.any { it.id == 6L })
    }

    private fun createTestTournament(id: Long, code: Int): Tournament {
        return Tournament(
            id = id,
            name = "Tournament $id",
            game = "Game",
            creatorId = 1L,
            creatorNickname = "Creator",
            maxParticipants = 8,
            date = System.currentTimeMillis(),
            location = "Location",
            prize = "Prize",
            code = code,
            type = TournamentType.ELIMINATION
        )
    }
}
