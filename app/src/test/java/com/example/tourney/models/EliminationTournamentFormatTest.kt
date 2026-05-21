package com.example.tourney.models

import com.example.tourney.entities.EliminationTournamentFormat
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
import com.ventura.bracketslib.model.CompetitorData
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

        // Se añade un participante fantasma para cuadrar
        assertEquals(4, tournament.getNotDead().size)
        assertEquals("DESCANSO", tournament.getNotDead()[3].name)
    }

    @Test
    fun testNextRound_WinnerProgresses() {
        for (i in 1..2) tournament.addParticipant(User(i.toLong(), "User$i", "e@e.com", "pass", 0))
        format.initMatches(tournament)

        val matches = tournament.columnMatches[0].matches
        matches[0].competitorOne.score = "2"
        matches[0].competitorTwo.score = "1" // User1 gana

        format.nextRound(tournament, null)

        assertEquals(TournamentStatus.FINISHED, tournament.tournamentStatus)
    }
}
