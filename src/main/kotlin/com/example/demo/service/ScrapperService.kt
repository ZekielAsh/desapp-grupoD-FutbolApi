package com.example.demo.service

import com.example.demo.model.PlayerStats
import com.example.demo.model.TeamPlayersResponse
import com.example.demo.model.football.CompetitionStats
import com.example.demo.model.football.PlayerStatsResponse
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
     * Gets summary statistics (by competition) for a specific player.
     *
     * @param playerId The numeric player ID (e.g., "300713")
     * @param playerName The player name for URL (e.g., "kylian-mbappé")
     * @return A PlayerStatsResponse containing competitions and total/average.
     */
    fun getPlayerSummaryStats(playerId: String, playerName: String): PlayerStatsResponse {

        val encodedPlayerName = URLEncoder.encode(playerName, StandardCharsets.UTF_8.toString())
        val url = "https://es.whoscored.com/players/$playerId/show/$encodedPlayerName"
        val allCompetitionStats = mutableListOf<CompetitionStats>()
        var totalAverageStats: StatsData? = null

        val mainContentWait = WebDriverWait(webDriver, Duration.ofSeconds(30))
        val js = webDriver as JavascriptExecutor

        try {
            // 1. Navigate to URL
            println("Navigating to: $url")
            webDriver.get(url)

            // 2. Wait for page to load completely
            mainContentWait.until {
                js.executeScript("return document.readyState") == "complete"
            }

            Thread.sleep(3000)

            // 3. Handle pop-ups
            handleConsentPopups()

            // 4. Wait for statistics table with multiple strategies
            println("Searching for player statistics table...")

            val tableBody = try {
                // Strategy 1: Try common ID patterns for player stats
                println("Strategy 1: Trying ID 'player-table-statistics-body'...")
                mainContentWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("player-table-statistics-body")
                ))
            } catch (e1: Exception) {
                println("Strategy 1 failed, trying alternative selectors...")
                try {
                    // Strategy 2: Try other common IDs
                    println("Strategy 2: Trying alternative IDs...")
                    mainContentWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("#player-tournament-stats-summary tbody, #statistics-table-summary tbody, " +
                                "div[id*='player-tournament'] tbody, div[id*='statistics-summary'] tbody")
                    ))
                } catch (e2: Exception) {
                    println("Strategy 2 failed, trying XPath...")
                    try {
                        // Strategy 3: Look for any table in statistics sections
                        println("Strategy 3: Looking for tables in statistics containers...")
                        mainContentWait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@id, 'statistics') or contains(@id, 'tournament')]//table//tbody[.//tr]")
                        ))
                    } catch (e3: Exception) {
                        println("Strategy 3 failed, trying to click tabs...")
                        try {
                            // Strategy 4: Click on Statistics/Summary tab if exists
                            println("Strategy 4: Trying to click on Statistics/Summary tab...")
                            val statsTab = webDriver.findElements(By.xpath(
                                "//a[contains(text(), 'Estadísticas') or contains(text(), 'Statistics') or " +
                                "contains(text(), 'Resumen') or contains(text(), 'Summary')]"
                            ))

                            if (statsTab.isNotEmpty()) {
                                println("Found tab, clicking...")
                                statsTab[0].click()
                                Thread.sleep(3000)
                            }

                            mainContentWait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("tbody tr td, tbody tr th")
                            ))
                        } catch (e4: Exception) {
                            println("Strategy 4 failed, analyzing page structure...")

                            // Strategy 5: Debug - find any tbody with content
                            println("Strategy 5: Looking for any tbody with rows...")
                            val bodyHTML = js.executeScript("return document.body.innerHTML").toString()

                            // Log what we found in the HTML
                            println("Page analysis:")
                            println("  - Contains 'statistics': ${bodyHTML.contains("statistics", ignoreCase = true)}")
                            println("  - Contains 'tournament': ${bodyHTML.contains("tournament", ignoreCase = true)}")
                            println("  - Contains 'player': ${bodyHTML.contains("player", ignoreCase = true)}")

                            // Try to find any tbody
                            val anyTbody = webDriver.findElements(By.tagName("tbody"))
                            println("  - Found ${anyTbody.size} tbody elements on page")

                            if (anyTbody.isNotEmpty()) {
                                // Find the first tbody that has rows with actual data
                                anyTbody.firstOrNull { tbody ->
                                    val rows = tbody.findElements(By.tagName("tr"))
                                    rows.size > 0 && rows.any { row ->
                                        val cells = row.findElements(By.xpath("./th | ./td"))
                                        cells.size >= 10 // Table should have many columns for stats
                                    }
                                } ?: throw RuntimeException("No tbody with statistical data found")
                            } else {
                                throw RuntimeException("No tbody elements found on page at all")
                            }
                        }
                    }
                }
            }

            println("Statistics table found successfully!")

            // Scroll to table to ensure it's fully loaded
            js.executeScript("arguments[0].scrollIntoView(true);", tableBody)
            Thread.sleep(2000)

            // 5. Get the number of rows dynamically from the found table
            val rowCount = tableBody.findElements(By.tagName("tr")).size
            println("Processing $rowCount rows (competitions + total/average)...")

            // Process each row by re-querying to avoid stale elements
            for (i in 0 until rowCount) {
                try {
                    // Re-query rows from the tableBody to avoid stale element references
                    val rows = tableBody.findElements(By.tagName("tr"))
                    if (i >= rows.size) {
                        println("Row ${i + 1}: Index out of bounds, skipping")
                        continue
                    }

                    val row = rows[i]

                    // Get all cells (both th and td elements)
                    val allCells = row.findElements(By.xpath("./th | ./td"))

                    println("Row ${i + 1}: ${allCells.size} cells (th + td)")

                    if (allCells.size >= 12) {
                        // Check if it's a competition row (has tournament link in first cell)
                        val linkElements = allCells[0].findElements(By.cssSelector("a.tournament-link, a[href*='/Regions/'], a[href*='/tournaments/']"))

                        if (linkElements.isNotEmpty()) {
                            // This is a competition row
                            val competitionName = linkElements[0].text.trim()

                            // Extract statistics with safe text extraction
                            val stats = StatsData(
                                matches = allCells.getOrNull(1)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                minutes = allCells.getOrNull(2)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                goals = allCells.getOrNull(3)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                assists = allCells.getOrNull(4)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                yellowCards = allCells.getOrNull(5)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                redCards = allCells.getOrNull(6)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                shotsPerGame = allCells.getOrNull(7)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                keyPasses = allCells.getOrNull(8)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                dribbles = allCells.getOrNull(9)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                mvp = allCells.getOrNull(10)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                rating = allCells.getOrNull(11)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-"
                            )

                            println("Competition: $competitionName - Matches: ${stats.matches} - Goals: ${stats.goals}")

                            allCompetitionStats.add(CompetitionStats(competition = competitionName, statistics = stats))
                        } else {
                            // This is the Total/Average row (no tournament link)
                            println("Row ${i + 1}: Total/Average row found.")

                            totalAverageStats = StatsData(
                                matches = allCells.getOrNull(1)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                minutes = allCells.getOrNull(2)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                goals = allCells.getOrNull(3)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                assists = allCells.getOrNull(4)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                yellowCards = allCells.getOrNull(5)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                redCards = allCells.getOrNull(6)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                shotsPerGame = allCells.getOrNull(7)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                keyPasses = allCells.getOrNull(8)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                dribbles = allCells.getOrNull(9)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                mvp = allCells.getOrNull(10)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-",
                                rating = allCells.getOrNull(11)?.text?.trim()?.takeIf { it.isNotBlank() } ?: "-"
                            )

                            println("Total/Average - Matches: ${totalAverageStats.matches} - Goals: ${totalAverageStats.goals}")
                        }
                    } else {
                        println("Row ${i + 1}: Insufficient cells (${allCells.size})")
                    }
                } catch (e: Exception) {
                    println("Error processing row ${i + 1}: ${e.message}")
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            println("Error during scraping of $url: ${e.message}")
            e.printStackTrace()
        } finally {
            webDriver.switchTo().defaultContent()
        }

        println("Scraping completed. Found ${allCompetitionStats.size} competitions and total/average data.")
        return PlayerStatsResponse(competitions = allCompetitionStats, totalAverage = totalAverageStats)
    }

    // --- REUSABLE PRIVATE METHOD ---

    /**
     * Handles consent pop-ups (Privacy and Cookies)
     * that appear when loading the page.
     */
    private fun handleConsentPopups() {
        // 2.1. Try to close PRIVACY POP-UP ("Accept all")
        try {
            // Create a specific wait for this pop-up
            val popupWait = WebDriverWait(webDriver, Duration.ofSeconds(10))

            // Wait for the pop-up container to be visible
            popupWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("qc-cmp2-ui")))

            // Find the "Accept all" button
            val acceptAllButton = popupWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[text()='Aceptar todo']]")
            ))

            acceptAllButton.click()
            println("Privacy pop-up 'Accept all' clicked.")

            // Give time for animation
            Thread.sleep(500)

        } catch (e: Exception) {
            println("Info: 'Accept all' pop-up not found. ${e.message}")
        }

        // 2.2. Try to close COOKIE BANNER (the one in the iframe)
        try {
            // Create a short wait for the secondary banner
            val iframeWait = WebDriverWait(webDriver, Duration.ofSeconds(3))

            iframeWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("iframe[title='SP Consent Message']")))
            webDriver.switchTo().frame(webDriver.findElement(By.cssSelector("iframe[title='SP Consent Message']")))

            val acceptButton = iframeWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[.//span[text()='ACEPTO']]")
            ))
            acceptButton.click()
            println("Cookie banner (iframe) 'ACCEPT' clicked.")

        } catch (e: Exception) {
            println("Info: Cookie banner in iframe not found.")
        } finally {
            // CRITICAL! We must always exit the iframe,
            // whether we succeeded or not.
            webDriver.switchTo().defaultContent()
        }
    }
}