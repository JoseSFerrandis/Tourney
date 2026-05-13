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
        
        // Exceed max participants
        val user3 = User(3L, "Player3", "p3@test.com", "pass", 0)
        assertFalse(tournament.addParticipant(user3))
    }

    @Test
    fun testRemoveParticipant() {
        tournament.addParticipant(user1)
        tournament.addParticipant(user2)
        
        assertTrue(tournament.removeParticipant(user1))
        assertEquals(1, tournament.numParticipants)
        
        // Remove not added
        val user3 = User(3L, "Player3", "p3@test.com", "pass", 0)
        assertFalse(tournament.removeParticipant(user3))
    }

    @Test
    fun testRemoveParticipantAtPosition() {
        tournament.addParticipant(user1)
        tournament.addParticipant(user2)
        
        tournament.removeParticipantAtPosition(0)
        assertEquals(1, tournament.numParticipants)
        assertEquals(user2.id, tournament.participantList[0].userId)
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

    @Test
    fun testSyncViewFromMatches() {
        val match1 = TournamentMatch(1L, 1L, 0, 1L, 2L, "P1", "P2", "2", "1", null)
        tournament.matches = mutableListOf(match1)
        
        tournament.syncViewFromMatches()
        
        assertEquals(1, tournament.columnMatches.size)
        assertEquals(1, tournament.columnMatches[0].matches.size)
        assertEquals("P1", tournament.columnMatches[0].matches[0].competitorOne.name)
        assertEquals("2", tournament.columnMatches[0].matches[0].competitorOne.score)
    }

    @Test
    fun testUpdateMatchesFromView() {
        tournament.participantList.add(Participant(1L, 1L, "P1", 0f))
        tournament.participantList.add(Participant(2L, 2L, "P2", 0f))
        
        val col = com.ventura.bracketslib.model.ColomnData(mutableListOf(
            com.ventura.bracketslib.model.MatchData(
                CompetitorData("P1", "3"),
                CompetitorData("P2", "2")
            )
        ))
        tournament.columnMatches = mutableListOf(col)
        
        tournament.updateMatchesFromView()
        
        assertEquals(1, tournament.matches.size)
        assertEquals("P1", tournament.matches[0].participantOneName)
        assertEquals("3", tournament.matches[0].scoreOne)
        assertEquals(1L, tournament.matches[0].participantOneId)
    }
}
