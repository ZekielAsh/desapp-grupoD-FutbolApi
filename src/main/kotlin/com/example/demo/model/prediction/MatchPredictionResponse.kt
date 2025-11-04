package com.example.demo.model.prediction

data class MatchPredictionResponse(
    val homeTeam: String,
    val awayTeam: String,
    val trendAnalysis: TrendAnalysis,
    val statComparison: StatComparison,
    val probabilities: WinProbabilities,
    val homeRecentForm: RecentForm,
    val awayRecentForm: RecentForm
)

data class TrendAnalysis(
    val avgRating: Double,
    val avgGoals: Double,
    val avgAssists: Double,
    val recentFormScore: Double
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

data class RecentForm(
    val results: List<String>, // W/D/L
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int,
    val formScore: Double
)
