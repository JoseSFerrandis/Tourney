package com.example.tourney.tools

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TournamentDatabaseHelper(context: Context) : SQLiteOpenHelper (
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
){
    override fun onCreate(db: SQLiteDatabase?) {
        // 1. Tabla de Torneos
        val createTournamentsTable = ("CREATE TABLE $TABLE_TOURNAMENTS ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_NAME TEXT,"
                + "$COL_GAME TEXT,"
                + "$COL_CREATOR_ID INTEGER,"
                + "$COL_CREATOR_NICKNAME TEXT,"
                + "$COL_MAX_PARTICIPANTS INTEGER,"
                + "$COL_DATE INTEGER,"
                + "$COL_LOCATION TEXT,"
                + "$COL_PRIZE TEXT,"
                + "$COL_CODE INTEGER,"
                + "$COL_TYPE TEXT,"
                + "$COL_STATUS TEXT,"
                + "$COL_THUMBNAIL INTEGER DEFAULT 0" + ")")
        db?.execSQL(createTournamentsTable)

        // 2. Tabla de Participantes (Relacionada con el torneo)
        val createParticipantsTable = ("CREATE TABLE $TABLE_PARTICIPANTS ("
                + "$COL_PARTICIPANTS_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_PARTICIPANTS_TOURNAMENT_ID INTEGER,"
                + "$COL_PARTICIPANTS_USER_ID INTEGER," // NULL si es participante offline
                + "$COL_PARTICIPANTS_NICKNAME TEXT,"
                + "$COL_PARTICIPANTS_PUNTUATION REAL,"
                + "FOREIGN KEY($COL_PARTICIPANTS_TOURNAMENT_ID) REFERENCES $TABLE_TOURNAMENTS($COL_ID) ON DELETE CASCADE" + ")")
        db?.execSQL(createParticipantsTable)

        // 3. Tabla de Partidos (Lista plana para reconstruir llaves)
        val createMatchesTable = ("CREATE TABLE $TABLE_MATCHES ("
                + "$COL_MATCHES_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_MATCHES_TOURNAMENT_ID INTEGER,"
                + "$COL_MATCHES_ROUND INTEGER,"
                + "$COL_MATCHES_P1_ID INTEGER,"
                + "$COL_MATCHES_P2_ID INTEGER,"
                + "$COL_MATCHES_P1_NAME TEXT,"
                + "$COL_MATCHES_P2_NAME TEXT,"
                + "$COL_MATCHES_SCORE1 TEXT,"
                + "$COL_MATCHES_SCORE2 TEXT,"
                + "$COL_MATCHES_WINNER_ID INTEGER,"
                + "FOREIGN KEY($COL_MATCHES_TOURNAMENT_ID) REFERENCES $TABLE_TOURNAMENTS($COL_ID) ON DELETE CASCADE" + ")")
        db?.execSQL(createMatchesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int ) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TOURNAMENTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PARTICIPANTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        onCreate(db)
    }

    companion object{
        const val DATABASE_NAME = "tournaments.db"
        const val DATABASE_VERSION = 2

        // Constantes Tabla Torneos
        const val TABLE_TOURNAMENTS = "tournaments"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_GAME = "game"
        const val COL_CREATOR_ID = "creator_id"
        const val COL_CREATOR_NICKNAME = "creator_nickname"
        const val COL_MAX_PARTICIPANTS = "max_participants"
        const val COL_DATE = "date"
        const val COL_LOCATION = "location"
        const val COL_PRIZE = "prize"
        const val COL_CODE = "code"
        const val COL_TYPE = "type"
        const val COL_STATUS = "status"
        const val COL_THUMBNAIL = "thumbnail"

        // Constantes Tabla Participantes
        const val TABLE_PARTICIPANTS = "participants"
        const val COL_PARTICIPANTS_ID = "participants_id"
        const val COL_PARTICIPANTS_TOURNAMENT_ID = "participants_tournament_id"
        const val COL_PARTICIPANTS_USER_ID = "user_id"
        const val COL_PARTICIPANTS_NICKNAME = "nickname"
        const val COL_PARTICIPANTS_PUNTUATION = "puntuation"

        // Constantes Tabla Partidos (Lista plana)
        const val TABLE_MATCHES = "matches"
        const val COL_MATCHES_ID = "matches_id"
        const val COL_MATCHES_TOURNAMENT_ID = "matches_tournament_id"
        const val COL_MATCHES_ROUND = "round_number"
        const val COL_MATCHES_P1_ID = "p1_id"
        const val COL_MATCHES_P2_ID = "p2_id"
        const val COL_MATCHES_P1_NAME = "p1_name"
        const val COL_MATCHES_P2_NAME = "p2_name"
        const val COL_MATCHES_SCORE1 = "score1"
        const val COL_MATCHES_SCORE2 = "score2"
        const val COL_MATCHES_WINNER_ID = "winner_id"
    }
}
