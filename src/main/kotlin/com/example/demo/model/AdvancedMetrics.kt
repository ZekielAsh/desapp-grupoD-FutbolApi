package com.example.demo.model

// Team Comparison Models
data class TeamComparisonResponse(
    val team1: TeamComparisonData,
    val team2: TeamComparisonData,
    val headToHead: HeadToHeadData
)

data class TeamComparisonData(
    val id: Long,
    val name: String,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int,
    val position: Int?,
    val form: String
)

data class HeadToHeadData(
    val team1Wins: Int,
    val team2Wins: Int,
    val draws: Int,
    val totalMatches: Int,
    val lastMeetings: List<HeadToHeadMatch>
)

data class HeadToHeadMatch(
    val date: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val winner: String
)

// Advanced Metrics Models
data class TeamAdvancedMetrics(
    val teamId: Long,
    val teamName: String,
    val season: String,
    val averageGoalsScored: Double,
    val averageGoalsConceded: Double,
    val cleanSheets: Int,
    val winRate: Double,
    val drawRate: Double,
    val lossRate: Double,
    val goalsPerMatch: Double,
    val goalsConcededPerMatch: Double,
    val goalDifference: Int,
    val homePerformance: PerformanceData,
    val awayPerformance: PerformanceData,
    val recentForm: FormData,
    val attackStrength: Double,
    val defenseStrength: Double
)

data class PerformanceData(
    val played: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int,
    val winRate: Double
)

data class FormData(
    val last5Matches: List<String>,
    val points: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val formScore: Double
)

data class PlayerAdvancedMetrics(
    val playerId: String,
    val playerName: String,
    val season: String,
    val totalMatches: Int,
    val totalMinutes: Int,
    val goalsPerMatch: Double,
    val assistsPerMatch: Double,
    val goalsPer90: Double,
    val assistsPer90: Double,
    val goalContribution: Int,
    val goalContributionPer90: Double,
    val averageRating: Double,
    val keyPassesPer90: Double,
    val dribblesPer90: Double,
    val shotsPerGame: Double,
    val minutesPerGoal: Double?,
    val minutesPerAssist: Double?,
    val discipline: DisciplineData,
    // Calculated Metrics (not directly from WhoScored)
    val efficiency: EfficiencyMetrics,
    val impactScore: Double,
    val versatilityIndex: Double
)

data class EfficiencyMetrics(
    val shotAccuracy: Double,          // goals / shots ratio
    val creativeEfficiency: Double,     // assists / keyPasses ratio
    val dribbleSuccessRate: Double,     // estimated from dribbles per 90
    val playingTimePercentage: Double   // minutes / (matches * 90) to see how much of full matches played
)

data class DisciplineData(
    val yellowCards: Int,
    val redCards: Int,
    val yellowCardsPerMatch: Double,
    val redCardsPerMatch: Double
)

