package com.example.tourney.tools

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.tourney.entities.*

class TournamentsDao(context: Context) {
    private val helper = TournamentDatabaseHelper(context.applicationContext)

    fun dropAll(){
        val db = helper.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS ${TournamentDatabaseHelper.TABLE_TOURNAMENTS}")
        db.execSQL("DROP TABLE IF EXISTS ${TournamentDatabaseHelper.TABLE_PARTICIPANTS}")
        db.execSQL("DROP TABLE IF EXISTS ${TournamentDatabaseHelper.TABLE_MATCHES}")
        helper.onCreate(db)
        db.close()
    }

    /**
     * Inserta un torneo completo con sus participantes y partidos
     */
    fun insertTournament(t: Tournament): Long {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put(TournamentDatabaseHelper.COL_NAME, t.name)
                put(TournamentDatabaseHelper.COL_GAME, t.game)
                put(TournamentDatabaseHelper.COL_CREATOR_ID, t.creatorId)
                put(TournamentDatabaseHelper.COL_CREATOR_NICKNAME, t.creatorNickname)
                put(TournamentDatabaseHelper.COL_MAX_PARTICIPANTS, t.maxParticipants)
                put(TournamentDatabaseHelper.COL_DATE, t.date)
                put(TournamentDatabaseHelper.COL_LOCATION, t.location)
                put(TournamentDatabaseHelper.COL_PRIZE, t.prize)
                put(TournamentDatabaseHelper.COL_CODE, t.code)
                put(TournamentDatabaseHelper.COL_TYPE, t.type.name)
                put(TournamentDatabaseHelper.COL_STATUS, t.tournamentStatus.name)
                put(TournamentDatabaseHelper.COL_THUMBNAIL, t.thumbnail)
            }
            val tournamentId = db.insert(TournamentDatabaseHelper.TABLE_TOURNAMENTS, null, values)

