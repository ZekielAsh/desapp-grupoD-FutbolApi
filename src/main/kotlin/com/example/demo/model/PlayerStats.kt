package com.example.demo.model

data class PlayerStats(
    val nombre: String,
    val partidosJugados: String, // Cambio de Int a String
    val goles: Int,
    val asistencias: Int,
    val rating: Double
)
