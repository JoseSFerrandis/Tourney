package com.example.tourney

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
        // Se ejecuta SOLO la primera vez que se crea la BD
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                   $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                   $COL_NICKNAME TEXT NOT NULL,
                   $COL_EMAIL TEXT NOT NULL,
                   $COL_PASSWORD TEXT NOT NULL,
                   $COL_PHOTO INTEGER NOT NULL
                   );
                   """.trimIndent()
        db.execSQL(createTable)

        val insert = """
            INSERT INTO $TABLE_USERS ($COL_NICKNAME, $COL_EMAIL, $COL_PASSWORD, $COL_PHOTO) VALUES
            ('admin', 'admin@admin.com', 'admin', 0),
            ('user', 'user@user.com', 'user', 0)
        """.trimIndent()
        db.execSQL(insert)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int ) { }

    companion object {
        const val DATABASE_NAME = "users.db"
        const val DATABASE_VERSION = 1
        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_NICKNAME = "nickname"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
        const val COL_PHOTO = "photo"
    }
}
