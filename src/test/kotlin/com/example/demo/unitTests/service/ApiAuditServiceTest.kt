package com.example.demo.unitTests.service

import com.example.demo.model.ApiAuditLog
import com.example.demo.repository.ApiAuditLogRepository
import com.example.demo.service.ApiAuditService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ApiAuditServiceTest {

    @Mock
    private lateinit var auditLogRepository: ApiAuditLogRepository

    @InjectMocks
    private lateinit var apiAuditService: ApiAuditService

    @Test
    fun `test logApiCall saves log successfully`() {
        val log = ApiAuditLog(
            httpMethod = "GET",
            path = "/api/test",
            controllerName = "TestController",
            methodName = "test",
            params = "[]",
            executionTimeMs = 100L,
            wasSuccess = true,
            errorMessage = null,
            timestamp = LocalDateTime.now()
        )

        val savedLog = log.copy(id = 1L)
        whenever(auditLogRepository.save(any<ApiAuditLog>())).thenReturn(savedLog)

        apiAuditService.logApiCall(log)

        verify(auditLogRepository).save(any<ApiAuditLog>())
    }

    @Test
    fun `test logApiCall handles exception gracefully`() {
        val log = ApiAuditLog(
            httpMethod = "POST",
            path = "/api/error",
            wasSuccess = false
        )

        whenever(auditLogRepository.save(any<ApiAuditLog>()))
            .thenThrow(RuntimeException("Database error"))

        // Should not throw exception - it catches and logs the error
        apiAuditService.logApiCall(log)

        verify(auditLogRepository).save(any<ApiAuditLog>())
    }

    @Test
    fun `test logApiCall with error message`() {
        val log = ApiAuditLog(
            httpMethod = "DELETE",
            path = "/api/delete",
            controllerName = "DeleteController",
            methodName = "delete",
            params = "[id=1]",
            executionTimeMs = 50L,
            wasSuccess = false,
            errorMessage = "Resource not found"
        )

        val savedLog = log.copy(id = 2L)
        whenever(auditLogRepository.save(any<ApiAuditLog>())).thenReturn(savedLog)

        apiAuditService.logApiCall(log)

        verify(auditLogRepository).save(any<ApiAuditLog>())
    }
}

