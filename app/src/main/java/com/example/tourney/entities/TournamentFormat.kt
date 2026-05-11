package com.example.tourney.entities

import android.content.Context
import com.ventura.bracketslib.model.ColomnData
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData

interface TournamentFormat {
    /**
     * Inicializa la lista de columnas con el primer emparejamiento.
     * Si ya se ha inicializado, no hace nada.
     */
    fun initMatches(t: Tournament)
    /**
     * Reinicia la lista de columnas con el primer emparejamiento.
     */
    fun restartMatches(t: Tournament)
    fun createMatches(competitors : MutableList<CompetitorData>): MutableList<MatchData>

    /**
     * Ejecuta el round actual del torneo, actualiza la lista de columnas y crea el siguiente emparejamiento
     * @return true si se ha ejecutado correctamente, false en caso contrario
     */
    fun nextRound(t: Tournament, context: Context?): Boolean

    fun getCompetitorList(participants : MutableList<Participant>) : MutableList<CompetitorData>{
        return participants.map { CompetitorData(it.nickname, "0") }.toMutableList()
    }
    fun createColumn(matches : MutableList<MatchData>) : ColomnData { return ColomnData(matches)
    }

    fun getLastMatchList(t: Tournament) : MutableList<MatchData>{ return t.columnMatches.last().matches }
}