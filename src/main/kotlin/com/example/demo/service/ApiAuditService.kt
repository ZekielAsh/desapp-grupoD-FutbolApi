package com.example.demo.service

import com.example.demo.model.ApiAuditLog
import com.example.demo.repository.ApiAuditLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ApiAuditService(
    private val auditLogRepository: ApiAuditLogRepository
) {
    /**
     * Saves the audit log.
     * Uses NOT_SUPPORTED to avoid transaction conflicts during tests.
     * The log is saved outside of any existing transaction.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun logApiCall(log: ApiAuditLog) {
        try {
            auditLogRepository.save(log)
        } catch (e: Exception) {
            // Handle log save error (print to console)
            println("Error saving audit log: ${e.message}")
        }
    }
}