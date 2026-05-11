package com.example.tourney.repositories

import android.content.Context
import android.widget.Toast
import androidx.navigation.navOptions
import com.example.tourney.entities.User
import com.example.tourney.models.NewUserModel
import com.example.tourney.tools.APIService
import com.example.tourney.tools.UsersDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class UserRepository(private val dao: UsersDao, private val api: APIService) {
    fun insertNewUser(user: NewUserModel, context: Context, onSuccess: () -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val newUser = api.insertNewUser(user)
                dao.insertNewUser(User(
                    id = newUser.id,
                    nickname = newUser.nickname,
                    email = newUser.email,
                    password = user.passwordHash,
                    photo = newUser.photo
                ))
                withContext(Dispatchers.Main) { onSuccess() } // Toast de éxito y navegación a login
                println("Usuario insertado correctamente")
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