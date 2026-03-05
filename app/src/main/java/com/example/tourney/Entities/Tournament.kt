package com.example.tournamentapp.models

data class Tournament(
    val id: Int,
    val name: String,
    val game: String,
    val participants: Int,
    val maxParticipants: Int,
    val date: String,
    val location: String,
    val status: String,
    val prize: String
)
