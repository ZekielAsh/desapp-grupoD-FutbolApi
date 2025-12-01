package com.example.demo.security

import com.example.demo.model.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            val token = authHeader?.takeIf { it.startsWith("Bearer ") }?.substring(7)

            if (!token.isNullOrBlank()) {
                try {
                    val username = jwtService.extractUsername(token)
                    if (username != null && SecurityContextHolder.getContext().authentication == null) {
                        val userDetails = userDetailsService.loadUserByUsername(username)
                        if (jwtService.isTokenValid(token, userDetails)) {
                            val authToken = UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.authorities
                            )
                            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = authToken
                        } else {
                            handleInvalidToken(response, "Invalid or expired token")
                            return
                        }
                    }
                } catch (e: io.jsonwebtoken.ExpiredJwtException) {
                    handleInvalidToken(response, "Token has expired")
                    return
                } catch (e: io.jsonwebtoken.MalformedJwtException) {
                    handleInvalidToken(response, "Malformed token")
                    return
                } catch (e: io.jsonwebtoken.security.SignatureException) {
                    handleInvalidToken(response, "Invalid token signature")
                    return
                } catch (e: Exception) {
                    handleInvalidToken(response, "Invalid token: ${e.message}")
                    return
                }
            }
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            handleInvalidToken(response, "Authentication error: ${e.message}")
        }
    }

    private fun handleInvalidToken(response: HttpServletResponse, message: String) {
        val errorResponse = ErrorResponse(
            error = "Forbidden",
            message = message,
            status = HttpStatus.FORBIDDEN.value()
        )

        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
        response.writer.flush()
    }
}
