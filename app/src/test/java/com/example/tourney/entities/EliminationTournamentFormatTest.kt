package com.example.tourney.entities

import android.content.Context
import android.widget.Toast
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class EliminationTournamentFormatTest {

    private lateinit var format: EliminationTournamentFormat
    private lateinit var tournament: Tournament
    
    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockToast: Toast
    private lateinit var mockedToastStatic: MockedStatic<Toast>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockedToastStatic = mockStatic(Toast::class.java)
        mockedToastStatic.`when`<Toast> { 
            Toast.makeText(any(), anyString(), anyInt()) 
        }.thenReturn(mockToast)

        format = EliminationTournamentFormat()
        tournament = Tournament(
            id = 1L,
            name = "Test Tournament",
            game = "Test Game",
            creatorId = 0L,
            creatorNickname = "Creator",
            participantList = mutableListOf(),
            maxParticipants = 8,
            date = 0L,
            location = "Online",
            prize = "100",
            code = 1234,
            type = TournamentType.ELIMINATION
        )
    }

    @After
    fun tearDown() {
        mockedToastStatic.close()
    }

    @Test
    fun testInitMatches_EvenParticipants() {
        for (i in 1..4) {
            tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        }
        format.initMatches(tournament)
        assertEquals(4, tournament.getNotDead().size)
        assertEquals(1, tournament.columnMatches.size)
        assertEquals(2, tournament.columnMatches[0].matches.size)
    }

    @Test
    fun testInitMatches_OddParticipants() {
        for (i in 1..3) {
            tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        }
        format.initMatches(tournament)
        
        // 3 users + 1 DESCANSO = 4
        assertEquals(4, tournament.getNotDead().size)
        assertEquals("DESCANSO", tournament.getNotDead()[3].name)
    }

    @Test
    fun testCreateMatches() {
        val competitors = mutableListOf(
            CompetitorData("C1", "0"),
            CompetitorData("C2", "0"),
            CompetitorData("C3", "0"),
            CompetitorData("C4", "0")
        )
        val matches: MutableList<MatchData> = format.createMatches(competitors)
        assertEquals(2, matches.size)
        assertEquals("C1", matches[0].competitorOne.name)
        assertEquals("C2", matches[0].competitorTwo.name)
    }

    @Test
    fun testRestartMatches() {
        tournament.addParticipant(User(1L, "U1", "e", "p", 0))
        format.initMatches(tournament)
        assertEquals(1, tournament.columnMatches.size)
        
        format.restartMatches(tournament)
        assertEquals(1, tournament.columnMatches.size)
    }

    @Test
    fun testNextRound_NoParticipants() {
        assertFalse("Should return false when no participants", format.nextRound(tournament, mockContext))
    }

    @Test
    fun testNextRound_TieFails() {
        for (i in 1..2) tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)
        
        // Empate
        val match = tournament.columnMatches[0].matches[0]
        match.competitorOne.score = "1"
        match.competitorTwo.score = "1"
        
        assertFalse("Should not allow tie in elimination", format.nextRound(tournament, mockContext))
    }

    @Test
    fun testNextRound_WinnerProgressesToFinish() {
        for (i in 1..2) tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)

        val matches = tournament.columnMatches[0].matches
        matches[0].competitorOne.score = "2"
        matches[0].competitorTwo.score = "1"

        assertTrue(format.nextRound(tournament, mockContext))
        assertEquals(TournamentStatus.FINISHED, tournament.tournamentStatus)
        assertEquals(2, tournament.columnMatches.size)
    }

    @Test
    fun testNextRound_FullProgression() {
        // 4 participants -> 2 rounds
        for (i in 1..4) tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)
        
        // Round 1
        tournament.columnMatches[0].matches[0].competitorOne.score = "1"
        tournament.columnMatches[0].matches[0].competitorTwo.score = "0" // User1 wins
        tournament.columnMatches[0].matches[1].competitorOne.score = "0"
        tournament.columnMatches[0].matches[1].competitorTwo.score = "1" // User4 wins
        
        format.nextRound(tournament, mockContext)
        assertEquals(2, tournament.columnMatches.size)
        assertEquals(1, tournament.columnMatches[1].matches.size)
        assertEquals("User1", tournament.columnMatches[1].matches[0].competitorOne.name)
        assertEquals("User4", tournament.columnMatches[1].matches[0].competitorTwo.name)
    }
}
