package com.example.tourney.adapters

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.tourney.R
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentType
import com.example.tourney.viewModel.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations

class TournamentAdapterTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var viewModel: HomeViewModel

    // Mocks para los LiveData del ViewModel
    @Mock private lateinit var mockSearchQuery: MutableLiveData<String>
    @Mock private lateinit var mockFilterNames: MutableLiveData<Boolean>
    @Mock private lateinit var mockFilterGames: MutableLiveData<Boolean>
    @Mock private lateinit var mockFilterElimination: MutableLiveData<Boolean>
    @Mock private lateinit var mockFilterLiguilla: MutableLiveData<Boolean>
    @Mock private lateinit var mockFilterSuizo: MutableLiveData<Boolean>

    private lateinit var adapter: TournamentAdapter
    private lateinit var tournamentList: MutableList<Tournament>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        tournamentList = mutableListOf(
            Tournament(1L, "Copa Verano", "League of Legends", 0, "Admin", mutableListOf(), 8, 0, "Online", "100€", 111, TournamentType.ELIMINATION),
            Tournament(2L, "Liguilla Pro", "VALORANT", 0, "User1", mutableListOf(), 16, 0, "Presencial", "500€", 222, TournamentType.LIGUILLA),
            Tournament(3L, "Torneo Suizo", "Chess", 0, "Master", mutableListOf(), 32, 0, "Club", "Libro", 333, TournamentType.SUIZO)
        )
        
        adapter = spy(TournamentAdapter(tournamentList) { })
        doNothing().`when`(adapter).notifyDataSetChanged()

        // Vinculamos los mocks de LiveData al ViewModel mockeado
        `when`(viewModel.searchQuery).thenReturn(mockSearchQuery)
        `when`(viewModel.searchFilterByNames).thenReturn(mockFilterNames)
        `when`(viewModel.searchFilterByGames).thenReturn(mockFilterGames)
        `when`(viewModel.searchFilterByElimination).thenReturn(mockFilterElimination)
        `when`(viewModel.searchFilterByLiguilla).thenReturn(mockFilterLiguilla)
        `when`(viewModel.searchFilterBySuizo).thenReturn(mockFilterSuizo)
        
        // Valores por defecto para evitar NullPointerException con el operador !!
        setupDefaultFilters(true, true, true, true, true)
    }

    private fun setupDefaultFilters(names: Boolean, games: Boolean, elim: Boolean, lig: Boolean, suiz: Boolean) {
        `when`(mockFilterNames.value).thenReturn(names)
        `when`(mockFilterGames.value).thenReturn(games)
        `when`(mockFilterElimination.value).thenReturn(elim)
        `when`(mockFilterLiguilla.value).thenReturn(lig)
        `when`(mockFilterSuizo.value).thenReturn(suiz)
        `when`(mockSearchQuery.value).thenReturn("")
    }

    @Test
    fun testGetItemCount() {
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun testFilterTournaments_ByName() {
        `when`(mockSearchQuery.value).thenReturn("Copa")
        setupDefaultFilters(names = true, games = false, elim = true, lig = true, suiz = true)
        
        adapter.filterTournaments(viewModel)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun testFilterTournaments_ByGame() {
        `when`(mockSearchQuery.value).thenReturn("VALORANT")
        setupDefaultFilters(names = false, games = true, elim = true, lig = true, suiz = true)
        
        adapter.filterTournaments(viewModel)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun testFilterTournaments_ByType_OnlySuizo() {
        setupDefaultFilters(names = true, games = true, elim = false, lig = false, suiz = true)
        
        adapter.filterTournaments(viewModel)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun testUpdateTournaments() {
        val newList = listOf(
            Tournament(4L, "New", "Game", 0, "C", mutableListOf(), 4, 0, "L", "P", 999)
        )
        adapter.updateTournaments(newList)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun testEstablishedValue_Valid() {
        val result = adapter.establishedValue(mockContext, "Nombre")
        assertEquals("Nombre", result)
    }

    @Test
    fun testEstablishedValue_NullOrEmpty() {
        `when`(mockContext.getString(R.string.no_established)).thenReturn("No establecido")
        
        assertEquals("No establecido", adapter.establishedValue(mockContext, null))
        assertEquals("No establecido", adapter.establishedValue(mockContext, ""))
        assertEquals("No establecido", adapter.establishedValue(mockContext, "null"))
    }
}
