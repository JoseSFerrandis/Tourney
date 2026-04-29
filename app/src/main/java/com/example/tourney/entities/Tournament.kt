package com.example.tourney.entities

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import com.example.tourney.models.EliminationTournamentFormat
import com.example.tourney.models.LiguillaTournamentFormat
import com.example.tourney.models.SuizoTournamentFormat
import com.example.tourney.models.TournamentFormat
import com.ventura.bracketslib.model.ColomnData
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

enum class TournamentStatus {
    EDITABLE,
    FINISHED,
    IN_PROGRESS
}

enum class TournamentType {
    ELIMINATION,
    LIGUILLA,
    SUIZO,
    OTRO
}

@Parcelize
data class Tournament(
    var id: Long,
    var name: String,
    var game: String,
    var creatorId: Long,
    var creatorNickname: String,
    var participantList: MutableList<Participant> = mutableListOf(),
    var maxParticipants: Int,
    var date: Long?,
    var location: String,
    var prize: String,
    var code: Int,
    var type: TournamentType = TournamentType.ELIMINATION,
    var tournamentStatus: TournamentStatus = TournamentStatus.EDITABLE,

    var matches: MutableList<TournamentMatch> = mutableListOf()
) : Parcelable {
    @IgnoredOnParcel
    var columnMatches: MutableList<ColomnData> = mutableListOf()
    @IgnoredOnParcel
    private var notDead: MutableList<CompetitorData> = mutableListOf()

    val numParticipants: Int
        get() = participantList.size

    /**
     * Selecciona el formato de torneo correspondiente
     * @return TournamentFormat correspondiente al tipo de torneo
     */
    private fun getFormat(): TournamentFormat {
        return when (type) {
            TournamentType.ELIMINATION -> EliminationTournamentFormat()
            TournamentType.LIGUILLA -> LiguillaTournamentFormat()
            TournamentType.SUIZO -> SuizoTournamentFormat()
            else -> throw IllegalArgumentException("Tipo de torneo no soportado: $type")
        }
    }

    /**
     * Comprueba si hay espacio para más participantes
     * @return true si hay espacio, false en caso contrario
     */
    fun hasSpace(): Boolean { return numParticipants < maxParticipants }

    fun addParticipant(participant: Participant): Boolean {
        return tryAddParticipant(participant)
    }
    fun addParticipant(user: User): Boolean {
        return addParticipant(Participant(userId = user.id, nickname = user.nickname))
    }
    private fun tryAddParticipant(participant: Participant): Boolean {
        if(!hasSpace()) return false

        if(participant.userId == null)
            participantList.forEach {
                if(it.nickname == participant.nickname)
                    return false
            }
        else
            participantList.forEach {
                if(it.userId == participant.userId)
                    return false
            }

        participantList.add(participant)
        return true
    }

    fun removeParticipant(user: User): Boolean {
        val participant = participantList.find { it.userId == user.id }
        return removeParticipant(participant ?: return false)
    }
    fun removeParticipant(participant: Participant): Boolean {
        if (participantList.contains(participant)) {
            participantList.remove(participant)
            return true
        }
        return false
    }

    /**
     * Reconstruye la estructura visual del torneo (columnMatches)
     * a partir de la lista plana de matches cargada de la base de datos.
     */
    fun syncViewFromMatches() {
        if (matches.isEmpty()) return

        val matchesByRound = matches.groupBy { it.roundNumber }
        val newColumns = mutableListOf<ColomnData>()
        val sortedRounds = matchesByRound.keys.sorted()

        sortedRounds.forEach { roundNum ->
            val roundMatches = matchesByRound[roundNum] ?: return@forEach
            val matchDataList = mutableListOf<MatchData>()

            roundMatches.forEach { m ->
                matchDataList.add(MatchData(
                    CompetitorData(m.participantOneName, m.scoreOne),
                    CompetitorData(m.participantTwoName, m.scoreTwo)
                ))
            }
            newColumns.add(ColomnData(matchDataList))
        }

        this.columnMatches = newColumns

        // Reconstruimos la lista de competidores activos
        recalculateNotDead()
    }

    /**
     * Sincroniza los cambios de la UI (columnMatches) hacia la lista plana (matches)
     * para que el DAO pueda guardarlos en la base de datos.
     */
    fun updateMatchesFromView() {
        val flatMatches = mutableListOf<TournamentMatch>()
        columnMatches.forEachIndexed { roundIndex, column ->
            column.matches.forEach { m ->
                flatMatches.add(TournamentMatch(
                    tournamentId = this.id,
                    roundNumber = roundIndex,
                    participantOneName = m.competitorOne.name,
                    participantTwoName = m.competitorTwo.name,
                    scoreOne = m.competitorOne.score,
                    scoreTwo = m.competitorTwo.score,
                    participantOneId = participantList.find { it.nickname == m.competitorOne.name }?.userId,
                    participantTwoId = participantList.find { it.nickname == m.competitorTwo.name }?.userId
                ))
            }
        }
        this.matches = flatMatches
    }

    /**
     * Determina quiénes siguen activos basándose en el tipo de torneo y los resultados
     */
    fun recalculateNotDead() {
        val lastColumn = columnMatches.lastOrNull()
        if (type == TournamentType.ELIMINATION) {
            recalculateNotDeadFromLastRound()
        } else {
            val competitors = participantList.map { p ->
                val score = lastColumn?.matches?.find { it.competitorOne.name == p.nickname || it.competitorTwo.name == p.nickname }?.let { m ->
                    if (m.competitorOne.name == p.nickname) m.competitorOne.score else m.competitorTwo.score
                } ?: "0"
                CompetitorData(p.nickname, score)
            }.toMutableList()
            if (competitors.size % 2 != 0) {
                competitors.add(CompetitorData("DESCANSO", ""))
            }
            this.setNotDead(competitors)
        }
    }

    /**
     * Determina quiénes siguen vivos basándose en los resultados de la última ronda (Eliminación)
     */
    private fun recalculateNotDeadFromLastRound() {
        val lastColumn = columnMatches.lastOrNull() ?: return
        val winners = mutableListOf<CompetitorData>()

        if (tournamentStatus == TournamentStatus.FINISHED) {
            lastColumn.matches.forEach { match ->
                val s1 = match.competitorOne.score.toFloatOrNull() ?: Float.NEGATIVE_INFINITY
                val s2 = match.competitorTwo.score.toFloatOrNull() ?: Float.NEGATIVE_INFINITY
                if (s1 > s2) winners.add(CompetitorData(match.competitorOne.name, match.competitorOne.score))
                else if (s2 > s1) winners.add(CompetitorData(match.competitorTwo.name, match.competitorTwo.score))
            }
        } else {
            lastColumn.matches.forEach { match ->
                winners.add(CompetitorData(match.competitorOne.name, match.competitorOne.score))
                winners.add(CompetitorData(match.competitorTwo.name, match.competitorTwo.score))
            }
        }
        this.setNotDead(winners)
    }

    fun initMatches() {
        if (columnMatches.isEmpty() && matches.isNotEmpty()) {
            syncViewFromMatches()
        }
        if (columnMatches.isEmpty()) {
            getFormat().initMatches(this)
            updateMatchesFromView()
        }
    }

    fun restartMatches() {
        columnMatches.clear()
        matches.clear()
        initMatches()
    }

    fun nextRound(context: Context?) : Boolean {
        updateMatchesFromView()
        val success = getFormat().nextRound(this, context)
        updateMatchesFromView()
        return success
    }

    fun getLastMatchList() : MutableList<MatchData> = getFormat().getLastMatchList(this)

    fun getNotDead() : MutableList<CompetitorData>{ return notDead }
    fun setNotDead(notDead: MutableList<CompetitorData>){ this.notDead = notDead }

    fun setStatusEditable(){ tournamentStatus = TournamentStatus.EDITABLE }
    fun setStatusInProgress(context: Context?){
        tournamentStatus = TournamentStatus.IN_PROGRESS
        if(context != null)
            Toast.makeText(context, "Torneo iniciado", Toast.LENGTH_SHORT).show()
    }
    fun setStatusFinished(context: Context?){
        tournamentStatus = TournamentStatus.FINISHED
        if(context != null)
            Toast.makeText(context, "Torneo finalizado", Toast.LENGTH_SHORT).show()
    }

    fun removeParticipantAtPosition(position: Int){
        participantList.removeAt(position)
        if(tournamentStatus == TournamentStatus.EDITABLE){
            restartMatches()
        }
    }

    fun shuffleParticipants(): Boolean{
        if(tournamentStatus == TournamentStatus.EDITABLE){
            participantList.shuffle()
            restartMatches()
            return true
        } else {
            return false
        }
    }

    companion object{
        fun getTournamentTypeString(type: TournamentType): String {
            return when (type) {
                TournamentType.ELIMINATION -> "Eliminación"
                TournamentType.LIGUILLA -> "Liguilla"
                TournamentType.SUIZO -> "Suizo"
                else -> "Otro"
            }
        }

        fun getTournamentTypeFromString(type: String): TournamentType {
            return when (type) {
                "Eliminación" -> TournamentType.ELIMINATION
                "Liguilla" -> TournamentType.LIGUILLA
                "Suizo" -> TournamentType.SUIZO
                else -> TournamentType.OTRO
            }
        }
    }
}
