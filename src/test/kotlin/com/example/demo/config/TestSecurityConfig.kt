package com.example.demo.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Security configuration for test environment.
 * Provides AuthenticationManager and related beans for tests.
 * Does NOT redefine SecurityFilterChain to avoid conflicts.
 */
@TestConfiguration
@Profile("test")
class TestSecurityConfig {

    @Bean
    fun testAuthenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun testPasswordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun testAuthenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        return object : AuthenticationProvider {
            override fun authenticate(authentication: Authentication): Authentication {
                val username = authentication.name
                val password = authentication.credentials.toString()

                val userDetails = userDetailsService.loadUserByUsername(username)
                if (!passwordEncoder.matches(password, userDetails.password)) {
                    throw BadCredentialsException("Invalid credentials")
                }

                return UsernamePasswordAuthenticationToken(
                    userDetails.username,
                    userDetails.password,
                    userDetails.authorities
                )
            }

            override fun supports(authentication: Class<*>): Boolean {
                return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
            }
        }
    }
}

