package com.example.tourney.tools

import android.content.ContentValues
import android.content.Context
import com.example.tourney.entities.User

class UsersDao(context: Context){
    private val helper = UserDatabaseHelper(context.applicationContext)

    fun dropAll(){
        //DEBUG
        val db = helper.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS ${UserDatabaseHelper.TABLE_USERS}")
        helper.onCreate(db)
        db.close()
    }

    fun insertNewUser(nickname: String, email: String, password: String): Long {
        return insertNewUser(nickname, email, password, 1)
    }
    fun insertNewUser(nickname: String, email: String, password: String, photo: Int): Long {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.COL_NICKNAME, nickname)
            put(UserDatabaseHelper.COL_EMAIL, email)
            put(UserDatabaseHelper.COL_PASSWORD, password)
            put(UserDatabaseHelper.COL_PHOTO, photo)
            put(UserDatabaseHelper.COL_LIST_SHOWABLE_TOURNAMENTS, "")
        }
        return db.insert(UserDatabaseHelper.TABLE_USERS, null, values)
    }

    fun getAllUsers(): List<User> {
        val db = helper.readableDatabase
        val cursor = db.query(
            UserDatabaseHelper.TABLE_USERS,
            arrayOf(
                UserDatabaseHelper.COL_ID,
                UserDatabaseHelper.COL_NICKNAME,
                UserDatabaseHelper.COL_EMAIL,
                UserDatabaseHelper.COL_PASSWORD,
                UserDatabaseHelper.COL_PHOTO,
                UserDatabaseHelper.COL_LIST_SHOWABLE_TOURNAMENTS
            ),
            null,
            null,
            null,
            null,
            null
        )

        val users = mutableListOf<User>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(UserDatabaseHelper.COL_ID))
                val nickname = getString(getColumnIndexOrThrow(UserDatabaseHelper.COL_NICKNAME))
                val email = getString(getColumnIndexOrThrow(UserDatabaseHelper.COL_EMAIL))
                val password = getString(getColumnIndexOrThrow(UserDatabaseHelper.COL_PASSWORD))
                val photo = getInt(getColumnIndexOrThrow(UserDatabaseHelper.COL_PHOTO))
                val showableTournamentList = getString(getColumnIndexOrThrow(UserDatabaseHelper.COL_LIST_SHOWABLE_TOURNAMENTS))
                if(showableTournamentList != "")
                    users.add(User(id, nickname, email, password, photo, showableTournamentList.split(",").map { it.trim().toLong() }.toMutableList()))
                else
                    users.add(User(id, nickname, email, password, photo))
            }
        }
        cursor.close()
        return users
    }


    fun updatePassword(email: String, password: String): Int {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.COL_PASSWORD, password)
        }

        // Devuelve cuántas filas se actualizaron
        val rowsUpdated = db.update(
            UserDatabaseHelper.TABLE_USERS,
            values,
            "${UserDatabaseHelper.COL_EMAIL}=?",
            arrayOf(email)
        )
        db.close()
        return rowsUpdated
    }

    fun updateAvatar(email: String, photo: Int): Int {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.COL_PHOTO, photo)
        }

        val rowsUpdated = db.update(
            UserDatabaseHelper.TABLE_USERS,
            values,
            "${UserDatabaseHelper.COL_EMAIL}=?",
            arrayOf(email)
        )
        db.close()
        return rowsUpdated
    }

    public fun updateShowableTournamentList(email: String, showableTournamentList: String): Int {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.COL_LIST_SHOWABLE_TOURNAMENTS, showableTournamentList)
        }

        // Devuelve cuántas filas se actualizaron
        val rowsUpdated = db.update(
            UserDatabaseHelper.TABLE_USERS,
            values,
            "${UserDatabaseHelper.COL_EMAIL}=?",
            arrayOf(email)
        )
        db.close()
        return rowsUpdated
    }
}