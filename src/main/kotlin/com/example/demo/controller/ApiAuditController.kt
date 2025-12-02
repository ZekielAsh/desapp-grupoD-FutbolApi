package com.example.demo.controller

import com.example.demo.helpers.AuditLogsSuccessResponse
import com.example.demo.helpers.UnauthorizedResponses
import com.example.demo.model.ApiAuditLog
import com.example.demo.service.ApiAuditService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "API audit log endpoints")
class ApiAuditController(
    private val apiAuditService: ApiAuditService
) {

    @Operation(summary = "Get all audit logs", description = "Retrieve all API audit logs")
    @AuditLogsSuccessResponse
    @UnauthorizedResponses
    @GetMapping("/logs")
    fun getAllAuditLogs(): List<ApiAuditLog> {
        return apiAuditService.getAllAuditLogs()
    }
}