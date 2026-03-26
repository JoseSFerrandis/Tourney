package com.example.tourney.entities

import android.os.Parcelable
import com.ventura.bracketslib.model.ColomnData
import kotlinx.parcelize.Parcelize

enum class TournamentStatus {
    EDITABLE,
    FINISHED,
    IN_PROGRESS
}

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
    var code: Int,
    var tournamentStatus: TournamentStatus = TournamentStatus.EDITABLE,
    var matches: MutableList<ColomnData> = mutableListOf()
) : Parcelable {
    val numParticipants: Int
        get() = participantList.size



    fun hasSpace(): Boolean {
        return numParticipants < maxParticipants
    }

    fun addParticipant(user: User): Boolean {
        if (hasSpace()) {
            participantList.add(user)
            return true
        }
        return false
    }
    fun removeParticipant(user: User): Boolean {
        if (participantList.contains(user)) {
            participantList.remove(user)
            return true
        }
        return false
    }

}
