package com.example.demo.model.football

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerDtosTest {

    @Test
    fun `test CompetitionStats creation`() {
        val stats = StatsData(
            matches = "10",
            minutes = "900",
            goals = "5",
            assists = "3",
            yellowCards = "2",
            redCards = "0",
            shotsPerGame = "3.5",
            keyPasses = "2.1",
            dribbles = "4.0",
            mvp = "1",
            rating = "7.8"
        )

        val competitionStats = CompetitionStats(
            competition = "La Liga",
            statistics = stats
        )

        assertEquals("La Liga", competitionStats.competition)
        assertEquals("10", competitionStats.statistics.matches)
        assertEquals("5", competitionStats.statistics.goals)
    }

    @Test
    fun `test StatsData with all fields`() {
        val stats = StatsData(
            matches = "25",
            minutes = "2250",
            goals = "15",
            assists = "10",
            yellowCards = "3",
            redCards = "1",
            shotsPerGame = "4.2",
            keyPasses = "3.5",
            dribbles = "5.1",
            mvp = "3",
            rating = "8.5"
        )

        assertEquals("25", stats.matches)
        assertEquals("2250", stats.minutes)
        assertEquals("15", stats.goals)
        assertEquals("10", stats.assists)
        assertEquals("3", stats.yellowCards)
        assertEquals("1", stats.redCards)
        assertEquals("4.2", stats.shotsPerGame)
        assertEquals("3.5", stats.keyPasses)
        assertEquals("5.1", stats.dribbles)
        assertEquals("3", stats.mvp)
        assertEquals("8.5", stats.rating)
    }

    @Test
    fun `test StatsData with zero values`() {
        val stats = StatsData(
            matches = "0",
            minutes = "0",
            goals = "0",
            assists = "0",
            yellowCards = "0",
            redCards = "0",
            shotsPerGame = "0.0",
            keyPasses = "0.0",
            dribbles = "0.0",
            mvp = "0",
            rating = "0.0"
        )

        assertEquals("0", stats.matches)
        assertEquals("0.0", stats.rating)
    }

    @Test
    fun `test CompetitionStats copy functionality`() {
        val original = CompetitionStats(
            competition = "Premier League",
            statistics = StatsData(
                "20", "1800", "12", "8", "2", "0",
                "3.0", "2.0", "3.5", "2", "7.9"
            )
        )

        val updated = original.copy(competition = "Champions League")

        assertEquals("Champions League", updated.competition)
        assertEquals("20", updated.statistics.matches)
    }

    @Test
    fun `test StatsData with decimal values`() {
        val stats = StatsData(
            matches = "15",
            minutes = "1350",
            goals = "8",
            assists = "6",
            yellowCards = "1",
            redCards = "0",
            shotsPerGame = "3.75",
            keyPasses = "2.85",
            dribbles = "4.33",
            mvp = "1",
            rating = "8.25"
        )

        assertTrue(stats.shotsPerGame.contains("."))
        assertTrue(stats.rating.contains("."))
    }

    @Test
    fun `test multiple CompetitionStats for different leagues`() {
        val laLigaStats = CompetitionStats(
            "La Liga",
            StatsData("20", "1800", "10", "5", "2", "0", "3.0", "2.0", "3.0", "1", "7.5")
        )

        val championsStats = CompetitionStats(
            "Champions League",
            StatsData("8", "720", "5", "3", "1", "0", "4.0", "2.5", "4.0", "1", "8.0")
        )

        val allStats = listOf(laLigaStats, championsStats)

        assertEquals(2, allStats.size)
        assertEquals("La Liga", allStats[0].competition)
        assertEquals("Champions League", allStats[1].competition)
    }

    @Test
    fun `test PlayerStatsResponse with competitions and total`() {
        val competitions = listOf(
            CompetitionStats(
                "La Liga",
                StatsData("20", "1800", "10", "5", "2", "0", "3.0", "2.0", "3.0", "1", "7.5")
            ),
            CompetitionStats(
                "Champions League",
                StatsData("8", "720", "5", "3", "1", "0", "4.0", "2.5", "4.0", "1", "8.0")
            )
        )

        val totalAverage = StatsData(
            "28", "2520", "15", "8", "3", "0", "3.3", "2.2", "3.3", "2", "7.7"
        )

        val response = PlayerStatsResponse(competitions, totalAverage)

        assertEquals(2, response.competitions.size)
        assertNotNull(response.totalAverage)
        assertEquals("15", response.totalAverage?.goals)
        assertEquals("28", response.totalAverage?.matches)
    }

    @Test
    fun `test PlayerStatsResponse with null total`() {
        val competitions = listOf(
            CompetitionStats(
                "Premier League",
                StatsData("10", "900", "5", "2", "1", "0", "2.5", "1.5", "2.0", "0", "7.0")
            )
        )

        val response = PlayerStatsResponse(competitions, null)

        assertEquals(1, response.competitions.size)
        assertNull(response.totalAverage)
    }

    @Test
    fun `test PlayerStatsResponse with empty competitions`() {
        val response = PlayerStatsResponse(emptyList(), null)

        assertTrue(response.competitions.isEmpty())
        assertNull(response.totalAverage)
    }
}

