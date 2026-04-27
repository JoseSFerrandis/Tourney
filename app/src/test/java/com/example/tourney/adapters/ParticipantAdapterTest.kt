package com.example.tourney.adapters

import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
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
            creator = "Test",
            maxParticipants = 10,
            date = "2023",
            location = "Test",
            prize = "0",
            code = 0
        )
        // Añadimos algunos participantes
        tournament.addParticipant(User(1, "User1", "u1", "p", 0))
        tournament.addParticipant(User(2, "User2", "u2", "p", 0))
        
        adapter = ParticipantAdapter(tournament, mockRefresh)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun testRemoveParticipantAtPosition() {
        // En un unit test puro no podemos testear notifyItemRemoved fácilmente sin mockear el RecyclerView,
        // pero podemos testear que la lógica de remover del torneo se dispara.
        
        // Simulamos lo que haría el click listener si pudiéramos llamarlo
        val initialSize = tournament.participantList.size
        tournament.removeParticipantAtPosition(0)
        
        assertEquals(initialSize - 1, tournament.participantList.size)
    }
}
