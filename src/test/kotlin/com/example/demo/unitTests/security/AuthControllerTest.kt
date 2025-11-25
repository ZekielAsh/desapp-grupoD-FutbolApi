package com.example.demo.unitTests.security

import com.example.demo.model.User
import com.example.demo.repositories.UserRepository
import com.example.demo.security.AuthController
import com.example.demo.security.AuthRequest
import com.example.demo.security.AuthResponse
import com.example.demo.security.JwtService
import com.example.demo.security.MyUserDetailsService
import com.example.demo.security.RegisterRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.junit.jupiter.api.Assertions.*
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockitoExtension::class)
class AuthControllerTest {

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtService: JwtService

    @Mock
    private lateinit var userDetailsService: MyUserDetailsService

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var userDetails: UserDetails

    @InjectMocks
    private lateinit var authController: AuthController

    @Test
    fun `test authenticate returns token on successful login`() {
        val authRequest = AuthRequest("testuser", "password123")
        val expectedToken = "jwt-token-12345"

        whenever(authenticationManager.authenticate(any())).thenReturn(null)
        whenever(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails)
        whenever(jwtService.generateToken(userDetails)).thenReturn(expectedToken)

        val response = authController.authenticate(authRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(expectedToken, response.body!!.token)
        verify(authenticationManager).authenticate(any<UsernamePasswordAuthenticationToken>())
    }

    @Test
    fun `test authenticate returns unauthorized on invalid credentials`() {
        val authRequest = AuthRequest("baduser", "badpass")

        whenever(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException("Invalid credentials"))

        val response = authController.authenticate(authRequest)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `test authenticate returns unauthorized on user not found`() {
        val authRequest = AuthRequest("nonexistent", "password")

        whenever(authenticationManager.authenticate(any()))
            .thenThrow(UsernameNotFoundException("User not found"))

        val response = authController.authenticate(authRequest)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `test register creates new user and returns token`() {
        val registerRequest = RegisterRequest("newuser", "newpass")
        val encodedPassword = "encoded-password"
        val expectedToken = "new-user-token"
        val savedUser = User(id = 1L, username = "newuser", password = encodedPassword)

        whenever(userRepository.existsByUsername("newuser")).thenReturn(false)
        whenever(passwordEncoder.encode("newpass")).thenReturn(encodedPassword)
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)
        whenever(userDetailsService.loadUserByUsername("newuser")).thenReturn(userDetails)
        whenever(jwtService.generateToken(userDetails)).thenReturn(expectedToken)

        val response = authController.register(registerRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(expectedToken, response.body!!.token)
        verify(userRepository).save(argThat {
            username == "newuser" && password == encodedPassword
        })
    }

    @Test
    fun `test register returns conflict when username already exists`() {
        val registerRequest = RegisterRequest("existinguser", "password")

        whenever(userRepository.existsByUsername("existinguser")).thenReturn(true)

        val response = authController.register(registerRequest)

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNull(response.body)
        verify(userRepository, never()).save(any())
        verify(passwordEncoder, never()).encode(any())
    }

    @Test
    fun `test register encodes password before saving`() {
        val registerRequest = RegisterRequest("user", "plainpassword")
        val encodedPassword = "super-encoded-password"
        val savedUser = User(id = 1L, username = "user", password = encodedPassword)

        whenever(userRepository.existsByUsername(any())).thenReturn(false)
        whenever(passwordEncoder.encode("plainpassword")).thenReturn(encodedPassword)
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)
        whenever(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails)
        whenever(jwtService.generateToken(any())).thenReturn("token")

        authController.register(registerRequest)

        verify(passwordEncoder).encode("plainpassword")
        verify(userRepository).save(argThat { password == encodedPassword })
    }

    @Test
    fun `test AuthRequest data class`() {
        val authRequest = AuthRequest("user", "pass")

        assertEquals("user", authRequest.username)
        assertEquals("pass", authRequest.password)
    }

    @Test
    fun `test RegisterRequest data class`() {
        val registerRequest = RegisterRequest("newuser", "newpass")

        assertEquals("newuser", registerRequest.username)
        assertEquals("newpass", registerRequest.password)
    }

    @Test
    fun `test AuthResponse data class`() {
        val authResponse = AuthResponse("my-token-123")

        assertEquals("my-token-123", authResponse.token)
    }

    @Test
    fun `test authenticate with different users`() {
        val user1Request = AuthRequest("user1", "pass1")
        val user2Request = AuthRequest("user2", "pass2")

        whenever(authenticationManager.authenticate(any())).thenReturn(null)
        whenever(userDetailsService.loadUserByUsername("user1")).thenReturn(userDetails)
        whenever(userDetailsService.loadUserByUsername("user2")).thenReturn(userDetails)
        whenever(jwtService.generateToken(any())).thenReturn("token1", "token2")

        val response1 = authController.authenticate(user1Request)
        val response2 = authController.authenticate(user2Request)

        assertEquals("token1", response1.body!!.token)
        assertEquals("token2", response2.body!!.token)
    }
}

