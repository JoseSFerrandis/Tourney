package com.example.models

import com.example.tourney.models.UserModel

data class LoginResponse (
    val token: String,
    val user: UserModel
)