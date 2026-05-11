package com.example.tourney.models

data class NewUserModel(
    val nickname: String,
    val email: String,
    val password: String,
    val photo: Int
)