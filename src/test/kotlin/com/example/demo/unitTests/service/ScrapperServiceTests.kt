package com.example.demo.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.*
import java.net.URLEncoder
import java.time.Duration

// Usamos la extensión de MockK para JUnit 5
@ExtendWith(MockKExtension::class)
class ScrapperServiceTest {

    // Mockeamos las dependencias principales
    @MockK
    lateinit var webDriver: WebDriver

    @MockK
    lateinit var jsExecutor: JavascriptExecutor

    // Mocks reutilizables para elementos web
    @MockK
    lateinit var mockWebElement: WebElement

    @MockK
    lateinit var mockTargetLocator: WebDriver.TargetLocator

    // El servicio que vamos a testear
    private lateinit var scrapperService: ScrapperService

    @BeforeEach
    fun setUp() {
        // Inicializa los mocks anotados con @MockK
        MockKAnnotations.init(this)

        // Instanciamos el servicio con el WebDriver mockeado
        scrapperService = ScrapperService(webDriver)

        // Mockeamos la conversión de WebDriver a JavascriptExecutor
        every { webDriver as JavascriptExecutor } returns jsExecutor

        // Mockeamos comportamientos comunes del WebDriver
        every { webDriver.get(any()) } returns Unit
        every { webDriver.title } returns "Default Title"
        every { webDriver.switchTo() } returns mockTargetLocator
        every { mockTargetLocator.defaultContent() } returns webDriver
        every { mockTargetLocator.frame(any<WebElement>()) } returns webDriver

        // Comportamientos comunes del JavascriptExecutor
        every { jsExecutor.executeScript("return document.readyState") } returns "complete"
        every { jsExecutor.executeScript("arguments[0].scrollIntoView(true);", any()) } returns Unit

        // Mockeamos Thread.sleep() para que no pause los tests
        // Esto es crucial para la cobertura y velocidad
        mockkStatic(Thread::class)
        every { Thread.sleep(any<Long>()) } returns Unit

        // Comportamiento por defecto para findElement (falla si no se mockea específicamente)
        every { webDriver.findElement(any()) } throws NoSuchElementException("Mock: Element not found by default")

        // Comportamiento por defecto para findElements (lista vacía si no se mockea)
        every { webDriver.findElements(any()) } returns emptyList()
    }

    // --- Tests para getTeamPlayersByName ---

    @Test
    fun `getTeamPlayersByName debe retornar jugadores cuando todo funciona correctamente`() {
        val teamName = "Real Madrid"
        val encodedTeamName = URLEncoder.encode(teamName, "UTF-8")
        val searchUrl = "https://es.whoscored.com/search/?t=$encodedTeamName"
        val teamUrl = "https://es.whoscored.com/teams/52/Real-Madrid"

        // 1. Mocks para searchTeam
        val mockHeader = mockk<WebElement>()
        val mockTable = mockk<WebElement>()
        val mockLink = mockk<WebElement>()

        every { webDriver.get(searchUrl) } returns Unit
        every { webDriver.findElement(By.xpath("//h2[contains(text(), 'Equipos:')]")) } returns mockHeader
        every { mockHeader.findElement(By.xpath("./following-sibling::table[1]")) } returns mockTable
        every { mockTable.findElement(By.cssSelector("tbody tr td a[href*='/teams/']")) } returns mockLink
        every { mockLink.getDomAttribute("href") } returns "/teams/52/Real-Madrid" // URL Relativa

        // 2. Mocks para getTeamPlayers
        val mockPlayerTable = mockk<WebElement>()
        val mockTeamNameElement = mockk<WebElement>()
        val mockRow1 = mockk<WebElement>()
        val mockRow2 = mockk<WebElement>()
        val mockCells1 = createMockCells("10\nPlayer One", "30(2)", "15", "8", "8.50")
        val mockCells2 = createMockCells("Player Two", "10", "-", "-", "6.20") // Testeando defaults

        every { webDriver.get(teamUrl) } returns Unit
        every { webDriver.findElement(By.id("top-player-stats-summary-grid")) } returns mockPlayerTable

        // Mock para la primera estrategia de búsqueda de nombre
        every { webDriver.findElement(By.xpath("//*[@id='layout-wrapper']/div[3]/div[1]/div[1]/h1/span")) } returns mockTeamNameElement
        every { mockTeamNameElement.text } returns "Real Madrid CF"

        every { webDriver.findElements(By.cssSelector("#top-player-stats-summary-grid tbody tr")) } returns listOf(mockRow1, mockRow2)
        every { mockRow1.findElements(By.tagName("td")) } returns mockCells1
        every { mockRow2.findElements(By.tagName("td")) } returns mockCells2

        // --- Ejecución ---
        val response = scrapperService.getTeamPlayersByName(teamName)

        // --- Aserciones ---
        assertThat(response.team).isEqualTo("Real Madrid CF")
        assertThat(response.players).hasSize(2)

        assertThat(response.players[0].name).isEqualTo("Player One")
        assertThat(response.players[0].appearances).isEqualTo("30(2)")
        assertThat(response.players[0].goals).isEqualTo(15)
        assertThat(response.players[0].assists).isEqualTo(8)
        assertThat(response.players[0].rating).isEqualTo(8.50)

        assertThat(response.players[1].name).isEqualTo("Player Two")
        assertThat(response.players[1].goals).isEqualTo(0) // Testeando "-"
        assertThat(response.players[1].assists).isEqualTo(0) // Testeando "-"
        assertThat(response.players[1].rating).isEqualTo(6.20)
    }

