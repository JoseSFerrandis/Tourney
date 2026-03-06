package com.example.tourney

import android.content.ContentValues
import android.content.Context
import com.example.tourney.entities.User

// TODO: fix this
class UsersDao(context: Context){
    private val helper = UserDatabaseHelper(context.applicationContext)

    fun insertNewUser(nickname: String, email: String, password: String): Long {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.Companion.COL_NICKNAME, nickname)
            put(UserDatabaseHelper.Companion.COL_EMAIL, email)
            put(UserDatabaseHelper.Companion.COL_PASSWORD, password)
            put(UserDatabaseHelper.Companion.COL_PHOTO, 0)
        }
        return db.insert(UserDatabaseHelper.Companion.TABLE_USERS, null, values)
    }
    fun insertNewUser(nickname: String, email: String, password: String, photo: Int): Long {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.Companion.COL_NICKNAME, nickname)
            put(UserDatabaseHelper.Companion.COL_EMAIL, email)
            put(UserDatabaseHelper.Companion.COL_PASSWORD, password)
            put(UserDatabaseHelper.Companion.COL_PHOTO, photo)
        }
        return db.insert(UserDatabaseHelper.Companion.TABLE_USERS, null, values)
    }

    fun getAllUsers(): List<User> {
        val db = helper.readableDatabase
        val cursor = db.query(
            UserDatabaseHelper.Companion.TABLE_USERS,
            arrayOf(
                UserDatabaseHelper.Companion.COL_ID,
                UserDatabaseHelper.Companion.COL_NICKNAME,
                UserDatabaseHelper.Companion.COL_EMAIL,
                UserDatabaseHelper.Companion.COL_PASSWORD,
                UserDatabaseHelper.Companion.COL_PHOTO
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
                val id = getInt(getColumnIndexOrThrow(UserDatabaseHelper.Companion.COL_ID))
                val nickname = getString(getColumnIndexOrThrow(UserDatabaseHelper.Companion.COL_NICKNAME))
                val email = getString(getColumnIndexOrThrow(UserDatabaseHelper.Companion.COL_EMAIL))
                val password = getString(getColumnIndexOrThrow(UserDatabaseHelper.Companion.COL_PASSWORD))
                val photo = getInt(getColumnIndexOrThrow(UserDatabaseHelper.Companion.COL_PHOTO))
                users.add(User(id, nickname, email, password, photo))
            }
        }
        cursor.close()
        return users
    }


    /*fun actualizarPorId(id: Int, nombre: String, apellidos: String): Int {
        val db = helper.writableDatabase

        val values = ContentValues().apply {
            put(UserDatabaseHelper.COL_NOMBRE, nombre)
            put(UserDatabaseHelper.COL_APELLIDOS, apellidos)
        }

        // Devuelve cuántas filas se actualizaron
        val rowsUpdated = db.update(
            UserDatabaseHelper.TABLE_USERS,
            values,
            "${UserDatabaseHelper.COL_ID}=?",
            arrayOf(id.toString())
        )
        db.close()

        getAllUsers().forEach { user->
            if(user.id == id){
                //MainActivity.actualUser = user
            }
        }



        return rowsUpdated

    }*/
}