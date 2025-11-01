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
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null,
    val crest: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MatchDto(
    val competitionName: String?,
    val homeTeam: String?,
    val awayTeam: String?,
    val utcDate: String?
) {
    @JsonCreator
    constructor(
        @JsonProperty("competition") competition: CompetitionDto?,
        @JsonProperty("homeTeam") homeTeam: TeamInfoDto?,
        @JsonProperty("awayTeam") awayTeam: TeamInfoDto?,
        @JsonProperty("utcDate") utcDate: String?
    ) : this(
        competitionName = competition?.name,
        homeTeam = homeTeam?.name,
        awayTeam = awayTeam?.name,
        utcDate = utcDate
    )
}