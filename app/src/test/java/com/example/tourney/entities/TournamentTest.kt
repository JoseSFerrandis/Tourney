package com.example.tourney.entities

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.ventura.bracketslib.model.CompetitorData

class TournamentTest {

    private lateinit var tournament: Tournament
    private lateinit var user1: User
    private lateinit var user2: User

    @Before
    fun setUp() {
        tournament = Tournament(
            id = 1L,
            name = "Test Tournament",
            game = "Test Game",
            creatorId = "Creator",
            maxParticipants = 2,
            date = "01/01/2026",
            location = "Online",
            prize = "100",
            code = 1234
        )
        
        user1 = User(1, "Player1", "p1@test.com", "pass", 0)
        user2 = User(2, "Player2", "p2@test.com", "pass", 0)
    }

    @Test
    fun testHasSpace() {
        assertTrue(tournament.hasSpace())
        tournament.addParticipant(user1)
        assertTrue(tournament.hasSpace())
        tournament.addParticipant(user2)
        assertFalse(tournament.hasSpace())
    }

    @Test
    fun testAddParticipant() {
        assertTrue(tournament.addParticipant(user1))
        assertEquals(1, tournament.numParticipants)
        
        assertTrue(tournament.addParticipant(user2))
        assertEquals(2, tournament.numParticipants)
        
        // Exceed max participants
        val user3 = User(3, "Player3", "p3@test.com", "pass", 0)
        assertFalse(tournament.addParticipant(user3))
    }

    @Test
    fun testRemoveParticipant() {
        tournament.addParticipant(user1)
        tournament.addParticipant(user2)
        
        assertTrue(tournament.removeParticipant(user1))
        assertEquals(1, tournament.numParticipants)
        
        // Remove not added
        val user3 = User(3, "Player3", "p3@test.com", "pass", 0)
        assertFalse(tournament.removeParticipant(user3))
    }

    @Test
    fun testRemoveParticipantAtPosition() {
        tournament.addParticipant(user1)
        tournament.addParticipant(user2)
        
        tournament.removeParticipantAtPosition(0)
        assertEquals(1, tournament.numParticipants)
        assertEquals(user2, tournament.participantList[0])
    }

    @Test
    fun testShuffleParticipants() {
        tournament.addParticipant(user1)
        tournament.addParticipant(user2)
        
        // Editable state allows shuffle
        tournament.setStatusEditable()
        assertTrue(tournament.shuffleParticipants())
        
        // Finished state does not allow shuffle
        tournament.setStatusFinished(null)
        assertFalse(tournament.shuffleParticipants())
    }

    @Test
    fun testStatusUpdates() {
        tournament.setStatusEditable()
        assertEquals(TournamentStatus.EDITABLE, tournament.tournamentStatus)
        
        tournament.setStatusInProgress(null)
        assertEquals(TournamentStatus.IN_PROGRESS, tournament.tournamentStatus)
        
        tournament.setStatusFinished(null)
        assertEquals(TournamentStatus.FINISHED, tournament.tournamentStatus)
    }

    @Test
    fun testTournamentTypeStringConversion() {
        assertEquals("Eliminación", Tournament.getTournamentTypeString(TournamentType.ELIMINATION))
        assertEquals("Liguilla", Tournament.getTournamentTypeString(TournamentType.LIGUILLA))
        assertEquals("Suizo", Tournament.getTournamentTypeString(TournamentType.SUIZO))
        assertEquals("Otro", Tournament.getTournamentTypeString(TournamentType.OTRO))
        
        assertEquals(TournamentType.ELIMINATION, Tournament.getTournamentTypeFromString("Eliminación"))
        assertEquals(TournamentType.LIGUILLA, Tournament.getTournamentTypeFromString("Liguilla"))
        assertEquals(TournamentType.SUIZO, Tournament.getTournamentTypeFromString("Suizo"))
        assertEquals(TournamentType.OTRO, Tournament.getTournamentTypeFromString("Cualquier otra cosa"))
    }
    
    @Test
    fun testNotDead() {
        val notDeadList = mutableListOf(CompetitorData("1", "1"))
        tournament.setNotDead(notDeadList)
        assertEquals(1, tournament.getNotDead().size)
        assertEquals(notDeadList, tournament.getNotDead())
    }
}
