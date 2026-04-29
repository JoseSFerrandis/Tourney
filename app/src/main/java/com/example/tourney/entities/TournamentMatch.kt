package com.example.tourney.entities

// C:/Users/Dani/Desktop/Tourney/app/src/main/java/com/example/tourney/entities/TournamentMatch.kt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TournamentMatch(
    var id: Long = 0,
    var tournamentId: Long,
    var roundNumber: Int,
    var participantOneId: Long?, // Referencia al ID de Participante
    var participantTwoId: Long?,
    var participantOneName: String, // Referencia al nombre de Participante
    var participantTwoName: String,
    var scoreOne: String = "0",
    var scoreTwo: String = "0",
    var winnerId: Long? = null
) : Parcelable