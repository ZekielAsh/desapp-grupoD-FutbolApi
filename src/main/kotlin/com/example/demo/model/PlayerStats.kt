package com.example.demo.model

data class PlayerStats(
    val name: String,
    val appearances: String, // Cambio de Int a String
    val goals: Int,
    val assists: Int,
    val rating: Double
)
