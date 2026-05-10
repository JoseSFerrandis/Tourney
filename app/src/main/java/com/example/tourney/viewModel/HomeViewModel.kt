package com.example.tourney.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {
    // Query
    val searchQuery = MutableLiveData<String>("")
    fun updateSearch(query: String){ searchQuery.value = query }

    // Checkboxes
    val searchFilterByNames = MutableLiveData<Boolean>(true)
    fun updateFilterByNames(filterByNames: Boolean){ searchFilterByNames.value = filterByNames }

    val searchFilterByGames = MutableLiveData<Boolean>(true)
    fun updateFilterByGames(filterByGames: Boolean){ searchFilterByGames.value = filterByGames }

    // Buttons
    val searchFilterByElimination = MutableLiveData<Boolean>(true)
    fun updateFilterByElimination(filterByElimination: Boolean){ searchFilterByElimination.value = filterByElimination }

    val searchFilterByLiguilla = MutableLiveData<Boolean>(true)
    fun updateFilterByLiguilla(filterByLiguilla: Boolean){ searchFilterByLiguilla.value = filterByLiguilla }

    val searchFilterBySuizo = MutableLiveData<Boolean>(true)
    fun updateFilterBySuizo(filterBySuizo: Boolean){ searchFilterBySuizo.value = filterBySuizo }
}