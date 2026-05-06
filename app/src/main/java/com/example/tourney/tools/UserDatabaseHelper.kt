package com.example.tourney.tools

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    )
{
    override fun onCreate(db: SQLiteDatabase) {
        createTableUsers(db)
        insertDefaultUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Si subimos de versión, borramos todo y empezamos de cero
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    companion object {
        // Cambiamos el nombre radicalmente para forzar un archivo nuevo
        const val DATABASE_NAME = "tourney_v3_clean.db"
        const val DATABASE_VERSION = 1
        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_NICKNAME = "nickname"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_PHOTO = "photo"
        const val COL_LIST_SHOWABLE_TOURNAMENTS = "list_showable_tournaments"
        const val COL_LIST_FOLLOWING_TOURNAMENTS = "list_following_tournaments"
        const val COL_LIST_JOINED_TOURNAMENTS = "list_joined_tournaments"
        //const val COL_LIST_ADMIN_TOURNAMENTS = "list_admin_tournaments"
    }

    fun createTableUsers(db: SQLiteDatabase) {
        val createTable = """
        CREATE TABLE $TABLE_USERS (
               $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
               $COL_NICKNAME TEXT NOT NULL,
               $COL_EMAIL TEXT NOT NULL,
               $COL_PASSWORD TEXT NOT NULL,
               $COL_PHOTO INTEGER NOT NULL,
               $COL_LIST_SHOWABLE_TOURNAMENTS TEXT,
               $COL_LIST_FOLLOWING_TOURNAMENTS TEXT,
               $COL_LIST_JOINED_TOURNAMENTS TEXT
               );
               """.trimIndent()
        db.execSQL(createTable)
    }

    fun insertDefaultUsers(db: SQLiteDatabase) {
        val insert = """
        INSERT INTO $TABLE_USERS ($COL_NICKNAME, $COL_EMAIL, $COL_PASSWORD, $COL_PHOTO, $COL_LIST_SHOWABLE_TOURNAMENTS, $COL_LIST_FOLLOWING_TOURNAMENTS, $COL_LIST_JOINED_TOURNAMENTS) VALUES
        ('admin', 'admin@admin.com', 'admin', 0, "", "", ""),
        ('user', 'user@user.com', 'user', 0, "", "", "")
    """.trimIndent()
        db.execSQL(insert)
    }
}