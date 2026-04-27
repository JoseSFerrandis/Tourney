package com.example.tourney.adapters

import android.content.Context
import com.example.tourney.entities.Tournament
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

    private lateinit var adapter: TournamentAdapter
    private lateinit var tournamentList: MutableList<Tournament>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        tournamentList = mutableListOf(
            Tournament(1L, "Copa 1", "Game A", "Creator", mutableListOf(), 8, "Date", "Loc", "100", 123),
            Tournament(2L, "Torneo 2", "Game B", "Creator", mutableListOf(), 8, "Date", "Loc", "200", 456)
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
        adapter.filterTournaments("Copa")
        assertEquals(1, adapter.itemCount)
        
        adapter.filterTournaments("")
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun testUpdateTournaments() {
        val newList = listOf(
            Tournament(3L, "T3", "G3", "C3", mutableListOf(), 4, "D3", "L3", "300", 789)
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
}
