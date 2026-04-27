package com.example.tourney.models

import android.content.Context
import android.widget.Toast
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData

class EliminationTournamentFormat : TournamentFormat {

    override fun initMatches(t: Tournament) {
        // Inicializa la lista de columnas con el primer emparejamiento
        if(t.columnMatches.isEmpty()) {
            t.setNotDead(getCompetitorList(t.participantList))

            // Evita desajuste por el número de participantes impar
            if(t.getNotDead().size%2 != 0) t.getNotDead().add(CompetitorData("", ""))

            t.columnMatches.add(createColumn(createMatches(t.getNotDead())))
        }
    }

    override fun createMatches(competitors : MutableList<CompetitorData>) : MutableList<MatchData>{
        val matches = mutableListOf<MatchData>()
        for(i in 0 until competitors.size - 1 step 2){
            matches.add(MatchData(competitors[i], competitors[i + 1]))
        }
        return matches
    }

    override fun restartMatches(t: Tournament){
        t.columnMatches.clear()
        initMatches(t)
    }

    override fun nextRound(t: Tournament, context: Context?) : Boolean{
        if(t.participantList.isEmpty()){
            if(context != null) Toast.makeText(context, "No hay participantes", Toast.LENGTH_SHORT).show()
            return false
        }
        if(t.getNotDead().isNotEmpty() && t.tournamentStatus != TournamentStatus.FINISHED){
            // Lista de partidos de la ronda actual (la última guardada)
            val lastMatches = getLastMatchList(t)
            val winners = mutableListOf<CompetitorData>()

            for (match in lastMatches) {
                // Lógica para decidir quién pasa al siguiente round
                //val score1 = match.competitorOne.score.toIntOrNull() ?: -1
                val score1 = match.competitorOne.score.toFloatOrNull() ?: Float.NEGATIVE_INFINITY
                //val score2 = match.competitorTwo.score.toIntOrNull() ?: -
                val score2 = match.competitorTwo.score.toFloatOrNull() ?: Float.NEGATIVE_INFINITY

                val winner =
                    if (score1 > score2)
                        match.competitorOne
                    else if(score1 < score2)
                        match.competitorTwo
                    else { // Empate
                        if(context != null) {
                            Toast.makeText(context, "No puede haber empate en el torneo", Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, "Empate en ${match.competitorOne.name}: ${match.competitorOne.score} y ${match.competitorTwo.name}: ${match.competitorTwo.score}", Toast.LENGTH_SHORT).show()
                        }
                        return false
                    }

                // Creamos una instancia nueva para la siguiente ronda.
                // Esto equivale al 'new CompetitorData' de Java.
                winners.add(CompetitorData(winner.name, "0"))
            }

            t.setNotDead(winners)


            // Comprueba si ha terminado el torneo (solo queda un participante vivo)
            if(t.getNotDead().size == 1)
                t.setStatusFinished(context)

            // Evita desajuste por el número de participantes impar
            if(t.getNotDead().size%2 != 0){
                t.getNotDead().add(CompetitorData("", ""))
            }

            t.columnMatches.add(createColumn(createMatches(t.getNotDead())))

            return true
        } else {
            t.setStatusFinished(context)
            return false
        }
    }
}