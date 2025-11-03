package com.example.demo.service

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.football.MatchDto
import com.example.demo.model.football.MatchesResponse
import com.example.demo.model.football.PlayerDto
import com.example.demo.model.football.TeamResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class TeamService(private val footballRestClient: RestClient, private val scrapperService: ScrapperService) {

    fun getPlayers(teamId: Long): List<PlayerDto> {
        try {
            val team = footballRestClient.get()
                .uri("/teams/{id}", teamId)
                .retrieve()
                .body(TeamResponse::class.java)

            return team?.squad ?: emptyList()
        } catch (ex: RestClientResponseException) {
            // Podés mapear códigos específicos
            if (ex.statusCode == HttpStatus.NOT_FOUND) {
                // 404 → equipo inexistente
                return emptyList()
            }
            throw ex
        }
    }

    fun getTeamPlayersByName(teamName: String): TeamPlayersResponse {
        return scrapperService.getTeamPlayersByName(teamName)
    }

    fun getNextMatchesByTeamName(teamId: Long): List<MatchDto> {
        try {
            val response = footballRestClient.get()
                .uri("/teams/{id}/matches?status=SCHEDULED", teamId)
                .retrieve()
                .body(MatchesResponse::class.java)
            return response?.matches?: emptyList()
    }
        catch (ex: RestClientResponseException) {
            if (ex.statusCode == HttpStatus.NOT_FOUND) {
                return emptyList()
            }
            throw ex
        }
    }

}