            if (tournamentId != -1L) {
                t.id = tournamentId
                // Insertar participantes
                t.participantList.forEach { p ->
                    val pValues = ContentValues().apply {
                        put(TournamentDatabaseHelper.COL_PARTICIPANTS_TOURNAMENT_ID, tournamentId)
                        put(TournamentDatabaseHelper.COL_PARTICIPANTS_USER_ID, p.userId)
                        put(TournamentDatabaseHelper.COL_PARTICIPANTS_NICKNAME, p.nickname)
                        put(TournamentDatabaseHelper.COL_PARTICIPANTS_PUNTUATION, p.puntuation)
                    }
                    val pId = db.insert(TournamentDatabaseHelper.TABLE_PARTICIPANTS, null, pValues)
                    p.id = pId
                }

                // Insertar partidos
                t.updateMatchesFromView()
                t.matches.forEach { m ->
                    val mValues = ContentValues().apply {
                        put(TournamentDatabaseHelper.COL_MATCHES_TOURNAMENT_ID, tournamentId)
                        put(TournamentDatabaseHelper.COL_MATCHES_ROUND, m.roundNumber)
                        put(TournamentDatabaseHelper.COL_MATCHES_P1_ID, m.participantOneId)
                        put(TournamentDatabaseHelper.COL_MATCHES_P2_ID, m.participantTwoId)
                        put(TournamentDatabaseHelper.COL_MATCHES_P1_NAME, m.participantOneName)
                        put(TournamentDatabaseHelper.COL_MATCHES_P2_NAME, m.participantTwoName)
                        put(TournamentDatabaseHelper.COL_MATCHES_SCORE1, m.scoreOne)
                        put(TournamentDatabaseHelper.COL_MATCHES_SCORE2, m.scoreTwo)
                        put(TournamentDatabaseHelper.COL_MATCHES_WINNER_ID, m.winnerId)
                    }
                    val mId = db.insert(TournamentDatabaseHelper.TABLE_MATCHES, null, mValues)
                    m.id = mId
                }
            }
            db.setTransactionSuccessful()
            tournamentId
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * Recupera todos los torneos reconstruyendo sus listas internas
     */
    fun getAllTournaments(): MutableList<Tournament> {
        val db = helper.readableDatabase
        val tournaments = mutableListOf<Tournament>()
        val cursor = db.query(
            TournamentDatabaseHelper.TABLE_TOURNAMENTS,
            null, null, null, null, null,
            "${TournamentDatabaseHelper.COL_DATE} DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_ID))
                val tournament = Tournament(
                    id = id,
                    name = getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_NAME)),
                    game = getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_GAME)),
                    creatorId = getLong(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_CREATOR_ID)),
                    creatorNickname = getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_CREATOR_NICKNAME)),
                    maxParticipants = getInt(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MAX_PARTICIPANTS)),
                    date = getLong(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_DATE)),
                    location = getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_LOCATION)),
                    prize = getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PRIZE)),
                    code = getInt(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_CODE)),
                    type = TournamentType.valueOf(getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_TYPE))),
                    tournamentStatus = TournamentStatus.valueOf(getString(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_STATUS))),
                    thumbnail = getInt(getColumnIndexOrThrow(TournamentDatabaseHelper.COL_THUMBNAIL))
                )

                // Cargar Participantes
                tournament.participantList = getParticipantsForTournament(id, db)
                // Cargar Partidos
                tournament.matches = getMatchesForTournament(id, db)
                // Reconstruir vista visual (columnMatches) y recalculateNotDead
                tournament.syncViewFromMatches()

                tournaments.add(tournament)
            }
        }
        cursor.close()
        db.close()
        return tournaments
    }

    private fun getParticipantsForTournament(tournamentId: Long, db: SQLiteDatabase): MutableList<Participant> {
        val participants = mutableListOf<Participant>()
        val cursor = db.query(
            TournamentDatabaseHelper.TABLE_PARTICIPANTS,
            null,
            "${TournamentDatabaseHelper.COL_PARTICIPANTS_TOURNAMENT_ID}=?",
            arrayOf(tournamentId.toString()),
            null, null, null
        )
        while (cursor.moveToNext()) {
            participants.add(
                Participant(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PARTICIPANTS_ID)),
                    userId = if (cursor.isNull(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PARTICIPANTS_USER_ID))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PARTICIPANTS_USER_ID)
                    ),
                    nickname = cursor.getString(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PARTICIPANTS_NICKNAME)),
                    puntuation = if (cursor.isNull(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PARTICIPANTS_PUNTUATION))) 0f else cursor.getFloat(
                        cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_PARTICIPANTS_PUNTUATION)
                    )
                )
            )
        }
        cursor.close()
        return participants
    }

    private fun getMatchesForTournament(tId: Long, db: SQLiteDatabase): MutableList<TournamentMatch> {
        val matches = mutableListOf<TournamentMatch>()
        val cursor = db.query(
            TournamentDatabaseHelper.TABLE_MATCHES,
            null,
            "${TournamentDatabaseHelper.COL_MATCHES_TOURNAMENT_ID}=?",
            arrayOf(tId.toString()),
            null, null, null
        )
        while (cursor.moveToNext()) {
            matches.add(
                TournamentMatch(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_ID)),
                    tournamentId = tId,
                    roundNumber = cursor.getInt(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_ROUND)),
                    participantOneId = if (cursor.isNull(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_P1_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_P1_ID)),
                    participantTwoId = if (cursor.isNull(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_P2_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_P2_ID)),
                    scoreOne = cursor.getString(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_SCORE1)),
                    scoreTwo = cursor.getString(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_SCORE2)),
                    winnerId = if (cursor.isNull(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_WINNER_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_WINNER_ID)),
                    participantOneName = cursor.getString(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_P1_NAME)),
                    participantTwoName = cursor.getString(cursor.getColumnIndexOrThrow(TournamentDatabaseHelper.COL_MATCHES_P2_NAME))
                )
            )
        }
        cursor.close()
        return matches
    }

    /**
     * Borra un torneo completo de la base de datos
     */
    fun deleteTournament(id: Long): Int {
        val db = helper.writableDatabase
        val result = db.delete(TournamentDatabaseHelper.TABLE_TOURNAMENTS, "${TournamentDatabaseHelper.COL_ID}=?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun updateTournament(t: Tournament): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            // 1. Actualizar datos básicos del torneo
            val values = ContentValues().apply {
                put(TournamentDatabaseHelper.COL_STATUS, t.tournamentStatus.name)
                put(TournamentDatabaseHelper.COL_THUMBNAIL, t.thumbnail)
            }
            db.update(TournamentDatabaseHelper.TABLE_TOURNAMENTS, values, "${TournamentDatabaseHelper.COL_ID}=?", arrayOf(t.id.toString()))

            // 2. Actualizar partidos
            db.delete(TournamentDatabaseHelper.TABLE_MATCHES, "${TournamentDatabaseHelper.COL_MATCHES_TOURNAMENT_ID}=?", arrayOf(t.id.toString()))

            t.updateMatchesFromView()
            t.matches.forEach { m ->
                val mValues = ContentValues().apply {
                    put(TournamentDatabaseHelper.COL_MATCHES_TOURNAMENT_ID, t.id)
                    put(TournamentDatabaseHelper.COL_MATCHES_ROUND, m.roundNumber)
                    put(TournamentDatabaseHelper.COL_MATCHES_P1_ID, m.participantOneId)
                    put(TournamentDatabaseHelper.COL_MATCHES_P2_ID, m.participantTwoId)
                    put(TournamentDatabaseHelper.COL_MATCHES_P1_NAME, m.participantOneName)
                    put(TournamentDatabaseHelper.COL_MATCHES_P2_NAME, m.participantTwoName)
                    put(TournamentDatabaseHelper.COL_MATCHES_SCORE1, m.scoreOne)
                    put(TournamentDatabaseHelper.COL_MATCHES_SCORE2, m.scoreTwo)
                    put(TournamentDatabaseHelper.COL_MATCHES_WINNER_ID, m.winnerId)
                }
                db.insert(TournamentDatabaseHelper.TABLE_MATCHES, null, mValues)
            }

            // 3. Actualizar participantes
            updateParticipants(t, db)

            db.setTransactionSuccessful()
            true
        } catch (_: Exception) {
            false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun updateTournamentThumbnail(id: Long, thumbnail: Int): Int {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(TournamentDatabaseHelper.COL_THUMBNAIL, thumbnail)
        }
        val rows = db.update(TournamentDatabaseHelper.TABLE_TOURNAMENTS, values, "${TournamentDatabaseHelper.COL_ID}=?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    /**
     * Actualiza los participantes de un torneo en la base de datos
     */
    fun updateParticipants(t: Tournament, db: SQLiteDatabase? = null): Boolean {
        val innerDb = db ?: helper.writableDatabase
        val ownTransaction = db == null

        if (ownTransaction) innerDb.beginTransaction()
        return try {
            innerDb.delete(TournamentDatabaseHelper.TABLE_PARTICIPANTS, "${TournamentDatabaseHelper.COL_PARTICIPANTS_TOURNAMENT_ID}=?", arrayOf(t.id.toString()))

            t.participantList.forEach { p ->
                val pValues = ContentValues().apply {
                    put(TournamentDatabaseHelper.COL_PARTICIPANTS_TOURNAMENT_ID, t.id)
                    put(TournamentDatabaseHelper.COL_PARTICIPANTS_USER_ID, p.userId)
                    put(TournamentDatabaseHelper.COL_PARTICIPANTS_NICKNAME, p.nickname)
                    put(TournamentDatabaseHelper.COL_PARTICIPANTS_PUNTUATION, p.puntuation)
                }
                val pId = innerDb.insert(TournamentDatabaseHelper.TABLE_PARTICIPANTS, null, pValues)
                p.id = pId
            }

            if (ownTransaction) innerDb.setTransactionSuccessful()
            true
        } catch (_: Exception) {
            false
        } finally {
            if (ownTransaction) {
                innerDb.endTransaction()
                innerDb.close()
            }
        }
    }
}
