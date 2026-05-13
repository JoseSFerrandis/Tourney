package com.example.tourney.adapters

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentType
import com.example.tourney.viewModel.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.mockito.Mockito.doNothing
import org.mockito.MockitoAnnotations
import com.example.tourney.R

class TournamentAdapterTest {

    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockViewModel: HomeViewModel

    private lateinit var adapter: TournamentAdapter
    private lateinit var tournamentList: MutableList<Tournament>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Setup mock LiveData for ViewModel
        `when`(mockViewModel.searchQuery).thenReturn(MutableLiveData(""))
        `when`(mockViewModel.searchFilterByNames).thenReturn(MutableLiveData(true))
        `when`(mockViewModel.searchFilterByGames).thenReturn(MutableLiveData(true))
        `when`(mockViewModel.searchFilterByElimination).thenReturn(MutableLiveData(true))
        `when`(mockViewModel.searchFilterByLiguilla).thenReturn(MutableLiveData(true))
        `when`(mockViewModel.searchFilterBySuizo).thenReturn(MutableLiveData(true))
        
        tournamentList = mutableListOf(
            createTestTournament(1L, "Copa 1", "Game A"),
            createTestTournament(2L, "Torneo 2", "Game B")
        )
        adapter = spy(TournamentAdapter(tournamentList) { })
        doNothing().`when`(adapter).notifyDataSetChanged()
    }

    @Test
    fun testGetItemCount() {
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun testFilterTournaments() {
        // Filter by name "Copa"
        `when`(mockViewModel.searchQuery).thenReturn(MutableLiveData("Copa"))
        adapter.filterTournaments(mockViewModel)
        assertEquals(1, adapter.itemCount)
        
        // Clear filter
        `when`(mockViewModel.searchQuery).thenReturn(MutableLiveData(""))
        adapter.filterTournaments(mockViewModel)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun testUpdateTournaments() {
        val newList = listOf(
            createTestTournament(3L, "T3", "G3")
        )
        adapter.updateTournaments(newList)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun testEstablishedValue_Valid() {
        val result = adapter.establishedValue(mockContext, "Valid")
        assertEquals("Valid", result)
    }

    @Test
    fun testEstablishedValue_Empty() {
        `when`(mockContext.getString(R.string.no_established)).thenReturn("N/A")
        val result = adapter.establishedValue(mockContext, "")
        assertEquals("N/A", result)
    }
    
    private fun createTestTournament(id: Long, name: String, game: String): Tournament {
        return Tournament(
            id = id,
            name = name,
            game = game,
            creatorId = 0L,
            creatorNickname = "Creator",
            maxParticipants = 8,
            date = 0L,
            location = "Loc",
            prize = "100",
            code = 123
        )
    }
}
