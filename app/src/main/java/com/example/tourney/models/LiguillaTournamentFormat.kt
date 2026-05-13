package com.example.tourney.models

import android.content.Context
import android.widget.Toast
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData

class LiguillaTournamentFormat : TournamentFormat {

    /**
     * Genera la primera jornada (Jornada 1)
     */
    override fun initMatches(t: Tournament) {
        if (t.columnMatches.isEmpty()) {
            val competitors = getCompetitorList(t.participantList)

            // En Liguilla, si son impares, añadimos un DESCANSO para que el algoritmo sea par
            if (competitors.size % 2 != 0) {
                competitors.add(CompetitorData("DESCANSO", ""))
            }

            // Guardamos la lista de competidores (con el descanso si lo hay) para rotar sobre ella
            t.setNotDead(competitors)

            // Creamos la Jornada 1 (índice 0)
            val firstJornada = generateJornada(0, competitors)
            t.columnMatches.add(createColumn(firstJornada))
        }
    }

    override fun restartMatches(t: Tournament) {
        t.columnMatches.clear()
        initMatches(t)
    }

    /**
     * Avanza a la siguiente jornada.
     * Valida que la jornada actual tenga puntuaciones insertadas.
     */
    override fun nextRound(t: Tournament, context: Context?): Boolean {
        val competitors = t.getNotDead() // La lista que usamos para rotar
        if (competitors.isEmpty()) return false

        val numParticipants = competitors.size
        val totalJornadas = numParticipants - 1
        val nextJornadaIndex = t.columnMatches.size // La siguiente jornada a crear

        // 1. Validar que la jornada actual tenga resultados (igual que en Eliminación)
        val lastMatches = getLastMatchList(t)
        for (match in lastMatches) {
            // Si no es un partido de descanso, comprobamos que se haya insertado algo
            if (match.competitorOne.name != "DESCANSO" && match.competitorTwo.name != "DESCANSO") {
                val s1 = match.competitorOne.score.toFloatOrNull()
                val s2 = match.competitorTwo.score.toFloatOrNull()

                if (s1 == null || s2 == null) {
                    if (context != null) {
                        Toast.makeText(context, "Faltan puntuaciones en la jornada actual", Toast.LENGTH_SHORT).show()
                    }
                    return false
                }
            }
        }

        // 2. Comprobar si hemos terminado todas las jornadas
        if (nextJornadaIndex < totalJornadas) {
            // Generar la siguiente jornada usando el algoritmo de rotación
            val nextJornada = generateJornada(nextJornadaIndex, competitors)
            t.columnMatches.add(createColumn(nextJornada))

            if (context != null) {
                Toast.makeText(context, "Jornada ${nextJornadaIndex + 1} generada", Toast.LENGTH_SHORT).show()
            }
            return true
        } else {
            // Si ya no hay más jornadas, finalizamos el torneo
            t.setStatusFinished(context)
            return false
        }
    }

    /**
     * Algoritmo de Rotación (Round Robin) para generar los enfrentamientos
     * de una jornada específica sin repetir partidos.
     */
    private fun generateJornada(roundIndex: Int, competitors: List<CompetitorData>): MutableList<MatchData> {
        val matches = mutableListOf<MatchData>()
        val n = competitors.size

        for (i in 0 until n / 2) {
            // Algoritmo de rotación: un elemento fijo (el último) y los demás rotan
            var home = (roundIndex + i) % (n - 1)
            var away = (n - 1 - i + roundIndex) % (n - 1)

            if (i == 0) away = n - 1

            // Creamos el partido con puntuación "0" inicial
            matches.add(MatchData(
                CompetitorData(competitors[home].name, "0"),
                CompetitorData(competitors[away].name, "0")
            ))
        }
        return matches
    }

    override fun createMatches(competitors: MutableList<CompetitorData>): MutableList<MatchData> {
        // No se usa directamente en Liguilla ya que los partidos dependen del número de jornada
        return mutableListOf()
    }
}