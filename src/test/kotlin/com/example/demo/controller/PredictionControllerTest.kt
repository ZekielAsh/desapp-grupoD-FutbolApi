package com.example.demo.controller

import com.example.demo.model.prediction.*
import com.example.demo.service.PredictionService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration

@WebMvcTest(
    controllers = [PredictionController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ["com\\.example\\.demo\\.security\\..*"]
        )
    ],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class PredictionControllerTest {


    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var predictionService: PredictionService

    @Test
    fun `GET match returns 200`() {

        val mockResp = MatchPredictionResponse(
            homeTeam = "Home",
            awayTeam = "Away",
            trendAnalysis = TrendAnalysis(1.0, 1.0, 1.0, 1.0),
            statComparison = StatComparison(
                TeamAggregateStats(7.0, 10, 5),
                TeamAggregateStats(6.5, 8, 4)
            ),
            probabilities = WinProbabilities(0.5, 0.3, 0.2),
            homeRecentForm = RecentForm(emptyList(), 0, 0, 0, 0.0),
            awayRecentForm = RecentForm(emptyList(), 0, 0, 0, 0.0)
        )

        whenever(predictionService.predictMatch(any(), any(), any(), any()))
            .thenReturn(mockResp)

        mockMvc.perform(
            get("/predictions/match")
                .param("homeTeam", "Home")
                .param("awayTeam", "Away")
                .param("homeTeamId", "1")
                .param("awayTeamId", "2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.homeTeam").value("Home"))
            .andExpect(jsonPath("$.probabilities.homeWin").value(0.5))
    }
}
