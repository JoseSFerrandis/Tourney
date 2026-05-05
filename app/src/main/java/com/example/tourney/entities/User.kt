package com.example.tourney.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
data class User (
    var id: Long,
    var nickname: String,
    var email: String,
    var password: String,
    var photo: Int,
    var showableTournamentList: MutableList<Long> = mutableListOf(),
    var followingTournamentList: MutableList<Long> = mutableListOf(),
    var joinedTournamentList: MutableList<Long> = mutableListOf()
    //var adminTournamentList: MutableList<Long> = mutableListOf()
): Parcelable{
    fun addShowableTournament(id: Long){ showableTournamentList.add(id) }
    fun addFollowingTournament(id: Long){ followingTournamentList.add(id) }
    fun addJoinedTournament(id: Long){ joinedTournamentList.add(id) }
    fun removeShowableTournament(id: Long){ showableTournamentList.remove(id) }
    fun removeFollowingTournament(id: Long){ followingTournamentList.remove(id) }
    fun removeJoinedTournament(id: Long){ joinedTournamentList.remove(id) }
    fun hasShowableTournament(id: Long): Boolean{ return showableTournamentList.contains(id) }
    fun setShowableTournamentList(stringList: String){
        showableTournamentList = stringList.split(",").map { it.trim().toLong() }.toMutableList()
    }


    // Usuario actual
    companion object{
        var actualUser: User? = null
    }
}