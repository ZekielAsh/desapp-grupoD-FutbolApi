package com.example.demo.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager(
            "teamPlayers",
            "teamMatches",
            "playerStats",
            "teamComparison",
            "teamMetrics",
            "playerMetrics",
            "predictions"
        )
        cacheManager.setCaffeine(caffeineCacheBuilder())
        return cacheManager
    }

    private fun caffeineCacheBuilder(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES) // Cache expires after 15 minutes
            .maximumSize(100) // Maximum 100 entries per cache
            .recordStats() // Enable stats for monitoring
    }
}

