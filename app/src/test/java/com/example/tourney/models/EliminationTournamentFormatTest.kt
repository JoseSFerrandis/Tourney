package com.example.tourney.models

import android.content.Context
import android.widget.Toast
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
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
    fun testNextRound_WinnerProgresses() {
        for (i in 1..2) tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)

        val matches = tournament.columnMatches[0].matches
        matches[0].competitorOne.score = "2"
        matches[0].competitorTwo.score = "1"

        format.nextRound(tournament, mockContext)
        assertEquals(TournamentStatus.FINISHED, tournament.tournamentStatus)
    }
}
