package com.example.demo.model.football

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerDtosTest {

    @Test
    fun `test CompetitionStats creation`() {
        val stats = StatsData(
            partidos = "10",
            minutos = "900",
            goles = "5",
            asistencias = "3",
            amarillas = "2",
            rojas = "0",
            tpp = "3.5",
            pdec = "2.1",
            regates = "4.0",
            mvp = "1",
            rating = "7.8"
        )

        val competitionStats = CompetitionStats(
            campeonato = "La Liga",
            estadisticas = stats
        )

        assertEquals("La Liga", competitionStats.campeonato)
        assertEquals("10", competitionStats.estadisticas.partidos)
        assertEquals("5", competitionStats.estadisticas.goles)
    }

    @Test
    fun `test StatsData with all fields`() {
        val stats = StatsData(
            partidos = "25",
            minutos = "2250",
            goles = "15",
            asistencias = "10",
            amarillas = "3",
            rojas = "1",
            tpp = "4.2",
            pdec = "3.5",
            regates = "5.1",
            mvp = "3",
            rating = "8.5"
        )

        assertEquals("25", stats.partidos)
        assertEquals("2250", stats.minutos)
        assertEquals("15", stats.goles)
        assertEquals("10", stats.asistencias)
        assertEquals("3", stats.amarillas)
        assertEquals("1", stats.rojas)
        assertEquals("4.2", stats.tpp)
        assertEquals("3.5", stats.pdec)
        assertEquals("5.1", stats.regates)
        assertEquals("3", stats.mvp)
        assertEquals("8.5", stats.rating)
    }

    @Test
    fun `test StatsData with zero values`() {
        val stats = StatsData(
            partidos = "0",
            minutos = "0",
            goles = "0",
            asistencias = "0",
            amarillas = "0",
            rojas = "0",
            tpp = "0.0",
            pdec = "0.0",
            regates = "0.0",
            mvp = "0",
            rating = "0.0"
        )

        assertEquals("0", stats.partidos)
        assertEquals("0.0", stats.rating)
    }

    @Test
    fun `test CompetitionStats copy functionality`() {
        val original = CompetitionStats(
            campeonato = "Premier League",
            estadisticas = StatsData(
                "20", "1800", "12", "8", "2", "0",
                "3.0", "2.0", "3.5", "2", "7.9"
            )
        )

        val updated = original.copy(campeonato = "Champions League")

        assertEquals("Champions League", updated.campeonato)
        assertEquals("20", updated.estadisticas.partidos)
    }

    @Test
    fun `test StatsData with decimal values`() {
        val stats = StatsData(
            partidos = "15",
            minutos = "1350",
            goles = "8",
            asistencias = "6",
            amarillas = "1",
            rojas = "0",
            tpp = "3.75",
            pdec = "2.85",
            regates = "4.33",
            mvp = "1",
            rating = "8.25"
        )

        assertTrue(stats.tpp.contains("."))
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
        assertEquals("La Liga", allStats[0].campeonato)
        assertEquals("Champions League", allStats[1].campeonato)
    }
}

