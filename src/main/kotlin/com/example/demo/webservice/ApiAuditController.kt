package com.example.demo.controller

import com.example.demo.model.ApiAuditLog
import com.example.demo.repository.ApiAuditLogRepository
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Este controlador expone endpoints para ver los logs de auditoría guardados.
 */
@RestController
@RequestMapping("/api/audit") // Ruta base para todos los endpoints de este controlador
class ApiAuditController(
    // Inyectamos el REPOSITORIO, no el servicio,
    // porque queremos LEER, no escribir logs.
    private val apiAuditLogRepository: ApiAuditLogRepository
) {

    /**
     * Endpoint para obtener TODAS las consultas registradas.
     *
     * @return Una lista de todos los logs, ordenados por fecha más reciente primero.
     */
    @GetMapping("/logs")
    fun getAllAuditLogs(): List<ApiAuditLog> {
        // 1. Usamos el repositorio para buscar todos los logs.
        // 2. Usamos 'Sort' para ordenarlos por la columna 'timestamp'
        //    en orden descendente (DESC), así ves los más nuevos primero.
        return apiAuditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"))
    }
}