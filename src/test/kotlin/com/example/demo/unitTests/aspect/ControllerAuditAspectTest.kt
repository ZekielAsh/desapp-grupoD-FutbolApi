package com.example.demo.unitTests.aspect

import com.example.demo.aspect.ControllerAuditAspect
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
        assertEquals("anonymous", capturedLog.username) // Sin autenticación, debería ser anonymous
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

        // Configure the mock to throw an exception when proceed() is called
        doThrow(RuntimeException("Test error")).`when`(joinPoint).proceed()

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

    @Test
    fun `test audit aspect redacts sensitive data for auth login endpoint`() {
        request.method = "POST"
        request.requestURI = "/auth/login"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("login")
        // Don't stub joinPoint.args because it's not called for sensitive paths
        whenever(joinPoint.proceed()).thenReturn("token")

        controllerAuditAspect.auditControllerMethods(joinPoint)

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertEquals("[REDACTED - Sensitive Data]", capturedLog.params)
        assertTrue(capturedLog.wasSuccess)
    }

    @Test
    fun `test audit aspect redacts sensitive data for auth register endpoint`() {
        request.method = "POST"
        request.requestURI = "/auth/register"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("register")
        // Don't stub joinPoint.args because it's not called for sensitive paths
        whenever(joinPoint.proceed()).thenReturn("token")

        controllerAuditAspect.auditControllerMethods(joinPoint)

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertEquals("[REDACTED - Sensitive Data]", capturedLog.params)
    }

    @Test
    fun `test audit aspect sanitizes error messages for auth endpoints`() {
        request.method = "POST"
        request.requestURI = "/auth/login"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("login")
        // Don't stub joinPoint.args because it's not called for sensitive paths

        // Configure the mock to throw an exception when proceed() is called
        doThrow(RuntimeException("Invalid credentials for user: admin")).`when`(joinPoint).proceed()

        assertThrows(RuntimeException::class.java) {
            controllerAuditAspect.auditControllerMethods(joinPoint)
        }

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertEquals("Authentication failed", capturedLog.errorMessage)
        assertEquals("[REDACTED - Sensitive Data]", capturedLog.params)
        assertFalse(capturedLog.wasSuccess)
    }

    @Test
    fun `test audit aspect does not redact non-auth endpoints`() {
        request.method = "GET"
        request.requestURI = "/api/teams/123/players"

        val logCaptor = argumentCaptor<ApiAuditLog>()

        whenever(joinPoint.signature).thenReturn(signature)
        whenever(signature.declaringType).thenReturn(ControllerAuditAspectTest::class.java)
        whenever(signature.name).thenReturn("getPlayers")
        whenever(joinPoint.args).thenReturn(arrayOf(123L))
        whenever(joinPoint.proceed()).thenReturn("players")

        controllerAuditAspect.auditControllerMethods(joinPoint)

        verify(apiAuditService).logApiCall(logCaptor.capture())

        val capturedLog = logCaptor.firstValue
        assertTrue(capturedLog.params.contains("123"))
        assertNotEquals("[REDACTED - Sensitive Data]", capturedLog.params)
    }
}
