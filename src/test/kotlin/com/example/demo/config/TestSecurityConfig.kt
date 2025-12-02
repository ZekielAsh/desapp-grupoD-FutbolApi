package com.example.demo.config

import com.example.demo.security.CustomAccessDeniedHandler
import com.example.demo.security.CustomAuthenticationEntryPoint
import com.example.demo.security.JwtAuthenticationFilter
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Security configuration for test environment.
 * This configuration is active only when the "test" profile is active.
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
class TestSecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val userDetailsService: UserDetailsService,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val accessDeniedHandler: CustomAccessDeniedHandler
) {

    @Bean
    @Primary
    fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(org.springframework.http.HttpMethod.GET,
                    "/teams/*/players",
                    "/*/next-matches",
                    "/api/audit/*",
                    "/predictions/match",
                    "/teams/compare",
                    "/metrics/teams/*",
                    "/metrics/players/*/*"
                ).authenticated()
                it.requestMatchers(org.springframework.http.HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                it.requestMatchers("/auth/**").permitAll()
                it.requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**"
                ).permitAll()
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers("/h2-console/**").permitAll()
                it.anyRequest().authenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .authenticationProvider(testAuthenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    @Primary
    fun testAuthenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Primary
    fun testAuthenticationProvider(): AuthenticationProvider {
        return object : AuthenticationProvider {
            override fun authenticate(authentication: Authentication): Authentication {
                val username = authentication.name
                val password = authentication.credentials.toString()

                val userDetails = userDetailsService.loadUserByUsername(username)
                if (!testPasswordEncoder().matches(password, userDetails.password)) {
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

