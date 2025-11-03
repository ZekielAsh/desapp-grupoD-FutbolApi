package com.example.demo.model

import org.junit.jupiter.api.Assertions.*
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

    @Test
    fun `test User creation with id`() {
        val user = User(
            id = 1L,
            username = "userWithId",
            password = "pass123"
        )

        assertEquals(1L, user.id)
        assertEquals("userWithId", user.username)
        assertEquals("pass123", user.password)
    }

    @Test
    fun `test User no-arg constructor`() {
        val user = User()

        assertNull(user.id)
        assertEquals("", user.username)
        assertEquals("", user.password)
    }

    @Test
    fun `test User with empty username and password`() {
        val user = User(username = "", password = "")

        assertEquals("", user.username)
        assertEquals("", user.password)
    }

    @Test
    fun `test User property modification`() {
        val user = User(username = "original", password = "oldpass")

        user.username = "modified"
        user.password = "newpass"
        user.id = 10L

        assertEquals(10L, user.id)
        assertEquals("modified", user.username)
        assertEquals("newpass", user.password)
    }

    @Test
    fun `test User with long username and password`() {
        val longUsername = "a".repeat(100)
        val longPassword = "b".repeat(100)
        val user = User(username = longUsername, password = longPassword)

        assertEquals(100, user.username.length)
        assertEquals(100, user.password.length)
    }
}