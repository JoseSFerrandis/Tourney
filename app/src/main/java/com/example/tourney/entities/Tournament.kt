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
    var creator: String,
    var participantList: MutableList<User> = mutableListOf(),
    var maxParticipants: Int,
    var date: String,
    var location: String,
    var prize: String,
    var code: Int,
    var type: TournamentType = TournamentType.ELIMINATION,
    var tournamentStatus: TournamentStatus = TournamentStatus.EDITABLE,

    private var matches: MutableList<TournamentMatch> = mutableListOf(),
    private var competitors: MutableList<CompetitorData> = mutableListOf()
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

    /**
     * Inicializa la lista de columnas con el primer emparejamiento
     * Si ya se ha inicializado, no hace nada
     */
    fun initMatches() = getFormat().initMatches(this)


    /**
     * Reinicia la lista de columnas con el primer emparejamiento
     */
    fun restartMatches() = getFormat().restartMatches(this)

    /**
     * Ejecuta el round actual del torneo, actualiza la lista de columnas y crea el siguiente emparejamiento
     * @return true si se ha ejecutado correctamente, false en caso contrario
     */
    fun nextRound(context: Context?) : Boolean = getFormat().nextRound(this, context)

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
