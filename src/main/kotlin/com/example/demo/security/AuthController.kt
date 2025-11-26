package com.example.demo.security

import com.example.demo.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Authentication", description = "User authentication endpoints")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userDetailsService: MyUserDetailsService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Login successful",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AuthResponse::class),
                examples = [ExampleObject(value = """{"token": "string"}""")]
            )]),
        ApiResponse(responseCode = "401", description = "Invalid credentials")
    ])
    @PostMapping("/login")
    fun authenticate(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login credentials",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = AuthRequest::class),
                examples = [ExampleObject(value = """{"username": "string", "password": "string"}""")]
            )]
        )
        @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
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

    @Operation(summary = "User registration", description = "Register a new user and get JWT token")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Registration successful",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = AuthResponse::class),
                examples = [ExampleObject(value = """{"token": "string"}""")]
            )]),
        ApiResponse(responseCode = "409", description = "Username already exists")
    ])
    @PostMapping("/register")
    fun register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Registration credentials",
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = RegisterRequest::class),
                examples = [ExampleObject(value = """{"username": "string", "password": "string"}""")]
            )]
        )
        @RequestBody req: RegisterRequest
    ): ResponseEntity<AuthResponse> {
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

@Schema(description = "Authentication request")
data class AuthRequest(
    @Schema(description = "Username", example = "string")
    val username: String,
    @Schema(description = "Password", example = "string")
    val password: String
)

@Schema(description = "Registration request")
data class RegisterRequest(
    @Schema(description = "Username", example = "string")
    val username: String,
    @Schema(description = "Password", example = "string")
    val password: String
)

@Schema(description = "Authentication response")
data class AuthResponse(
    @Schema(description = "JWT token", example = "string")
    val token: String
)
