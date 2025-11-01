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
     * Guarda el log en una transacción separada.
     * Usamos REQUIRES_NEW para asegurar que el log se guarde
     * incluso si la transacción principal del scraping falla.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun logApiCall(log: ApiAuditLog) {
        try {
            auditLogRepository.save(log)
        } catch (e: Exception) {
            // Manejar error de guardado de log (ej. imprimir en consola)
            println("Error al guardar el log de auditoría: ${e.message}")
        }
    }
}