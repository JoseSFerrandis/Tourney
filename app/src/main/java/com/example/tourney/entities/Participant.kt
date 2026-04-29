package com.example.tourney.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Participant(
    var id: Long = 0,        // ID primario en la base de datos (tabla participants)
    var userId: Long? = null, // ID del usuario registrado (null si es offline)
    var nickname: String,     // Nombre que aparecerá en el torneo
    var puntuation: Float? = 0f // Puntuación del usuario en el torneo
) : Parcelable