package com.example.demo.security

import com.example.demo.model.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import com.example.demo.repositories.UserRepository

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userDetailsService: MyUserDetailsService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/login")
    fun authenticate(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        try {
            val authToken = UsernamePasswordAuthenticationToken(request.username, request.password)
            authenticationManager.authenticate(authToken)

            val userDetails = userDetailsService.loadUserByUsername(request.username)
            val token = jwtService.generateToken(userDetails)

            return ResponseEntity.ok(AuthResponse(token))
        } catch (e: BadCredentialsException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } catch (e: UsernameNotFoundException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        if (userRepository.existsByUsername(req.username)) {
            return ResponseEntity.status(409).build()
        }

        val hashed = passwordEncoder.encode(req.password)
        val user = User(username = req.username, password = hashed)
        userRepository.save(user)

        val userDetails = userDetailsService.loadUserByUsername(user.username)
        val token = jwtService.generateToken(userDetails)
        return ResponseEntity.ok(AuthResponse(token))
    }
}

data class AuthRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String)
data class AuthResponse(val token: String)
