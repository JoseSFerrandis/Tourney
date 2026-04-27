package com.example.tourney.tools

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tourney.entities.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsersDaoInstrumentedTest {

    private lateinit var usersDao: UsersDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        usersDao = UsersDao(context)
        // Nota: En un entorno de test real se preferiría usar una BD en memoria, 
        // pero como no podemos modificar UsersDao para inyectar el helper, 
        // el test actuará sobre la base de datos de la app de pruebas.
    }

    @Test
    fun testInsertAndGetAllUsers() {
        val nickname = "TestUser"
        val email = "test@example.com"
        val password = "password123"
        
        val id = usersDao.insertNewUser(nickname, email, password)
        assertTrue(id > 0)

        val users = usersDao.getAllUsers()
        val found = users.any { it.email == email && it.nickname == nickname }
        assertTrue("User should be in the list", found)
    }

    @Test
    fun testUpdatePassword() {
        val email = "update@test.com"
        usersDao.insertNewUser("UpdateMe", email, "oldPass")
        
        val rowsAffected = usersDao.updatePassword(email, "newPass")
        assertEquals(1, rowsAffected)

        val users = usersDao.getAllUsers()
        val user = users.find { it.email == email }
        assertEquals("newPass", user?.password)
    }

    @Test
    fun testUpdateAvatar() {
        val email = "avatar@test.com"
        usersDao.insertNewUser("AvatarUser", email, "pass", 1)
        
        val rowsAffected = usersDao.updateAvatar(email, 2)
        assertEquals(1, rowsAffected)

        val users = usersDao.getAllUsers()
        val user = users.find { it.email == email }
        assertEquals(2, user?.photo)
    }
}
