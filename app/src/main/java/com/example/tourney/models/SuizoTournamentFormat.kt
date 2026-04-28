package com.example.tourney.models

import android.content.Context
import android.widget.Toast
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
import kotlin.math.ceil
import kotlin.math.log2

/**
 * Implementación del Formato de Torneo Suizo.
 * Los jugadores se emparejan según su puntuación (victorias) en cada ronda.
 */
class SuizoTournamentFormat : TournamentFormat {

    override fun initMatches(t: Tournament) {
        if (t.columnMatches.isEmpty()) {
            val competitors = getCompetitorList(t.participantList)
            
            // Si el número de participantes es impar, añadimos un DESCANSO
            if (competitors.size % 2 != 0) {
                competitors.add(CompetitorData("DESCANSO", ""))
            }

            // Para la primera ronda, barajamos a los participantes
            competitors.shuffle()
            t.setNotDead(competitors)

            // Creamos la primera columna (Ronda 1)
            t.columnMatches.add(createColumn(createMatches(competitors)))
        }
    }

    override fun restartMatches(t: Tournament) {
        t.columnMatches.clear()
        initMatches(t)
    }

    /**
     * Crea los emparejamientos emparejando a los jugadores en orden de la lista.
     * En el sistema suizo, la lista vendrá ordenada por victorias previas.
     */
    override fun createMatches(competitors: MutableList<CompetitorData>): MutableList<MatchData> {
        val matches = mutableListOf<MatchData>()
        for (i in 0 until competitors.size - 1 step 2) {
            // Inicializamos el score a "0" para la nueva ronda
            matches.add(MatchData(
                CompetitorData(competitors[i].name, "0"),
                CompetitorData(competitors[i + 1].name, "0")
            ))
        }
        return matches
    }

    override fun nextRound(t: Tournament, context: Context?): Boolean {
        val competitors = t.getNotDead()
        if (competitors.isEmpty()) return false

        // 1. Validar resultados de la ronda actual
        val lastMatches = getLastMatchList(t)
        for (match in lastMatches) {
            // Ignoramos validación si uno es DESCANSO (se considera victoria automática para el otro)
            if (match.competitorOne.name != "DESCANSO" && match.competitorTwo.name != "DESCANSO") {
                val s1 = match.competitorOne.score.toFloatOrNull()
                val s2 = match.competitorTwo.score.toFloatOrNull()
                
                if (s1 == null || s2 == null) {
                    Toast.makeText(context, "Faltan puntuaciones en la ronda actual", Toast.LENGTH_SHORT).show()
                    return false
                }
                
                if (s1 == s2) {
                    Toast.makeText(context, "No puede haber empate en sistema Suizo (necesario para emparejar)", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }

        // 2. Comprobar si hemos alcanzado el límite de rondas recomendado (log2 de N)
        val numParticipantsReal = competitors.filter { it.name != "DESCANSO" }.size
        val maxRounds = if (numParticipantsReal > 0) ceil(log2(numParticipantsReal.toDouble())).toInt() else 0
        
        if (t.columnMatches.size >= maxRounds && maxRounds > 0) {
            t.setStatusFinished(context)
            return false
        }

        // 3. Calcular el total de victorias acumuladas para cada jugador
        // Esto servirá para el emparejamiento de la siguiente ronda
        val winsMap = calculateWins(t)

        // 4. Ordenar a los jugadores por número de victorias (Criterio principal del Suizo)
        val sortedCompetitors = competitors.sortedByDescending { winsMap[it.name] ?: 0 }.toMutableList()

        // 5. Crear la siguiente columna
        t.columnMatches.add(createColumn(createMatches(sortedCompetitors)))
        
        Toast.makeText(context, "Ronda ${t.columnMatches.size} generada", Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * Calcula cuántas victorias tiene cada participante basándose en todas las columnas (rondas)
     */
    private fun calculateWins(t: Tournament): Map<String, Int> {
        val wins = mutableMapOf<String, Int>()
        
        t.columnMatches.forEach { column ->
            column.matches.forEach { match ->
                val s1 = match.competitorOne.score.toFloatOrNull() ?: 0f
                val s2 = match.competitorTwo.score.toFloatOrNull() ?: 0f
                
                when {
                    s1 > s2 -> wins[match.competitorOne.name] = (wins[match.competitorOne.name] ?: 0) + 1
                    s2 > s1 -> wins[match.competitorTwo.name] = (wins[match.competitorTwo.name] ?: 0) + 1
                    // Caso de DESCANSO: El jugador real gana automáticamente
                    match.competitorOne.name == "DESCANSO" -> wins[match.competitorTwo.name] = (wins[match.competitorTwo.name] ?: 0) + 1
                    match.competitorTwo.name == "DESCANSO" -> wins[match.competitorOne.name] = (wins[match.competitorOne.name] ?: 0) + 1
                }
            }
        }
        return wins
    }
}
