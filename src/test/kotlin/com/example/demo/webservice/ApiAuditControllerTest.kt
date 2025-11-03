package com.example.demo.controller

import com.example.demo.model.ApiAuditLog
import com.example.demo.repository.ApiAuditLogRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.Sort
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ApiAuditControllerTest {

    @Mock
    private lateinit var apiAuditLogRepository: ApiAuditLogRepository

    @InjectMocks
    private lateinit var apiAuditController: ApiAuditController

    @Test
    fun `test getAllAuditLogs returns all logs sorted by timestamp`() {
        val log1 = ApiAuditLog(
            id = 1L,
            httpMethod = "GET",
            path = "/api/test1",
            timestamp = LocalDateTime.now().minusHours(2),
            wasSuccess = true
        )
        val log2 = ApiAuditLog(
            id = 2L,
            httpMethod = "POST",
            path = "/api/test2",
            timestamp = LocalDateTime.now().minusHours(1),
            wasSuccess = true
        )
        val log3 = ApiAuditLog(
            id = 3L,
            httpMethod = "DELETE",
            path = "/api/test3",
            timestamp = LocalDateTime.now(),
            wasSuccess = false
        )

        val expectedLogs = listOf(log3, log2, log1)

        whenever(apiAuditLogRepository.findAll(any<Sort>())).thenReturn(expectedLogs)

        val result = apiAuditController.getAllAuditLogs()

        assertEquals(3, result.size)
        assertEquals(log3.id, result[0].id)
        assertEquals(log2.id, result[1].id)
        assertEquals(log1.id, result[2].id)
        verify(apiAuditLogRepository).findAll(argThat<Sort> {
            this.getOrderFor("timestamp")?.direction == Sort.Direction.DESC
        })
    }

    @Test
    fun `test getAllAuditLogs returns empty list when no logs exist`() {
        whenever(apiAuditLogRepository.findAll(any<Sort>())).thenReturn(emptyList())

        val result = apiAuditController.getAllAuditLogs()

        assertTrue(result.isEmpty())
        verify(apiAuditLogRepository).findAll(any<Sort>())
    }

    @Test
    fun `test getAllAuditLogs returns single log`() {
        val singleLog = ApiAuditLog(
            id = 1L,
            httpMethod = "GET",
            path = "/api/single",
            wasSuccess = true
        )

        whenever(apiAuditLogRepository.findAll(any<Sort>())).thenReturn(listOf(singleLog))

        val result = apiAuditController.getAllAuditLogs()

        assertEquals(1, result.size)
        assertEquals("GET", result[0].httpMethod)
        assertEquals("/api/single", result[0].path)
    }

    @Test
    fun `test getAllAuditLogs includes both successful and failed requests`() {
        val successLog = ApiAuditLog(
            id = 1L,
            httpMethod = "GET",
            path = "/api/success",
            wasSuccess = true,
            errorMessage = null
        )
        val failLog = ApiAuditLog(
            id = 2L,
            httpMethod = "POST",
            path = "/api/fail",
            wasSuccess = false,
            errorMessage = "Error occurred"
        )

        whenever(apiAuditLogRepository.findAll(any<Sort>())).thenReturn(listOf(failLog, successLog))

        val result = apiAuditController.getAllAuditLogs()

        assertEquals(2, result.size)
        assertTrue(result.any { it.wasSuccess })
        assertTrue(result.any { !it.wasSuccess })
    }

    @Test
    fun `test getAllAuditLogs with various HTTP methods`() {
        val getLogs = ApiAuditLog(httpMethod = "GET", path = "/api/get", wasSuccess = true)
        val postLogs = ApiAuditLog(httpMethod = "POST", path = "/api/post", wasSuccess = true)
        val putLogs = ApiAuditLog(httpMethod = "PUT", path = "/api/put", wasSuccess = true)
        val deleteLogs = ApiAuditLog(httpMethod = "DELETE", path = "/api/delete", wasSuccess = true)

        whenever(apiAuditLogRepository.findAll(any<Sort>()))
            .thenReturn(listOf(getLogs, postLogs, putLogs, deleteLogs))

        val result = apiAuditController.getAllAuditLogs()

        assertEquals(4, result.size)
        assertTrue(result.any { it.httpMethod == "GET" })
        assertTrue(result.any { it.httpMethod == "POST" })
        assertTrue(result.any { it.httpMethod == "PUT" })
        assertTrue(result.any { it.httpMethod == "DELETE" })
    }
}

