package com.example.demo.model.football

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

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
    val position: String? = null,
    val nationality: String? = null,
    val dateOfBirth: String? = null,
    val shirtNumber: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MatchesResponse(
    val matches: List<MatchDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CompetitionDto(
    val name: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamInfoDto(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null,
    val crest: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MatchDto(
    val competitionName: String?,
    val homeTeam: TeamInfoDto,
    val awayTeam: TeamInfoDto,
    val utcDate: String?,
    val score: ScoreDto?
) {
    @JsonCreator
    constructor(
        @JsonProperty("competition") competition: CompetitionDto?,
        @JsonProperty("homeTeam") homeTeamDto: TeamInfoDto?,
        @JsonProperty("awayTeam") awayTeamDto: TeamInfoDto?,
        @JsonProperty("utcDate") utcDate: String?,
        @JsonProperty("score") scoreDto: ScoreDto?
    ) : this(
        competitionName = competition?.name,
        homeTeam = homeTeamDto ?: TeamInfoDto(),
        awayTeam = awayTeamDto ?: TeamInfoDto(),
        utcDate = utcDate,
        score = scoreDto
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScoreDto(
    val fullTime: FullTimeScoreDto?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FullTimeScoreDto(
    val home: Int?,
    val away: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StandingsResponse(
    val standings: List<StandingDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StandingDto(
    val table: List<TableEntryDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TableEntryDto(
    val position: Int,
    val team: TeamBasicDto,
    val playedGames: Int,
    val won: Int,
    val draw: Int,
    val lost: Int,
    val points: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TeamBasicDto(
    val id: Long,
    val name: String
)

