package com.example.tourney.tools

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.tourney.entities.*

class TournamentsDao(context: Context) {
    private val helper = AppDatabaseHelper(context.applicationContext)
    private val usersDao = UsersDao(context)

    fun dropAll() {
        val db = helper.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS ${AppDatabaseHelper.TABLE_MATCHES}")
        db.execSQL("DROP TABLE IF EXISTS ${AppDatabaseHelper.TABLE_PARTICIPANTS}")
        db.execSQL("DROP TABLE IF EXISTS ${AppDatabaseHelper.TABLE_USER_TRN_RELATIONS}")
        db.execSQL("DROP TABLE IF EXISTS ${AppDatabaseHelper.TABLE_TOURNAMENTS}")
        helper.onCreate(db)
        db.close()
    }

    /**
     * Inserta un torneo completo con sus participantes y partidos.
     * También crea la relación de propiedad (SHOWABLE) para el creador.
     */
    fun insertTournament(t: Tournament): Long {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put(AppDatabaseHelper.COL_TRN_NAME, t.name)
                put(AppDatabaseHelper.COL_TRN_GAME, t.game)
                put(AppDatabaseHelper.COL_TRN_CREATOR_ID, t.creatorId)
                put(AppDatabaseHelper.COL_TRN_CREATOR_NICKNAME, t.creatorNickname)
                put(AppDatabaseHelper.COL_TRN_MAX_PARTICIPANTS, t.maxParticipants)
                put(AppDatabaseHelper.COL_TRN_DATE, t.date)
                put(AppDatabaseHelper.COL_TRN_LOCATION, t.location)
                put(AppDatabaseHelper.COL_TRN_PRIZE, t.prize)
                put(AppDatabaseHelper.COL_TRN_CODE, t.code)
                put(AppDatabaseHelper.COL_TRN_TYPE, t.type.name)
                put(AppDatabaseHelper.COL_TRN_STATUS, t.tournamentStatus.name)
                put(AppDatabaseHelper.COL_TRN_THUMBNAIL, t.thumbnail)
            }
            val tournamentId = db.insert(AppDatabaseHelper.TABLE_TOURNAMENTS, null, values)

            if (tournamentId != -1L) {
                t.id = tournamentId
                
                // 1. Crear relación automática: El creador "posee" el torneo
                val relValues = ContentValues().apply {
                    put(AppDatabaseHelper.COL_REL_USER_ID, t.creatorId)
                    put(AppDatabaseHelper.COL_REL_TRN_ID, tournamentId)
                    put(AppDatabaseHelper.COL_REL_TYPE, AppDatabaseHelper.REL_TYPE_CREATED)
                }
                db.insert(AppDatabaseHelper.TABLE_USER_TRN_RELATIONS, null, relValues)

                // 2. Insertar participantes y partidos
                insertParticipants(t, db)
                t.updateMatchesFromView()
                insertMatches(t, db)
            }
            db.setTransactionSuccessful()
            tournamentId
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun insertParticipants(t: Tournament, db: SQLiteDatabase) {
        t.participantList.forEach { p ->
            val pValues = ContentValues().apply {
                put(AppDatabaseHelper.COL_PART_TRN_ID, t.id)
                put(AppDatabaseHelper.COL_PART_USER_ID, p.userId)
                put(AppDatabaseHelper.COL_PART_NICKNAME, p.nickname)
                put(AppDatabaseHelper.COL_PART_PUNTUATION, p.puntuation)
            }
            p.id = db.insert(AppDatabaseHelper.TABLE_PARTICIPANTS, null, pValues)
        }
    }

    private fun insertMatches(t: Tournament, db: SQLiteDatabase) {
        t.matches.forEach { m ->
            val mValues = ContentValues().apply {
                put(AppDatabaseHelper.COL_MATCH_TRN_ID, t.id)
                put(AppDatabaseHelper.COL_MATCH_ROUND, m.roundNumber)
                put(AppDatabaseHelper.COL_MATCH_P1_ID, m.participantOneId)
                put(AppDatabaseHelper.COL_MATCH_P2_ID, m.participantTwoId)
                put(AppDatabaseHelper.COL_MATCH_P1_NAME, m.participantOneName)
                put(AppDatabaseHelper.COL_MATCH_P2_NAME, m.participantTwoName)
                put(AppDatabaseHelper.COL_MATCH_SCORE1, m.scoreOne)
                put(AppDatabaseHelper.COL_MATCH_SCORE2, m.scoreTwo)
                put(AppDatabaseHelper.COL_MATCH_WINNER_ID, m.winnerId)
            }
            m.id = db.insert(AppDatabaseHelper.TABLE_MATCHES, null, mValues)
        }
    }