    @Test
    fun `getTeamPlayersByName debe lanzar excepcion cuando la busqueda de equipo falla`() {
        val teamName = "Equipo Inexistente"

        // Simulamos que el header "Equipos:" nunca aparece
        every { webDriver.findElement(By.xpath("//h2[contains(text(), 'Equipos:')]")) } throws TimeoutException("Elemento no encontrado")

        // --- Ejecución y Aserción ---
        val exception = assertThrows<RuntimeException> {
            scrapperService.getTeamPlayersByName(teamName)
        }

        assertThat(exception.message).contains("Error searching for team '$teamName'")
        assertThat(exception.cause?.message).contains("Could not find team '$teamName'")
    }

    @Test
    fun `getTeamPlayersByName debe lanzar excepcion cuando la tabla de jugadores no se encuentra`() {
        val teamName = "Real Madrid"

        // 1. Mock para searchTeam (funciona)
        mockSearchTeamSuccess(teamUrl = "/teams/52/Real-Madrid")

        // 2. Mock para getTeamPlayers (falla)
        // Simulamos que la tabla "top-player-stats-summary-grid" nunca aparece
        every { webDriver.findElement(By.id("top-player-stats-summary-grid")) } throws TimeoutException("Tabla no encontrada")

        // --- Ejecución y Aserción ---
        val exception = assertThrows<RuntimeException> {
            scrapperService.getTeamPlayersByName(teamName)
        }

        assertThat(exception.message).contains("Error searching for team '$teamName'")
        assertThat(exception.cause?.message).contains("Error getting team data")
        assertThat(exception.cause?.cause?.message).contains("Tabla no encontrada")
    }

    @Test
    fun `getTeamPlayers debe usar fallback de nombre (Titulo) cuando XPath falla`() {
        val teamName = "Real Madrid"

        // 1. Mock searchTeam (funciona)
        mockSearchTeamSuccess(teamUrl = "/teams/52/Real-Madrid")

        // 2. Mock getTeamPlayers (tabla funciona, nombre falla)
        every { webDriver.findElement(By.id("top-player-stats-summary-grid")) } returns mockWebElement
        every { webDriver.findElements(By.cssSelector("#top-player-stats-summary-grid tbody tr")) } returns emptyList() // No players needed

        // Hacemos fallar todas las estrategias de XPath/CSS/Class
        every { webDriver.findElement(By.xpath(any())) } throws NoSuchElementException()
        every { webDriver.findElement(By.cssSelector(any())) } throws NoSuchElementException()
        every { webDriver.findElement(By.className(any())) } throws NoSuchElementException()

        // Proveemos un título de página para el fallback (Opción 6)
        every { webDriver.title } returns "Real Madrid - Estadísticas - WhoScored.com"

        // --- Ejecución ---
        val response = scrapperService.getTeamPlayersByName(teamName)

        // --- Aserción ---
        assertThat(response.team).isEqualTo("Real Madrid")
    }

