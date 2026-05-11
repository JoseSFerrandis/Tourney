package com.example.tourney.repositories

import android.content.Context
import android.widget.Toast
import com.example.tourney.entities.User
import com.example.tourney.tools.APIService
import com.example.tourney.tools.UsersDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class UserRepository(private val dao: UsersDao, private val api: APIService) {
    fun insertNewUser(user: User, context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val newUser = api.insertNewUser(user)
                dao.insertNewUser(newUser)
            }catch (e: Exception){
                println("Error al insertar usuario")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al insertar usuario", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    companion object{
        private var instance: UserRepository? = null
        fun getInstance(dao: UsersDao, api: APIService): UserRepository{
            if(instance == null){
                instance = UserRepository(dao, api)
            }
            return instance!!
        }
    }
}