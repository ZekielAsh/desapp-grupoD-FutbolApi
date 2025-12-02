package com.example.demo.unitTests.authentication

import com.example.demo.model.User
import com.example.demo.repositories.UserRepository
import com.example.demo.security.AuthRequest
import com.example.demo.security.RegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = ["spring.jpa.show-sql=false"])
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder


    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `test register new user successfully`() {
        val registerRequest = RegisterRequest("newuser", "password123")

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test register returns conflict when user already exists`() {
        val user = User(username = "existinguser", password = passwordEncoder.encode("pass"))
        userRepository.saveAndFlush(user)

        val registerRequest = RegisterRequest("existinguser", "newpass")

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `test login with valid credentials`() {
        val user = User(username = "testuser", password = passwordEncoder.encode("testpass"))
        userRepository.saveAndFlush(user)

        val authRequest = AuthRequest("testuser", "testpass")

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }

    @Test
    fun `test login with invalid credentials`() {
        val user = User(username = "testuser", password = passwordEncoder.encode("correctpass"))
        userRepository.saveAndFlush(user)

        val authRequest = AuthRequest("testuser", "wrongpass")

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `test login with non-existent user`() {
        val authRequest = AuthRequest("nonexistent", "password")

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
        )
            .andExpect(status().isUnauthorized)
    }
}