    @Test
    fun `getTeamPlayers debe usar fallback de URL cuando todo lo demas falla`() {
        val teamName = "Real Madrid"
        val teamUrl = "/teams/123/Club-Atletico-Boca-Juniors"

        // 1. Mock searchTeam (funciona)
        mockSearchTeamSuccess(teamUrl = teamUrl)

        // 2. Mock getTeamPlayers (tabla funciona, nombre falla)
        every { webDriver.findElement(By.id("top-player-stats-summary-grid")) } returns mockWebElement
        every { webDriver.findElements(By.cssSelector("#top-player-stats-summary-grid tbody tr")) } returns emptyList()

        // Hacemos fallar todas las estrategias
        every { webDriver.findElement(By.xpath(any())) } throws NoSuchElementException()
        every { webDriver.findElement(By.cssSelector(any())) } throws NoSuchElementException()
        every { webDriver.findElement(By.className(any())) } throws NoSuchElementException()
        every { webDriver.title } returns "   " // Título vacío

        // --- Ejecución ---
        val response = scrapperService.getTeamPlayersByName(teamName)

        // --- Aserción ---
        // Debe parsear "Club-Atletico-Boca-Juniors" desde la URL (Último recurso)
        assertThat(response.team).isEqualTo("Club Atletico Boca Juniors")
    }

    @Test
    fun `getTeamPlayers debe ignorar filas con pocas celdas o filas fallidas`() {
        val teamName = "Real Madrid"

        // 1. Mock searchTeam (funciona)
        mockSearchTeamSuccess()

        // 2. Mock getTeamPlayers
        every { webDriver.findElement(By.id("top-player-stats-summary-grid")) } returns mockWebElement
        // Mock de nombre de equipo (la primera estrategia)
        val mockTeamNameElement = mockk<WebElement>()
        every { webDriver.findElement(By.xpath("//*[@id='layout-wrapper']/div[3]/div[1]/div[1]/h1/span")) } returns mockTeamNameElement
        every { mockTeamNameElement.text } returns "Real Madrid CF"

        // Creamos las filas
        val mockRowValid = mockk<WebElement>()
        val mockRowBadCells = mockk<WebElement>() // Pocas celdas (menos de 10)
        val mockRowError = mockk<WebElement>() // Lanza excepción
        val mockRowBlankName = mockk<WebElement>() // Nombre vacío

        every { webDriver.findElements(By.cssSelector("#top-player-stats-summary-grid tbody tr")) } returns
                listOf(mockRowValid, mockRowBadCells, mockRowError, mockRowBlankName)

        // Fila válida
        val validCells = createMockCells("Player One", "10", "5", "5", "7.0")
        every { mockRowValid.findElements(By.tagName("td")) } returns validCells

        // Fila con pocas celdas
        val badCells = List(5) { mockk<WebElement>() } // Solo 5 celdas
        every { mockRowBadCells.findElements(By.tagName("td")) } returns badCells

        // Fila que lanza excepción al procesarla
        every { mockRowError.findElements(By.tagName("td")) } throws RuntimeException("Error en fila")

        // Fila con nombre en blanco
        val blankNameCells = createMockCells("-", "10", "5", "5", "7.0")
        every { mockRowBlankName.findElements(By.tagName("td")) } returns blankNameCells

        // --- Ejecución ---
        val response = scrapperService.getTeamPlayersByName(teamName)

        // --- Aserción ---
        // Solo la fila válida debe ser procesada
        assertThat(response.players).hasSize(1)
        assertThat(response.players[0].name).isEqualTo("Player One")
    }

    // --- Tests para getPlayerSummaryStats ---

