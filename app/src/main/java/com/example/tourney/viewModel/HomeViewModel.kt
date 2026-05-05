package com.example.tourney.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() { val searchQuery = MutableLiveData<String>("")
    fun updateSearch(query: String){ searchQuery.value = query }
}