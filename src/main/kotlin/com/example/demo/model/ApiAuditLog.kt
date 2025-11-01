package com.example.demo.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
data class ApiAuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val httpMethod: String = "", // <--- Valor por defecto
    val path: String = "", // <--- Valor por defecto
    val controllerName: String = "", // <--- Valor por defecto
    val methodName: String = "", // <--- Valor por defecto
    val params: String = "", // <--- Valor por defecto
    val executionTimeMs: Long = 0L, // <--- Valor por defecto
    val wasSuccess: Boolean = false, // <--- Valor por defecto
    val errorMessage: String? = null, // (Este ya era nullable, está bien)
    val timestamp: LocalDateTime = LocalDateTime.now() // (Este ya tenía un default)
)