    @Test
    fun `getPlayerSummaryStats debe retornar estadisticas y manejar Popups`() {
        val playerId = "300713"
        val playerName = "kylian-mbappe"

        // 1. Mock para handleConsentPopups (AMBOS popups aparecen)
        // 1.1 Privacy Popup
        val mockPrivacyPopup = mockk<WebElement>()
        val mockAcceptPrivacyBtn = mockk<WebElement>()
        every { webDriver.findElement(By.id("qc-cmp2-ui")) } returns mockPrivacyPopup
        every { webDriver.findElement(By.xpath("//button[.//span[text()='Aceptar todo']]")) } returns mockAcceptPrivacyBtn
        every { mockAcceptPrivacyBtn.click() } returns Unit

        // 1.2 Iframe Cookie Popup
        val mockIframe = mockk<WebElement>()
        val mockAcceptCookieBtn = mockk<WebElement>()
        every { webDriver.findElement(By.cssSelector("iframe[title='SP Consent Message']")) } returns mockIframe
        // Simulamos el cambio al iframe
        every { mockTargetLocator.frame(mockIframe) } returns webDriver
        every { webDriver.findElement(By.xpath("//button[.//span[text()='ACEPTO']]")) } returns mockAcceptCookieBtn
        every { mockAcceptCookieBtn.click() } returns Unit

        // 2. Mock para la tabla de estadísticas
        val mockStatsTable = mockk<WebElement>()
        every { webDriver.findElement(By.id("player-table-statistics-body")) } returns mockStatsTable

        val mockCompRow = mockk<WebElement>()
        val mockTotalRow = mockk<WebElement>()

        // Definimos el número de filas (2)
        every { webDriver.findElements(By.cssSelector("#player-table-statistics-body tr")) } returns listOf(mockCompRow, mockTotalRow)

        // Mock de la re-búsqueda de filas
        every { webDriver.findElement(By.cssSelector("#player-table-statistics-body tr:nth-child(1)")) } returns mockCompRow
        every { webDriver.findElement(By.cssSelector("#player-table-statistics-body tr:nth-child(2)")) } returns mockTotalRow

        // 2.1 Fila de Competición
        val compCells = createStatsCells(isTotalRow = false, competitionName = "Ligue 1", goals = "27", rating = "7.80")
        every { mockCompRow.findElements(By.xpath("./th | ./td")) } returns compCells

        // 2.2 Fila de Total/Average
        val totalCells = createStatsCells(isTotalRow = true, competitionName = "", goals = "35", rating = "7.70")
        every { mockTotalRow.findElements(By.xpath("./th | ./td")) } returns totalCells

        // --- Ejecución ---
        val response = scrapperService.getPlayerSummaryStats(playerId, playerName)

        // --- Aserciones ---
        assertThat(response.competitions).hasSize(1)
        assertThat(response.competitions[0].competition).isEqualTo("Ligue 1")
        assertThat(response.competitions[0].statistics.goals).isEqualTo("27")
        assertThat(response.competitions[0].statistics.rating).isEqualTo("7.80")

        assertThat(response.totalAverage).isNotNull
        assertThat(response.totalAverage?.goals).isEqualTo("35")
        assertThat(response.totalAverage?.rating).isEqualTo("7.70")

        // Verificamos que los popups fueron clickeados
        verify { mockAcceptPrivacyBtn.click() }
        verify { mockAcceptCookieBtn.click() }
        // Verificamos que siempre vuelve al default content
        verify(atLeast = 3) { mockTargetLocator.defaultContent() }
    }

    @Test
    fun `getPlayerSummaryStats debe funcionar cuando no hay Popups`() {
        val playerId = "123"
        val playerName = "test-player"

        // 1. Mock para handleConsentPopups (NINGÚN popup aparece)
        every { webDriver.findElement(By.id("qc-cmp2-ui")) } throws TimeoutException()
        every { webDriver.findElement(By.cssSelector("iframe[title='SP Consent Message']")) } throws TimeoutException()

        // 2. Mock de la tabla (vacía)
        every { webDriver.findElement(By.id("player-table-statistics-body")) } returns mockWebElement
        every { webDriver.findElements(By.cssSelector("#player-table-statistics-body tr")) } returns emptyList()

        // --- Ejecución ---
        val response = scrapperService.getPlayerSummaryStats(playerId, playerName)

        // --- Aserciones ---
        assertThat(response.competitions).isEmpty()
        assertThat(response.totalAverage).isNull()

        // Verificamos que SIEMPRE llama a defaultContent() por el 'finally' en handleConsentPopups
        verify(atLeast = 1) { mockTargetLocator.defaultContent() }
    }

    @Test
    fun `getPlayerSummaryStats debe retornar vacio cuando la tabla no se encuentra`() {
        val playerId = "123"
        val playerName = "test-player"

        // Mock popups (no aparecen)
        every { webDriver.findElement(By.id("qc-cmp2-ui")) } throws TimeoutException()
        every { webDriver.findElement(By.cssSelector("iframe[title='SP Consent Message']")) } throws TimeoutException()

        // Mock de tabla (falla al encontrarla)
        every { webDriver.findElement(By.id("player-table-statistics-body")) } throws TimeoutException("Tabla no encontrada")

        // --- Ejecución ---
        val response = scrapperService.getPlayerSummaryStats(playerId, playerName)

        // --- Aserciones ---
        // El método tiene un try/catch general, no debe lanzar excepción, sino retornar vacío
        assertThat(response.competitions).isEmpty()
        assertThat(response.totalAverage).isNull()

        // El 'finally' general del método se ejecuta
        verify(atLeast = 1) { mockTargetLocator.defaultContent() }
    }

