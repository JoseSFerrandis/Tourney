package com.example.tourney.repositories

import android.content.Context
import com.example.tourney.entities.Tournament
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class TournamentRepositoryTest {

    private lateinit var repository: TournamentRepository
    private val mockContext = mock(Context::class.java)

    @Before
    fun setUp() {
        repository = TournamentRepository.getInstance(mockContext)
        repository.clear()
    }

    @Test
    fun testSingletonInstance() {
        val instance1 = TournamentRepository.getInstance(mockContext)
        val instance2 = TournamentRepository.getInstance(mockContext)
        assertSame(instance1, instance2)
    }

    @Test
    fun testAddAndGetTournaments() {
        val tournament = Tournament(1L, "T1", "G1", 100L, "Admin", mutableListOf(), 16, null, "Online", "100€", 111)
        repository.addTournamentsToMemory(mutableListOf(tournament))

        val tournaments = repository.getTournaments()
        assertEquals(1, tournaments.size)
        assertEquals(tournament, tournaments[0])
    }

    @Test
    fun testAddTournamentAvoidsDuplicates() {
        val t1 = Tournament(1L, "T1", "G1", 100L, "Admin", mutableListOf(), 16, null, null, null, 111)
        val t1Duplicate = Tournament(1L, "T1 Duplicate", "G1", 100L, "Admin", mutableListOf(), 16, null, null, null, 111)
        
        repository.addTournamentsToMemory(mutableListOf(t1))
        repository.addTournamentsToMemory(mutableListOf(t1Duplicate))
        
        assertEquals(1, repository.getTournaments().size)
        assertEquals("T1", repository.getTournaments()[0].name)
    }

    @Test
    fun testUpdateTournamentInList() {
        val original = Tournament(1L, "Original", "G1", 100L, "Admin", mutableListOf(), 16, null, null, null, 111)
        repository.addTournamentsToMemory(mutableListOf(original))
        
        val updated = Tournament(1L, "Updated Name", "G1", 100L, "Admin", mutableListOf(), 16, null, null, null, 111)
        repository.updateTournamentInList(updated)
        
        val found = repository.searchTournamentById(1L)
        assertEquals("Updated Name", found?.name)
    }

    @Test
    fun testRemoveTournamentById() {
        val tournament = Tournament(2L, "T2", "G2", 100L, "Admin", mutableListOf(), 16, null, null, null, 222)
        repository.addTournamentsToMemory(mutableListOf(tournament))
        
        repository.removeTournament(2L)
        assertTrue(repository.getTournaments().isEmpty())
    }

    @Test
    fun testSearchTournamentByCode() {
        val tournament = Tournament(3L, "T3", "G3", 100L, "Admin", mutableListOf(), 16, null, null, null, 333)
        repository.addTournamentsToMemory(mutableListOf(tournament))

        val found = repository.searchTournamentByCode(333)
        assertNotNull(found)
        assertEquals(tournament, found)
        
        assertNull(repository.searchTournamentByCode(999))
    }

    @Test
    fun testSearchTournamentById() {
        val tournament = Tournament(4L, "T4", "G4", 100L, "Admin", mutableListOf(), 16, null, null, null, 444)
        repository.addTournamentsToMemory(mutableListOf(tournament))

        val found = repository.searchTournamentById(4L)
        assertNotNull(found)
        assertEquals("T4", found?.name)
        
        assertNull(repository.searchTournamentById(99L))
    }

    @Test
    fun testSearchTournamentListByIds() {
        val t1 = Tournament(5L, "T5", "G5", 100L, "Admin", mutableListOf(), 16, null, null, null, 555)
        val t2 = Tournament(6L, "T6", "G6", 100L, "Admin", mutableListOf(), 16, null, null, null, 666)
        repository.addTournamentsToMemory(mutableListOf(t1))
        repository.addTournamentsToMemory(mutableListOf(t2))

        val foundList = repository.searchTournamentListByIds(listOf(5L, 6L, 99L))
        assertEquals(2, foundList.size)
        assertTrue(foundList.any { it.id == 5L })
        assertTrue(foundList.any { it.id == 6L })
    }

    @Test
    fun testClear() {
        repository.addTournamentsToMemory(mutableListOf(Tournament(10L, "T10", "G10", 100L, "Admin", mutableListOf(), 16, null, null, null, 1010)))
        assertFalse(repository.getTournaments().isEmpty())
        
        repository.clear()
        assertTrue(repository.getTournaments().isEmpty())
    }
}
