package com.example.demo.model

data class TeamPlayersResponse(
    val equipo: String,
    val jugadores: List<PlayerStats>
)
