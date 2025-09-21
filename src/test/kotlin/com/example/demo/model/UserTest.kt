package com.example.demo.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun testUserCreation() {
        val user = User(
            username = "testuser",
            password = "password123")
        assertEquals("testuser", user.username)
        assertEquals("password123", user.password)
    }
}