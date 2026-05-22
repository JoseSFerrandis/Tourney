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
    fun testAddAndRemoveShowableTournament() {
        user.addShowableTournament(3L)
        assertTrue(user.showableTournamentList.contains(3L))
        
        user.removeShowableTournament(3L)
        assertFalse(user.showableTournamentList.contains(3L))
    }

    @Test
    fun testFollowingTournamentList() {
        user.addFollowingTournament(10L)
        assertTrue(user.followingTournamentList.contains(10L))
        
        user.removeFollowingTournament(10L)
        assertTrue(user.followingTournamentList.isEmpty())
    }

    @Test
    fun testJoinedTournamentList() {
        user.addJoinedTournament(20L)
        assertTrue(user.joinedTournamentList.contains(20L))
        
        user.removeJoinedTournament(20L)
        assertTrue(user.joinedTournamentList.isEmpty())
    }

    @Test
    fun testHasShowableTournament() {
        assertTrue(user.hasShowableTournament(1L))
        assertFalse(user.hasShowableTournament(99L))
    }

    @Test
    fun testSetShowableTournamentList_Parsing() {
        // Test con espacios y valores duplicados
        val stringList = "10, 20 , 30"
        user.setShowableTournamentList(stringList)
        
        assertEquals(3, user.showableTournamentList.size)
        assertEquals(10L, user.showableTournamentList[0])
        assertEquals(20L, user.showableTournamentList[1])
        assertEquals(30L, user.showableTournamentList[2])
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
