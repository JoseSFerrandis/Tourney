package com.example.tourney.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tournament(
    var id: Int,
    var name: String,
    var game: String,
    var creator: String,
    var participantList: MutableList<User> = mutableListOf(),
    var maxParticipants: Int,
    var date: String,
    var location: String,
    var status: String,
    var prize: String,
    var code: Int
) : Parcelable {
    val numParticipants: Int
        get() = participantList.size
}