    /**
     * Recupera todos los torneos reconstruyendo sus listas internas
     */
    fun getAllTournaments(): MutableList<Tournament> {
        val db = helper.readableDatabase
        val tournaments = mutableListOf<Tournament>()
        val cursor = db.query(AppDatabaseHelper.TABLE_TOURNAMENTS, null, null, null, null, null, "${AppDatabaseHelper.COL_TRN_DATE} DESC")

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_ID))
                val tournament = Tournament(
                    id = id,
                    name = getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_NAME)),
                    game = getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_GAME)),
                    creatorId = getLong(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_CREATOR_ID)),
                    creatorNickname = getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_CREATOR_NICKNAME)),
                    maxParticipants = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_MAX_PARTICIPANTS)),
                    date = if (isNull(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_DATE))) null else getLong(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_DATE)),
                    location = if (isNull(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_LOCATION))) "" else getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_LOCATION)),
                    prize = if(isNull(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_PRIZE))) "" else getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_PRIZE)),
                    code = if (isNull(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_CODE))) null else getInt(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_CODE)),
                    type = TournamentType.valueOf(getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_TYPE))),
                    tournamentStatus = TournamentStatus.valueOf(getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_STATUS))),
                    thumbnail = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COL_TRN_THUMBNAIL))
                )
                tournament.participantList = getParticipantsForTournament(id, db)
                tournament.matches = getMatchesForTournament(id, db)
                tournament.syncViewFromMatches()
                tournaments.add(tournament)
            }
        }
        cursor.close()
        db.close()
        return tournaments
    }

    private fun getParticipantsForTournament(tId: Long, db: SQLiteDatabase): MutableList<Participant> {
        val list = mutableListOf<Participant>()
        val cursor = db.query(AppDatabaseHelper.TABLE_PARTICIPANTS, null, "${AppDatabaseHelper.COL_PART_TRN_ID}=?", arrayOf(tId.toString()), null, null, null)
        while (cursor.moveToNext()) {
            list.add(Participant(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PART_ID)),
                userId = if (cursor.isNull(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PART_USER_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PART_USER_ID)),
                nickname = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PART_NICKNAME)),
                puntuation = cursor.getFloat(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_PART_PUNTUATION))
            ))
        }
        cursor.close()
        return list
    }

    private fun getMatchesForTournament(tId: Long, db: SQLiteDatabase): MutableList<TournamentMatch> {
        val list = mutableListOf<TournamentMatch>()
        val cursor = db.query(AppDatabaseHelper.TABLE_MATCHES, null, "${AppDatabaseHelper.COL_MATCH_TRN_ID}=?", arrayOf(tId.toString()), null, null, null)
        while (cursor.moveToNext()) {
            list.add(TournamentMatch(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_ID)),
                tournamentId = tId,
                roundNumber = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_ROUND)),
                participantOneId = if (cursor.isNull(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_P1_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_P1_ID)),
                participantTwoId = if (cursor.isNull(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_P2_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_P2_ID)),
                scoreOne = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_SCORE1)),
                scoreTwo = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_SCORE2)),
                winnerId = if (cursor.isNull(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_WINNER_ID))) null else cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_WINNER_ID)),
                participantOneName = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_P1_NAME)),
                participantTwoName = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_MATCH_P2_NAME))
            ))
        }
        cursor.close()
        return list
    }

    /**
     * Borra un torneo completo de la base de datos.
     * Gracias a ON DELETE CASCADE, esto borrará participantes, partidos y relaciones automáticamente.
     */
    fun deleteTournament(id: Long): Int {
        val db = helper.writableDatabase
        val result = db.delete(AppDatabaseHelper.TABLE_TOURNAMENTS, "${AppDatabaseHelper.COL_TRN_ID}=?", arrayOf(id.toString()))
        db.close()
        return result
    }

    /**
     * Actualiza un torneo existente en la base de datos
     */
    fun updateTournament(t: Tournament): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put(AppDatabaseHelper.COL_TRN_NAME, t.name)
                put(AppDatabaseHelper.COL_TRN_GAME, t.game)
                put(AppDatabaseHelper.COL_TRN_DATE, t.date)
                put(AppDatabaseHelper.COL_TRN_LOCATION, t.location)
                put(AppDatabaseHelper.COL_TRN_PRIZE, t.prize)
                put(AppDatabaseHelper.COL_TRN_CODE, t.code)
                put(AppDatabaseHelper.COL_TRN_TYPE, t.type.name)
                put(AppDatabaseHelper.COL_TRN_STATUS, t.tournamentStatus.name)
                put(AppDatabaseHelper.COL_TRN_THUMBNAIL, t.thumbnail)
            }
            db.update(AppDatabaseHelper.TABLE_TOURNAMENTS, values, "${AppDatabaseHelper.COL_TRN_ID}=?", arrayOf(t.id.toString()))
            
            // Si el torneo ha cambiado de estructura (tipo o participantes), a veces es necesario resetear partidos.
            // Aquí simplificamos actualizando lo que haya en t.matches
            db.delete(AppDatabaseHelper.TABLE_MATCHES, "${AppDatabaseHelper.COL_MATCH_TRN_ID}=?", arrayOf(t.id.toString()))
            t.updateMatchesFromView()
            insertMatches(t, db)
            
            updateParticipants(t, db)
            
            db.setTransactionSuccessful()
            true
        } catch (_: Exception) { false } finally {
            db.endTransaction()
            db.close()
        }
    }

    /**
     * Actualiza los participantes de un torneo en la base de datos
     */
    fun updateParticipants(t: Tournament, db: SQLiteDatabase? = null): Boolean {
        val innerDb = db ?: helper.writableDatabase
        val ownTransaction = db == null
        if (ownTransaction) innerDb.beginTransaction()
        return try {
            innerDb.delete(AppDatabaseHelper.TABLE_PARTICIPANTS, "${AppDatabaseHelper.COL_PART_TRN_ID}=?", arrayOf(t.id.toString()))
            insertParticipants(t, innerDb)
            if (ownTransaction) innerDb.setTransactionSuccessful()
            true
        } catch (_: Exception) { false } finally {
            if (ownTransaction) {
                innerDb.endTransaction()
                innerDb.close()
            }
        }
    }

    /**
     * Actualiza la miniatura (thumbnail) de un torneo específico.
     * @param id El ID del torneo a actualizar
     * @param thumbnail El nuevo recurso o identificador de la miniatura
     * @return El número de filas actualizadas
     */
    fun updateTournamentThumbnail(id: Long, thumbnail: Int): Int {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_TRN_THUMBNAIL, thumbnail)
        }
        val rows = db.update(AppDatabaseHelper.TABLE_TOURNAMENTS, values, "${AppDatabaseHelper.COL_TRN_ID}=?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}
