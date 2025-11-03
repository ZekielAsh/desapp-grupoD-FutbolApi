package com.example.demo.model.football

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Almacena las estadísticas de un jugador en una competición específica.
 * Esto es lo que devolverá la lista final.
 */
data class CompetitionStats(
    val campeonato: String,
    val estadisticas: StatsData
)

/**
 * Representa la fila de estadísticas de la tabla de resumen de WhoScored.
 * Los campos coinciden con las columnas de la tabla.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos JSON que no mapeamos
data class StatsData(
    val partidos: String,
    val minutos: String,
    val goles: String,
    val asistencias: String,
    val amarillas: String,
    val rojas: String,
    val tpp: String,       // Tiros por partido
    val pdec: String,      // Pases decisivos
    val regates: String,   // Regates
    val mvp: String,       // Man of the Match
    val rating: String
)