    @Test
    fun `getPlayerSummaryStats debe manejar datos en blanco usando default`() {
        val playerId = "123"
        val playerName = "test-player"

        // Mock popups (no aparecen)
        every { webDriver.findElement(By.id("qc-cmp2-ui")) } throws TimeoutException()
        every { webDriver.findElement(By.cssSelector("iframe[title='SP Consent Message']")) } throws TimeoutException()

        // Mock tabla
        every { webDriver.findElement(By.id("player-table-statistics-body")) } returns mockWebElement
        val mockCompRow = mockk<WebElement>()
        every { webDriver.findElements(By.cssSelector("#player-table-statistics-body tr")) } returns listOf(mockCompRow)
        every { webDriver.findElement(By.cssSelector("#player-table-statistics-body tr:nth-child(1)")) } returns mockCompRow

        // Mock celdas con datos en blanco (espacios o vacíos)
        val cells = createStatsCells(isTotalRow = false, competitionName = "Test League", goals = " ", rating = "")
        every { mockCompRow.findElements(By.xpath("./th | ./td")) } returns cells

        // --- Ejecución ---
        val response = scrapperService.getPlayerSummaryStats(playerId, playerName)

        // --- Aserciones ---
        assertThat(response.competitions).hasSize(1)
        // El código convierte " " o "" en "-"
        assertThat(response.competitions[0].statistics.goals).isEqualTo("-")
        assertThat(response.competitions[0].statistics.rating).isEqualTo("-")
    }

    // --- Funciones de Ayuda (Helpers) ---

    /**
     * Mockea un `searchTeam` exitoso
     */
    private fun mockSearchTeamSuccess(teamUrl: String = "/teams/123/Test-Team") {
        val mockHeader = mockk<WebElement>()
        val mockTable = mockk<WebElement>()
        val mockLink = mockk<WebElement>()

        every { webDriver.findElement(By.xpath("//h2[contains(text(), 'Equipos:')]")) } returns mockHeader
        every { mockHeader.findElement(By.xpath("./following-sibling::table[1]")) } returns mockTable
        every { mockTable.findElement(By.cssSelector("tbody tr td a[href*='/teams/']")) } returns mockLink
        every { mockLink.getDomAttribute("href") } returns teamUrl
    }

    /**
     * Crea una lista de mocks de celdas (WebElement) para `getTeamPlayers`
     */
    private fun createMockCells(
        name: String,
        appearances: String,
        goals: String,
        assists: String,
        rating: String
    ): List<WebElement> {
        // Creamos 15 celdas mockeadas. `relaxed = true` evita mockear `text` en celdas no usadas.
        val cells = List(15) { mockk<WebElement>(relaxed = true) }

        // Mock del link del jugador (celda 0)
        val mockPlayerLink = mockk<WebElement>()
        every { cells[0].findElement(By.cssSelector("a.player-link, a")) } returns mockPlayerLink
        every { mockPlayerLink.text } returns name

        // Mocks de estadísticas
        every { cells[4].text } returns appearances
        every { cells[6].text } returns goals
        every { cells[7].text } returns assists
        every { cells[14].text } returns rating

        return cells
    }

    /**
     * Crea una lista de mocks de celdas (WebElement) para `getPlayerSummaryStats`
     */
    private fun createStatsCells(
        isTotalRow: Boolean,
        competitionName: String,
        goals: String,
        rating: String
    ): List<WebElement> {
        val cells = List(12) { mockk<WebElement>(relaxed = true) } // 12 celdas

        // Celda 0 (Competición o 'Total')
        val mockTournLink = mockk<WebElement>()
        every { mockTournLink.text } returns competitionName

        if (isTotalRow) {
            // La fila 'Total' no tiene link
            every { cells[0].findElements(By.cssSelector("a.tournament-link")) } returns emptyList()
        } else {
            // La fila de competición sí tiene link
            every { cells[0].findElements(By.cssSelector("a.tournament-link")) } returns listOf(mockTournLink)
        }

        // Mocks de estadísticas
        every { cells[1].text } returns "30" // Partidos
        every { cells[2].text } returns "2500" // Minutos
        every { cells[3].text } returns goals
        every { cells[4].text } returns "10" // Asistencias
        every { cells[5].text } returns "2" // Amarillas
        every { cells[6].text } returns "0" // Rojas
        every { cells[7].text } returns "3.5" // Tiros
        every { cells[8].text } returns "2.1" // Pases clave
        every { cells[9].text } returns "1.8" // Regates
        every { cells[10].text } returns "5" // MVP
        every { cells[11].text } returns rating

        return cells
    }
}