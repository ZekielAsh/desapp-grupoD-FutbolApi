package com.example.demo.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.util.ReflectionTestUtils
import java.util.Date

class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private val secretKey = "mySecretKeyThatIsLongEnoughForHS256AlgorithmToWork"

    @BeforeEach
    fun setup() {
        jwtService = JwtService()
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey)
    }

    @Test
    fun `test generateToken creates valid token`() {
        val userDetails: UserDetails = User("testuser", "password", emptyList())

        val token = jwtService.generateToken(userDetails)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `test extractUsername returns correct username`() {
        val userDetails: UserDetails = User("johndoe", "password123", emptyList())
        val token = jwtService.generateToken(userDetails)

        val username = jwtService.extractUsername(token)

        assertEquals("johndoe", username)
    }

    @Test
    fun `test isTokenValid returns true for valid token`() {
        val userDetails: UserDetails = User("validuser", "password", emptyList())
        val token = jwtService.generateToken(userDetails)

        val isValid = jwtService.isTokenValid(token, userDetails)

        assertTrue(isValid)
    }

    @Test
    fun `test isTokenValid returns false for different user`() {
        val userDetails1: UserDetails = User("user1", "password", emptyList())
        val userDetails2: UserDetails = User("user2", "password", emptyList())
        val token = jwtService.generateToken(userDetails1)

        val isValid = jwtService.isTokenValid(token, userDetails2)

        assertFalse(isValid)
    }

    @Test
    fun `test extractClaim extracts subject`() {
        val userDetails: UserDetails = User("claimuser", "password", emptyList())
        val token = jwtService.generateToken(userDetails)

        val subject = jwtService.extractClaim(token) { it.subject }

        assertEquals("claimuser", subject)
    }

    @Test
    fun `test extractClaim extracts expiration date`() {
        val userDetails: UserDetails = User("expiryuser", "password", emptyList())
        val token = jwtService.generateToken(userDetails)

        val expiration = jwtService.extractClaim(token) { it.expiration }

        assertNotNull(expiration)
        assertTrue(expiration.after(Date()))
    }

    @Test
    fun `test token contains issued at date`() {
        val userDetails: UserDetails = User("issueduser", "password", emptyList())
        val token = jwtService.generateToken(userDetails)

        val issuedAt = jwtService.extractClaim(token) { it.issuedAt }

        assertNotNull(issuedAt)
        assertTrue(issuedAt.before(Date(System.currentTimeMillis() + 1000)))
    }

    @Test
    fun `test token expiration is set correctly`() {
        val userDetails: UserDetails = User("timeuser", "password", emptyList())
        val beforeGeneration = System.currentTimeMillis()
        val token = jwtService.generateToken(userDetails)
        val afterGeneration = System.currentTimeMillis()

        val expiration = jwtService.extractClaim(token) { it.expiration }
        val expirationTime = expiration.time

        // Token should expire in 10 hours (36000000 ms)
        val tenHoursInMs = 10 * 60 * 60 * 1000L
        val buffer = 1000L // 1 second buffer for timing differences

        val expectedMinExpiration = beforeGeneration + tenHoursInMs - buffer
        val expectedMaxExpiration = afterGeneration + tenHoursInMs + buffer

        assertTrue(expirationTime >= expectedMinExpiration,
            "Expiration time $expirationTime should be >= $expectedMinExpiration")
        assertTrue(expirationTime <= expectedMaxExpiration,
            "Expiration time $expirationTime should be <= $expectedMaxExpiration")
    }

    @Test
    fun `test multiple tokens for same user are different`() {
        val userDetails: UserDetails = User("multiuser", "password", emptyList())

        val token1 = jwtService.generateToken(userDetails)
        Thread.sleep(1001) // JWT uses seconds precision, so we need to wait at least 1 second
        val token2 = jwtService.generateToken(userDetails)

        assertNotEquals(token1, token2, "Tokens generated at different times should be different")
    }

    @Test
    fun `test token structure has three parts`() {
        val userDetails: UserDetails = User("structureuser", "password", emptyList())
        val token = jwtService.generateToken(userDetails)

        val parts = token.split(".")

        assertEquals(3, parts.size) // JWT has header.payload.signature
    }
}

