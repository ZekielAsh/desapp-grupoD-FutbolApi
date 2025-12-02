package com.example.demo.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

/**
 * DataSource configuration for production (Render) environment.
 * Parses Render's PostgreSQL URL format (postgresql://user:pass@host/db)
 * and converts it to JDBC format (jdbc:postgresql://host/db).
 *
 * This configuration is ONLY active when spring.profiles.active=prod
 * For local development and tests, Spring Boot uses application.properties defaults.
 */
@Configuration
@Profile("prod")
class DataSourceConfig {

    @Value("\${DATABASE_URL:}")
    private lateinit var databaseUrl: String

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val config = HikariConfig()

        // Parse Render PostgreSQL URL format: postgresql://user:password@host:port/database
        val parsedConfig = parseDatabaseUrl(databaseUrl)

        config.jdbcUrl = parsedConfig.jdbcUrl
        config.username = parsedConfig.username
        config.password = parsedConfig.password
        config.driverClassName = "org.postgresql.Driver"

        // HikariCP settings optimized for Render free tier
        config.maximumPoolSize = 5
        config.minimumIdle = 2
        config.connectionTimeout = 30000
        config.idleTimeout = 600000
        config.maxLifetime = 1800000

        return HikariDataSource(config)
    }

    private data class DatabaseConfig(
        val jdbcUrl: String,
        val username: String,
        val password: String
    )

    private fun parseDatabaseUrl(url: String): DatabaseConfig {
        if (url.isBlank()) {
            throw IllegalStateException("DATABASE_URL environment variable is required in production")
        }

        // If already in JDBC format, return as-is (shouldn't happen in Render)
        if (url.startsWith("jdbc:")) {
            return DatabaseConfig(
                jdbcUrl = url,
                username = "",
                password = ""
            )
        }

        // Parse Render format: postgresql://username:password@host:port/database
        // or postgres://username:password@host:port/database
        val regex = Regex("^postgres(?:ql)?://([^:]+):([^@]+)@(.+)$")
        val matchResult = regex.find(url)

        return if (matchResult != null) {
            val (username, password, hostAndDb) = matchResult.destructured
            DatabaseConfig(
                jdbcUrl = "jdbc:postgresql://$hostAndDb",
                username = username,
                password = password
            )
        } else {
            throw IllegalArgumentException("Invalid DATABASE_URL format. Expected: postgresql://user:pass@host:port/db")
        }
    }
}

