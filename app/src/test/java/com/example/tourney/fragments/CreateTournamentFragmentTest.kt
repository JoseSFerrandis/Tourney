package com.example.tourney.fragments

import android.content.Context
import com.example.tourney.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CreateTournamentFragmentTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var fragment: CreateTournamentFragment

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fragment = CreateTournamentFragment()
    }

    @Test
    fun testEstablishedValue_WithValidValue() {
        val result = fragment.establishedValue(mockContext, "Marquitos")
        assertEquals("Marquitos", result)
    }

    @Test
    fun testEstablishedValue_WithNull() {
        `when`(mockContext.getString(R.string.no_established)).thenReturn("No establecido")
        val result = fragment.establishedValue(mockContext, null)
        assertEquals("No establecido", result)
    }

    @Test
    fun testEstablishedValue_WithEmptyString() {
        `when`(mockContext.getString(R.string.no_established)).thenReturn("No establecido")
        val result = fragment.establishedValue(mockContext, "")
        assertEquals("No establecido", result)
    }

    @Test
    fun testEstablishedValue_WithNullStringLiteral() {
        `when`(mockContext.getString(R.string.no_established)).thenReturn("No establecido")
        val result = fragment.establishedValue(mockContext, "null")
        assertEquals("No establecido", result)
    }
}
