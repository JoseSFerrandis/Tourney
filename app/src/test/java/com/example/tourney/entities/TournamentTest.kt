package com.example.tourney.entities

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
import com.ventura.bracketslib.model.ColomnData

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
            maxParticipants = 4,
            date = 0L,
            location = "Online",
            prize = "100",
            code = 1234
        )
        
        user1 = User(1L, "Player1", "p1@test.com", "pass", 0)
        user2 = User(2L, "Player2", "p2@test.com", "pass", 0)
    }

    @Test
    fun testUpdateMatchesFromView() {
        val matchData = MatchData(CompetitorData("P1", "2"), CompetitorData("P2", "1"))
        val column = ColomnData(mutableListOf(matchData))
        tournament.columnMatches = mutableListOf(column)
        
        tournament.updateMatchesFromView()
        
        assertEquals(1, tournament.matches.size)
        val flatMatch = tournament.matches[0]
        assertEquals("P1", flatMatch.participantOneName)
        assertEquals("P2", flatMatch.participantTwoName)
    }

    @Test
    fun testAddParticipant_PreventDuplicates() {
        assertTrue(tournament.addParticipant(user1))
        
        // No debe permitir el mismo ID de usuario (user1 tiene ID 1)
        val userSameId = User(1L, "OtroNick", "e@e.com", "p", 0)
        assertFalse("Should fail due to duplicate ID", tournament.addParticipant(userSameId))
        
        // La lógica actual de la app permite duplicar Nickname SI el userId no es nulo y es diferente.
        // Pero no permite duplicar Nickname SI el userId es nulo.
        val p1 = Participant(nickname = "Gamer", userId = null)
        assertTrue(tournament.addParticipant(p1))
        
        val p2 = Participant(nickname = "Gamer", userId = null)
        assertFalse("Should fail due to duplicate Nickname for null userId", tournament.addParticipant(p2))
        
        assertEquals(2, tournament.numParticipants)
    }

    @Test
    fun testRecalculateNotDead_Elimination() {
        tournament.type = TournamentType.ELIMINATION
        tournament.setStatusFinished(null)
        
        val match = MatchData(CompetitorData("Winner", "5"), CompetitorData("Loser", "2"))
        tournament.columnMatches = mutableListOf(ColomnData(mutableListOf(match)))
        
        tournament.recalculateNotDead()
        
        assertEquals(1, tournament.getNotDead().size)
        assertEquals("Winner", tournament.getNotDead()[0].name)
    }

    @Test
    fun testStatusUpdates() {
        tournament.setStatusInProgress(null)
        assertEquals(TournamentStatus.IN_PROGRESS, tournament.tournamentStatus)
        
        tournament.setStatusFinished(null)
        assertEquals(TournamentStatus.FINISHED, tournament.tournamentStatus)
    }
}
