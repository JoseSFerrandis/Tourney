package com.example.tourney.entities

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.tourney.MainActivity
import com.ventura.bracketslib.model.ColomnData
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
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
    var prize: String,
    var code: Int,
    var tournamentStatus: TournamentStatus = TournamentStatus.EDITABLE,
    var columnMatches: MutableList<ColomnData> = mutableListOf(),
    private var notDead: MutableList<CompetitorData> = mutableListOf()
) : Parcelable {
    val numParticipants: Int
        get() = participantList.size

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
    fun initMatches(){
        // Inicializa la lista de columnas con el primer emparejamiento
        if(columnMatches.isEmpty()) {
            notDead = getCompetitorList(participantList)

            // Evita desajuste por el número de participantes impar
            if(notDead.size%2 != 0)
                notDead.add(CompetitorData("", ""))

            columnMatches.add(createColumn(createMatches(notDead)))
        }
    }

    /**
     * Reinicia la lista de columnas con el primer emparejamiento
     */
    fun restartMatches(){
        columnMatches.clear()
        initMatches()
    }

    fun nextRound(context: Context?) : Boolean{
        if(notDead.isNotEmpty() && tournamentStatus != TournamentStatus.FINISHED){
            // Lista de partidos de la ronda actual (la última guardada)
            val lastMatches = getLastMatchList()
            val winners = mutableListOf<CompetitorData>()

            for (match in lastMatches) {
                // Lógica para decidir quién pasa (ejemplo: el que tenga más puntuación)
                //val score1 = match.competitorOne.score.toIntOrNull() ?: -1
                val score1 = match.competitorOne.score.toFloatOrNull() ?: -Float.MIN_VALUE
                //val score2 = match.competitorTwo.score.toIntOrNull() ?: -
                val score2 = match.competitorTwo.score.toFloatOrNull() ?: Float.MIN_VALUE

                val winner =
                    if (score1 > score2)
                        match.competitorOne
                    else if(score1 < score2)
                        match.competitorTwo
                    else { // Empate
                        Toast.makeText(context, "No puede haber empate en el torneo", Toast.LENGTH_SHORT).show()
                        Toast.makeText(context, "Empate en ${match.competitorOne.name}: ${match.competitorOne.score} y ${match.competitorTwo.name}: ${match.competitorTwo.score}", Toast.LENGTH_SHORT).show()
                        return false
                    }

                // Creamos una instancia nueva para la siguiente ronda.
                // Esto equivale al 'new CompetitorData' de Java.
                // Si la librería no tiene .copy(), usa el constructor:
                winners.add(CompetitorData(winner.name, "0"))
            }

            notDead = winners

            // Comprueba si ha terminado el torneo (solo queda un participante vivo)
            if(notDead.size == 1)
                setStatusFinished(context)

            // Evita desajuste por el número de participantes impar
            if(notDead.size%2 != 0){
                notDead.add(CompetitorData("", ""))
            }

            columnMatches.add(createColumn(createMatches(notDead)))

            return true
        } else {
            setStatusFinished(context)
            return false
        }
    }

    private fun createMatches(competitors : MutableList<CompetitorData>) : MutableList<MatchData>{
        val matches = mutableListOf<MatchData>()
        for(i in 0 until competitors.size - 1 step 2){
            matches.add(MatchData(competitors[i], competitors[i + 1]))
        }
        return matches
    }

    private fun createColumn(matches : MutableList<MatchData>) : ColomnData{ return ColomnData(matches) }

    private fun getCompetitorList(participants : MutableList<User>) : MutableList<CompetitorData>{
        return participants.map { CompetitorData(it.nickname, "0") }.toMutableList()
    }
    
    fun getLastMatchList() : MutableList<MatchData>{ return columnMatches.last().matches }
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

    fun shufleParticipants(): Boolean{
        if(tournamentStatus == TournamentStatus.EDITABLE){
            participantList.shuffle()
            restartMatches()
            return true
        } else {
            return false
        }
    }

    // Funciones de acceso estático
    companion object{
        private var tournaments: MutableList<Tournament> = mutableListOf()
        fun getTournaments() : MutableList<Tournament> { return tournaments }
        fun setTournaments(tournaments: MutableList<Tournament>){ this.tournaments = tournaments}

        fun addTournament(tournament: Tournament){ tournaments.add(tournament) }
        fun removeTournament(tournament: Tournament){ tournaments.remove(tournament) }
        fun removeTournament(id: Int){ tournaments.removeIf { it.id == id } }

        fun searchTournamentByCode(code: Int) : Tournament ? {
            tournaments.find { it.code == code }?.let { return it }
            return null
        }
    }
}
