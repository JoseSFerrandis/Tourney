package com.example.tourney.tools

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.tourney.entities.User

class UsersDao(context: Context) {
    private val helper = AppDatabaseHelper(context.applicationContext)

    /**
     * Borra las tablas de usuarios y relaciones para empezar de cero (Uso en Debug)
     */
    fun dropAll() {
        val db = helper.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS ${AppDatabaseHelper.TABLE_USER_TRN_RELATIONS}")
        db.execSQL("DROP TABLE IF EXISTS ${AppDatabaseHelper.TABLE_USERS}")
        helper.onCreate(db)
        db.close()
    }

    /**
     * Inserta un nuevo usuario en la base de datos
     * @return el ID del usuario insertado o -1 si hubo un error
     */
    fun insertNewUser(nickname: String, email: String, password: String, photo: Int = 1): Long {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_USER_NICKNAME, nickname)
            put(AppDatabaseHelper.COL_USER_EMAIL, email)
            put(AppDatabaseHelper.COL_USER_PASSWORD, password)
            put(AppDatabaseHelper.COL_USER_PHOTO, photo)
        }
        val id = db.insert(AppDatabaseHelper.TABLE_USERS, null, values)
        db.close()
        return id
    }

    /**
     * Recupera un usuario por su ID
     */
    fun getUserById(id: Long): User? {
        val db = helper.readableDatabase
        val cursor = db.query(
            AppDatabaseHelper.TABLE_USERS,
            null,
            "${AppDatabaseHelper.COL_USER_ID}=?",
            arrayOf(id.toString()),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = id,
                nickname = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_NICKNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PASSWORD)),
                photo = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PHOTO)),
                showableTournamentList = getRelationsForUser(id, AppDatabaseHelper.REL_TYPE_SHOWABLE, db),
                followingTournamentList = getRelationsForUser(id, AppDatabaseHelper.REL_TYPE_FOLLOWING, db),
                joinedTournamentList = getRelationsForUser(id, AppDatabaseHelper.REL_TYPE_JOINED, db)
            )
        }
        cursor.close()
        db.close()
        return user
    }

    /**
     * Recupera un usuario por su ID
     */
    fun getUserById(id: Long): User? {
        val db = helper.readableDatabase
        val cursor = db.query(
            AppDatabaseHelper.TABLE_USERS,
            null,
            "${AppDatabaseHelper.COL_USER_ID}=?",
            arrayOf(id.toString()),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = id,
                nickname = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_NICKNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PASSWORD)),
                photo = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PHOTO)),
                showableTournamentList = getRelationsForUser(id, AppDatabaseHelper.REL_TYPE_SHOWABLE, db),
                followingTournamentList = getRelationsForUser(id, AppDatabaseHelper.REL_TYPE_FOLLOWING, db),
                joinedTournamentList = getRelationsForUser(id, AppDatabaseHelper.REL_TYPE_JOINED, db)
            )
        }
        cursor.close()
        db.close()
        return user
    }

    /**
     * Recupera todos los usuarios de la base de datos reconstruyendo sus listas de torneos
     */
    fun getAllUsers(): List<User> {
        val db = helper.readableDatabase
        val cursor = db.query(AppDatabaseHelper.TABLE_USERS, null, null, null, null, null, null)
        val users = mutableListOf<User>()

        with(cursor) {
            while (moveToNext()) {
                val userId = getLong(getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_ID))
                val user = User(
                    id = userId,
                    nickname = getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_NICKNAME)),
                    email = getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_EMAIL)),
                    password = getString(getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PASSWORD)),
                    photo = getInt(getColumnIndexOrThrow(AppDatabaseHelper.COL_USER_PHOTO)),
                    showableTournamentList = getRelationsForUser(userId, AppDatabaseHelper.REL_TYPE_SHOWABLE, db),
                    followingTournamentList = getRelationsForUser(userId, AppDatabaseHelper.REL_TYPE_FOLLOWING, db),
                    joinedTournamentList = getRelationsForUser(userId, AppDatabaseHelper.REL_TYPE_JOINED, db)
                )
                users.add(user)
            }
        }
        cursor.close()
        db.close()
        return users
    }

    /**
     * Obtiene los IDs de torneos relacionados con un usuario específico
     */
    private fun getRelationsForUser(userId: Long, type: String, db: SQLiteDatabase): MutableList<Long> {
        val list = mutableListOf<Long>()
        val cursor = db.query(
            AppDatabaseHelper.TABLE_USER_TRN_RELATIONS,
            arrayOf(AppDatabaseHelper.COL_REL_TRN_ID),
            "${AppDatabaseHelper.COL_REL_USER_ID}=? AND ${AppDatabaseHelper.COL_REL_TYPE}=?",
            arrayOf(userId.toString(), type),
            null, null, null
        )
        while (cursor.moveToNext()) {
            list.add(cursor.getLong(0))
        }
        cursor.close()
        return list
    }

    /**
     * Actualiza la contraseña de un usuario
     */
    fun updatePassword(email: String, password: String): Int {
        val db = helper.writableDatabase
        val values = ContentValues().apply { put(AppDatabaseHelper.COL_USER_PASSWORD, password) }
        val rows = db.update(AppDatabaseHelper.TABLE_USERS, values, "${AppDatabaseHelper.COL_USER_EMAIL}=?", arrayOf(email))
        db.close()
        return rows
    }

    /**
     * Actualiza el avatar del usuario
     */
    fun updateAvatar(email: String, photo: Int): Int {
        val db = helper.writableDatabase
        val values = ContentValues().apply { put(AppDatabaseHelper.COL_USER_PHOTO, photo) }
        val rows = db.update(AppDatabaseHelper.TABLE_USERS, values, "${AppDatabaseHelper.COL_USER_EMAIL}=?", arrayOf(email))
        db.close()
        return rows
    }

    /**
     * Crea una relación entre un usuario y un torneo
     */
    fun addTournamentRelation(userId: Long, tournamentId: Long, type: String) {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_REL_USER_ID, userId)
            put(AppDatabaseHelper.COL_REL_TRN_ID, tournamentId)
            put(AppDatabaseHelper.COL_REL_TYPE, type)
        }
        db.insertWithOnConflict(AppDatabaseHelper.TABLE_USER_TRN_RELATIONS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        db.close()
    }

    /**
     * Elimina una relación específica entre un usuario y un torneo
     */
    fun removeTournamentRelation(userId: Long, tournamentId: Long, type: String) {
        val db = helper.writableDatabase
        db.delete(
            AppDatabaseHelper.TABLE_USER_TRN_RELATIONS,
            "${AppDatabaseHelper.COL_REL_USER_ID}=? AND ${AppDatabaseHelper.COL_REL_TRN_ID}=? AND ${AppDatabaseHelper.COL_REL_TYPE}=?",
            arrayOf(userId.toString(), tournamentId.toString(), type)
        )
        db.close()
    }

    /**
     * Método interno para sincronizar relaciones desde un String separado por comas
     */
    private fun syncTournamentRelations(email: String, listString: String, type: String) {
        val userId = getUserIdByEmail(email)
        if (userId == -1L) return

        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                AppDatabaseHelper.TABLE_USER_TRN_RELATIONS,
                "${AppDatabaseHelper.COL_REL_USER_ID}=? AND ${AppDatabaseHelper.COL_REL_TYPE}=?",
                arrayOf(userId.toString(), type)
            )

            val ids = listString.split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { it.trim().toLongOrNull() }

            ids.forEach { trnId ->
                val values = ContentValues().apply {
                    put(AppDatabaseHelper.COL_REL_USER_ID, userId)
                    put(AppDatabaseHelper.COL_REL_TRN_ID, trnId)
                    put(AppDatabaseHelper.COL_REL_TYPE, type)
                }
                db.insert(AppDatabaseHelper.TABLE_USER_TRN_RELATIONS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun updateShowableTournamentList(email: String, listString: String) {
        syncTournamentRelations(email, listString, AppDatabaseHelper.REL_TYPE_SHOWABLE)
    }

    fun updateFollowingTournamentList(email: String, listString: String) {
        syncTournamentRelations(email, listString, AppDatabaseHelper.REL_TYPE_FOLLOWING)
    }

    fun updateJoinedTournamentList(email: String, listString: String) {
        syncTournamentRelations(email, listString, AppDatabaseHelper.REL_TYPE_JOINED)
    }

    fun getUserIdByEmail(email: String): Long {
        val db = helper.readableDatabase
        val cursor = db.query(AppDatabaseHelper.TABLE_USERS, arrayOf(AppDatabaseHelper.COL_USER_ID), "${AppDatabaseHelper.COL_USER_EMAIL}=?", arrayOf(email), null, null, null)
        val id = if (cursor.moveToFirst()) cursor.getLong(0) else -1L
        cursor.close()
        db.close()
        return id
    }

    fun getUsernameById(id: Long): String {
        val db = helper.readableDatabase
        val cursor = db.query(AppDatabaseHelper.TABLE_USERS, arrayOf(AppDatabaseHelper.COL_USER_NICKNAME), "${AppDatabaseHelper.COL_USER_ID}=?", arrayOf(id.toString()), null, null, null)
        val name = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        db.close()
        return name
    }

    fun updateUser(id: Long, nickname: String, email: String, password: String): Int {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(AppDatabaseHelper.COL_USER_NICKNAME, nickname)
            put(AppDatabaseHelper.COL_USER_EMAIL, email)
            put(AppDatabaseHelper.COL_USER_PASSWORD, password)
        }
        val rows = db.update(AppDatabaseHelper.TABLE_USERS, values, "${AppDatabaseHelper.COL_USER_ID}=?", arrayOf(id.toString()))
        db.close()
        return rows
    }
}
