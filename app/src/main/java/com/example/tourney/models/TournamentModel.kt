package com.example.tourney.models

import com.example.tourney.entities.Participant
import com.example.tourney.entities.TournamentMatch
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType

data class TournamentModel(
    var id: Long,
    var name: String,
    var game: String,
    var creatorId: Long,
    var creatorNickname: String,
    var participantList: MutableList<Participant> = mutableListOf(),
    var maxParticipants: Int,
    var date: Long? = null,
    var location: String? = null,
    var prize: String? = null,
    var code: Int? = null,
    var type: TournamentType = TournamentType.ELIMINATION,
    var tournamentStatus: TournamentStatus = TournamentStatus.EDITABLE,
    var thumbnail: Int = 0,

    var matches: MutableList<TournamentMatch> = mutableListOf()
)