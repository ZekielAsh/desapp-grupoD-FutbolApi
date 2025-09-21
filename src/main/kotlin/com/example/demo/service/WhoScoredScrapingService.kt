package com.example.demo.service

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class WhoScoredScrapingService(private val webDriver: WebDriver) {

    fun getTeamPlayers(teamUrl: String): TeamPlayersResponse {
        try {
            webDriver.get(teamUrl)

            // Esperar a que la página cargue completamente
            val wait = WebDriverWait(webDriver, Duration.ofSeconds(30))
            val js = webDriver as JavascriptExecutor

            // Esperar a que el documento esté completamente cargado
            wait.until {
                js.executeScript("return document.readyState") == "complete"
            }

            // Esperar un poco más para que se ejecuten los scripts
            Thread.sleep(2000)

            // Esperar a que aparezca la tabla
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("top-player-stats-summary-grid")))

            // Hacer scroll para asegurar que la tabla está visible
            val table = webDriver.findElement(By.id("top-player-stats-summary-grid"))
            js.executeScript("arguments[0].scrollIntoView(true);", table)

            // Esperar más tiempo para que se carguen las filas dinámicamente
            Thread.sleep(3000)

            // Esperar específicamente a que haya filas en la tabla
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("#top-player-stats-summary-grid tbody tr"), 0
            ))

            // Obtener nombre del equipo
            val teamName = try {
                val teamElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".team-header-name, h1, .team-name")
                ))
                teamElement.text
            } catch (e: Exception) {
                "Equipo desconocido"
            }

            // Obtener todas las filas de la tabla
            val playerRows = webDriver.findElements(By.cssSelector("#top-player-stats-summary-grid tbody tr"))

            println("Número de filas encontradas: ${playerRows.size}")

            val players = playerRows.mapIndexedNotNull { index, row ->
                try {
                    val cells = row.findElements(By.tagName("td"))
                    println("Fila $index: ${cells.size} celdas")

                    if (cells.size >= 10) {

                        // Obtener nombre del jugador
                        val nombre = try {
                            cells[0].findElement(By.cssSelector("a.player-link, a")).text.trim()
                        } catch (e: Exception) {
                            cells[0].text.trim()
                        }

                        if (nombre.isBlank() || nombre == "-") return@mapIndexedNotNull null

                        // Obtener estadísticas con validación de índices
                        val partidosText = cells.getOrNull(4)?.text?.trim() ?: "0"
                        val golesText = cells.getOrNull(6)?.text?.trim() ?: "0"
                        val asistenciasText = cells.getOrNull(7)?.text?.trim() ?: "0"
                        val ratingText = cells.getOrNull(14)?.text?.trim() ?: "0.0"

                        // Limpiar el nombre del jugador (quitar número al inicio)
                        val nombreLimpio = nombre.replace(Regex("^\\d+\\s*\\n?"), "").trim()

                        // Parsear datos
                        val partidos = partidosText // Mantener como String
                        val goles = if (golesText == "-") 0 else golesText.toIntOrNull() ?: 0
                        val asistencias = if (asistenciasText == "-") 0 else asistenciasText.toIntOrNull() ?: 0
                        val rating = ratingText.toDoubleOrNull() ?: 0.0

                        println("Jugador: $nombreLimpio - Partidos: $partidos - Goles: $goles - Asistencias: $asistencias - Rating: $rating")

                        PlayerStats(
                            nombre = nombreLimpio, // Usar el nombre limpio
                            partidosJugados = partidos, // Ahora es String
                            goles = goles,
                            asistencias = asistencias,
                            rating = rating
                        )
                    } else null
                } catch (e: Exception) {
                    println("Error procesando fila $index: ${e.message}")
                    null
                }
            }

            println("Total de jugadores procesados: ${players.size}")

            return TeamPlayersResponse(teamName, players)

        } catch (e: Exception) {
            println("Error general: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Error al obtener datos del equipo: ${e.message}", e)
        }
    }
}
