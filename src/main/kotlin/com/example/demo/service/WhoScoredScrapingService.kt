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
import java.net.URLEncoder

@Service
class WhoScoredScrapingService(private val webDriver: WebDriver) {

    fun getTeamPlayersByName(teamName: String): TeamPlayersResponse {
        try {
            // Buscar el equipo
            val teamUrl = searchTeam(teamName)

            // Obtener jugadores del equipo encontrado
            return getTeamPlayers(teamUrl)

        } catch (e: Exception) {
            throw RuntimeException("Error al buscar equipo '$teamName': ${e.message}", e)
        }
    }

    private fun searchTeam(teamName: String): String {
        try {
            // Construir URL de búsqueda
            val encodedTeamName = URLEncoder.encode(teamName, "UTF-8")
            val searchUrl = "https://es.whoscored.com/search/?t=$encodedTeamName"

            webDriver.get(searchUrl)

            val wait = WebDriverWait(webDriver, Duration.ofSeconds(15))
            val js = webDriver as JavascriptExecutor

            // Esperar a que la página cargue completamente
            wait.until {
                js.executeScript("return document.readyState") == "complete"
            }

            Thread.sleep(3000)

            // Buscar específicamente la sección de "Equipos:"
            val equiposHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h2[contains(text(), 'Equipos:')]")
            ))

            // Encontrar la tabla que sigue al header "Equipos:"
            val equiposTable = equiposHeader.findElement(By.xpath("./following-sibling::table[1]"))

            // Buscar el primer enlace de equipo en esa tabla específica
            val firstTeamLink = equiposTable.findElement(By.cssSelector("tbody tr td a[href*='/teams/']"))
            val relativeUrl = firstTeamLink.getDomAttribute("href")

            // Construir URL completa si es necesario
            val teamUrl = if (relativeUrl?.startsWith("http") == true) {
                relativeUrl
            } else {
                "https://es.whoscored.com$relativeUrl"
            }

            println("Equipo encontrado: $teamUrl")

            return teamUrl

        } catch (e: Exception) {
            throw RuntimeException("No se pudo encontrar el equipo '$teamName': ${e.message}", e)
        }
    }

    private fun getTeamPlayers(teamUrl: String): TeamPlayersResponse {
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

            val eDesc = "Equipo desconocido"
            // Obtener nombre del equipo
            val teamName = try {
                // Estrategia múltiple para obtener el nombre del equipo
                val name = try {
                    // Opción 1: XPath específico
                    val teamNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[@id='layout-wrapper']/div[3]/div[1]/div[1]/h1/span")
                    ))
                    teamNameElement.text.trim()
                } catch (e1: Exception) {
                    try {
                        // Opción 2: Selector CSS más general
                        val teamHeaderElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("h1.team-header span.team-header-name")
                        ))
                        teamHeaderElement.text.trim()
                    } catch (e2: Exception) {
                        try {
                            // Opción 3: Cualquier span dentro de h1.team-header
                            val headerElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("h1.team-header span")
                            ))
                            headerElement.text.trim()
                        } catch (e3: Exception) {
                            try {
                                // Opción 4: Buscar por clase team-header-name en cualquier lugar
                                val nameElement = webDriver.findElement(By.className("team-header-name"))
                                nameElement.text.trim()
                            } catch (e4: Exception) {
                                try {
                                    // Opción 5: XPath más flexible
                                    val flexibleElement = webDriver.findElement(
                                        By.xpath("//h1[contains(@class, 'team-header')]//span[contains(@class, 'team-header-name') or position()=last()]")
                                    )
                                    flexibleElement.text.trim()
                                } catch (e5: Exception) {
                                    // Opción 6: Extraer del título de la página
                                    val pageTitle = webDriver.title
                                    if (pageTitle.contains(" - ")) {
                                        pageTitle.split(" - ").firstOrNull()?.trim() ?: eDesc
                                    } else {
                                        eDesc
                                    }
                                }
                            }
                        }
                    }
                }

                println("Nombre del equipo encontrado: '$name'")

                // Validar que el nombre no esté vacío
                if (name.isBlank() || name == eDesc) {
                    // Último recurso: extraer del URL
                    val urlParts = teamUrl.split("/")
                    val lastPart = urlParts.lastOrNull() ?: ""
                    if (lastPart.contains("-")) {
                        lastPart.split("-").drop(1).joinToString(" ").replaceFirstChar { it.uppercaseChar() }
                    } else {
                        eDesc
                    }
                } else {
                    name
                }

            } catch (e: Exception) {
                println("Error obteniendo nombre del equipo: ${e.message}")
                e.printStackTrace()
                eDesc
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
                        } catch (_: Exception) {
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
                            nombre = nombreLimpio,
                            partidosJugados = partidos,
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
