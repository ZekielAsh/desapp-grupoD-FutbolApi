package com.example.demo.unitTests.security

import com.example.demo.model.User
import com.example.demo.repositories.UserRepository
import com.example.demo.security.MyUserDetailsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class MyUserDetailsServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userDetailsService: MyUserDetailsService

    @Test
    fun `test loadUserByUsername returns UserDetails when user exists`() {
        val username = "testuser"
        val password = "encodedPassword"
        val user = User(id = 1L, username = username, password = password)

        whenever(userRepository.findByUsername(username)).thenReturn(user)

        val userDetails = userDetailsService.loadUserByUsername(username)

        assertNotNull(userDetails)
        assertEquals(username, userDetails.username)
        assertEquals(password, userDetails.password)
        assertTrue(userDetails.authorities.isEmpty())
    }

    @Test
    fun `test loadUserByUsername throws exception when user not found`() {
        val username = "nonexistent"

        whenever(userRepository.findByUsername(username)).thenReturn(null)

        val exception = assertThrows(UsernameNotFoundException::class.java) {
            userDetailsService.loadUserByUsername(username)
        }

        assertEquals("Usuario no encontrado", exception.message)
    }

    @Test
    fun `test loadUserByUsername with different users`() {
        val user1 = User(id = 1L, username = "user1", password = "pass1")
        val user2 = User(id = 2L, username = "user2", password = "pass2")

        whenever(userRepository.findByUsername("user1")).thenReturn(user1)
        whenever(userRepository.findByUsername("user2")).thenReturn(user2)

        val details1 = userDetailsService.loadUserByUsername("user1")
        val details2 = userDetailsService.loadUserByUsername("user2")

        assertEquals("user1", details1.username)
        assertEquals("pass1", details1.password)
        assertEquals("user2", details2.username)
        assertEquals("pass2", details2.password)
    }

    @Test
    fun `test loadUserByUsername returns user with empty authorities`() {
        val user = User(id = 1L, username = "simpleuser", password = "password")

        whenever(userRepository.findByUsername("simpleuser")).thenReturn(user)

        val userDetails = userDetailsService.loadUserByUsername("simpleuser")

        assertTrue(userDetails.authorities.isEmpty())
    }
}

