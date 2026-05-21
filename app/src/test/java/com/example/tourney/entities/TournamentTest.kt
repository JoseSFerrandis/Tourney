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
            creatorId = 0L,
            creatorNickname = "Creator",
            participantList = mutableListOf(),
            maxParticipants = 2,
            date = 0L,
            location = "Online",
            prize = "100",
            code = 1234
        )
        
        user1 = User(1L, "Player1", "p1@test.com", "pass", 0)
        user2 = User(2L, "Player2", "p2@test.com", "pass", 0)
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
        
        val user3 = User(3L, "Player3", "p3@test.com", "pass", 0)
        assertFalse(tournament.addParticipant(user3))
    }

    @Test
    fun testRemoveParticipant() {
        tournament.addParticipant(user1)
        tournament.addParticipant(user2)
        
        assertTrue(tournament.removeParticipant(user1))
        assertEquals(1, tournament.numParticipants)
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
}
