package com.example.demo.model

data class TeamPlayersResponse(
    val team: String,
    val players: List<PlayerStats>
)
