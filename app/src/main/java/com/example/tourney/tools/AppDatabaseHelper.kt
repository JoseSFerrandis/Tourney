package com.example.tourney.tools

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        // 1. Tabla de Usuarios
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NICKNAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COL_USER_PASSWORD TEXT NOT NULL,
                $COL_USER_PHOTO INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Tabla de Torneos
        db.execSQL("""
            CREATE TABLE $TABLE_TOURNAMENTS (
                $COL_TRN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TRN_NAME TEXT,
                $COL_TRN_GAME TEXT,
                $COL_TRN_CREATOR_ID INTEGER,
                $COL_TRN_CREATOR_NICKNAME TEXT,
                $COL_TRN_MAX_PARTICIPANTS INTEGER,
                $COL_TRN_DATE INTEGER,
                $COL_TRN_LOCATION TEXT,
                $COL_TRN_PRIZE TEXT,
                $COL_TRN_CODE INTEGER,
                $COL_TRN_TYPE TEXT,
                $COL_TRN_STATUS TEXT,
                $COL_TRN_THUMBNAIL INTEGER DEFAULT 0,
                FOREIGN KEY($COL_TRN_CREATOR_ID) REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE SET NULL
            )
        """.trimIndent())

        // 3. Tabla de Participantes
        db.execSQL("""
            CREATE TABLE $TABLE_PARTICIPANTS (
                $COL_PART_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PART_TRN_ID INTEGER,
                $COL_PART_USER_ID INTEGER,
                $COL_PART_NICKNAME TEXT,
                $COL_PART_PUNTUATION REAL,
                FOREIGN KEY($COL_PART_TRN_ID) REFERENCES $TABLE_TOURNAMENTS($COL_TRN_ID) ON DELETE CASCADE,
                FOREIGN KEY($COL_PART_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE SET NULL
            )
        """.trimIndent())

        // 4. Tabla de Partidos
        db.execSQL("""
            CREATE TABLE $TABLE_MATCHES (
                $COL_MATCH_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MATCH_TRN_ID INTEGER,
                $COL_MATCH_ROUND INTEGER,
                $COL_MATCH_P1_ID INTEGER,
                $COL_MATCH_P2_ID INTEGER,
                $COL_MATCH_P1_NAME TEXT,
                $COL_MATCH_P2_NAME TEXT,
                $COL_MATCH_SCORE1 TEXT,
                $COL_MATCH_SCORE2 TEXT,
                $COL_MATCH_WINNER_ID INTEGER,
                FOREIGN KEY($COL_MATCH_TRN_ID) REFERENCES $TABLE_TOURNAMENTS($COL_TRN_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // 5. Tabla de Relaciones Usuario-Torneo
        db.execSQL("""
            CREATE TABLE $TABLE_USER_TRN_RELATIONS (
                $COL_REL_USER_ID INTEGER,
                $COL_REL_TRN_ID INTEGER,
                $COL_REL_TYPE TEXT, -- 'SHOWABLE', 'FOLLOWING', 'JOINED'
                PRIMARY KEY ($COL_REL_USER_ID, $COL_REL_TRN_ID, $COL_REL_TYPE),
                FOREIGN KEY($COL_REL_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID) ON DELETE CASCADE,
                FOREIGN KEY($COL_REL_TRN_ID) REFERENCES $TABLE_TOURNAMENTS($COL_TRN_ID) ON DELETE CASCADE
            )
        """.trimIndent())

        // Insertar usuarios por defecto (incluyendo invitado)
        db.execSQL("INSERT INTO $TABLE_USERS ($COL_USER_NICKNAME, $COL_USER_EMAIL, $COL_USER_PASSWORD, $COL_USER_PHOTO) VALUES ('admin', 'admin@admin.com', 'admin', 0)")
        db.execSQL("INSERT INTO $TABLE_USERS ($COL_USER_NICKNAME, $COL_USER_EMAIL, $COL_USER_PASSWORD, $COL_USER_PHOTO) VALUES ('user', 'user@user.com', 'user', 0)")
        db.execSQL("INSERT INTO $TABLE_USERS ($COL_USER_NICKNAME, $COL_USER_EMAIL, $COL_USER_PASSWORD, $COL_USER_PHOTO) VALUES ('invitado', 'invitado@invitado.com', 'invitado', 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Al detectar cambio de versión, borramos y recreamos todo para que aparezca el 'invitado'
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_TRN_RELATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MATCHES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTICIPANTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOURNAMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "tourney_app.db"
        const val DATABASE_VERSION = 2 // <--- Subido a 2

        // Tabla Usuarios
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NICKNAME = "nickname"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PASSWORD = "password"
        const val COL_USER_PHOTO = "photo"

        // Tabla Torneos
        const val TABLE_TOURNAMENTS = "tournaments"
        const val COL_TRN_ID = "id"
        const val COL_TRN_NAME = "name"
        const val COL_TRN_GAME = "game"
        const val COL_TRN_CREATOR_ID = "creator_id"
        const val COL_TRN_CREATOR_NICKNAME = "creator_nickname"
        const val COL_TRN_MAX_PARTICIPANTS = "max_participants"
        const val COL_TRN_DATE = "date"
        const val COL_TRN_LOCATION = "location"
        const val COL_TRN_PRIZE = "prize"
        const val COL_TRN_CODE = "code"
        const val COL_TRN_TYPE = "type"
        const val COL_TRN_STATUS = "status"
        const val COL_TRN_THUMBNAIL = "thumbnail"

        // Tabla Participantes
        const val TABLE_PARTICIPANTS = "participants"
        const val COL_PART_ID = "participants_id"
        const val COL_PART_TRN_ID = "participants_tournament_id"
        const val COL_PART_USER_ID = "user_id"
        const val COL_PART_NICKNAME = "nickname"
        const val COL_PART_PUNTUATION = "puntuation"

        // Tabla Partidos
        const val TABLE_MATCHES = "matches"
        const val COL_MATCH_ID = "matches_id"
        const val COL_MATCH_TRN_ID = "matches_tournament_id"
        const val COL_MATCH_ROUND = "round_number"
        const val COL_MATCH_P1_ID = "p1_id"
        const val COL_MATCH_P2_ID = "p2_id"
        const val COL_MATCH_P1_NAME = "p1_name"
        const val COL_MATCH_P2_NAME = "p2_name"
        const val COL_MATCH_SCORE1 = "score1"
        const val COL_MATCH_SCORE2 = "score2"
        const val COL_MATCH_WINNER_ID = "winner_id"

        // Tabla Relaciones
        const val TABLE_USER_TRN_RELATIONS = "user_tournament_relations"
        const val COL_REL_USER_ID = "user_id"
        const val COL_REL_TRN_ID = "tournament_id"
        const val COL_REL_TYPE = "relation_type"
        
        const val REL_TYPE_SHOWABLE = "SHOWABLE"
        const val REL_TYPE_FOLLOWING = "FOLLOWING"
        const val REL_TYPE_JOINED = "JOINED"
    }
}
