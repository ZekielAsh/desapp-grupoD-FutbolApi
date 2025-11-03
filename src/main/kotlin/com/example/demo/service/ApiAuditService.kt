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
     * Saves the audit log in a new transaction.
     * Uses REQUIRES_NEW to ensure the log is saved and committed
     * independently of the main transaction.
     * If the main transaction fails, the audit log is still saved.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = [Exception::class])
    fun logApiCall(log: ApiAuditLog) {
        try {
            auditLogRepository.save(log)
            auditLogRepository.flush()
        } catch (e: Exception) {
            // Handle log save error (print to console)
            // Don't throw - we don't want audit failures to break the main flow
            println("Error saving audit log: ${e.message}")
            e.printStackTrace()
        }
    }
}