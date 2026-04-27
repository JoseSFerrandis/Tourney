package com.example.tourney.models

import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EliminationTournamentFormatTest {

    private lateinit var format: EliminationTournamentFormat
    private lateinit var tournament: Tournament

    @Before
    fun setUp() {
        format = EliminationTournamentFormat()
        tournament = Tournament(
            id = 1L,
            name = "Test Tournament",
            game = "Test Game",
            creator = "Creator",
            maxParticipants = 8,
            date = "01/01/2026",
            location = "Online",
            prize = "100",
            code = 1234,
            type = TournamentType.ELIMINATION
        )
    }

    @Test
    fun testInitMatches_EvenParticipants() {
        // Add 4 participants
        for (i in 1..4) {
            tournament.addParticipant(User(i, "User$i", "e@e.com", "pass", 0))
        }

        format.initMatches(tournament)

        // Participants should be 4
        assertEquals(4, tournament.getNotDead().size)
        // Should create 1 column
        assertEquals(1, tournament.columnMatches.size)
        // Should create 2 matches in the column
        assertEquals(2, tournament.columnMatches[0].matches.size)
    }

    @Test
    fun testInitMatches_OddParticipants() {
        // Add 3 participants
        for (i in 1..3) {
            tournament.addParticipant(User(i, "User$i", "e@e.com", "pass", 0))
        }

        format.initMatches(tournament)

        // Should add a dummy participant to make it even
        assertEquals(4, tournament.getNotDead().size)
        assertEquals("", tournament.getNotDead()[3].name)
    }

    @Test
    fun testCreateMatches() {
        val competitors = mutableListOf(
            CompetitorData("P1", "0"),
            CompetitorData("P2", "0"),
            CompetitorData("P3", "0"),
            CompetitorData("P4", "0")
        )

        val matches = format.createMatches(competitors)
        assertEquals(2, matches.size)
        assertEquals("P1", matches[0].competitorOne.name)
        assertEquals("P2", matches[0].competitorTwo.name)
        assertEquals("P3", matches[1].competitorOne.name)
        assertEquals("P4", matches[1].competitorTwo.name)
    }

    @Test
    fun testRestartMatches() {
        for (i in 1..2) tournament.addParticipant(User(i, "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)
        assertEquals(1, tournament.columnMatches.size)

        format.restartMatches(tournament)
        assertEquals(1, tournament.columnMatches.size)
    }

    @Test
    fun testNextRound_NoParticipants() {
        assertFalse(format.nextRound(tournament, null))
    }

    @Test
    fun testNextRound_HasParticipants_WinnerProgresses() {
        for (i in 1..4) tournament.addParticipant(User(i, "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)

        // Set scores
        val matches = tournament.columnMatches[0].matches
        matches[0].competitorOne.score = "2"
        matches[0].competitorTwo.score = "1" // P1 wins

        matches[1].competitorOne.score = "0"
        matches[1].competitorTwo.score = "3" // P4 wins

        assertTrue(format.nextRound(tournament, null))

        // Should have 2 columns now
        assertEquals(2, tournament.columnMatches.size)

        // Second round should have 1 match
        val nextRoundMatches = tournament.columnMatches[1].matches
        assertEquals(1, nextRoundMatches.size)

        // Winners should be P1 and P4
        assertEquals("User1", nextRoundMatches[0].competitorOne.name)
        assertEquals("User4", nextRoundMatches[0].competitorTwo.name)
    }

    @Test
    fun testNextRound_TieFails() {
        for (i in 1..4) tournament.addParticipant(User(i, "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)

        val matches = tournament.columnMatches[0].matches
        matches[0].competitorOne.score = "1"
        matches[0].competitorTwo.score = "1" // Tie

        // Tie should return false
        assertFalse(format.nextRound(tournament, null))
    }

    @Test
    fun testNextRound_FinishesTournament() {
        for (i in 1..2) tournament.addParticipant(User(i, "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)

        val matches = tournament.columnMatches[0].matches
        matches[0].competitorOne.score = "1"
        matches[0].competitorTwo.score = "0" // P1 wins

        format.nextRound(tournament, null)

        // Only 1 winner remains, so tournament should be finished
        assertEquals(TournamentStatus.FINISHED, tournament.tournamentStatus)
    }
}
