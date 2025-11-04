package com.example.demo.model

data class MatchPredictionResponse(
    val homeTeam: String,
    val awayTeam: String,
    val trendAnalysis: TrendAnalysis,
    val statComparison: StatComparison,
    val probabilities: WinProbabilities
)

data class TrendAnalysis(
    val avgRating: Double,
    val avgGoals: Double,
    val avgAssists: Double,
    val recentFormScore: Double // Siempre 0.0 con tus datos actuales
)

data class StatComparison(
    val homeTeamStats: TeamAggregateStats,
    val awayTeamStats: TeamAggregateStats
)

data class TeamAggregateStats(
    val avgRating: Double,
    val totalGoals: Int,
    val totalAssists: Int
)

data class WinProbabilities(
    val homeWin: Double,
    val draw: Double,
    val awayWin: Double
)