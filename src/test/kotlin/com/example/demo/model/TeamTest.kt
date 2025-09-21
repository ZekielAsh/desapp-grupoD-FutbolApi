package com.example.demo.model

import org.junit.jupiter.api.Test

class TeamTest {

    @Test
    fun testTeamCreation() {
        val teamName = "Manchester City"
        val teamLeague = "Premier League"
        val teamCountry = "England"

        val team = Team(
            id = 1L,
            name = teamName,
            league = teamLeague,
            country = teamCountry
        )

        assert(team.id == 1L)
        assert(team.name == teamName)
        assert(team.league == teamLeague)
        assert(team.country == teamCountry)
        assert(team.players.isEmpty()) // Team starts with no players
    }
}