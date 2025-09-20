package com.example.demo.service

import com.example.demo.model.football.PlayerDto
import com.example.demo.model.football.TeamResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class TeamService(private val footballRestClient: RestClient) {

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
}
