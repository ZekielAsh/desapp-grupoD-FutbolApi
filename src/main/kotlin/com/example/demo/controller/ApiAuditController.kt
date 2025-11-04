package com.example.demo.controller

import com.example.demo.model.ApiAuditLog
import com.example.demo.repository.ApiAuditLogRepository
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to expose endpoints for viewing saved audit logs.
 */
@RestController
@RequestMapping("/api/audit")
class ApiAuditController(
    private val apiAuditLogRepository: ApiAuditLogRepository
) {

    /**
     * Endpoint to get all audit logs.
     *
     * @return List of all logs ordered by most recent first (timestamp DESC, then id DESC)
     */
    @GetMapping("/logs")
    fun getAllAuditLogs(): List<ApiAuditLog> {
        // Order by timestamp DESC (most recent first), then by id DESC as tiebreaker
        val sort = Sort.by(
            Sort.Order.desc("timestamp"),
            Sort.Order.desc("id")
        )
        return apiAuditLogRepository.findAll(sort)
    }
}