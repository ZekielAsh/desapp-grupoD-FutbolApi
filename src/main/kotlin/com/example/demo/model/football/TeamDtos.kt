package com.example.demo.model.football

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamResponse(
    val id: Long? = null,
    val name: String? = null,
    val squad: List<PlayerDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerDto(
    val id: Long? = null,
    val name: String? = null,
    val position: String? = null,      // "Goalkeeper", "Defender", etc.
    val nationality: String? = null,
    val dateOfBirth: String? = null,
    val shirtNumber: Int? = null
)
