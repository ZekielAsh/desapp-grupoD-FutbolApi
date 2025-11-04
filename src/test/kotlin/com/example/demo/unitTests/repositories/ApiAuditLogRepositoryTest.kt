package com.example.demo.repository

import com.example.demo.model.ApiAuditLog
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime

@DataJpaTest
class ApiAuditLogRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var auditLogRepository: ApiAuditLogRepository

    @Test
    fun `test save audit log successfully`() {
        val log = ApiAuditLog(
            httpMethod = "GET",
            path = "/api/test",
            controllerName = "TestController",
            methodName = "testMethod",
            params = "[]",
            executionTimeMs = 150L,
            wasSuccess = true,
            errorMessage = null,
            timestamp = LocalDateTime.now()
        )

        val saved = auditLogRepository.save(log)

        assertNotNull(saved.id)
        assertEquals("GET", saved.httpMethod)
        assertEquals("/api/test", saved.path)
    }

    @Test
    fun `test find audit log by id`() {
        val log = ApiAuditLog(
            httpMethod = "POST",
            path = "/api/create",
            controllerName = "CreateController",
            methodName = "create",
            params = "[id=1]",
            executionTimeMs = 200L,
            wasSuccess = true
        )
        entityManager.persist(log)
        entityManager.flush()

        val found = auditLogRepository.findById(log.id!!)

        assertTrue(found.isPresent)
        assertEquals("POST", found.get().httpMethod)
    }

    @Test
    fun `test findAll returns all logs`() {
        val log1 = ApiAuditLog(httpMethod = "GET", path = "/api/1", wasSuccess = true)
        val log2 = ApiAuditLog(httpMethod = "POST", path = "/api/2", wasSuccess = false)
        entityManager.persist(log1)
        entityManager.persist(log2)
        entityManager.flush()

        val logs = auditLogRepository.findAll()

        assertTrue(logs.size >= 2)
    }

    @Test
    fun `test save audit log with error message`() {
        val log = ApiAuditLog(
            httpMethod = "DELETE",
            path = "/api/delete/1",
            controllerName = "DeleteController",
            methodName = "delete",
            params = "[id=1]",
            executionTimeMs = 50L,
            wasSuccess = false,
            errorMessage = "Resource not found"
        )

        val saved = auditLogRepository.save(log)

        assertNotNull(saved.id)
        assertFalse(saved.wasSuccess)
        assertEquals("Resource not found", saved.errorMessage)
    }

    @Test
    fun `test delete audit log`() {
        val log = ApiAuditLog(httpMethod = "GET", path = "/api/test", wasSuccess = true)
        entityManager.persist(log)
        entityManager.flush()
        val logId = log.id!!

        auditLogRepository.deleteById(logId)

        val found = auditLogRepository.findById(logId)
        assertFalse(found.isPresent)
    }

    @Test
    fun `test audit log timestamp is persisted correctly`() {
        val timestamp = LocalDateTime.of(2025, 11, 2, 10, 30, 0)
        val log = ApiAuditLog(
            httpMethod = "PUT",
            path = "/api/update",
            wasSuccess = true,
            timestamp = timestamp
        )

        val saved = auditLogRepository.save(log)

        assertEquals(timestamp, saved.timestamp)
    }

    @Test
    fun `test audit log with long execution time`() {
        val log = ApiAuditLog(
            httpMethod = "GET",
            path = "/api/slow",
            executionTimeMs = 5000L,
            wasSuccess = true
        )

        val saved = auditLogRepository.save(log)

        assertEquals(5000L, saved.executionTimeMs)
    }

    @Test
    fun `test audit log with empty params`() {
        val log = ApiAuditLog(
            httpMethod = "GET",
            path = "/api/noparams",
            params = "",
            wasSuccess = true
        )

        val saved = auditLogRepository.save(log)

        assertEquals("", saved.params)
    }
}

