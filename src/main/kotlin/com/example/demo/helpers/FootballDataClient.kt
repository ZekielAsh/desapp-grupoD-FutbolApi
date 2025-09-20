package com.example.demo.helpers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class FootballDataClient(private val props: FootballDataProperties) {

    @Bean
    fun footballRestClient(): RestClient =
        RestClient.builder()
            .baseUrl(props.baseUrl)
            .defaultHeader("X-Auth-Token", props.apiKey) // header que exige la API
            .build()
}
