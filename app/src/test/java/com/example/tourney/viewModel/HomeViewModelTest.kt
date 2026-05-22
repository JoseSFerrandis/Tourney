package com.example.tourney.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel()
    }

    @Test
    fun testInitialValues() {
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(true, viewModel.searchFilterByNames.value)
        assertEquals(true, viewModel.searchFilterByGames.value)
        assertEquals(true, viewModel.searchFilterByElimination.value)
        assertEquals(true, viewModel.searchFilterByLiguilla.value)
        assertEquals(true, viewModel.searchFilterBySuizo.value)
    }

    @Test
    fun testUpdateSearch() {
        viewModel.updateSearch("Tournament Name")
        assertEquals("Tournament Name", viewModel.searchQuery.value)
    }

    @Test
    fun testUpdateFilterByNames() {
        viewModel.updateFilterByNames(false)
        assertEquals(false, viewModel.searchFilterByNames.value)
    }

    @Test
    fun testUpdateFilterByGames() {
        viewModel.updateFilterByGames(false)
        assertEquals(false, viewModel.searchFilterByGames.value)
    }

    @Test
    fun testUpdateFilterByElimination() {
        viewModel.updateFilterByElimination(false)
        assertEquals(false, viewModel.searchFilterByElimination.value)
    }

    @Test
    fun testUpdateFilterByLiguilla() {
        viewModel.updateFilterByLiguilla(false)
        assertEquals(false, viewModel.searchFilterByLiguilla.value)
    }

    @Test
    fun testUpdateFilterBySuizo() {
        viewModel.updateFilterBySuizo(false)
        assertEquals(false, viewModel.searchFilterBySuizo.value)
    }
}
