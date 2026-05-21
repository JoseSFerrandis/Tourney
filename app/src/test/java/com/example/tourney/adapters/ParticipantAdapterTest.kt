package com.example.tourney.adapters

import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ParticipantAdapterTest {

    private lateinit var tournament: Tournament
    private lateinit var adapter: ParticipantAdapter
    
    @Mock
    private lateinit var mockRefresh: () -> Unit

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        tournament = Tournament(
            id = 1L,
            name = "Test",
            game = "Test",
            creatorId = 0L,
            creatorNickname = "Test",
            participantList = mutableListOf(),
            maxParticipants = 10,
            date = 0L,
            location = "Test",
            prize = "0",
            code = 0
        )
        // Añadimos algunos participantes con IDs Long
        tournament.addParticipant(User(1L, "User1", "u1", "p", 0))
        tournament.addParticipant(User(2L, "User2", "u2", "p", 0))
        
        adapter = ParticipantAdapter(tournament, mockRefresh)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun testRemoveParticipantAtPosition() {
        val initialSize = tournament.participantList.size
        tournament.removeParticipantAtPosition(0)
        assertEquals(initialSize - 1, tournament.participantList.size)
    }
}
