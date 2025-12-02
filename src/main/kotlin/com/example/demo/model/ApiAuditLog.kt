package com.example.demo.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class ApiAuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val username: String = "anonymous", // Usuario autenticado que realizó la petición
    val httpMethod: String = "",
    val path: String = "", 
    val controllerName: String = "", 
    val methodName: String = "", 

    @Column(length = 1000)
    val params: String = "",

    val executionTimeMs: Long = 0L,
    val wasSuccess: Boolean = false, 

    @Column(length = 2000)
    val errorMessage: String? = null,

    val timestamp: LocalDateTime = LocalDateTime.now()
)