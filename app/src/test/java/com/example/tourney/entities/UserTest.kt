package com.example.tourney.entities

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserTest {

    private lateinit var user: User

    @Before
    fun setUp() {
        user = User(
            id = 1L,
            nickname = "testuser",
            email = "test@example.com",
            password = "password123",
            photo = 0,
            showableTournamentList = mutableListOf(1L, 2L)
        )
    }

    @Test
    fun testAddShowableTournament() {
        user.addShowableTournament(3L)
        assertTrue(user.showableTournamentList.contains(3L))
    }

    @Test
    fun testSetShowableTournamentList() {
        val stringList = "10, 20, 30"
        user.setShowableTournamentList(stringList)
        assertEquals(3, user.showableTournamentList.size)
        assertTrue(user.showableTournamentList.contains(10L))
    }
    
    @Test
    fun testActualUserCompanionObject() {
        User.actualUser = null
        assertNull(User.actualUser)
        User.actualUser = user
        assertEquals(user, User.actualUser)
        User.actualUser = null
    }
}
