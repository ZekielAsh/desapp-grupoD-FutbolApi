package com.example.demo.aspect

import com.example.demo.model.ApiAuditLog
import com.example.demo.service.ApiAuditService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class ControllerAuditAspectTest {

    @Mock
    private lateinit var apiAuditService: ApiAuditService

    @Mock
    private lateinit var joinPoint: ProceedingJoinPoint

    @Mock
    private lateinit var signature: Signature

    @InjectMocks
    private lateinit var controllerAuditAspect: ControllerAuditAspect

    private lateinit var request: MockHttpServletRequest

    @BeforeEach
    fun setup() {
        request = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @Test
    fun `test audit aspect logs successful controller execution`() {
        request.method = "GET"
        request.requestURI = "/api/teams"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("getTeams")
        whenever(joinPoint.args).thenReturn(arrayOf("param1", "param2"))
        whenever(joinPoint.proceed()).thenReturn("Success result")

        val result = controllerAuditAspect.auditControllerMethods(joinPoint)

        assertEquals("Success result", result)
        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertEquals("GET", capturedLog.httpMethod)
        assertEquals("/api/teams", capturedLog.path)
        assertEquals("ControllerAuditAspectTest", capturedLog.controllerName)
        assertEquals("getTeams", capturedLog.methodName)
        assertTrue(capturedLog.wasSuccess)
        assertNull(capturedLog.errorMessage)
    }

    @Test
    fun `test audit aspect logs failed controller execution`() {
        request.method = "POST"
        request.requestURI = "/api/players"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("createPlayer")
        whenever(joinPoint.args).thenReturn(arrayOf())
        whenever(joinPoint.proceed()).thenThrow(RuntimeException("Test error"))

        assertThrows(RuntimeException::class.java) {
            controllerAuditAspect.auditControllerMethods(joinPoint)
        }

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertEquals("POST", capturedLog.httpMethod)
        assertEquals("/api/players", capturedLog.path)
        assertFalse(capturedLog.wasSuccess)
        assertEquals("Test error", capturedLog.errorMessage)
    }

    @Test
    fun `test audit aspect measures execution time`() {
        request.method = "GET"
        request.requestURI = "/api/test"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("slowMethod")
        whenever(joinPoint.args).thenReturn(arrayOf())
        whenever(joinPoint.proceed()).thenAnswer {
            Thread.sleep(100)
            "result"
        }

        controllerAuditAspect.auditControllerMethods(joinPoint)

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertTrue(capturedLog.executionTimeMs >= 100)
    }

    @Test
    fun `test audit aspect with multiple parameters`() {
        request.method = "PUT"
        request.requestURI = "/api/update"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("updateMethod")
        whenever(joinPoint.args).thenReturn(arrayOf(1L, "name", true))
        whenever(joinPoint.proceed()).thenReturn("Updated")

        controllerAuditAspect.auditControllerMethods(joinPoint)

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertTrue(capturedLog.params.contains("1"))
        assertTrue(capturedLog.params.contains("name"))
        assertTrue(capturedLog.params.contains("true"))
    }

    @Test
    fun `test audit aspect with DELETE method`() {
        request.method = "DELETE"
        request.requestURI = "/api/teams/123"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("deleteTeam")
        whenever(joinPoint.args).thenReturn(arrayOf(123L))
        whenever(joinPoint.proceed()).thenReturn(null)

        controllerAuditAspect.auditControllerMethods(joinPoint)

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertEquals("DELETE", capturedLog.httpMethod)
        assertTrue(capturedLog.wasSuccess)
    }
}

