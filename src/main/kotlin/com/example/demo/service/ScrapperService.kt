package com.example.demo.service

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.football.CompetitionStats
import com.example.demo.model.football.StatsData
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.springframework.stereotype.Service
import java.time.Duration
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class ScrapperService(private val webDriver: WebDriver) {

    fun getTeamPlayersByName(teamName: String): TeamPlayersResponse {
        try {
            // Search for the team
            val teamUrl = searchTeam(teamName)

            // Get players from the found team
            return getTeamPlayers(teamUrl)

        } catch (e: Exception) {
            throw RuntimeException("Error searching for team '$teamName': ${e.message}", e)
        }
    }

    private fun searchTeam(teamName: String): String {
        try {
            // Build search URL
            val encodedTeamName = URLEncoder.encode(teamName, "UTF-8")
            val searchUrl = "https://es.whoscored.com/search/?t=$encodedTeamName"

            webDriver.get(searchUrl)

            val wait = WebDriverWait(webDriver, Duration.ofSeconds(15))
            val js = webDriver as JavascriptExecutor

            // Wait for page to load completely
            wait.until {
                js.executeScript("return document.readyState") == "complete"
            }

            Thread.sleep(3000)

            // Search specifically for the "Teams:" section
            val teamHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//h2[contains(text(), 'Equipos:')]")
            ))

            // Find the table that follows the "Teams:" header
            val teamTable = teamHeader.findElement(By.xpath("./following-sibling::table[1]"))

            // Search for the first team link in that specific table
            val firstTeamLink = teamTable.findElement(By.cssSelector("tbody tr td a[href*='/teams/']"))
            val relativeUrl = firstTeamLink.getDomAttribute("href")

            // Build complete URL if necessary
            val teamUrl = if (relativeUrl?.startsWith("http") == true) {
                relativeUrl
            } else {
                "https://es.whoscored.com$relativeUrl"
            }

            println("Team found: $teamUrl")

            return teamUrl

        } catch (e: Exception) {
            throw RuntimeException("Could not find team '$teamName': ${e.message}", e)
        }
    }

    private fun getTeamPlayers(teamUrl: String): TeamPlayersResponse {
        try {
            webDriver.get(teamUrl)

            // Wait for page to load completely
            val wait = WebDriverWait(webDriver, Duration.ofSeconds(30))
            val js = webDriver as JavascriptExecutor

            // Wait for document to be completely loaded
            wait.until {
                js.executeScript("return document.readyState") == "complete"
            }

            // Wait a bit more for scripts to execute
            Thread.sleep(2000)

            // Wait for the table to appear
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("top-player-stats-summary-grid")))

            // Scroll to ensure the table is visible
            val table = webDriver.findElement(By.id("top-player-stats-summary-grid"))
            js.executeScript("arguments[0].scrollIntoView(true);", table)

            // Wait more time for rows to load dynamically
            Thread.sleep(3000)

            // Wait specifically for rows to exist in the table
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("#top-player-stats-summary-grid tbody tr"), 0
            ))

            // Get team name
            val teamName = try {
                // Multiple strategy to get team name
                val name = try {
                    // Option 1: Specific XPath
                    val teamNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[@id='layout-wrapper']/div[3]/div[1]/div[1]/h1/span")
                    ))
                    teamNameElement.text.trim()
                } catch (e1: Exception) {
                    try {
                        // Option 2: More general CSS selector
                        val teamHeaderElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("h1.team-header span.team-header-name")
                        ))
                        teamHeaderElement.text.trim()
                    } catch (e2: Exception) {
                        try {
                            // Option 3: Any span inside h1.team-header
                            val headerElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("h1.team-header span")
                            ))
                            headerElement.text.trim()
                        } catch (e3: Exception) {
                            try {
                                // Option 4: Search by team-header-name class anywhere
                                val nameElement = webDriver.findElement(By.className("team-header-name"))
                                nameElement.text.trim()
                            } catch (e4: Exception) {
                                try {
                                    // Option 5: More flexible XPath
                                    val flexibleElement = webDriver.findElement(
                                        By.xpath("//h1[contains(@class, 'team-header')]//span[contains(@class, 'team-header-name') or position()=last()]")
                                    )
                                    flexibleElement.text.trim()
                                } catch (e5: Exception) {
                                    // Option 6: Extract from page title
                                    val pageTitle = webDriver.title
                                    if (pageTitle.contains(" - ")) {
                                        pageTitle.split(" - ").firstOrNull()?.trim() ?: "Unknown Team"
                                    } else {
                                        "Unknown Team"
                                    }
                                }
                            }
                        }
                    }
                }

                println("Team name found: '$name'")

                // Validate that name is not empty
                if (name.isBlank() || name == "Unknown Team") {
                    // Last resort: extract from URL
                    val urlParts = teamUrl.split("/")
                    val lastPart = urlParts.lastOrNull() ?: ""
                    if (lastPart.contains("-")) {
                        lastPart.split("-").drop(1).joinToString(" ").replaceFirstChar { it.uppercaseChar() }
                    } else {
                        "Unknown Team"
                    }
                } else {
                    name
                }

            } catch (e: Exception) {
                println("Error getting team name: ${e.message}")
                e.printStackTrace()
                "Unknown Team"
            }


            // Get all table rows
            val playerRows = webDriver.findElements(By.cssSelector("#top-player-stats-summary-grid tbody tr"))

            println("Number of rows found: ${playerRows.size}")

            val players = playerRows.mapIndexedNotNull { index, row ->
                try {
                    val cells = row.findElements(By.tagName("td"))
                    println("Row $index: ${cells.size} cells")

                    if (cells.size >= 10) { // keep a conservative minimum

                        // Get player name (may include number/newline)
                        val name = try {
                            cells[0].findElement(By.cssSelector("a.player-link, a")).text.trim()
                        } catch (_: Exception) {
                            cells[0].text.trim()
                        }

                        if (name.isBlank() || name == "-") return@mapIndexedNotNull null

                        // Get statistics with index validation
                        val appearancesText = cells.getOrNull(4)?.text?.trim() ?: "0"
                        val goalsText = cells.getOrNull(6)?.text?.trim() ?: "0"
                        val assistsText = cells.getOrNull(7)?.text?.trim() ?: "0"
                        val ratingText = cells.getOrNull(14)?.text?.trim() ?: "0.0"

                        // Clean player name (remove number at the beginning)
                        val clearName = name.replace(Regex("^\\d+\\s*\\n?"), "").trim()

                        // Parse data
                        val appearances = appearancesText // Keep as String
                        val goals = if (goalsText == "-") 0 else goalsText.toIntOrNull() ?: 0
                        val assists = if (assistsText == "-") 0 else assistsText.toIntOrNull() ?: 0
                        val rating = ratingText.toDoubleOrNull() ?: 0.0

                        println("Player: $clearName - Appearances: $appearances - Goals: $goals - Assists: $assists - Rating: $rating")

                        PlayerStats(
                            name = clearName,
                            appearances = appearances,
                            goals = goals,
                            assists = assists,
                            rating = rating
                        )
                    } else null
                } catch (e: Exception) {
                    println("Error processing row $index: ${e.message}")
                    null
                }
            }

            println("Total players processed: ${players.size}")

            return TeamPlayersResponse(teamName, players)

        } catch (e: Exception) {
            println("General error: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Error getting team data: ${e.message}", e)
        }
    }
    /**
     * Obtiene las estadísticas de resumen (por competición) de un jugador específico.
     *
     * @param playerId El ID numérico del jugador (ej: "300713")
     * @param playerName El nombre del jugador para la URL (ej: "kylian-mbappé")
     * @return Una lista de CompetitionStats.
     */
    fun getPlayerSummaryStats(playerId: String, playerName: String): List<CompetitionStats> {

        val encodedPlayerName = URLEncoder.encode(playerName, StandardCharsets.UTF_8.toString())
        val url = "https://es.whoscored.com/players/$playerId/show/$encodedPlayerName"
        val allCompetitionStats = mutableListOf<CompetitionStats>()

        // Esta espera es para el contenido principal (la tabla)
        val mainContentWait = WebDriverWait(webDriver, Duration.ofSeconds(15))

        try {
            // 1. Navegar a la URL
            println("Navegando a: $url")
            webDriver.get(url)

            // 2. MANEJO DE POP-UPS (Refactorizado)
            // Llamamos a nuestro método privado para limpiar la pantalla
            handleConsentPopups()

            // 3. Esperar y seleccionar la tabla de estadísticas
            val tableBodySelector = By.id("player-table-statistics-body")
            mainContentWait.until(ExpectedConditions.visibilityOfElementLocated(tableBodySelector))
            println("Tabla de estadísticas encontrada.")

            // 4. Procesar la tabla (lógica de bucle robusta)
            val rowCount = webDriver.findElements(By.cssSelector("#player-table-statistics-body tr")).size
            println("Procesando $rowCount filas de competiciones...")

            for (i in 0 until rowCount) {
                // Volvemos a buscar la fila en cada iteración para evitar StaleElementReferenceException
                val row = webDriver.findElement(By.cssSelector("#player-table-statistics-body tr:nth-child(${i + 1})"))
                val cells = row.findElements(By.tagName("td"))

                if (cells.size == 12) {
                    val linkElements = cells[0].findElements(By.cssSelector("a.tournament-link"))
                    if (linkElements.isNotEmpty()) {
                        val competitionName = linkElements[0].text
                        val stats = StatsData(
                            partidos = cells[1].text, minutos = cells[2].text,
                            goles = cells[3].text, asistencias = cells[4].text,
                            amarillas = cells[5].text, rojas = cells[6].text,
                            tpp = cells[7].text, pdec = cells[8].text,
                            regates = cells[9].text, mvp = cells[10].text,
                            rating = cells[11].text
                        )
                        allCompetitionStats.add(CompetitionStats(campeonato = competitionName, estadisticas = stats))
                    } else {
                        println("Fila de 'Total / Promedio' ignorada.")
                    }
                }
            }

        } catch (e: Exception) {
            println("Error durante el scraping de $url: ${e.message}")
            e.printStackTrace()
        } finally {
            // Nos aseguramos de volver al contenido principal por si algo falló
            webDriver.switchTo().defaultContent()
        }

        println("Scraping completado. Se encontraron ${allCompetitionStats.size} competiciones.")
        return allCompetitionStats
    }

    // --- MÉTODO PRIVADO REUTILIZABLE ---

    /**
     * Maneja los pop-ups de consentimiento (Privacidad y Cookies)
     * que aparecen al cargar la página.
     */
    private fun handleConsentPopups() {
        // 2.1. Intentar cerrar el POP-UP DE PRIVACIDAD ("Aceptar todo")
        try {
            // Creamos una espera específica para este pop-up
            val popupWait = WebDriverWait(webDriver, Duration.ofSeconds(10))

            // Espera a que el contenedor del pop-up sea visible
            popupWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("qc-cmp2-ui")))

            // Busca el botón "Aceptar todo"
            val acceptAllButton = popupWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[text()='Aceptar todo']]")
            ))

            acceptAllButton.click()
            println("Pop-up de Privacidad 'Aceptar todo' clickeado.")

            // Damos un respiro para la animación
            Thread.sleep(500)

        } catch (e: Exception) {
            println("Info: No se encontró el pop-up 'Aceptar todo'. ${e.message}")
        }

        // 2.2. Intentar cerrar el BANNER DE COOKIES (el del iframe)
        try {
            // Creamos una espera corta para el banner secundario
            val iframeWait = WebDriverWait(webDriver, Duration.ofSeconds(3))

            iframeWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("iframe[title='SP Consent Message']")))
            webDriver.switchTo().frame(webDriver.findElement(By.cssSelector("iframe[title='SP Consent Message']")))

            val acceptButton = iframeWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[text()='ACEPTO']]")
            ))
            acceptButton.click()
            println("Banner de cookies (iframe) 'ACEPTO' aceptado.")

        } catch (e: Exception) {
            println("Info: No se encontró el banner de cookies en el iframe.")
        } finally {
            // ¡CRÍTICO! Siempre debemos salir del iframe,
            // hayamos tenido éxito o no.
            webDriver.switchTo().defaultContent()
        }
    }
}