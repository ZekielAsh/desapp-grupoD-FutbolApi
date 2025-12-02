package com.example.demo.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private var jwtExpiration: Long = 86400000

    fun extractUsername(token: String): String? =
        extractClaim(token) { it.subject }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    fun generateToken(userDetails: UserDetails): String {
        val claims: Map<String, Any> = HashMap()
        return createToken(claims, userDetails.username)
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean =
        extractExpiration(token).before(Date())

    private fun extractExpiration(token: String): Date =
        extractClaim(token) { it.expiration }

    private fun createToken(claims: Map<String, Any>, subject: String): String {
        val now = Date()
        val validity = Date(now.time + jwtExpiration)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }
    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(secretKey.toByteArray())
            .build()
            .parseClaimsJws(token)
            .body
}
